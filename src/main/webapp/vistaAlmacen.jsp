<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Almacén" scope="request" />
<%@ include file="/WEB-INF/partials/header.jspf" %>

<h1>Gestión de Almacén e Inventario</h1>
<p class="subtitulo">Control de insumos, alertas de stock mínimo, ingresos y mermas.</p>

<c:if test="${not empty alertas}">
    <section class="alerta alerta-aviso" role="alert">
        <strong>⚠️ Alertas de stock mínimo:</strong>
        <ul class="lista-alertas">
            <c:forEach var="insumo" items="${alertas}">
                <li><c:out value="${insumo.nombreInsumo}"/> —
                    stock actual <strong><fmt:formatNumber value="${insumo.stockActual}" pattern="#,##0.##"/></strong>
                    ${insumo.unidadMedida} (mínimo <fmt:formatNumber value="${insumo.stockMinimo}" pattern="#,##0.##"/>)</li>
            </c:forEach>
        </ul>
    </section>
</c:if>

<section class="tarjeta">
    <h2>Insumos</h2>
    <c:choose>
        <c:when test="${empty insumos}">
            <p class="muted">No hay insumos registrados en el inventario.</p>
        </c:when>
        <c:otherwise>
            <div class="tabla-scroll">
                <table class="tabla tabla-compacta">
                    <thead>
                    <tr>
                        <th>Insumo</th><th>Stock actual</th><th>Stock mínimo</th><th>Unidad</th>
                        <th>Registrar entrada</th><th>Registrar merma</th><th>Ajustar stock a</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="insumo" items="${insumos}">
                        <c:set var="filaAlerta" value="${insumo.stockActual <= insumo.stockMinimo ? 'fila-alerta' : ''}" />
                        <tr class="${filaAlerta}">
                            <td>
                                <strong><c:out value="${insumo.nombreInsumo}"/></strong>
                                <c:if test="${insumo.stockActual <= insumo.stockMinimo}">
                                    <span class="punto-alerta" title="Stock bajo">● bajo</span>
                                </c:if>
                            </td>
                            <td><fmt:formatNumber value="${insumo.stockActual}" pattern="#,##0.##"/></td>
                            <td><fmt:formatNumber value="${insumo.stockMinimo}" pattern="#,##0.##"/></td>
                            <td><c:out value="${insumo.unidadMedida}"/></td>
                            <td>
                                <form method="post" action="${ctx}/almacen" class="form-inline">
                                    <input type="hidden" name="action" value="entrada">
                                    <input type="hidden" name="idInsumo" value="${insumo.idInsumo}">
                                    <label class="sr-only" for="ent-${insumo.idInsumo}">Cantidad entrada</label>
                                    <input type="number" id="ent-${insumo.idInsumo}" name="cantidad" step="0.01" min="0.01" placeholder="Cant." required>
                                    <button type="submit" class="boton boton-exito boton-mini">+ Entrada</button>
                                </form>
                            </td>
                            <td>
                                <form method="post" action="${ctx}/almacen" class="form-inline"
                                      onsubmit="return confirm('¿Registrar merma para este insumo?');">
                                    <input type="hidden" name="action" value="merma">
                                    <input type="hidden" name="idInsumo" value="${insumo.idInsumo}">
                                    <label class="sr-only" for="mer-${insumo.idInsumo}">Cantidad merma</label>
                                    <input type="number" id="mer-${insumo.idInsumo}" name="cantidad" step="0.01" min="0.01" placeholder="Cant." required>
                                    <button type="submit" class="boton boton-peligro boton-mini">Merma</button>
                                </form>
                            </td>
                            <td>
                                <form method="post" action="${ctx}/almacen" class="form-inline">
                                    <input type="hidden" name="action" value="ajuste">
                                    <input type="hidden" name="idInsumo" value="${insumo.idInsumo}">
                                    <label class="sr-only" for="aju-${insumo.idInsumo}">Nuevo stock</label>
                                    <input type="number" id="aju-${insumo.idInsumo}" name="cantidad" step="0.01" min="0"
                                           value="<fmt:formatNumber value='${insumo.stockActual}' pattern='0.##'/>" required>
                                    <button type="submit" class="boton boton-suave boton-mini">Aplicar</button>
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
