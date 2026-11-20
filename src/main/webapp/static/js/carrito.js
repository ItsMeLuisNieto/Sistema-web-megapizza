/* MegaPizza: interacciones del carrito y utilidades de UI */
(function () {
    'use strict';

    var ctx = window.MEGAPIZZA_CTX || '';

    function mostrarToast(mensaje, tipo) {
        var cont = document.getElementById('toast-container');
        if (!cont) {
            return; // fallback silencioso si no existe contenedor
        }
        var toast = document.createElement('div');
        toast.className = 'toast ' + (tipo === 'error' ? 'toast-error' : 'toast-ok');
        toast.textContent = mensaje;
        cont.appendChild(toast);
        setTimeout(function () { toast.classList.add('visible'); }, 10);
        setTimeout(function () {
            toast.classList.remove('visible');
            setTimeout(function () { if (toast.parentNode) toast.parentNode.removeChild(toast); }, 300);
        }, 2200);
    }

    function actualizarBadge(unidades) {
        var badge = document.getElementById('badge-carrito');
        if (badge) {
            badge.textContent = unidades;
        }
    }

    function cambiarCarrito(idProducto, delta) {
        var body = new URLSearchParams();
        body.append('action', delta > 0 ? 'agregar' : 'quitar');
        body.append('id', String(idProducto));

        return fetch(ctx + '/pedido', {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: body.toString(),
            credentials: 'same-origin'
        }).then(function (respuesta) {
            if (respuesta.redirected) {
                // Sesión expirada o sin permisos: ir a destino indicado
                window.location.href = respuesta.url;
                return null;
            }
            var ct = respuesta.headers.get('content-type') || '';
            if (!respuesta.ok) {
                throw new Error('No se pudo actualizar el carrito');
            }
            if (ct.indexOf('application/json') === -1) {
                // Fallback: recargar si el servidor no devolvió JSON
                window.location.reload();
                return null;
            }
            return respuesta.json();
        }).then(function (datos) {
            if (datos && typeof datos.unidades === 'number') {
                actualizarBadge(datos.unidades);
            }
            return datos;
        });
    }

    window.agregarAlCarrito = function (idProducto, boton) {
        if (!idProducto) return;
        cambiarCarrito(idProducto, 1).then(function (datos) {
            if (!datos) return;
            mostrarToast('Producto agregado al carrito', 'ok');
            if (boton) {
                var textoOriginal = boton.textContent;
                boton.textContent = 'Agregado ✓';
                boton.disabled = true;
                setTimeout(function () {
                    boton.textContent = textoOriginal;
                    boton.disabled = false;
                }, 1000);
            }
        }).catch(function (error) {
            mostrarToast(error.message || 'Error al agregar', 'error');
        });
    };

    window.cambiarCantidad = function (idProducto, delta) {
        if (!idProducto) return;
        cambiarCarrito(idProducto, delta).then(function (datos) {
            if (datos) {
                window.location.reload();
            }
        }).catch(function (error) {
            mostrarToast(error.message || 'Error al actualizar', 'error');
        });
    };

    window.vaciarCarrito = function () {
        if (!window.confirm('¿Vaciar el carrito?')) {
            return;
        }
        var formulario = document.createElement('form');
        formulario.method = 'POST';
        formulario.action = ctx + '/pedido';

        var campo = document.createElement('input');
        campo.type = 'hidden';
        campo.name = 'action';
        campo.value = 'vaciar';
        formulario.appendChild(campo);

        document.body.appendChild(formulario);
        formulario.submit();
    };

    window.confirmarPedido = function () {
        var comprobante = document.getElementById('comprobanteUrl');
        if (comprobante && !comprobante.value.trim()) {
            mostrarToast('Debes ingresar el comprobante de pago', 'error');
            comprobante.focus();
            return false;
        }
        return window.confirm('¿Confirmar el pedido? Se registrará como Pendiente para validación en caja.');
    };

    // Delegación de eventos: los IDs se leen de atributos data-* (no de código JS
    // en el JSP), así el IDE no marca "${...}" como error de JavaScript.
    document.addEventListener('click', function (evento) {
        var objetivo = evento.target;
        if (!objetivo || objetivo.nodeType !== 1) {
            return;
        }

        var botonAgregar = objetivo.closest('[data-agregar]');
        if (botonAgregar) {
            var idAgregar = parseInt(botonAgregar.getAttribute('data-id-producto'), 10);
            if (idAgregar > 0) {
                evento.preventDefault();
                window.agregarAlCarrito(idAgregar, botonAgregar);
            }
            return;
        }

        var botonCantidad = objetivo.closest('[data-delta]');
        if (botonCantidad) {
            var idProducto = parseInt(botonCantidad.getAttribute('data-id-producto'), 10);
            var delta = parseInt(botonCantidad.getAttribute('data-delta'), 10);
            if (idProducto > 0 && (delta === 1 || delta === -1)) {
                evento.preventDefault();
                window.cambiarCantidad(idProducto, delta);
            }
        }
    });

    document.addEventListener('submit', function (evento) {
        var formulario = evento.target;
        if (formulario && formulario.nodeType === 1 && formulario.hasAttribute('data-rechazo')) {
            var idPedido = formulario.getAttribute('data-id-pedido');
            if (!window.confirm('¿Rechazar el comprobante del pedido #' + idPedido + '?')) {
                evento.preventDefault();
            }
        }
    });

    // Auto-ocultar alertas de servidor tras unos segundos
    document.addEventListener('DOMContentLoaded', function () {
        var alertas = document.querySelectorAll('.contenedor > .alerta');
        if (alertas.length) {
            setTimeout(function () {
                alertas.forEach(function (a) { a.classList.add('alerta-oculta'); });
            }, 4500);
        }
    });
})();
