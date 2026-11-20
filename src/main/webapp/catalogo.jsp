<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="titulo" value="Catálogo" scope="request" />
<%@ include file="/WEB-INF/partials/header.jspf" %>

<section class="hero">
    <div>
        <h1>¿Qué se te antoja hoy?</h1>
        <p>Elige tus favoritos de nuestra carta de pizzas, pastas, bebidas y combos.</p>
    </div>
    <div class="hero-icono" aria-hidden="true">🍕</div>
</section>

<form class="buscador" method="get" action="${ctx}/catalogo" role="search">
    <label for="q" class="sr-only">Buscar en la carta</label>
    <input type="search" id="q" name="q" value="<c:out value='${filtro}'/>" placeholder="Buscar pizza, pasta, bebida o combo...">
    <button type="submit" class="boton">Buscar</button>
    <a class="boton boton-suave" href="${ctx}/catalogo">Ver todo</a>
</form>

<div class="chips" aria-label="Filtrar por categoría">
    <a class="chip ${empty filtro ? 'activo' : ''}" href="${ctx}/catalogo">Todas</a>
    <c:forEach var="categoria" items="${categorias}">
        <a class="chip ${filtro == categoria ? 'activo' : ''}"
           href="${ctx}/catalogo?q=<c:out value='${categoria}'/>"><c:out value="${categoria}"/></a>
    </c:forEach>
</div>

<c:choose>
    <c:when test="${empty productos}">
        <section class="tarjeta">
            <h2>No encontramos productos</h2>
            <p>Prueba con otro término o vuelve al catálogo completo.</p>
            <a class="boton" href="${ctx}/catalogo">Ver catálogo</a>
        </section>
    </c:when>
    <c:otherwise>
        <div class="grid-productos">
            <c:forEach var="producto" items="${productos}">
                <article class="tarjeta-producto">
                    <div class="producto-imagen">
                        <c:choose>
                            <c:when test="${not empty producto.imagenUrl}">
                                <c:set var="imgSrc" value="${producto.imagenUrl}" />
                                <c:choose>
                                    <c:when test="${fn:startsWith(imgSrc, 'http://') or fn:startsWith(imgSrc, 'https://') or fn:startsWith(imgSrc, '/')}">
                                        <img src="<c:out value='${imgSrc}'/>" alt="<c:out value='${producto.nombre}'/>">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${ctx}/<c:out value='${imgSrc}'/>" alt="<c:out value='${producto.nombre}'/>">
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise><span aria-hidden="true">🍕</span></c:otherwise>
                        </c:choose>
                    </div>
                    <div class="producto-cuerpo">
                        <span class="etiqueta"><c:out value="${producto.categoria}"/></span>
                        <h3><c:out value="${producto.nombre}"/></h3>
                        <p class="descripcion"><c:out value="${producto.descripcion}"/></p>
                        <div class="producto-pie">
                            <strong class="precio">S/ <fmt:formatNumber value="${producto.precioBase}" pattern="#,##0.00"/></strong>
                            <button type="button" class="boton" data-agregar
                                    data-id-producto="${producto.idProducto}">Agregar +</button>
                        </div>
                    </div>
                </article>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/partials/footer.jspf" %>
