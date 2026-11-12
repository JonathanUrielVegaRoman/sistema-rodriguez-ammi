package mx.rodriguezammi.comun.config;

import mx.rodriguezammi.optica.tratamiento.Tratamiento;
import mx.rodriguezammi.optica.tratamiento.TratamientoRepository;
import mx.rodriguezammi.papeleria.impresion.ImpresionService;
import mx.rodriguezammi.papeleria.producto.Producto;
import mx.rodriguezammi.papeleria.producto.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos que el sistema NECESITA para funcionar, también en el negocio real (no solo en pruebas):
 *   - Los productos-servicio de impresión (los usa el buzón de impresión para cobrar).
 *   - Un catálogo inicial de tratamientos (el dueño ajusta precios en Administración).
 *
 * Solo se crean si no existen: volver a arrancar el sistema NO los duplica ni los sobrescribe.
 * Los PRECIOS son de ejemplo: hay que confirmarlos con el cliente.
 */
@Configuration
public class DatosIniciales {

    @Bean
    @Order(1)
    CommandLineRunner cargarDatosIniciales(ProductoRepository productos, TratamientoRepository tratamientos) {
        return args -> {
            servicio(productos, ImpresionService.CODIGO_BN, "Impresión blanco y negro (hoja)", "2.00");
            servicio(productos, ImpresionService.CODIGO_COLOR, "Impresión a color (hoja)", "6.00");

            if (tratamientos.count() == 0) {
                tratamientos.saveAll(List.of(
                        new Tratamiento("Antirreflejante", new BigDecimal("350.00"), true),
                        new Tratamiento("Endurecido", new BigDecimal("150.00"), true),
                        new Tratamiento("Filtro de luz azul", new BigDecimal("450.00"), false),
                        new Tratamiento("Fotocromático", new BigDecimal("900.00"), false),
                        new Tratamiento("Polarizado", new BigDecimal("1200.00"), false)
                ));
            }
        };
    }

    private void servicio(ProductoRepository productos, String codigo, String nombre, String precio) {
        if (productos.findByCodigoBarras(codigo).isEmpty()) {
            Producto p = new Producto(nombre, codigo, new BigDecimal(precio), 0, 0);
            p.setServicio(true);
            p.setIcono("🖨️");
            productos.save(p);
        }
    }
}
