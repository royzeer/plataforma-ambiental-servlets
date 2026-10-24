<%-- Página de error general (configurada en web.xml) --%>
<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Algo salió mal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<main class="contenedor">
    <div class="tarjeta confirmar">
        <h1>Algo salió mal</h1>
        <p>Código: ${requestScope['jakarta.servlet.error.status_code']}</p>
        <p class="sutil">
            <%= exception != null ? exception.getMessage() : "La página que busca no existe." %>
        </p>
        <a class="boton" href="${pageContext.request.contextPath}/proyectos">Ir al listado de proyectos</a>
    </div>
</main>
</body>
</html>
