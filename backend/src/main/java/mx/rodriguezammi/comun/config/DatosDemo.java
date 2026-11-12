package mx.rodriguezammi.comun.config;

import mx.rodriguezammi.papeleria.producto.Producto;
import mx.rodriguezammi.papeleria.producto.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;

/**
 * Carga productos de ejemplo SOLO cuando se arranca con el perfil "h2" (modo prueba).
 * Con PostgreSQL (uso real) esta clase no hace nada.
 *
 * @Configuration → clase de configuración.
 * @Profile("h2") → solo existe si el perfil activo es "h2".
 * @Bean          → "Spring, crea este objeto y adminístralo tú".
 * CommandLineRunner → código que corre UNA vez, justo después de arrancar.
 */
@Configuration
@Profile("h2")
public class DatosDemo {

    @Bean
    CommandLineRunner cargarProductosDemo(ProductoRepository productos) {
        return args -> {
            if (productos.count() > 0) {
                return;
            }
            productos.saveAll(List.of(
                    new Producto("Cuaderno profesional raya", "7501000000011", new BigDecimal("45.00"), 20, 5),
                    new Producto("Pluma azul punto mediano", "7501000000028", new BigDecimal("8.50"), 60, 15),
                    new Producto("Lápiz del 2", "7501000000035", new BigDecimal("6.00"), 4, 10),
                    new Producto("Hojas blancas carta (paquete 100)", null, new BigDecimal("38.00"), 12, 4),
                    new Producto("Pegamento en barra 21 g", null, new BigDecimal("22.00"), 3, 5),
                    new Producto("Impresión B/N por hoja", null, new BigDecimal("2.00"), 9999, 0)
            ));
        };
    }
}
