package mx.rodriguezammi.comun.acceso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SesionAccesoRepository extends JpaRepository<SesionAcceso, String> {

    List<SesionAcceso> findByRolOrderByCreadaDesc(Rol rol);

    @Modifying
    @Query("DELETE FROM SesionAcceso s WHERE s.expira < :ahora")
    int borrarVencidas(LocalDateTime ahora);

    @Modifying
    @Query("DELETE FROM SesionAcceso s WHERE s.rol = :rol")
    int borrarPorRol(Rol rol);
}
