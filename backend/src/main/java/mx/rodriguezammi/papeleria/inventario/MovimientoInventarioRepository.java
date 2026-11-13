package mx.rodriguezammi.papeleria.inventario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    /** Los 50 movimientos más recientes de un producto. */
    List<MovimientoInventario> findTop50ByProductoIdOrderByFechaDescIdDesc(Long productoId);
}
