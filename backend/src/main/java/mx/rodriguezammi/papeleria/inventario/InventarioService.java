package mx.rodriguezammi.papeleria.inventario;

import mx.rodriguezammi.papeleria.producto.Producto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Anota y consulta los movimientos de inventario.
 *
 * Los demás Services lo llaman DESPUÉS de cambiar la existencia de un producto:
 *   producto.setExistencia(...);
 *   inventario.registrar(producto, Tipo.RESURTIDO, +24, null);
 * Como corre dentro de la misma transacción, o se guardan los dos cambios o ninguno:
 * el historial nunca queda desfasado de la existencia.
 */
@Service
public class InventarioService {

    public record Movimiento(LocalDateTime fecha, MovimientoInventario.Tipo tipo, int cantidad, int existenciaResultante, String nota) {
    }

    private final MovimientoInventarioRepository movimientos;

    public InventarioService(MovimientoInventarioRepository movimientos) {
        this.movimientos = movimientos;
    }

    /** No anota nada si la cantidad es 0 o si es un servicio (los servicios no llevan inventario). */
    @Transactional
    public void registrar(Producto producto, MovimientoInventario.Tipo tipo, int cantidad, String nota) {
        if (cantidad == 0 || producto.isServicio()) {
            return;
        }
        movimientos.save(new MovimientoInventario(producto, tipo, cantidad, nota));
    }

    @Transactional(readOnly = true)
    public List<Movimiento> historial(Long productoId) {
        return movimientos.findTop50ByProductoIdOrderByFechaDescIdDesc(productoId).stream()
                .map(m -> new Movimiento(m.getFecha(), m.getTipo(), m.getCantidad(), m.getExistenciaResultante(), m.getNota()))
                .toList();
    }
}
