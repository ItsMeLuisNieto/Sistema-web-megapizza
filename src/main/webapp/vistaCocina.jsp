<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Cocina" scope="request" />
<%@ include file="/WEB-INF/partials/header.jspf" %>

<div class="encabezado-seccion">
    <div>
        <h1>Panel de Cocina</h1>
        <p class="subtitulo">Comandas confirmadas y en preparación. Actualiza esta página para ver nuevos pedidos.</p>
    </div>
    <a class="boton boton-suave" href="${ctx}/cocina">Actualizar</a>
</div>

<c:choose>
    <c:when test="${empty pedidos}">
        <section class="tarjeta">
            <h2>No hay comandas en cola</h2>
            <p class="muted">Los pedidos confirmados por caja aparecerán aquí automáticamente.</p>
        </section>
    </c:when>
    <c:otherwise>
        <div class="grid-tarjetas">
            <c:forEach var="pedido" items="${pedidos}">
                <c:set var="estadoClase" value="${fn:toLowerCase(fn:replace(pedido.estadoPedido, ' ', '_'))}" />
                <article class="tarjeta tarjeta-pedido">
                    <header class="tarjeta-pedido-cabecera">
                        <h2>Pedido #${pedido.idPedido}</h2>
                        <span class="estado estado-${estadoClase}"><c:out value="${pedido.estadoPedido}"/></span>
                    </header>
                    <p class="muted">
                        ${fn:replace(fn:substring(pedido.fechaHora, 0, 16), 'T', ' ')}
                        &middot; Cliente: <c:out value="${pedido.nombreCliente}"/>
                        &middot; <c:out value="${pedido.tipoEntrega}"/>
                    </p>
                    <ul class="lista-detalle lista-comanda">
                        <c:forEach var="detalle" items="${pedido.detalles}">
                            <li><strong>${detalle.cantidad}&times;</strong> <c:out value="${detalle.nombreProducto}"/></li>
                        </c:forEach>
                    </ul>
                    <footer class="tarjeta-pedido-pie">
                        <span>Total: <strong>S/ <fmt:formatNumber value="${pedido.montoTotal}" pattern="#,##0.00"/></strong></span>
                        <form method="post" action="${ctx}/cocina">
                            <input type="hidden" name="id" value="${pedido.idPedido}">
                            <c:choose>
                                <c:when test="${pedido.estadoPedido == 'Confirmado'}">
                                    <input type="hidden" name="estado" value="En Preparacion">
                                    <button type="submit" class="boton">Tomar pedido</button>
                                </c:when>
                                <c:when test="${pedido.estadoPedido == 'En Preparacion'}">
                                    <input type="hidden" name="estado" value="Listo">
                                    <button type="submit" class="boton boton-exito">Marcar listo</button>
                                </c:when>
                                <c:otherwise>
                                    <span class="muted">Sin acciones</span>
                                </c:otherwise>
                            </c:choose>
                        </form>
                    </footer>
                </article>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/partials/footer.jspf" %>
