<%-- Pie de página. Se incluye con la acción <jsp:include> (se ejecuta en cada petición) --%>
<%@ page contentType="text/html;charset=UTF-8" import="java.time.Year" %>
</main>
<footer class="pie">
    Plataforma Digital Integral para la Gestión de Proyectos de Impacto Ambiental &middot;
    SENA ADSO 228118 &middot; Roycer Cervantes y Sebastián Rodríguez &middot; <%= Year.now() %>
</footer>
</body>
</html>
