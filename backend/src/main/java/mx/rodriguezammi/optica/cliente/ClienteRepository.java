package mx.rodriguezammi.optica.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Busca por parte del nombre o del celular. "ana" encuentra a "Ana López"; "5512" encuentra su número. */
    List<Cliente> findTop50ByNombreContainingIgnoreCaseOrCelularContainingOrderByNombreAsc(String nombre, String celular);

    /** Los últimos registrados (para la lista inicial, sin búsqueda). */
    List<Cliente> findTop30ByOrderByFechaRegistroDesc();

    /**
     * Cumpleañeros de un día (sin importar el año). Ejemplo: mes = 11, dia = 11.
     * La usa la tarea automática de cada mañana (CuponService).
     */
    @Query("""
            SELECT c FROM Cliente c
            WHERE c.aceptaPromociones = true
              AND EXTRACT(MONTH FROM c.fechaNacimiento) = :mes
              AND EXTRACT(DAY FROM c.fechaNacimiento) = :dia
            ORDER BY c.nombre
            """)
    List<Cliente> cumpleanerosDel(@Param("mes") int mes, @Param("dia") int dia);
}
