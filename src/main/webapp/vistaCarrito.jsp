<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="titulo" value="Carrito" scope="request" />
<%@ include file="/WEB-INF/partials/header.jspf" %>

<h1>Tu carrito</h1>

<c:choose>
    <c:when test="${empty detalles}">
        <section class="tarjeta">
            <h2>Todavía no agregas productos</h2>
            <p>Explora el catálogo y arma tu pedido.</p>
            <a class="boton" href="${ctx}/catalogo">Ir al catálogo</a>
        </section>
    </c:when>
    <c:otherwise>
        <section class="tarjeta">
            <div class="tabla-scroll">
                <table class="tabla">
                    <thead>
                    <tr><th>Producto</th><th>Precio unit.</th><th>Cantidad</th><th>Subtotal</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="detalle" items="${detalles}">
                        <tr>
                            <td><c:out value="${detalle.nombreProducto}"/></td>
                            <td>S/ <fmt:formatNumber value="${detalle.precioUnitario}" pattern="#,##0.00"/></td>
                            <td>
                                <div class="contador" aria-label="Cantidad">
                                    <button type="button" class="boton boton-mini" data-delta="-1"
                                            data-id-producto="${detalle.idProducto}" aria-label="Disminuir">&minus;</button>
                                    <span class="cantidad-valor">${detalle.cantidad}</span>
                                    <button type="button" class="boton boton-mini" data-delta="1"
                                            data-id-producto="${detalle.idProducto}" aria-label="Aumentar">+</button>
                                </div>
                            </td>
                            <td>S/ <fmt:formatNumber value="${detalle.subtotal}" pattern="#,##0.00"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                    <tfoot>
                    <tr><th colspan="3" class="derecha">Total</th>
                        <th>S/ <fmt:formatNumber value="${total}" pattern="#,##0.00"/></th></tr>
                    </tfoot>
                </table>
            </div>

            <div class="acciones acciones-carrito">
                <button type="button" class="boton boton-peligro" onclick="vaciarCarrito()">Vaciar carrito</button>
                <a class="boton boton-suave" href="${ctx}/catalogo">Seguir comprando</a>
            </div>
        </section>

        <section class="tarjeta">
            <h2>Confirmar pedido y pago</h2>
            <p class="muted">Tu pedido se registrará en estado <strong>Pendiente</strong> hasta que caja valide el comprobante.</p>

            <c:if test="${empty sessionScope.usuario}">
                <div class="alerta alerta-aviso">Debes <a href="${ctx}/login.jsp">iniciar sesión como cliente</a> para confirmar tu pedido.</div>
            </c:if>

            <form method="post" action="${ctx}/pedido" class="formulario formulario-checkout" id="form-confirmar">
                <input type="hidden" name="action" value="confirmar">

                <fieldset class="grupo-opciones">
                    <legend>Tipo de entrega</legend>
                    <label class="opcion-radio"><input type="radio" name="tipoEntrega" value="Delivery" checked> Delivery</label>
                    <label class="opcion-radio"><input type="radio" name="tipoEntrega" value="Recojo"> Recojo en tienda</label>
                </fieldset>

                <div id="bloque-direccion">
                    <label for="direccion">Dirección de entrega</label>
                    <input type="text" id="direccion" name="direccion" maxlength="255"
                           value="<c:out value='${cliente.direccion}'/>"
                           placeholder="Calle, número, referencia">
                    <p class="ayuda-campo">Se usará como referencia para el delivery (también puedes actualizarla en tu perfil de cliente).</p>
                </div>

                <fieldset class="grupo-opciones">
                    <legend>Método de pago digital</legend>
                    <label class="opcion-radio"><input type="radio" name="metodoPago" value="Yape" checked> Yape</label>
                    <label class="opcion-radio"><input type="radio" name="metodoPago" value="Plin"> Plin</label>
                    <label class="opcion-radio"><input type="radio" name="metodoPago" value="Tarjeta"> Tarjeta</label>
                </fieldset>

                <label for="comprobanteUrl">Comprobante de pago (URL, código o referencia) *</label>
                <input type="text" id="comprobanteUrl" name="comprobanteUrl" required maxlength="255"
                       placeholder="Ej: enlace/código de operación Yape/Plin/Tarjeta">

                <button type="submit" class="boton boton-grande" ${empty sessionScope.usuario ? 'disabled' : ''}
                        onclick="return confirmarPedido()">Confirmar pedido</button>
            </form>
        </section>
    </c:otherwise>
</c:choose>

<script>
(function () {
    var radios = document.querySelectorAll('input[name="tipoEntrega"]');
    var bloque = document.getElementById('bloque-direccion');
    var dir = document.getElementById('direccion');
    if (!radios.length || !bloque) return;
    function actualizar() {
        var delivery = document.querySelector('input[name="tipoEntrega"]:checked');
        var esDelivery = delivery && delivery.value === 'Delivery';
        bloque.style.display = esDelivery ? 'block' : 'none';
        if (dir) dir.required = esDelivery;
    }
    radios.forEach(function (r) { r.addEventListener('change', actualizar); });
    actualizar();
})();
</script>

<%@ include file="/WEB-INF/partials/footer.jspf" %>
