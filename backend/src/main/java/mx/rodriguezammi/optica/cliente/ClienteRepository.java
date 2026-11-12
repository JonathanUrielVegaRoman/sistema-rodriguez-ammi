package mx.rodriguezammi.optica.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNombreContainingIgnoreCaseOrderByNombreAsc(String texto);

    List<Cliente> findByCelular(String celular);

    /**
     * Cumpleañeros de un día (sin importar el año). Ejemplo: mes = 11, dia = 11.
     * Esta consulta la usará la tarea automática de cada mañana (ver EJERCICIOS.md).
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
