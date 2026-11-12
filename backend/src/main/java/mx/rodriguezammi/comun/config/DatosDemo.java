package mx.rodriguezammi.comun.config;

import mx.rodriguezammi.optica.armazon.Armazon;
import mx.rodriguezammi.optica.armazon.ArmazonRepository;
import mx.rodriguezammi.optica.cliente.Cliente;
import mx.rodriguezammi.optica.cliente.ClienteRepository;
import mx.rodriguezammi.optica.receta.Receta;
import mx.rodriguezammi.optica.receta.RecetaRepository;
import mx.rodriguezammi.papeleria.producto.Producto;
import mx.rodriguezammi.papeleria.producto.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Datos de EJEMPLO, solo con el perfil "h2" (modo prueba). Con PostgreSQL no hace nada.
 *
 * @Configuration → clase de configuración.
 * @Profile("h2") → solo existe si el perfil activo es "h2".
 * @Bean          → "Spring, crea este objeto y adminístralo tú".
 * CommandLineRunner → código que corre UNA vez, justo después de arrancar.
 * @Order(2)      → corre después de DatosIniciales (@Order(1)).
 */
@Configuration
@Profile("h2")
public class DatosDemo {

    @Bean
    @Order(2)
    CommandLineRunner cargarDemo(ProductoRepository productos, ArmazonRepository armazones,
                                 ClienteRepository clientes, RecetaRepository recetas) {
        return args -> {
            if (productos.count() <= 2) {
                productos.saveAll(List.of(
                        producto("Cuaderno profesional raya", "7501000000011", "45.00", 20, 5, "📓"),
                        producto("Pluma azul punto mediano", "7501000000028", "8.50", 60, 15, "🖊️"),
                        producto("Lápiz del 2", "7501000000035", "6.00", 4, 10, "✏️"),
                        producto("Hojas blancas carta (paquete 100)", null, "38.00", 12, 4, "📄"),
                        producto("Pegamento en barra 21 g", null, "22.00", 3, 5, "🧴"),
                        producto("Tijeras escolares", null, "25.00", 8, 3, "✂️"),
                        producto("Colores (caja 12)", null, "55.00", 10, 3, "🖍️")
                ));
            }
            if (armazones.count() == 0) {
                armazones.saveAll(List.of(
                        armazon("RB-2140", "Ray-Ban", "Wayfarer", "Negro", 52, 18, 140, "2450.00", 2, "Vitrina 1"),
                        armazon("RB-2140-M", "Ray-Ban", "Wayfarer", "Negro", 50, 22, 150, "2450.00", 1, "Vitrina 1"),
                        armazon("OK-101", "Genérico", "Clásico", "Carey", 54, 17, 145, "980.00", 3, "Vitrina 2"),
                        armazon("INF-07", "Infantil", "Osito", "Azul", 44, 16, 125, "650.00", 4, "Charola niños"),
                        armazon("TR-330", "Titanio", "Ligero", "Plata", 52, 19, 140, "1850.00", 0, "Vitrina 2")
                ));
            }
            if (clientes.count() == 0) {
                // Una clienta que cumple años HOY, para probar la pantalla de cumpleaños.
                LocalDate hoy = LocalDate.now();
                Cliente ana = clientes.save(new Cliente("Ana López García", "5512345678", hoy.minusYears(34), true));
                clientes.save(new Cliente("Jorge Martínez", "5598765432", LocalDate.of(1980, 3, 15), true));
                clientes.save(new Cliente("Lupita Hernández", null, null, false));

                Receta r = new Receta(ana, hoy.minusDays(3));
                r.setOdEsfera(new BigDecimal("-2.25"));
                r.setOdCilindro(new BigDecimal("-0.75"));
                r.setOdEje(180);
                r.setOiEsfera(new BigDecimal("-2.00"));
                r.setOiCilindro(new BigDecimal("-0.50"));
                r.setOiEje(170);
                r.setDip(new BigDecimal("62.0"));
                recetas.save(r);
            }
        };
    }

    private Producto producto(String nombre, String codigo, String precio, int existencia, int minimo, String icono) {
        Producto p = new Producto(nombre, codigo, new BigDecimal(precio), existencia, minimo);
        p.setIcono(icono);
        return p;
    }

    private Armazon armazon(String codigo, String marca, String modelo, String color, int ancho, int puente,
                            int varilla, String precio, int existencia, String ubicacion) {
        Armazon a = new Armazon(codigo, marca, modelo, new BigDecimal(precio));
        a.setColor(color);
        a.setAnchoLente(ancho);
        a.setPuente(puente);
        a.setVarilla(varilla);
        a.setExistencia(existencia);
        a.setUbicacion(ubicacion);
        return a;
    }
}
