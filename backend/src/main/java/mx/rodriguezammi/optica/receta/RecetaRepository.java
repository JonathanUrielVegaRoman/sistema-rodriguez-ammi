package mx.rodriguezammi.optica.receta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaRepository extends JpaRepository<Receta, Long> {

    /** Historial de graduaciones de un cliente, la más reciente primero. */
    List<Receta> findByClienteIdOrderByFechaDesc(Long clienteId);
}
