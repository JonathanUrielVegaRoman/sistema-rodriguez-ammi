package mx.rodriguezammi.optica.orden;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrdenRepository extends JpaRepository<OrdenLentes, Long> {

    List<OrdenLentes> findTop100ByEstadoInOrderByFechaDesc(Collection<EstadoOrden> estados);

    List<OrdenLentes> findByClienteIdOrderByFechaDesc(Long clienteId);

    List<OrdenLentes> findByFechaBetweenOrderByFechaAsc(LocalDateTime desde, LocalDateTime hasta);

    /** Pagos recibidos en un rango de fechas (para el reporte de dinero que entró). */
    @Query("SELECT p FROM PagoOrden p JOIN FETCH p.orden WHERE p.fecha BETWEEN :desde AND :hasta ORDER BY p.fecha")
    List<PagoOrden> pagosEntre(LocalDateTime desde, LocalDateTime hasta);
}
