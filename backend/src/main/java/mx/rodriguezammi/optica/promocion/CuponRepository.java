package mx.rodriguezammi.optica.promocion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CuponRepository extends JpaRepository<Cupon, Long> {

    Optional<Cupon> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigo(String codigo);

    /** ¿Ya se le generó su cupón de cumpleaños este año? (creado después del 1 de enero) */
    boolean existsByClienteIdAndTipoAndCreadoAfter(Long clienteId, TipoCupon tipo, LocalDateTime desde);

    Optional<Cupon> findFirstByClienteIdAndTipoAndCreadoAfterOrderByCreadoDesc(Long clienteId, TipoCupon tipo, LocalDateTime desde);

    List<Cupon> findTop100ByOrderByCreadoDesc();
}
