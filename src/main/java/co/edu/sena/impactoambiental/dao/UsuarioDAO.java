package co.edu.sena.impactoambiental.dao;

import co.edu.sena.impactoambiental.config.ConexionBD;
import co.edu.sena.impactoambiental.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas sobre la tabla "usuarios" que necesita el módulo de proyectos.
 *
 * @author Roycer Cervantes
 * @author Sebastián Rodríguez
 */
public class UsuarioDAO {

    /** Solo admin y gestor pueden ser responsables de un proyecto. */
    private static final String SQL_RESPONSABLES =
            "SELECT id, nombre, rol FROM usuarios "
            + "WHERE activo = 1 AND rol IN ('admin', 'gestor') ORDER BY nombre";

    private static final String SQL_EXISTE_RESPONSABLE =
            "SELECT COUNT(*) FROM usuarios WHERE id = ? AND activo = 1 AND rol IN ('admin', 'gestor')";

    /**
     * Lista los usuarios que pueden quedar como responsables (para el select del formulario).
     *
     * @return usuarios activos con rol admin o gestor
     * @throws SQLException si falla la consulta
     */
    public List<Usuario> listarResponsables() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_RESPONSABLES);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                usuarios.add(new Usuario(resultado.getInt("id"),
                        resultado.getString("nombre"),
                        resultado.getString("rol")));
            }
        }
        return usuarios;
    }

    /**
     * Revisa que el id enviado en el formulario sea un responsable válido
     * (alguien podría modificar el HTML y mandar cualquier número).
     *
     * @param id id del usuario
     * @return true si existe, está activo y es admin o gestor
     * @throws SQLException si falla la consulta
     */
    public boolean esResponsableValido(int id) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_EXISTE_RESPONSABLE)) {

            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }
        }
    }
}
