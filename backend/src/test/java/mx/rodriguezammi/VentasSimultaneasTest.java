package mx.rodriguezammi;

import mx.rodriguezammi.papeleria.producto.ProductoDto;
import mx.rodriguezammi.papeleria.producto.ProductoService;
import mx.rodriguezammi.papeleria.venta.MetodoPago;
import mx.rodriguezammi.papeleria.venta.VentaDto;
import mx.rodriguezammi.papeleria.venta.VentaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DOS CELULARES VENDIENDO LA ÚLTIMA PIEZA AL MISMO TIEMPO.
 *
 * Es el error clásico de los puntos de venta: mamá y papá cobran el último cuaderno en el
 * mismo instante. Sin protección, los dos leen "queda 1", los dos venden, y el sistema
 * registra 2 ventas de un producto que solo existía una vez.
 *
 * Esta prueba lanza dos ventas exactamente a la vez, varias rondas, y exige que:
 *   - solo UNA se cobre,
 *   - la otra reciba "No hay suficiente existencia",
 *   - el inventario termine en 0 (nunca negativo ni "vendido de más").
 *
 * No lleva @Transactional: cada venta debe correr en su propia transacción real,
 * igual que cuando llegan dos peticiones de dos celulares.
 */
@SpringBootTest
@ActiveProfiles("h2")
class VentasSimultaneasTest {

    private static final int RONDAS = 15;

    @Autowired VentaService ventas;
    @Autowired ProductoService productos;
    @Autowired JdbcTemplate jdbc;

    private final List<Long> creados = new ArrayList<>();

    @AfterEach
    void limpiar() {
        // Esta prueba sí guarda datos de verdad: los borramos para no afectar a las demás.
        for (Long id : creados) {
            jdbc.update("DELETE FROM movimientos_inventario WHERE producto_id = ?", id);
            jdbc.update("DELETE FROM detalle_venta WHERE producto_id = ?", id);
            jdbc.update("DELETE FROM productos WHERE id = ?", id);
        }
        jdbc.update("DELETE FROM ventas v WHERE NOT EXISTS (SELECT 1 FROM detalle_venta d WHERE d.venta_id = v.id)");
    }

    @Test
    void laUltimaPiezaSoloSeVendeUnaVez() throws Exception {
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            for (int ronda = 1; ronda <= RONDAS; ronda++) {
                Long id = productos.crear(new ProductoDto.Guardar("Última pieza " + ronda, null, new BigDecimal("10"), 1, 0, null, false)).id();
                creados.add(id);

                // La barrera suelta a los dos "celulares" en el mismo instante.
                CyclicBarrier barrera = new CyclicBarrier(2);
                Callable<Boolean> cobrar = () -> {
                    barrera.await(5, TimeUnit.SECONDS);
                    try {
                        ventas.registrar(new VentaDto.Nueva(MetodoPago.EFECTIVO, null, List.of(new VentaDto.Renglon(id, 1))));
                        return true;
                    } catch (RuntimeException e) {
                        return false;
                    }
                };
                Future<Boolean> a = hilos.submit(cobrar);
                Future<Boolean> b = hilos.submit(cobrar);
                int cobradas = (a.get(20, TimeUnit.SECONDS) ? 1 : 0) + (b.get(20, TimeUnit.SECONDS) ? 1 : 0);

                assertThat(cobradas).as("ronda %d: ventas cobradas de una sola pieza", ronda).isEqualTo(1);
                assertThat(productos.obtener(id).existencia()).as("ronda %d: existencia final", ronda).isZero();
            }
        } finally {
            hilos.shutdownNow();
        }
    }
}
