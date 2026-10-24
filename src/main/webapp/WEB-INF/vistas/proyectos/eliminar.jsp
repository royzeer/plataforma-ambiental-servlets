<%-- Confirmación antes de eliminar. El borrado se hace solo por POST, nunca por un enlace GET. --%>
<%@ include file="/WEB-INF/vistas/comunes/encabezado.jspf" %>

<div class="tarjeta confirmar">
    <h1>¿Eliminar este proyecto?</h1>
    <p>Va a eliminar <strong><c:out value="${proyecto.nombre}"/></strong> (${proyecto.codigo}).</p>
    <p class="sutil">También se borrarán sus impactos, actividades y monitoreos. Esta acción no se puede deshacer.</p>

    <form method="post" action="${pageContext.request.contextPath}/proyectos">
        <input type="hidden" name="accion" value="eliminar">
        <input type="hidden" name="id" value="${proyecto.id}">
        <div class="botones">
            <button type="submit" class="boton peligro-btn">Sí, eliminar</button>
            <a class="boton secundario" href="${pageContext.request.contextPath}/proyectos?accion=ver&id=${proyecto.id}">Cancelar</a>
        </div>
    </form>
</div>

<jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>
