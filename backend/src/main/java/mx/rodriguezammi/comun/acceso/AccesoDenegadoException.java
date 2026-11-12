package mx.rodriguezammi.comun.acceso;

/** PIN o contraseña incorrectos, o falta el pase (token). Se responde con 401 Unauthorized. */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
