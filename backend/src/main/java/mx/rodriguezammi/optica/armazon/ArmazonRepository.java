package mx.rodriguezammi.optica.armazon;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArmazonRepository extends JpaRepository<Armazon, Long> {

    Optional<Armazon> findByCodigoIgnoreCase(String codigo);

    /** Lee con CANDADO (ver ProductoRepository.bloquearPorId): evita vender el mismo armazón dos veces. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Armazon a WHERE a.id = :id")
    Optional<Armazon> bloquearPorId(@Param("id") Long id);

    /** Todo el catálogo activo. Son pocos cientos, así que los filtros se aplican en el Service. */
    List<Armazon> findByActivoTrueOrderByCodigoAsc();
}
