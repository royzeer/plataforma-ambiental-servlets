<%-- Detalle de un proyecto (consulta por GET: /proyectos?accion=ver&id=...) --%>
<%@ include file="/WEB-INF/vistas/comunes/encabezado.jspf" %>

<section class="titulo-seccion">
    <div>
        <p class="mono sutil">${proyecto.codigo}</p>
        <h1><c:out value="${proyecto.nombre}"/></h1>
    </div>
    <span class="estado grande ${proyecto.estado}">${proyecto.estado}</span>
</section>

<div class="tarjeta">
    <dl class="datos">
        <dt>Descripción</dt><dd><c:out value="${empty proyecto.descripcion ? 'Sin descripción' : proyecto.descripcion}"/></dd>
        <dt>Tipo</dt><dd>${proyecto.tipo}</dd>
        <dt>Ubicación</dt><dd><c:out value="${proyecto.municipio}"/>, <c:out value="${proyecto.departamento}"/></dd>
        <dt>Fecha de inicio</dt><dd>${proyecto.fechaInicio}</dd>
        <dt>Fecha de fin</dt><dd>${empty proyecto.fechaFin ? 'Sin definir' : proyecto.fechaFin}</dd>
        <dt>Presupuesto</dt><dd>$ <fmt:formatNumber value="${proyecto.presupuesto}" type="number" maxFractionDigits="2"/> COP</dd>
        <dt>Responsable</dt><dd><c:out value="${proyecto.nombreResponsable}"/></dd>
    </dl>
    <div class="botones">
        <a class="boton" href="${pageContext.request.contextPath}/proyectos?accion=editar&id=${proyecto.id}">Editar</a>
        <a class="boton peligro-btn" href="${pageContext.request.contextPath}/proyectos?accion=eliminar&id=${proyecto.id}">Eliminar</a>
        <a class="boton secundario" href="${pageContext.request.contextPath}/proyectos">Volver al listado</a>
    </div>
</div>

<jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>
