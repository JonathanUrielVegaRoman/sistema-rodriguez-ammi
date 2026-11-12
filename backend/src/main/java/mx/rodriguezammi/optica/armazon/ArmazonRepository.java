package mx.rodriguezammi.optica.armazon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArmazonRepository extends JpaRepository<Armazon, Long> {

    Optional<Armazon> findByCodigo(String codigo);

    /** Buscar armazones parecidos por talla: "todos los de 52 de ancho". */
    List<Armazon> findByAnchoLenteAndActivoTrue(Integer anchoLente);

    List<Armazon> findByModeloContainingIgnoreCaseAndActivoTrue(String texto);
}
