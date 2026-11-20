<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Acceso | MegaPizza</title>
    <link rel="stylesheet" href="${ctx}/static/css/estilos.css">
</head>
<body class="fondo-inicio">
<div class="login-caja login-caja-amplia">
    <div class="login-encabezado">
        <div class="inicio-marca" aria-label="MegaPizza">🍕 Mega<span>Pizza</span></div>
        <h1>Bienvenido</h1>
        <p>Inicia sesión o crea tu cuenta de cliente</p>
    </div>

    <c:if test="${not empty param.error}">
        <div class="alerta alerta-error" role="alert">
            <c:choose>
                <c:when test="${param.error == 'credenciales'}">Usuario o contraseña incorrectos.</c:when>
                <c:otherwise><c:out value="${param.error}"/></c:otherwise>
            </c:choose>
        </div>
    </c:if>
    <c:if test="${not empty param.ok}">
        <div class="alerta alerta-ok" role="status"><c:out value="${param.ok}"/></div>
    </c:if>

    <div class="tabs-auth" role="tablist" aria-label="Acceso">
        <button type="button" class="tab-auth activa" data-tab="login" role="tab" aria-selected="true">Iniciar sesión</button>
        <button type="button" class="tab-auth" data-tab="registro" role="tab" aria-selected="false">Registrarse</button>
    </div>

    <section id="tab-login" class="panel-auth activo" role="tabpanel">
        <form method="post" action="${ctx}/auth" class="formulario">
            <label for="login-username">Usuario</label>
            <input type="text" id="login-username" name="username" required autocomplete="username" placeholder="Tu usuario">

            <label for="login-password">Contraseña</label>
            <input type="password" id="login-password" name="password" required autocomplete="current-password" placeholder="Tu contraseña">

            <button type="submit" class="boton boton-ancho">Ingresar</button>
        </form>

        <details class="demo">
            <summary>Usuarios de demostración</summary>
            <p class="ayuda-campo">Contraseña para todos: <strong>123456</strong> (según <code>database/data.sql</code>).</p>
            <table class="tabla tabla-demo">
                <thead><tr><th>Usuario</th><th>Contraseña</th><th>Rol</th></tr></thead>
                <tbody>
                <tr><td>cliente</td><td>123456</td><td>Cliente</td></tr>
                <tr><td>cajero</td><td>123456</td><td>Cajero</td></tr>
                <tr><td>cocina</td><td>123456</td><td>Cocinero</td></tr>
                <tr><td>almacen</td><td>123456</td><td>Almacén</td></tr>
                <tr><td>admin</td><td>123456</td><td>Administrador</td></tr>
                </tbody>
            </table>
        </details>
    </section>

    <section id="tab-registro" class="panel-auth" role="tabpanel" hidden>
        <form method="post" action="${ctx}/auth" class="formulario">
            <input type="hidden" name="action" value="registro">

            <div class="form-grid">
                <div>
                    <label for="reg-username">Usuario *</label>
                    <input type="text" id="reg-username" name="username" required maxlength="50" autocomplete="username">
                </div>
                <div>
                    <label for="reg-password">Contraseña *</label>
                    <input type="password" id="reg-password" name="password" required autocomplete="new-password">
                </div>
            </div>

            <label for="reg-nombre">Nombre completo *</label>
            <input type="text" id="reg-nombre" name="nombre" required maxlength="100" autocomplete="name">

            <div class="form-grid">
                <div>
                    <label for="reg-telefono">Teléfono</label>
                    <input type="text" id="reg-telefono" name="telefono" maxlength="15" autocomplete="tel">
                </div>
                <div>
                    <label for="reg-correo">Correo</label>
                    <input type="email" id="reg-correo" name="correo" maxlength="100" autocomplete="email">
                </div>
            </div>

            <label for="reg-direccion">Dirección (para delivery)</label>
            <input type="text" id="reg-direccion" name="direccion" maxlength="255" autocomplete="street-address">

            <button type="submit" class="boton boton-ancho">Crear cuenta de cliente</button>
        </form>
    </section>

    <p class="volver-catalogo"><a href="${ctx}/catalogo">← Volver al catálogo</a></p>
</div>
<script>
(function () {
    var tabs = document.querySelectorAll('.tab-auth');
    function activar(nombre) {
        tabs.forEach(function (t) {
            var activo = t.getAttribute('data-tab') === nombre;
            t.classList.toggle('activa', activo);
            t.setAttribute('aria-selected', activo ? 'true' : 'false');
        });
        document.querySelectorAll('.panel-auth').forEach(function (p) {
            var activo = p.id === 'tab-' + nombre;
            p.classList.toggle('activo', activo);
            p.hidden = !activo;
        });
    }
    tabs.forEach(function (t) {
        t.addEventListener('click', function () { activar(t.getAttribute('data-tab')); });
    });
    if (new URLSearchParams(window.location.search).get('registro') === '1') {
        activar('registro');
    }
})();
</script>
</body>
</html>
