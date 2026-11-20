<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Mis pedidos" scope="request" />
<%@ include file="/WEB-INF/partials/header.jspf" %>

<h1>Mis pedidos</h1>
<p class="subtitulo">Sigue el estado de tus pedidos en tiempo real.</p>

<c:choose>
    <c:when test="${empty pedidos}">
        <section class="tarjeta">
            <h2>Aún no tienes pedidos</h2>
            <p>Cuando confirmes un pedido podrás seguir su estado desde aquí.</p>
            <a class="boton" href="${ctx}/catalogo">Hacer un pedido</a>
        </section>
    </c:when>
    <c:otherwise>
        <section class="tarjeta">
            <div class="tabla-scroll">
                <table class="tabla">
                    <thead>
                    <tr><th>#</th><th>Fecha</th><th>Entrega</th><th>Pago</th><th>Estado</th><th>Detalle</th><th>Total</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="pedido" items="${pedidos}">
                        <c:set var="estadoClase" value="${fn:toLowerCase(fn:replace(pedido.estadoPedido, ' ', '_'))}" />
                        <tr>
                            <td><strong>#${pedido.idPedido}</strong></td>
                            <td>${fn:replace(fn:substring(pedido.fechaHora, 0, 16), 'T', ' ')}</td>
                            <td><c:out value="${pedido.tipoEntrega}"/></td>
                            <td>
                                <c:out value="${pedido.metodoPago}"/>
                                <c:if test="${not empty pedido.comprobanteUrl}">
                                    <br><small class="muted">Comp.: <c:out value="${pedido.comprobanteUrl}"/></small>
                                </c:if>
                            </td>
                            <td><span class="estado estado-${estadoClase}"><c:out value="${pedido.estadoPedido}"/></span></td>
                            <td>
                                <details>
                                    <summary>Ver productos</summary>
                                    <ul class="lista-detalle">
                                        <c:forEach var="detalle" items="${pedido.detalles}">
                                            <li>${detalle.cantidad} &times; <c:out value="${detalle.nombreProducto}"/>
                                                &mdash; S/ <fmt:formatNumber value="${detalle.subtotal}" pattern="#,##0.00"/></li>
                                        </c:forEach>
                                        <c:if test="${empty pedido.detalles}">
                                            <li class="muted">Detalle disponible al consultar el pedido.</li>
                                        </c:if>
                                    </ul>
                                </details>
                            </td>
                            <td><strong>S/ <fmt:formatNumber value="${pedido.montoTotal}" pattern="#,##0.00"/></strong></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/partials/footer.jspf" %>
