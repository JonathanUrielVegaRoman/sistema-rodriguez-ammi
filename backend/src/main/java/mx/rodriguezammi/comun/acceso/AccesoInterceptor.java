package mx.rodriguezammi.comun.acceso;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * EL GUARDIA DE LA PUERTA. Revisa cada petición a /api ANTES de que llegue al Controller.
 *
 *   /api/publico/**  → pasa cualquiera (buzón de impresión, fotos de armazones, entrar con PIN)
 *   /api/admin/**    → solo con pase de ADMIN   (encabezado X-Token-Admin)
 *   /api/**  (resto) → con pase de PERSONAL o de ADMIN (encabezado X-Token-Personal o X-Token-Admin)
 *
 * Un "interceptor" es como el detector de metales del aeropuerto: todos pasan por ahí y él
 * decide si sigues (return true) o no (lanza AccesoDenegadoException → 401).
 *
 * DETALLE DE SEGURIDAD: la decisión NO se toma con el texto de la dirección que escribió el
 * visitante, sino con la RUTA DEL CONTROLLER que realmente va a atender la petición
 * (el "patrón", por ejemplo "/api/admin/reportes"). El texto de una dirección se puede
 * disfrazar ("/api/publico/../admin/reportes"); el patrón del Controller no.
 *
 * Se registra en WebConfig.java.
 */
@Component
public class AccesoInterceptor implements HandlerInterceptor {

    public static final String ENCABEZADO_PERSONAL = "X-Token-Personal";
    public static final String ENCABEZADO_ADMIN = "X-Token-Admin";

    private final AccesoService acceso;

    public AccesoInterceptor(AccesoService acceso) {
        this.acceso = acceso;
    }

    @Override
    public boolean preHandle(HttpServletRequest peticion, HttpServletResponse respuesta, Object handler) {
        // Las peticiones OPTIONS las hace el navegador para "preguntar" antes; se dejan pasar.
        if ("OPTIONS".equalsIgnoreCase(peticion.getMethod())) {
            return true;
        }
        // Si no hay ningún Controller para esa dirección, no hay datos que proteger:
        // se deja seguir y Spring responde "404 no existe".
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String ruta = rutaDelController(peticion);
        if (ruta.startsWith("/api/publico/")) {
            return true;
        }

        boolean esAdmin = acceso.tieneRol(peticion.getHeader(ENCABEZADO_ADMIN), Rol.ADMIN);
        if (ruta.startsWith("/api/admin/")) {
            if (!esAdmin) {
                throw new AccesoDenegadoException("Esta sección requiere la contraseña del administrador");
            }
            return true;
        }

        if (esAdmin || acceso.tieneRol(peticion.getHeader(ENCABEZADO_PERSONAL), Rol.PERSONAL)) {
            return true;
        }
        throw new AccesoDenegadoException("Este dispositivo no tiene acceso. Escribe el PIN del personal.");
    }

    /**
     * El patrón del Controller que atenderá la petición, tal como está escrito en su
     * @GetMapping/@PostMapping (ej. "/api/optica/clientes/{id}").
     * Si por alguna razón no estuviera disponible, se trata como ruta protegida.
     */
    private String rutaDelController(HttpServletRequest peticion) {
        Object patron = peticion.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return patron != null ? patron.toString() : "/api/protegida";
    }
}
