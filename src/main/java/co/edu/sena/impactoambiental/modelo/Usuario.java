package co.edu.sena.impactoambiental.modelo;

/**
 * Datos básicos de un usuario (tabla "usuarios").
 * En este módulo solo se usa para escoger el responsable de un proyecto.
 *
 * @author Roycer Cervantes
 * @author Sebastián Rodríguez
 */
public class Usuario {

    private Integer id;
    private String nombre;
    private String rol;

    public Usuario() {
    }

    public Usuario(Integer id, String nombre, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.rol = rol;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
