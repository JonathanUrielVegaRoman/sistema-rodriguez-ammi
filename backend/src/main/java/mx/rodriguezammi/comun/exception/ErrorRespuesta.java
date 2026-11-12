package mx.rodriguezammi.comun.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Forma ÚNICA en la que el backend le cuenta al frontend que algo salió mal.
 * Siempre se ve así en JSON:
 *
 *   {
 *     "estado": 409,
 *     "mensaje": "No hay suficiente existencia de Cuaderno profesional",
 *     "detalles": [],
 *     "fecha": "2026-11-11T10:30:00"
 *   }
 *
 * "record" es una forma corta de Java para crear una clase que solo guarda datos
 * (Java genera solito el constructor y los métodos para leer cada campo).
 */
public record ErrorRespuesta(
        int estado,
        String mensaje,
        List<String> detalles,
        LocalDateTime fecha
) {
    public static ErrorRespuesta de(int estado, String mensaje, List<String> detalles) {
        return new ErrorRespuesta(estado, mensaje, detalles, LocalDateTime.now());
    }
}
