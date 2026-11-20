<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Caja" scope="request" />
<%@ include file="/WEB-INF/partials/header.jspf" %>

<h1>Panel de Caja</h1>
<p class="subtitulo">Valida comprobantes de pago digital y registra entregas.</p>

<div class="kpis">
    <div class="kpi">
        <span class="kpi-etiqueta">Total vendido (entregado)</span>
        <strong class="kpi-valor">S/ <fmt:formatNumber value="${empty totalVendido ? 0 : totalVendido}" pattern="#,##0.00"/></strong>
    </div>
    <div class="kpi">
        <span class="kpi-etiqueta">Pagos pendientes</span>
        <strong class="kpi-valor">${fn:length(pedidos)}</strong>
    </div>
    <div class="kpi">
        <span class="kpi-etiqueta">Listos para entrega</span>
        <strong class="kpi-valor">${fn:length(listos)}</strong>
    </div>
</div>

<section class="tarjeta">
    <h2>Pagos pendientes de validación</h2>
    <c:choose>
        <c:when test="${empty pedidos}">
            <p class="muted">No hay pedidos en estado Pendiente.</p>
        </c:when>
        <c:otherwise>
            <div class="tabla-scroll">
                <table class="tabla">
                    <thead>
                    <tr><th>#</th><th>Cliente</th><th>Fecha</th><th>Entrega</th><th>Pago / Comprobante</th><th>Total</th><th>Acciones</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="pedido" items="${pedidos}">
                        <tr>
                            <td><strong>#${pedido.idPedido}</strong></td>
                            <td><c:out value="${pedido.nombreCliente}"/></td>
                            <td>${fn:replace(fn:substring(pedido.fechaHora, 0, 16), 'T', ' ')}</td>
                            <td><c:out value="${pedido.tipoEntrega}"/></td>
                            <td>
                                <strong><c:out value="${pedido.metodoPago}"/></strong><br>
                                <small class="muted">Comprobante: <c:out value="${pedido.comprobanteUrl}"/></small>
                            </td>
                            <td>S/ <fmt:formatNumber value="${pedido.montoTotal}" pattern="#,##0.00"/></td>
                            <td class="acciones-celda">
                                <form method="post" action="${ctx}/caja" class="accion-form">
                                    <input type="hidden" name="id" value="${pedido.idPedido}">
                                    <input type="hidden" name="action" value="confirmar">
                                    <button type="submit" class="boton boton-exito boton-mini">Confirmar</button>
                                </form>
                                <form method="post" action="${ctx}/caja" class="accion-form" data-rechazo
                                      data-id-pedido="${pedido.idPedido}">
                                    <input type="hidden" name="id" value="${pedido.idPedido}">
                                    <input type="hidden" name="action" value="rechazar">
                                    <button type="submit" class="boton boton-peligro boton-mini">Rechazar</button>
                                </form>
                                <details class="detalle-inline">
                                    <summary>Ver detalle</summary>
                                    <ul class="lista-detalle">
                                        <c:forEach var="d" items="${pedido.detalles}">
                                            <li>${d.cantidad} &times; <c:out value="${d.nombreProducto}"/></li>
                                        </c:forEach>
                                    </ul>
                                </details>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<section class="tarjeta">
    <h2>Pedidos listos para entrega</h2>
    <c:choose>
        <c:when test="${empty listos}">
            <p class="muted">No hay pedidos listos por entregar.</p>
        </c:when>
        <c:otherwise>
            <div class="tabla-scroll">
                <table class="tabla">
                    <thead>
                    <tr><th>#</th><th>Cliente</th><th>Entrega</th><th>Pago</th><th>Total</th><th>Acción</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="pedido" items="${listos}">
                        <tr>
                            <td><strong>#${pedido.idPedido}</strong></td>
                            <td><c:out value="${pedido.nombreCliente}"/></td>
                            <td><span class="estado estado-listo"><c:out value="${pedido.tipoEntrega}"/></span></td>
                            <td><c:out value="${pedido.metodoPago}"/></td>
                            <td>S/ <fmt:formatNumber value="${pedido.montoTotal}" pattern="#,##0.00"/></td>
                            <td>
                                <form method="post" action="${ctx}/caja" class="accion-form">
                                    <input type="hidden" name="id" value="${pedido.idPedido}">
                                    <input type="hidden" name="action" value="entregar">
                                    <button type="submit" class="boton boton-exito">Registrar entrega</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="/WEB-INF/partials/footer.jspf" %>
