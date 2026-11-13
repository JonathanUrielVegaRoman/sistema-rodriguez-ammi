package mx.rodriguezammi;

import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.papeleria.impresion.ImpresionService;
import mx.rodriguezammi.papeleria.inventario.MovimientoInventario;
import mx.rodriguezammi.papeleria.producto.ProductoDto;
import mx.rodriguezammi.papeleria.producto.ProductoService;
import mx.rodriguezammi.papeleria.venta.MetodoPago;
import mx.rodriguezammi.papeleria.venta.VentaDto;
import mx.rodriguezammi.papeleria.venta.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de ROBUSTEZ: cosas que pasan en la vida real y que el sistema debe aguantar.
 *   - El Wi-Fi falla y el celular manda el mismo cobro dos veces.
 *   - Alguien quiere saber por qué cambió la existencia de un producto.
 *   - Un cliente intenta saturar el buzón de impresión o entrar a donde no debe.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class RobustezTest {

    @Autowired VentaService ventas;
    @Autowired ProductoService productos;
    @Autowired ImpresionService buzon;
    @Autowired MockMvc mvc;

    @Test
    void elMismoCobroEnviadoDosVecesSoloSeCobraUnaVez() {
        var p = productos.crear(new ProductoDto.Guardar("Producto reintento", null, new BigDecimal("20"), 5, 0, null, false));
        var cobro = new VentaDto.Nueva(MetodoPago.TRANSFERENCIA, null, List.of(new VentaDto.Renglon(p.id(), 2)), "SPEI-123456", "clave-de-prueba-001");

        var primero = ventas.registrar(cobro);
        var reintento = ventas.registrar(cobro); // el celular lo mandó otra vez por mala señal

        assertThat(reintento.folio()).isEqualTo(primero.folio());
        assertThat(productos.obtener(p.id()).existencia()).isEqualTo(3); // se descontó UNA vez, no dos
        assertThat(primero.referenciaPago()).isEqualTo("SPEI-123456");
    }

    @Test
    void cadaCambioDeExistenciaQuedaEnElHistorial() {
        var p = productos.crear(new ProductoDto.Guardar("Producto con historial", null, new BigDecimal("5"), 10, 0, null, false));
        productos.resurtir(p.id(), 4);
        var ticket = ventas.registrar(new VentaDto.Nueva(MetodoPago.EFECTIVO, null, List.of(new VentaDto.Renglon(p.id(), 3))));
        ventas.cancelar(ticket.folio(), "Prueba");

        var historial = productos.historial(p.id()); // del más nuevo al más viejo

        assertThat(historial).extracting(m -> m.tipo()).containsExactly(
                MovimientoInventario.Tipo.CANCELACION, MovimientoInventario.Tipo.VENTA,
                MovimientoInventario.Tipo.RESURTIDO, MovimientoInventario.Tipo.ALTA);
        assertThat(historial).extracting(m -> m.cantidad()).containsExactly(3, -3, 4, 10);
        assertThat(historial).extracting(m -> m.existenciaResultante()).containsExactly(14, 11, 14, 10);
    }

    @Test
    void elBuzonNoAceptaMasDeSeisEnviosSeguidosDelMismoDispositivo() {
        var archivo = new MockMultipartFile("archivos", "nota.txt", "text/plain", "hola".getBytes());
        for (int i = 0; i < 6; i++) {
            buzon.recibir(List.of(archivo), 1, false, false, null, "10.0.0.99");
        }

        assertThatThrownBy(() -> buzon.recibir(List.of(archivo), 1, false, false, null, "10.0.0.99"))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("Espera unos minutos");

        // Otro dispositivo sí puede. (Y se cancelan todos para borrar los archivos de prueba del disco.)
        buzon.recibir(List.of(archivo), 1, false, false, null, "10.0.0.100");
        buzon.cola().forEach(t -> buzon.cancelar(t.id()));
    }

    @Test
    void unArchivoQueDiceSerImagenPeroNoLoEsSeRechaza() {
        var falso = new MockMultipartFile("archivos", "foto.png", "image/png", "esto no es una imagen".getBytes());

        assertThatThrownBy(() -> buzon.recibir(List.of(falso), 1, false, false, null, "10.0.0.101"))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("no es una imagen válida");
    }

    @Test
    void unaDireccionDisfrazadaNoAbreLasPuertasProtegidas() throws Exception {
        // Intentos de llegar a los reportes del dueño "pasando" por la zona pública.
        for (String ruta : List.of(
                "/api/publico/../admin/reportes",
                "/api/publico/%2e%2e/admin/reportes",
                "/api/publico/acceso/../../admin/ventas",
                "/api/admin/reportes;x=/api/publico/",
                "/api/publico/../optica/clientes")) {
            int codigo = mvc.perform(get(java.net.URI.create(ruta))).andReturn().getResponse().getStatus();
            assertThat(codigo).as(ruta).isBetween(400, 499);
        }
        // Y lo protegido sigue protegido por su ruta normal.
        mvc.perform(get("/api/admin/reportes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/papeleria/productos/1/movimientos")).andExpect(status().isUnauthorized());
    }
}
