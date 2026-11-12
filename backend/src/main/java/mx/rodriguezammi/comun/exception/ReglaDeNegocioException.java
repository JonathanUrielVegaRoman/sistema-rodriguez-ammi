package mx.rodriguezammi.comun.exception;

/**
 * Se lanza cuando la petición está bien escrita, pero va contra una regla del negocio.
 * Ejemplos:
 *   - Vender 5 cuadernos cuando solo quedan 3.
 *   - Usar un cupón de cumpleaños que ya se usó.
 *
 * El ManejadorGlobalErrores la convierte en un error 409 (Conflict).
 */
public class ReglaDeNegocioException extends RuntimeException {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
