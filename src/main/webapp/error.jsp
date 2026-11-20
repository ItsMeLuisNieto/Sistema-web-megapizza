<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Error" scope="request" />
<c:set var="statusCode" value="${requestScope['jakarta.servlet.error.status_code']}" />
<c:set var="uri" value="${requestScope['jakarta.servlet.error.request_uri']}" />
<%@ include file="/WEB-INF/partials/header.jspf" %>

<section class="tarjeta tarjeta-error">
    <c:choose>
        <c:when test="${statusCode == 403}">
            <h1>403 - Acceso denegado</h1>
            <p>No tienes permisos para acceder a esta sección.</p>
        </c:when>
        <c:when test="${statusCode == 404}">
            <h1>404 - Página no encontrada</h1>
            <p>El recurso solicitado no existe o fue movido.</p>
        </c:when>
        <c:when test="${statusCode == 500}">
            <h1>500 - Error interno</h1>
            <p>Ocurrió un problema al procesar tu solicitud. Intenta nuevamente en unos momentos.</p>
        </c:when>
        <c:otherwise>
            <h1>Ha ocurrido un error</h1>
            <p>No pudimos completar tu solicitud.</p>
            <c:if test="${not empty statusCode}">
                <p class="muted">Código: <c:out value="${statusCode}"/></p>
            </c:if>
        </c:otherwise>
    </c:choose>
    <c:if test="${not empty uri}">
        <p class="muted"><small>Ruta: <c:out value="${uri}"/></small></p>
    </c:if>
    <div class="acciones">
        <a class="boton" href="${ctx}/catalogo">Ir al catálogo</a>
        <a class="boton boton-suave" href="javascript:history.back()">Volver</a>
    </div>
</section>

<%@ include file="/WEB-INF/partials/footer.jspf" %>
