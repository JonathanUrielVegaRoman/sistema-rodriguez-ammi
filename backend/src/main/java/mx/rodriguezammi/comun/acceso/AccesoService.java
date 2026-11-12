package mx.rodriguezammi.comun.acceso;

import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reglas de acceso al sistema.
 *
 * ¿Por qué no usamos Spring Security? Porque para 3 personas y 2 niveles basta con esto,
 * y se entiende leyéndolo. Si el sistema crece (usuarios con nombre, permisos finos,
 * acceso desde internet), el siguiente paso natural es Spring Security.
 *
 * El PIN y la contraseña NO están en el código: vienen de application.properties,
 * que a su vez los lee de variables de entorno (APP_PIN y APP_ADMIN_PASSWORD).
 */
@Service
@Transactional(readOnly = true)
public class AccesoService {

    private static final Logger log = LoggerFactory.getLogger(AccesoService.class);

    /** Cuántos intentos fallidos se permiten antes de bloquear un momento. */
    private static final int INTENTOS_MAXIMOS = 5;
    private static final int MINUTOS_BLOQUEO = 5;

    private final SesionAccesoRepository sesiones;
    private final String pinPersonal;
    private final String passwordAdmin;
    private final int horasAdmin;
    private final SecureRandom aleatorio = new SecureRandom();

    /** Intentos fallidos por dirección IP (en memoria; se reinicia al apagar el servidor). */
    private final Map<String, Intentos> fallidos = new ConcurrentHashMap<>();

    public AccesoService(SesionAccesoRepository sesiones,
                         @Value("${app.acceso.pin}") String pinPersonal,
                         @Value("${app.acceso.admin-password}") String passwordAdmin,
                         @Value("${app.acceso.horas-admin:8}") int horasAdmin) {
        this.sesiones = sesiones;
        this.pinPersonal = pinPersonal;
        this.passwordAdmin = passwordAdmin;
        this.horasAdmin = horasAdmin;
        if ("1234".equals(pinPersonal) || "admin123".equals(passwordAdmin)) {
            log.warn("⚠ Se está usando el PIN o la contraseña de ejemplo. Cámbialos con APP_PIN y APP_ADMIN_PASSWORD antes de usarlo en el negocio.");
        }
    }

    /** El personal escribe el PIN → recibe un token que dura 1 año en ese celular. */
    @Transactional
    public SesionAcceso entrarComoPersonal(String pin, String ip, String dispositivo) {
        validarIntento(ip, pin, pinPersonal, "PIN incorrecto");
        return crear(Rol.PERSONAL, LocalDateTime.now().plusYears(1), dispositivo);
    }

    /** El dueño escribe su contraseña → recibe un token que dura pocas horas. */
    @Transactional
    public SesionAcceso entrarComoAdmin(String password, String ip, String dispositivo) {
        validarIntento(ip, password, passwordAdmin, "Contraseña incorrecta");
        return crear(Rol.ADMIN, LocalDateTime.now().plusHours(horasAdmin), dispositivo);
    }

    /** ¿Este token es válido y tiene (al menos) el rol pedido? */
    public boolean tieneRol(String token, Rol rol) {
        return buscarVigente(token).map(s -> s.getRol() == rol).orElse(false);
    }

    public Optional<SesionAcceso> buscarVigente(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return sesiones.findById(token).filter(SesionAcceso::vigente);
    }

    @Transactional
    public void salir(String token) {
        if (token != null) {
            sesiones.deleteById(token);
        }
    }

    /** Si se pierde un celular: el dueño cierra TODAS las sesiones del personal y se vuelve a pedir el PIN. */
    @Transactional
    public int cerrarSesionesPersonal() {
        return sesiones.borrarPorRol(Rol.PERSONAL);
    }

    /** Limpieza automática cada noche a las 3:00. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limpiarVencidas() {
        sesiones.borrarVencidas(LocalDateTime.now());
    }

    // ---------------- privados ----------------

    private SesionAcceso crear(Rol rol, LocalDateTime expira, String dispositivo) {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String disp = dispositivo == null ? null : dispositivo.substring(0, Math.min(200, dispositivo.length()));
        return sesiones.save(new SesionAcceso(token, rol, expira, disp));
    }

    private void validarIntento(String ip, String recibido, String esperado, String mensajeError) {
        Intentos intentos = fallidos.computeIfAbsent(ip, k -> new Intentos());
        if (intentos.bloqueadoHasta != null && LocalDateTime.now().isBefore(intentos.bloqueadoHasta)) {
            throw new ReglaDeNegocioException("Demasiados intentos. Espera " + MINUTOS_BLOQUEO + " minutos.");
        }
        if (!iguales(recibido, esperado)) {
            intentos.cuenta++;
            if (intentos.cuenta >= INTENTOS_MAXIMOS) {
                intentos.bloqueadoHasta = LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO);
                intentos.cuenta = 0;
            }
            throw new AccesoDenegadoException(mensajeError);
        }
        fallidos.remove(ip);
    }

    /** Comparación en tiempo constante: evita que alguien adivine midiendo cuánto tarda la respuesta. */
    private boolean iguales(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private static class Intentos {
        int cuenta;
        LocalDateTime bloqueadoHasta;
    }
}
