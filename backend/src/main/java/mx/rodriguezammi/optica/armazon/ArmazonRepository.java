package mx.rodriguezammi.optica.armazon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArmazonRepository extends JpaRepository<Armazon, Long> {

    Optional<Armazon> findByCodigoIgnoreCase(String codigo);

    /** Todo el catálogo activo. Son pocos cientos, así que los filtros se aplican en el Service. */
    List<Armazon> findByActivoTrueOrderByCodigoAsc();
}
