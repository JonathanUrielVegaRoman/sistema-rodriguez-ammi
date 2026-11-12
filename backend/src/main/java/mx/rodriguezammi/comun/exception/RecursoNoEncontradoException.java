package mx.rodriguezammi.comun.exception;

/**
 * Se lanza cuando alguien pide algo que no existe.
 * Ejemplo: "dame el producto 999" y no hay producto 999.
 *
 * El ManejadorGlobalErrores la atrapa y responde al frontend con un
 * error 404 (Not Found) y un mensaje claro, en vez de un error feo.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
