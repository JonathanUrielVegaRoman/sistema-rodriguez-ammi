package mx.rodriguezammi.papeleria.venta;

import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.papeleria.producto.ProductoDto;
import mx.rodriguezammi.papeleria.producto.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PRUEBAS AUTOMÁTICAS: código que revisa que otro código funcione.
 * Se corren con:   ./mvnw test
 *
 * Cada prueba sigue el patrón "Dado / Cuando / Entonces" (Arrange / Act / Assert):
 *   Dado    → preparamos los datos
 *   Cuando  → ejecutamos lo que queremos probar
 *   Entonces → revisamos que el resultado sea el esperado
 *
 * @ActiveProfiles("h2") → usa la base en memoria; no toca la base real.
 * @Transactional        → al terminar cada prueba se deshacen los cambios (cada prueba empieza limpia).
 */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class VentaServiceTest {

    @Autowired
    VentaService ventas;

    @Autowired
    ProductoService productos;

    @Test
    void venderDescuentaExistenciaYCalculaTotalYCambio() {
        // Dado
        var cuaderno = productos.crear(new ProductoDto.Guardar("Cuaderno prueba", null, new BigDecimal("45.00"), 10, 2, null, false));

        // Cuando
        var ticket = ventas.registrar(new VentaDto.Nueva(MetodoPago.EFECTIVO, new BigDecimal("100"),
                List.of(new VentaDto.Renglon(cuaderno.id(), 2))));

        // Entonces
        assertThat(ticket.total()).isEqualByComparingTo("90.00");
        assertThat(ticket.cambio()).isEqualByComparingTo("10.00");
        assertThat(productos.obtener(cuaderno.id()).existencia()).isEqualTo(8);
    }

    @Test
    void noPermiteVenderMasDeLoQueHay() {
        var lapiz = productos.crear(new ProductoDto.Guardar("Lápiz prueba", null, new BigDecimal("6.00"), 1, 0, null, false));

        assertThatThrownBy(() -> ventas.registrar(new VentaDto.Nueva(MetodoPago.TARJETA, null,
                List.of(new VentaDto.Renglon(lapiz.id(), 5)))))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("No hay suficiente existencia");
    }

    @Test
    void elCorteSumaLasVentasDelDia() {
        var pluma = productos.crear(new ProductoDto.Guardar("Pluma prueba", null, new BigDecimal("10.00"), 50, 5, null, false));
        ventas.registrar(new VentaDto.Nueva(MetodoPago.EFECTIVO, null, List.of(new VentaDto.Renglon(pluma.id(), 3))));
        ventas.registrar(new VentaDto.Nueva(MetodoPago.TARJETA, null, List.of(new VentaDto.Renglon(pluma.id(), 1))));

        var corte = ventas.corte(LocalDate.now());

        assertThat(corte.numeroVentas()).isEqualTo(2);
        assertThat(corte.total()).isEqualByComparingTo("40.00");
        assertThat(corte.totalPorMetodo().get(MetodoPago.EFECTIVO)).isEqualByComparingTo("30.00");
    }
}
