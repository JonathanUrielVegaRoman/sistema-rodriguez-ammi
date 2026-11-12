package mx.rodriguezammi.comun.acceso;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba del "guardia": simula peticiones HTTP (MockMvc = un navegador de mentiras)
 * y revisa quién pasa y quién no.
 */
@SpringBootTest(properties = {"app.acceso.pin=9999", "app.acceso.admin-password=secreta"})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class AccesoTest {

    @Autowired MockMvc mvc;
    @Autowired AccesoService acceso;

    @Test
    void sinPaseNoSeVenLosDatos() throws Exception {
        mvc.perform(get("/api/papeleria/productos")).andExpect(status().isUnauthorized());
    }

    @Test
    void elBuzonDeImpresionYElAccesoSonPublicos() throws Exception {
        mvc.perform(get("/api/publico/acceso/estado")).andExpect(status().isOk());
    }

    @Test
    void conPinEntraAlDiaADiaPeroNoALosReportes() throws Exception {
        String token = acceso.entrarComoPersonal("9999", "prueba-1", "test").getToken();

        mvc.perform(get("/api/papeleria/productos").header(AccesoInterceptor.ENCABEZADO_PERSONAL, token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admin/reportes").header(AccesoInterceptor.ENCABEZADO_PERSONAL, token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conContrasenaDeAdminSeVenLosReportes() throws Exception {
        String admin = acceso.entrarComoAdmin("secreta", "prueba-2", "test").getToken();
        mvc.perform(get("/api/admin/reportes").header(AccesoInterceptor.ENCABEZADO_ADMIN, admin))
                .andExpect(status().isOk());
    }

    @Test
    void pinIncorrectoResponde401() throws Exception {
        mvc.perform(post("/api/publico/acceso/personal").contentType(MediaType.APPLICATION_JSON).content("{\"clave\":\"0000\"}"))
                .andExpect(status().isUnauthorized());
    }
}
