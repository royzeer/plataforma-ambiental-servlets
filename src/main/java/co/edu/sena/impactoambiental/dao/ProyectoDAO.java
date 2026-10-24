package co.edu.sena.impactoambiental.dao;

import co.edu.sena.impactoambiental.config.ConexionBD;
import co.edu.sena.impactoambiental.modelo.Proyecto;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla "proyectos" (patrón DAO).
 *
 * Aquí está todo el SQL del módulo: insertar, consultar, actualizar y eliminar.
 * Siempre usamos PreparedStatement con parámetros (?) para evitar inyección SQL.
 *
 * @author Roycer Cervantes
 * @author Sebastián Rodríguez
 */
public class ProyectoDAO {

    /** Consulta base con el nombre del responsable (JOIN con usuarios). */
    private static final String SQL_SELECT_BASE =
            "SELECT p.id, p.codigo, p.nombre, p.descripcion, p.tipo, p.municipio, p.departamento, "
            + "p.fecha_inicio, p.fecha_fin, p.presupuesto, p.estado, p.responsable_id, "
            + "u.nombre AS nombre_responsable "
            + "FROM proyectos p INNER JOIN usuarios u ON u.id = p.responsable_id ";

    private static final String SQL_INSERTAR =
            "INSERT INTO proyectos (nombre, descripcion, tipo, municipio, departamento, "
            + "fecha_inicio, fecha_fin, presupuesto, estado, responsable_id) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR_CODIGO =
            "UPDATE proyectos SET codigo = ? WHERE id = ?";

    private static final String SQL_ACTUALIZAR =
            "UPDATE proyectos SET nombre = ?, descripcion = ?, tipo = ?, municipio = ?, "
            + "departamento = ?, fecha_inicio = ?, fecha_fin = ?, presupuesto = ?, estado = ?, "
            + "responsable_id = ? WHERE id = ?";

    private static final String SQL_ELIMINAR = "DELETE FROM proyectos WHERE id = ?";

    /**
     * Lista los proyectos. Si llega texto de búsqueda, filtra por nombre o municipio;
     * si llega un estado, filtra también por estado.
     *
     * @param textoBusqueda texto a buscar (puede ser null o vacío)
     * @param estado        estado del proyecto (puede ser null o vacío)
     * @return lista de proyectos ordenada del más reciente al más antiguo
     * @throws SQLException si falla la consulta
     */
    public List<Proyecto> listar(String textoBusqueda, String estado) throws SQLException {
        StringBuilder sql = new StringBuilder(SQL_SELECT_BASE).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            sql.append("AND (p.nombre LIKE ? OR p.municipio LIKE ?) ");
            String patron = "%" + textoBusqueda.trim() + "%";
            parametros.add(patron);
            parametros.add(patron);
        }
        if (estado != null && !estado.isBlank()) {
            sql.append("AND p.estado = ? ");
            parametros.add(estado);
        }
        sql.append("ORDER BY p.id DESC");

        List<Proyecto> proyectos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                sentencia.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    proyectos.add(mapearProyecto(resultado));
                }
            }
        }
        return proyectos;
    }

    /**
     * Busca un proyecto por su id.
     *
     * @param id identificador del proyecto
     * @return el proyecto, o vacío si no existe
     * @throws SQLException si falla la consulta
     */
    public Optional<Proyecto> buscarPorId(int id) throws SQLException {
        String sql = SQL_SELECT_BASE + "WHERE p.id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(mapearProyecto(resultado));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Inserta un proyecto nuevo y le asigna un código tipo PIA-2026-0007
     * (mismo formato que usa la API del proyecto: año de inicio + id).
     * Las dos sentencias van en una transacción: o se guardan las dos o ninguna.
     *
     * @param proyecto datos del formulario ya validados
     * @return id generado por MySQL
     * @throws SQLException si falla la inserción
     */
    public int insertar(Proyecto proyecto) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try (PreparedStatement insertar = conexion.prepareStatement(SQL_INSERTAR,
                         Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement actualizarCodigo = conexion.prepareStatement(SQL_ACTUALIZAR_CODIGO)) {

                asignarParametros(insertar, proyecto);
                insertar.executeUpdate();

                int idGenerado;
                try (ResultSet llaves = insertar.getGeneratedKeys()) {
                    llaves.next();
                    idGenerado = llaves.getInt(1);
                }

                actualizarCodigo.setString(1, String.format("PIA-%d-%04d",
                        proyecto.getFechaInicio().getYear(), idGenerado));
                actualizarCodigo.setInt(2, idGenerado);
                actualizarCodigo.executeUpdate();

                conexion.commit();
                return idGenerado;
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /**
     * Actualiza todos los datos editables de un proyecto.
     *
     * @param proyecto proyecto con el id y los datos nuevos
     * @return true si se actualizó una fila
     * @throws SQLException si falla la actualización
     */
    public boolean actualizar(Proyecto proyecto) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {

            asignarParametros(sentencia, proyecto);
            sentencia.setInt(11, proyecto.getId());
            return sentencia.executeUpdate() == 1;
        }
    }

    /**
     * Elimina un proyecto. Sus impactos, actividades y monitoreos se borran
     * en cascada por las llaves foráneas (ON DELETE CASCADE).
     *
     * @param id identificador del proyecto
     * @return true si se eliminó
     * @throws SQLException si falla la eliminación
     */
    public boolean eliminar(int id) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {

            sentencia.setInt(1, id);
            return sentencia.executeUpdate() == 1;
        }
    }

    /** Pone los 10 parámetros comunes de INSERT y UPDATE en el mismo orden. */
    private void asignarParametros(PreparedStatement sentencia, Proyecto proyecto) throws SQLException {
        sentencia.setString(1, proyecto.getNombre());
        sentencia.setString(2, proyecto.getDescripcion());
        sentencia.setString(3, proyecto.getTipo());
        sentencia.setString(4, proyecto.getMunicipio());
        sentencia.setString(5, proyecto.getDepartamento());
        sentencia.setDate(6, Date.valueOf(proyecto.getFechaInicio()));
        if (proyecto.getFechaFin() != null) {
            sentencia.setDate(7, Date.valueOf(proyecto.getFechaFin()));
        } else {
            sentencia.setNull(7, Types.DATE);
        }
        sentencia.setBigDecimal(8, proyecto.getPresupuesto());
        sentencia.setString(9, proyecto.getEstado());
        sentencia.setInt(10, proyecto.getResponsableId());
    }

    /** Convierte la fila actual del ResultSet en un objeto Proyecto. */
    private Proyecto mapearProyecto(ResultSet fila) throws SQLException {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(fila.getInt("id"));
        proyecto.setCodigo(fila.getString("codigo"));
        proyecto.setNombre(fila.getString("nombre"));
        proyecto.setDescripcion(fila.getString("descripcion"));
        proyecto.setTipo(fila.getString("tipo"));
        proyecto.setMunicipio(fila.getString("municipio"));
        proyecto.setDepartamento(fila.getString("departamento"));
        proyecto.setFechaInicio(fila.getDate("fecha_inicio").toLocalDate());
        Date fechaFin = fila.getDate("fecha_fin");
        proyecto.setFechaFin(fechaFin != null ? fechaFin.toLocalDate() : null);
        proyecto.setPresupuesto(fila.getBigDecimal("presupuesto"));
        proyecto.setEstado(fila.getString("estado"));
        proyecto.setResponsableId(fila.getInt("responsable_id"));
        proyecto.setNombreResponsable(fila.getString("nombre_responsable"));
        return proyecto;
    }
}
