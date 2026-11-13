package mx.rodriguezammi.optica.orden;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrdenRepository extends JpaRepository<OrdenLentes, Long> {

    List<OrdenLentes> findTop100ByEstadoInOrderByFechaDesc(Collection<EstadoOrden> estados);

    /** Todas (sin tope), para sumar lo que falta por cobrar en los reportes. */
    List<OrdenLentes> findByEstadoIn(Collection<EstadoOrden> estados);

    /** Con candado: dos abonos simultáneos a la misma orden no pueden pasarse del saldo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrdenLentes o WHERE o.id = :id")
    Optional<OrdenLentes> bloquearPorId(@Param("id") Long id);

    Optional<OrdenLentes> findByClaveUnica(String claveUnica);

    /** ¿Este armazón aparece en alguna orden? (si sí, no se puede borrar de verdad) */
    @Query("SELECT COUNT(p) > 0 FROM PartidaLente p WHERE p.armazon.id = :armazonId")
    boolean existeOrdenConArmazon(@Param("armazonId") Long armazonId);

    @Query("SELECT p FROM PagoOrden p JOIN FETCH p.orden WHERE p.claveUnica = :clave")
    Optional<PagoOrden> pagoPorClave(@Param("clave") String clave);

    List<OrdenLentes> findByClienteIdOrderByFechaDesc(Long clienteId);

    boolean existsByClienteId(Long clienteId);

    List<OrdenLentes> findByFechaBetweenOrderByFechaAsc(LocalDateTime desde, LocalDateTime hasta);

    /** Pagos recibidos en un rango de fechas (para el reporte de dinero que entró). */
    @Query("SELECT p FROM PagoOrden p JOIN FETCH p.orden WHERE p.fecha BETWEEN :desde AND :hasta ORDER BY p.fecha")
    List<PagoOrden> pagosEntre(LocalDateTime desde, LocalDateTime hasta);
}
