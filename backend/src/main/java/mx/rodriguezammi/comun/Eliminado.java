package mx.rodriguezammi.comun;

/**
 * Respuesta al ELIMINAR algo del catálogo (producto o armazón).
 *
 * La regla es la misma que con los clientes, para no romper el historial de ventas:
 *   ELIMINADO → nunca se vendió: se borra de verdad.
 *   ARCHIVADO → ya tiene ventas: se oculta del catálogo, pero las ventas viejas lo conservan
 *               (un ticket de hace un mes debe seguir diciendo qué se vendió).
 */
public record Eliminado(Resultado resultado, String mensaje) {

    public enum Resultado { ELIMINADO, ARCHIVADO }

    public static Eliminado borrado(String que) {
        return new Eliminado(Resultado.ELIMINADO, que + " se eliminó.");
    }

    public static Eliminado archivado(String que) {
        return new Eliminado(Resultado.ARCHIVADO, que + " ya tiene ventas: se quitó del catálogo, pero sus ventas se conservan en los reportes.");
    }
}
