package co.edu.sena.impactoambiental.servlet;

import co.edu.sena.impactoambiental.dao.ProyectoDAO;
import co.edu.sena.impactoambiental.dao.UsuarioDAO;
import co.edu.sena.impactoambiental.modelo.Proyecto;
import co.edu.sena.impactoambiental.util.ValidadorProyecto;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador del módulo de proyectos.
 *
 * Usa los dos métodos HTTP como pide la evidencia:
 *  - GET  : todo lo que solo consulta (listar, buscar, ver detalle, abrir formularios).
 *  - POST : todo lo que cambia datos (crear, actualizar, eliminar).
 *
 * Después de un POST exitoso hacemos redirect (patrón Post-Redirect-Get) para que,
 * si el usuario recarga la página, el formulario no se envíe dos veces.
 *
 * URL: /proyectos?accion=...
 *
 * @author Roycer Cervantes
 * @author Sebastián Rodríguez
 */
@WebServlet(name = "ProyectoServlet", urlPatterns = {"/proyectos"})
public class ProyectoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VISTA_LISTA = "/WEB-INF/vistas/proyectos/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/proyectos/formulario.jsp";
    private static final String VISTA_DETALLE = "/WEB-INF/vistas/proyectos/detalle.jsp";
    private static final String VISTA_ELIMINAR = "/WEB-INF/vistas/proyectos/eliminar.jsp";

    private ProyectoDAO proyectoDAO;
    private UsuarioDAO usuarioDAO;

    /** Se ejecuta una vez cuando Tomcat carga el servlet. */
    @Override
    public void init() {
        proyectoDAO = new ProyectoDAO();
        usuarioDAO = new UsuarioDAO();
    }

    // =====================================================================
    //  GET: consultas
    // =====================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = valorOVacio(request.getParameter("accion"));

        try {
            switch (accion) {
                case "nuevo" -> mostrarFormularioNuevo(request, response);
                case "editar" -> mostrarFormularioEditar(request, response);
                case "ver" -> mostrarDetalle(request, response);
                case "eliminar" -> mostrarConfirmacionEliminar(request, response);
                default -> listarProyectos(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException("Error consultando la base de datos: " + e.getMessage(), e);
        }
    }

    // =====================================================================
    //  POST: cambios en los datos
    // =====================================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8"); // para que lleguen bien las tildes y la ñ
        String accion = valorOVacio(request.getParameter("accion"));

        try {
            switch (accion) {
                case "guardar" -> guardarProyecto(request, response);
                case "eliminar" -> eliminarProyecto(request, response);
                default -> response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no válida");
            }
        } catch (SQLException e) {
            throw new ServletException("Error guardando en la base de datos: " + e.getMessage(), e);
        }
    }

    // ----------------------------- Acciones GET -----------------------------

    /** Lista los proyectos aplicando los filtros del formulario de búsqueda (GET). */
    private void listarProyectos(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String buscar = request.getParameter("buscar");
        String estado = request.getParameter("estado");

        request.setAttribute("proyectos", proyectoDAO.listar(buscar, estado));
        request.setAttribute("buscar", buscar);
        request.setAttribute("estadoFiltro", estado);
        request.setAttribute("estados", ValidadorProyecto.ESTADOS_VALIDOS);
        moverMensajeDeSesion(request);
        request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
    }

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Proyecto proyecto = new Proyecto();
        proyecto.setEstado("planeacion");
        abrirFormulario(request, response, proyecto, new HashMap<>());
    }

    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Optional<Proyecto> proyecto = buscarProyectoDesdeParametro(request);
        if (proyecto.isEmpty()) {
            redirigirConMensaje(request, response, "error", "El proyecto que busca no existe.");
            return;
        }
        abrirFormulario(request, response, proyecto.get(), new HashMap<>());
    }

    private void mostrarDetalle(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Optional<Proyecto> proyecto = buscarProyectoDesdeParametro(request);
        if (proyecto.isEmpty()) {
            redirigirConMensaje(request, response, "error", "El proyecto que busca no existe.");
            return;
        }
        request.setAttribute("proyecto", proyecto.get());
        moverMensajeDeSesion(request);
        request.getRequestDispatcher(VISTA_DETALLE).forward(request, response);
    }

    private void mostrarConfirmacionEliminar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Optional<Proyecto> proyecto = buscarProyectoDesdeParametro(request);
        if (proyecto.isEmpty()) {
            redirigirConMensaje(request, response, "error", "El proyecto que busca no existe.");
            return;
        }
        request.setAttribute("proyecto", proyecto.get());
        request.getRequestDispatcher(VISTA_ELIMINAR).forward(request, response);
    }

    // ----------------------------- Acciones POST ----------------------------

    /**
     * Crea o actualiza: si el formulario trae id es una edición, si no, es nuevo.
     * Si hay errores de validación se devuelve el mismo formulario con los datos escritos.
     */
    private void guardarProyecto(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Map<String, String> campos = new HashMap<>();
        for (String campo : new String[]{"nombre", "descripcion", "tipo", "municipio", "departamento",
                "fechaInicio", "fechaFin", "presupuesto", "estado", "responsableId"}) {
            campos.put(campo, request.getParameter(campo));
        }

        Proyecto proyecto = new Proyecto();
        String id = valorOVacio(request.getParameter("id"));
        if (!id.isEmpty()) {
            proyecto.setId(Integer.valueOf(id));
        }

        Map<String, String> errores = ValidadorProyecto.validarYLlenar(campos, proyecto);
        if (proyecto.getResponsableId() != null && !usuarioDAO.esResponsableValido(proyecto.getResponsableId())) {
            errores.put("responsableId", "El responsable seleccionado no es válido.");
        }

        if (!errores.isEmpty()) {
            // Se devuelve el formulario con lo que el usuario escribió y los mensajes de error
            request.setAttribute("mensajeError", "Revise los campos marcados en rojo.");
            abrirFormulario(request, response, proyecto, errores);
            return;
        }

        if (proyecto.getId() == null) {
            int nuevoId = proyectoDAO.insertar(proyecto);
            request.getSession().setAttribute("mensajeExito",
                    "Proyecto \"" + proyecto.getNombre() + "\" registrado correctamente.");
            response.sendRedirect(request.getContextPath() + "/proyectos?accion=ver&id=" + nuevoId);
        } else {
            proyectoDAO.actualizar(proyecto);
            request.getSession().setAttribute("mensajeExito", "Los cambios del proyecto se guardaron.");
            response.sendRedirect(request.getContextPath() + "/proyectos?accion=ver&id=" + proyecto.getId());
        }
    }

    private void eliminarProyecto(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        Optional<Proyecto> proyecto = buscarProyectoDesdeParametro(request);
        if (proyecto.isPresent() && proyectoDAO.eliminar(proyecto.get().getId())) {
            redirigirConMensaje(request, response, "exito",
                    "Se eliminó el proyecto \"" + proyecto.get().getNombre() + "\".");
        } else {
            redirigirConMensaje(request, response, "error", "No se pudo eliminar: el proyecto no existe.");
        }
    }

    // ------------------------------ Utilidades ------------------------------

    private void abrirFormulario(HttpServletRequest request, HttpServletResponse response,
                                 Proyecto proyecto, Map<String, String> errores)
            throws SQLException, ServletException, IOException {

        request.setAttribute("proyecto", proyecto);
        request.setAttribute("errores", errores);
        request.setAttribute("tipos", ValidadorProyecto.TIPOS_VALIDOS);
        request.setAttribute("estados", ValidadorProyecto.ESTADOS_VALIDOS);
        request.setAttribute("responsables", usuarioDAO.listarResponsables());
        request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
    }

    /** Lee el parámetro "id"; si no es un número válido devuelve vacío. */
    private Optional<Proyecto> buscarProyectoDesdeParametro(HttpServletRequest request) throws SQLException {
        try {
            int id = Integer.parseInt(valorOVacio(request.getParameter("id")));
            return proyectoDAO.buscarPorId(id);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private void redirigirConMensaje(HttpServletRequest request, HttpServletResponse response,
                                     String tipo, String mensaje) throws IOException {
        String atributo = "exito".equals(tipo) ? "mensajeExito" : "mensajeError";
        request.getSession().setAttribute(atributo, mensaje);
        response.sendRedirect(request.getContextPath() + "/proyectos");
    }

    /** Pasa los mensajes guardados en sesión al request y los borra (mensaje "flash"). */
    private void moverMensajeDeSesion(HttpServletRequest request) {
        for (String atributo : new String[]{"mensajeExito", "mensajeError"}) {
            Object mensaje = request.getSession().getAttribute(atributo);
            if (mensaje != null) {
                request.setAttribute(atributo, mensaje);
                request.getSession().removeAttribute(atributo);
            }
        }
    }

    private static String valorOVacio(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
