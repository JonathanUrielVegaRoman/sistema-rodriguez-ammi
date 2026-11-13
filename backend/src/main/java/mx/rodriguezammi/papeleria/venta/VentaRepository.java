package mx.rodriguezammi.papeleria.venta;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Ventas entre dos momentos (por ejemplo, de las 00:00 a las 23:59 de hoy).
     * Spring arma la consulta solo a partir del nombre: "fecha Between ... ordenado por fecha".
     *
     * @EntityGraph le dice a JPA: "trae también los detalles en la MISMA consulta".
     * Sin esto, haría una consulta extra por cada venta (problema conocido como "N+1").
     */
    @EntityGraph(attributePaths = "detalles")
    List<Venta> findByFechaBetweenOrderByFechaAsc(LocalDateTime desde, LocalDateTime hasta);

    @EntityGraph(attributePaths = "detalles")
    Optional<Venta> findWithDetallesById(Long id);

    /** ¿Ya se registró un cobro con esta clave? (para no cobrar doble si el celular reintenta) */
    @EntityGraph(attributePaths = "detalles")
    Optional<Venta> findByClaveUnica(String claveUnica);
}
