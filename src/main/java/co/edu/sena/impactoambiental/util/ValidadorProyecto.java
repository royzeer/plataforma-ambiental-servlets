package co.edu.sena.impactoambiental.util;

import co.edu.sena.impactoambiental.modelo.Proyecto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Valida en el servidor los datos que llegan del formulario de proyectos.
 *
 * Aunque el HTML ya tiene "required", "maxlength", etc., esas reglas se pueden
 * saltar fácilmente, por eso repetimos la validación aquí en Java.
 * Cada error se guarda con el nombre del campo para mostrarlo debajo del input.
 *
 * @author Roycer Cervantes
 * @author Sebastián Rodríguez
 */
public final class ValidadorProyecto {

    public static final List<String> TIPOS_VALIDOS = List.of(
            "infraestructura", "restauracion", "conservacion",
            "saneamiento", "energia", "educacion", "otro");

    public static final List<String> ESTADOS_VALIDOS = List.of(
            "planeacion", "ejecucion", "finalizado", "suspendido");

    /** Letras (con tildes y ñ), números, espacios y algunos signos comunes. */
    private static final Pattern PATRON_NOMBRE =
            Pattern.compile("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9 .,()\\-]+$");

    /** Para municipio y departamento: solo letras, espacios y puntos. */
    private static final Pattern PATRON_LUGAR =
            Pattern.compile("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ .]+$");

    private static final BigDecimal PRESUPUESTO_MAXIMO = new BigDecimal("999999999999");
    private static final int MAX_DESCRIPCION = 1000;

    private ValidadorProyecto() {
    }

    /**
     * Arma un Proyecto con los textos del formulario y valida cada campo.
     *
     * @param campos   valores tal como llegan de request.getParameter
     * @param proyecto objeto donde se van guardando los valores convertidos
     * @return mapa campo -> mensaje de error (vacío si todo está bien)
     */
    public static Map<String, String> validarYLlenar(Map<String, String> campos, Proyecto proyecto) {
        Map<String, String> errores = new LinkedHashMap<>();

        // ----- Nombre -----
        String nombre = limpiar(campos.get("nombre"));
        proyecto.setNombre(nombre);
        if (nombre.isEmpty()) {
            errores.put("nombre", "El nombre es obligatorio.");
        } else if (nombre.length() < 5 || nombre.length() > 150) {
            errores.put("nombre", "El nombre debe tener entre 5 y 150 caracteres.");
        } else if (!PATRON_NOMBRE.matcher(nombre).matches()) {
            errores.put("nombre", "El nombre tiene caracteres no permitidos (solo letras, números y . , ( ) -).");
        }

        // ----- Descripción (opcional) -----
        String descripcion = limpiar(campos.get("descripcion"));
        proyecto.setDescripcion(descripcion.isEmpty() ? null : descripcion);
        if (descripcion.length() > MAX_DESCRIPCION) {
            errores.put("descripcion", "La descripción no puede pasar de " + MAX_DESCRIPCION + " caracteres.");
        }

        // ----- Tipo y estado: deben ser valores de la lista -----
        String tipo = limpiar(campos.get("tipo"));
        proyecto.setTipo(tipo);
        if (!TIPOS_VALIDOS.contains(tipo)) {
            errores.put("tipo", "Seleccione un tipo de proyecto válido.");
        }

        String estado = limpiar(campos.get("estado"));
        proyecto.setEstado(estado.isEmpty() ? "planeacion" : estado);
        if (!ESTADOS_VALIDOS.contains(proyecto.getEstado())) {
            errores.put("estado", "Seleccione un estado válido.");
        }

        // ----- Municipio y departamento -----
        validarLugar("municipio", limpiar(campos.get("municipio")), errores);
        proyecto.setMunicipio(limpiar(campos.get("municipio")));
        validarLugar("departamento", limpiar(campos.get("departamento")), errores);
        proyecto.setDepartamento(limpiar(campos.get("departamento")));

        // ----- Fechas -----
        LocalDate fechaInicio = convertirFecha(campos.get("fechaInicio"), "fechaInicio", true, errores);
        LocalDate fechaFin = convertirFecha(campos.get("fechaFin"), "fechaFin", false, errores);
        proyecto.setFechaInicio(fechaInicio);
        proyecto.setFechaFin(fechaFin);

        if (fechaInicio != null && fechaInicio.isBefore(LocalDate.of(2000, 1, 1))) {
            errores.put("fechaInicio", "La fecha de inicio no puede ser anterior al año 2000.");
        }
        if (fechaInicio != null && fechaFin != null && !fechaFin.isAfter(fechaInicio)) {
            errores.put("fechaFin", "La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        // ----- Presupuesto -----
        String textoPresupuesto = limpiar(campos.get("presupuesto"));
        if (textoPresupuesto.isEmpty()) {
            proyecto.setPresupuesto(BigDecimal.ZERO);
        } else {
            try {
                BigDecimal presupuesto = new BigDecimal(textoPresupuesto);
                proyecto.setPresupuesto(presupuesto);
                if (presupuesto.signum() < 0) {
                    errores.put("presupuesto", "El presupuesto no puede ser negativo.");
                } else if (presupuesto.compareTo(PRESUPUESTO_MAXIMO) > 0) {
                    errores.put("presupuesto", "El presupuesto es demasiado alto (máximo 999.999.999.999).");
                } else if (presupuesto.scale() > 2) {
                    errores.put("presupuesto", "El presupuesto admite máximo 2 decimales.");
                }
            } catch (NumberFormatException e) {
                errores.put("presupuesto", "El presupuesto debe ser un número (sin puntos de miles ni $).");
            }
        }

        // ----- Responsable -----
        try {
            proyecto.setResponsableId(Integer.valueOf(limpiar(campos.get("responsableId"))));
        } catch (NumberFormatException e) {
            errores.put("responsableId", "Seleccione el responsable del proyecto.");
        }

        return errores;
    }

    private static void validarLugar(String campo, String valor, Map<String, String> errores) {
        if (valor.isEmpty()) {
            errores.put(campo, "Este campo es obligatorio.");
        } else if (valor.length() < 3 || valor.length() > 80) {
            errores.put(campo, "Debe tener entre 3 y 80 caracteres.");
        } else if (!PATRON_LUGAR.matcher(valor).matches()) {
            errores.put(campo, "Solo se permiten letras y espacios.");
        }
    }

    private static LocalDate convertirFecha(String texto, String campo, boolean obligatoria,
                                            Map<String, String> errores) {
        String valor = limpiar(texto);
        if (valor.isEmpty()) {
            if (obligatoria) {
                errores.put(campo, "La fecha es obligatoria.");
            }
            return null;
        }
        try {
            return LocalDate.parse(valor); // formato yyyy-MM-dd que manda el input type="date"
        } catch (DateTimeParseException e) {
            errores.put(campo, "Fecha inválida, use el formato AAAA-MM-DD.");
            return null;
        }
    }

    /** Quita espacios al inicio y al final; convierte null en cadena vacía. */
    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
