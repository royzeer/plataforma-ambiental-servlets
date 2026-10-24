package co.edu.sena.impactoambiental.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Se encarga de abrir conexiones a la base de datos MySQL usando JDBC.
 *
 * Los datos de conexión (url, usuario y contraseña) se leen del archivo
 * db.properties que está en src/main/resources. Así no quedan "quemados"
 * en el código y cada uno los cambia según su MySQL local.
 *
 * @author Roycer Cervantes
 * @author Sebastián Rodríguez
 */
public final class ConexionBD {

    private static final String ARCHIVO_CONFIGURACION = "db.properties";

    private static final String URL;
    private static final String USUARIO;
    private static final String CONTRASENA;

    // Bloque estático: se ejecuta una sola vez cuando la clase se carga
    static {
        Properties propiedades = new Properties();
        try (InputStream entrada = ConexionBD.class.getClassLoader()
                .getResourceAsStream(ARCHIVO_CONFIGURACION)) {

            if (entrada == null) {
                throw new IllegalStateException("No se encontró el archivo " + ARCHIVO_CONFIGURACION);
            }
            propiedades.load(entrada);

            // Cargamos el driver de MySQL de forma explícita (necesario en algunos Tomcat)
            Class.forName(propiedades.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("No se pudo configurar la conexión a la base de datos", e);
        }

        URL = propiedades.getProperty("db.url");
        USUARIO = propiedades.getProperty("db.usuario");
        CONTRASENA = propiedades.getProperty("db.contrasena", "");
    }

    /** Constructor privado: esta clase solo tiene métodos estáticos. */
    private ConexionBD() {
    }

    /**
     * Abre una conexión nueva. Quien la pide debe cerrarla
     * (lo hacemos siempre con try-with-resources en los DAO).
     *
     * @return conexión abierta a MySQL
     * @throws SQLException si MySQL no está encendido o los datos son incorrectos
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}
