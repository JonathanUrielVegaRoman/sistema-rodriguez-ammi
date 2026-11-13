package mx.rodriguezammi.comun.acceso;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 *   POST /api/publico/acceso/personal   { "clave": "1234" }      → pase de personal (1 año)
 *   POST /api/publico/acceso/admin      { "clave": "..." }       → pase de admin (8 horas)
 *   GET  /api/publico/acceso/estado     (con encabezados)        → ¿mis pases siguen vigentes?
 *   POST /api/publico/acceso/salir      (con encabezados)        → cierra sesión en este dispositivo
 *   POST /api/admin/acceso/cerrar-personal                        → saca a TODOS los celulares del personal
 */
@RestController
public class AccesoController {

    private final AccesoService acceso;

    public AccesoController(AccesoService acceso) {
        this.acceso = acceso;
    }

    public record Entrada(@NotBlank(message = "Escribe la clave") String clave) {
    }

    public record Pase(String token, Rol rol, LocalDateTime expira) {
        static Pase desde(SesionAcceso s) {
            return new Pase(s.getToken(), s.getRol(), s.getExpira());
        }
    }

    /** minutosInactividad: la pantalla se bloquea sola después de ese tiempo sin uso. */
    public record Estado(boolean personal, boolean admin, int minutosInactividad) {
    }

    @PostMapping("/api/publico/acceso/personal")
    @ResponseStatus(HttpStatus.CREATED)
    public Pase personal(@Valid @RequestBody Entrada entrada, HttpServletRequest peticion) {
        return Pase.desde(acceso.entrarComoPersonal(entrada.clave(), peticion.getRemoteAddr(), peticion.getHeader("User-Agent")));
    }

    @PostMapping("/api/publico/acceso/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public Pase admin(@Valid @RequestBody Entrada entrada, HttpServletRequest peticion) {
        return Pase.desde(acceso.entrarComoAdmin(entrada.clave(), peticion.getRemoteAddr(), peticion.getHeader("User-Agent")));
    }

    @GetMapping("/api/publico/acceso/estado")
    public Estado estado(@RequestHeader(value = AccesoInterceptor.ENCABEZADO_PERSONAL, required = false) String personal,
                         @RequestHeader(value = AccesoInterceptor.ENCABEZADO_ADMIN, required = false) String admin) {
        return new Estado(acceso.tieneRol(personal, Rol.PERSONAL), acceso.tieneRol(admin, Rol.ADMIN), acceso.minutosInactividad());
    }

    @PostMapping("/api/publico/acceso/salir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void salir(@RequestHeader(value = AccesoInterceptor.ENCABEZADO_ADMIN, required = false) String admin) {
        // Solo cerramos la sesión de ADMIN: el celular del personal sigue recordado.
        acceso.salir(admin);
    }

    /** La pantalla se bloqueó por inactividad: se borra el pase del personal de este dispositivo. */
    @PostMapping("/api/publico/acceso/bloquear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bloquear(@RequestHeader(value = AccesoInterceptor.ENCABEZADO_PERSONAL, required = false) String personal) {
        acceso.salir(personal);
    }

    @GetMapping("/api/admin/acceso/claves-de-ejemplo")
    public boolean clavesDeEjemplo() {
        return acceso.usaClavesDeEjemplo();
    }

    @PostMapping("/api/admin/acceso/cerrar-personal")
    public int cerrarPersonal() {
        return acceso.cerrarSesionesPersonal();
    }
}
