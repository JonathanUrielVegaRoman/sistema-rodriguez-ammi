package mx.rodriguezammi.optica.tratamiento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TratamientoRepository extends JpaRepository<Tratamiento, Long> {

    List<Tratamiento> findByActivoTrueOrderByNombreAsc();

    List<Tratamiento> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
