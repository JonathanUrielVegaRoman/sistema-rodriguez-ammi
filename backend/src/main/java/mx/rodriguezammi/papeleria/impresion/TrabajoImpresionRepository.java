package mx.rodriguezammi.papeleria.impresion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrabajoImpresionRepository extends JpaRepository<TrabajoImpresion, Long> {

    Optional<TrabajoImpresion> findByCodigoSeguimiento(String codigo);

    /** La cola: pendientes e impresos, del más viejo al más nuevo (el primero en llegar, primero en atenderse). */
    List<TrabajoImpresion> findByEstadoInOrderByFechaAsc(Collection<EstadoImpresion> estados);

    /** Cuántos llegaron hoy (para numerar el siguiente turno). */
    long countByFechaAfter(LocalDateTime desde);

    /** Cuántos turnos siguen abiertos (para no aceptar más de los que se pueden atender). */
    long countByEstadoIn(Collection<EstadoImpresion> estados);

    /** Trabajos que todavía tienen archivos en disco y son más viejos que cierta fecha. */
    List<TrabajoImpresion> findByArchivosBorradosFalseAndFechaBefore(LocalDateTime limite);
}
