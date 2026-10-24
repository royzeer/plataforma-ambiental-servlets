<%-- Formulario para registrar o editar un proyecto. Se envía con method="post". --%>
<%@ include file="/WEB-INF/vistas/comunes/encabezado.jspf" %>
<%-- Declaración JSP: método de apoyo que se compila dentro del servlet generado --%>
<%!
    /** Devuelve la clase CSS "con-error" si el campo tiene un error de validación. */
    private String claseError(java.util.Map<?, ?> errores, String campo) {
        return (errores != null && errores.containsKey(campo)) ? "con-error" : "";
    }
%>
<%
    // Scriptlet: tomamos el mapa de errores que dejó el servlet para usarlo en las expresiones
    java.util.Map<?, ?> errores = (java.util.Map<?, ?>) request.getAttribute("errores");
%>
<c:set var="esEdicion" value="${not empty proyecto.id}"/>

<section class="titulo-seccion">
    <div>
        <h1>${esEdicion ? 'Editar proyecto' : 'Registrar proyecto'}</h1>
        <p class="sutil">Los campos con * son obligatorios.</p>
    </div>
</section>

<form class="tarjeta formulario" method="post" action="${pageContext.request.contextPath}/proyectos" novalidate>
    <input type="hidden" name="accion" value="guardar">
    <input type="hidden" name="id" value="${proyecto.id}">

    <div class="campo ancho <%= claseError(errores, "nombre") %>">
        <label for="nombre">Nombre del proyecto *</label>
        <input id="nombre" name="nombre" type="text" maxlength="150" required
               value="<c:out value='${proyecto.nombre}'/>" placeholder="Ej: Reforestación de la cuenca del río Gaira">
        <small class="msg-error">${errores.nombre}</small>
    </div>

    <div class="campo ancho <%= claseError(errores, "descripcion") %>">
        <label for="descripcion">Descripción</label>
        <textarea id="descripcion" name="descripcion" rows="3" maxlength="1000"><c:out value="${proyecto.descripcion}"/></textarea>
        <small class="msg-error">${errores.descripcion}</small>
    </div>

    <div class="campo <%= claseError(errores, "tipo") %>">
        <label for="tipo">Tipo *</label>
        <select id="tipo" name="tipo" required>
            <option value="">Seleccione...</option>
            <c:forEach var="t" items="${tipos}">
                <option value="${t}" ${t == proyecto.tipo ? 'selected' : ''}>${t}</option>
            </c:forEach>
        </select>
        <small class="msg-error">${errores.tipo}</small>
    </div>

    <div class="campo <%= claseError(errores, "estado") %>">
        <label for="estado">Estado *</label>
        <select id="estado" name="estado">
            <c:forEach var="e" items="${estados}">
                <option value="${e}" ${e == proyecto.estado ? 'selected' : ''}>${e}</option>
            </c:forEach>
        </select>
        <small class="msg-error">${errores.estado}</small>
    </div>

    <div class="campo <%= claseError(errores, "municipio") %>">
        <label for="municipio">Municipio *</label>
        <input id="municipio" name="municipio" type="text" maxlength="80" required
               value="<c:out value='${proyecto.municipio}'/>">
        <small class="msg-error">${errores.municipio}</small>
    </div>

    <div class="campo <%= claseError(errores, "departamento") %>">
        <label for="departamento">Departamento *</label>
        <input id="departamento" name="departamento" type="text" maxlength="80" required
               value="<c:out value='${proyecto.departamento}'/>">
        <small class="msg-error">${errores.departamento}</small>
    </div>

    <div class="campo <%= claseError(errores, "fechaInicio") %>">
        <label for="fechaInicio">Fecha de inicio *</label>
        <input id="fechaInicio" name="fechaInicio" type="date" required value="${proyecto.fechaInicio}">
        <small class="msg-error">${errores.fechaInicio}</small>
    </div>

    <div class="campo <%= claseError(errores, "fechaFin") %>">
        <label for="fechaFin">Fecha de fin</label>
        <input id="fechaFin" name="fechaFin" type="date" value="${proyecto.fechaFin}">
        <small class="msg-error">${errores.fechaFin}</small>
    </div>

    <div class="campo <%= claseError(errores, "presupuesto") %>">
        <label for="presupuesto">Presupuesto (COP)</label>
        <input id="presupuesto" name="presupuesto" type="text" inputmode="decimal"
               value="${proyecto.presupuesto}" placeholder="Ej: 120000000">
        <small class="msg-error">${errores.presupuesto}</small>
    </div>

    <div class="campo <%= claseError(errores, "responsableId") %>">
        <label for="responsableId">Responsable *</label>
        <select id="responsableId" name="responsableId" required>
            <option value="">Seleccione...</option>
            <c:forEach var="u" items="${responsables}">
                <option value="${u.id}" ${u.id == proyecto.responsableId ? 'selected' : ''}>
                    <c:out value="${u.nombre}"/> (${u.rol})
                </option>
            </c:forEach>
        </select>
        <small class="msg-error">${errores.responsableId}</small>
    </div>

    <div class="botones ancho">
        <button type="submit" class="boton">${esEdicion ? 'Guardar cambios' : 'Registrar proyecto'}</button>
        <a class="boton secundario" href="${pageContext.request.contextPath}/proyectos">Cancelar</a>
    </div>
</form>

<jsp:include page="/WEB-INF/vistas/comunes/pie.jsp"/>
