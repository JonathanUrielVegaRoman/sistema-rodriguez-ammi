package mx.rodriguezammi.comun.acceso;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
        String ruta = peticion.getRequestURI();

        // Las peticiones OPTIONS las hace el navegador para "preguntar" antes; se dejan pasar.
        if ("OPTIONS".equalsIgnoreCase(peticion.getMethod()) || ruta.startsWith("/api/publico/")) {
            return true;
        }

        String tokenAdmin = peticion.getHeader(ENCABEZADO_ADMIN);
        boolean esAdmin = acceso.tieneRol(tokenAdmin, Rol.ADMIN);

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
}
