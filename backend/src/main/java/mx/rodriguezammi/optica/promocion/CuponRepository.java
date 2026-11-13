package mx.rodriguezammi.optica.promocion;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CuponRepository extends JpaRepository<Cupon, Long> {

    Optional<Cupon> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigo(String codigo);

    /** Con candado: evita que el mismo cupón se use en dos órdenes creadas al mismo tiempo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cupon c WHERE LOWER(c.codigo) = LOWER(:codigo)")
    Optional<Cupon> bloquearPorCodigo(@Param("codigo") String codigo);

    /** ¿Ya se le generó su cupón de cumpleaños este año? (creado después del 1 de enero) */
    boolean existsByClienteIdAndTipoAndCreadoAfter(Long clienteId, TipoCupon tipo, LocalDateTime desde);

    Optional<Cupon> findFirstByClienteIdAndTipoAndCreadoAfterOrderByCreadoDesc(Long clienteId, TipoCupon tipo, LocalDateTime desde);

    List<Cupon> findTop100ByOrderByCreadoDesc();

    void deleteByClienteId(Long clienteId);
}
