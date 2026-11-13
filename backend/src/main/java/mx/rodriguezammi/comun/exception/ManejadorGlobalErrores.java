package mx.rodriguezammi.comun.exception;

import mx.rodriguezammi.comun.acceso.AccesoDenegadoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

/**
 * LA "RED DE SEGURIDAD" de todo el backend.
 *
 * Cualquier excepción que se lance en cualquier Controller o Service y que nadie
 * atrape, cae aquí. Según el tipo de error, decidimos qué código HTTP y qué
 * mensaje devolver.
 *
 * Ventaja: los Controllers quedan limpios (sin try/catch por todos lados) y
 * el frontend SIEMPRE recibe errores con la misma forma (ErrorRespuesta).
 *
 * Códigos HTTP más comunes:
 *   200 OK              → todo bien
 *   201 Created         → se creó algo nuevo
 *   400 Bad Request     → datos mal capturados (falta el nombre, precio negativo...)
 *   404 Not Found       → no existe
 *   409 Conflict        → choca con una regla del negocio
 *   500 Internal Error  → error nuestro (bug)
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ResponseEntity<ErrorRespuesta> reglaDeNegocio(ReglaDeNegocioException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    /** Errores de validación: cuando un DTO con @Valid trae datos que no cumplen las reglas. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Revisa los datos capturados", detalles);
    }

    /** Falta el PIN/contraseña o es incorrecto. 401 = "no sé quién eres". */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorRespuesta> accesoDenegado(AccesoDenegadoException ex) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), List.of());
    }

    /** Archivo demasiado grande (foto de armazón o archivo del buzón de impresión). */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorRespuesta> archivoGrande(MaxUploadSizeExceededException ex) {
        return responder(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo es demasiado grande", List.of());
    }

    /**
     * La base de datos rechazó el cambio por una regla suya (por ejemplo, un valor que debe ser
     * único y ya existe). Suele pasar cuando dos personas guardan lo mismo al mismo tiempo.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> datoDuplicado(DataIntegrityViolationException ex) {
        log.warn("La base de datos rechazó un cambio: {}", ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "No se pudo guardar: ese dato ya existe o alguien más lo acaba de cambiar. Revisa e intenta de nuevo.", List.of());
    }

    /** Un candado tardó demasiado (dos personas modificando lo mismo). Se pide reintentar. */
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ErrorRespuesta> candadoOcupado(PessimisticLockingFailureException ex) {
        return responder(HttpStatus.CONFLICT, "Alguien más está modificando lo mismo en este momento. Intenta de nuevo.", List.of());
    }

    /** El cuerpo de la petición no es JSON válido (por ejemplo, texto en un campo numérico). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "La información enviada no tiene el formato esperado", List.of());
    }

    /** Cualquier otro error inesperado. Se registra en consola para poder depurarlo. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> inesperado(Exception ex) {
        // Errores propios de Spring (ruta inexistente, método no permitido...) ya traen su código HTTP.
        if (ex instanceof ErrorResponse errorDeSpring) {
            HttpStatus estado = HttpStatus.valueOf(errorDeSpring.getStatusCode().value());
            return responder(estado, estado.getReasonPhrase(), List.of());
        }
        log.error("Error inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intenta de nuevo o avisa al administrador.", List.of());
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, String mensaje, List<String> detalles) {
        return ResponseEntity.status(estado).body(ErrorRespuesta.de(estado.value(), mensaje, detalles));
    }
}
