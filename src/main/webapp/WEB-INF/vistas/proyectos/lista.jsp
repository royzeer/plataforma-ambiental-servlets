<%-- Listado de proyectos con buscador. El formulario de búsqueda usa method="get". --%>
<%@ include file="/WEB-INF/vistas/comunes/encabezado.jspf" %>

<section class="titulo-seccion">
    <div>
        <h1>Proyectos de impacto ambiental</h1>
        <p class="sutil">Consulte, registre y actualice los proyectos de la plataforma.</p>
    </div>
    <a class="boton" href="${pageContext.request.contextPath}/proyectos?accion=nuevo">+ Registrar proyecto</a>
</section>

<%-- Formulario GET: los filtros quedan en la URL y se pueden compartir --%>
<form class="filtros" method="get" action="${pageContext.request.contextPath}/proyectos">
    <input type="text" name="buscar" maxlength="60" placeholder="Buscar por nombre o municipio"
           value="<c:out value='${buscar}'/>" aria-label="Buscar">
    <select name="estado" aria-label="Estado">
        <option value="">Todos los estados</option>
        <c:forEach var="e" items="${estados}">
            <option value="${e}" ${e == estadoFiltro ? 'selected' : ''}>${e}</option>
        </c:forEach>
    </select>
    <button type="submit" class="boton secundario">Buscar</button>
    <a class="enlace" href="${pageContext.request.contextPath}/proyectos">Limpiar</a>
</form>

<c:choose>
    <c:when test="${empty proyectos}">
        <div class="vacio">No se encontraron proyectos con esos filtros.</div>
    </c:when>
    <c:otherwise>
        <p class="sutil">${proyectos.size()} proyecto(s) encontrado(s)</p>
        <div class="tabla-envoltura">
        <table>
            <thead>
            <tr>
                <th>Código</th><th>Nombre</th><th>Tipo</th><th>Municipio</th>
                <th>Inicio</th><th class="num">Presupuesto</th><th>Estado</th><th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="p" items="${proyectos}">
                <tr>
                    <td class="mono">${p.codigo}</td>
                    <td><c:out value="${p.nombre}"/></td>
                    <td>${p.tipo}</td>
                    <td><c:out value="${p.municipio}"/>, <c:out value="${p.departamento}"/></td>
                    <td class="fecha">${p.fechaInicio}</td>
                    <td class="num">$ <fmt:formatNumber value="${p.presupuesto}" type="number" maxFractionDigits="0"/></td>
                    <td><span class="estado ${p.estado}">${p.estado}</span></td>
                    <td class="acciones">
                        <a href="${pageContext.request.contextPath}/proyectos?accion=ver&id=${p.id}">Ver</a>
                        <a href="${pageContext.request.contextPath}/proyectos?accion=editar&id=${p.id}">Editar</a>
                        <a class="peligro" href="${pageContext.request.contextPath}/proyectos?accion=eliminar&id=${p.id}">Eliminar</a>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>
