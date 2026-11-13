package mx.rodriguezammi;

import mx.rodriguezammi.comun.acceso.AccesoService;
import mx.rodriguezammi.comun.acceso.Rol;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.optica.cliente.ClienteDto;
import mx.rodriguezammi.optica.cliente.ClienteService;
import mx.rodriguezammi.papeleria.producto.ProductoDto;
import mx.rodriguezammi.papeleria.producto.ProductoService;
import mx.rodriguezammi.papeleria.venta.MetodoPago;
import mx.rodriguezammi.papeleria.venta.VentaDto;
import mx.rodriguezammi.papeleria.venta.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas de: cancelar ventas, bloqueo por inactividad y borrar varios clientes. */
@SpringBootTest(properties = "app.acceso.pin=4321")
@ActiveProfiles("h2")
@Transactional
class CancelacionEInactividadTest {

    @Autowired VentaService ventas;
    @Autowired ProductoService productos;
    @Autowired AccesoService acceso;
    @Autowired ClienteService clientes;
    @Autowired JdbcTemplate jdbc;
    @Autowired jakarta.persistence.EntityManager em;

    @Test
    void cancelarUnaVentaRegresaLaMercanciaYNoCuentaEnElCorte() {
        var p = productos.crear(new ProductoDto.Guardar("Producto Cancelable", null, new BigDecimal("10"), 5, 1, null, false));
        var t = ventas.registrar(new VentaDto.Nueva(MetodoPago.EFECTIVO, null, List.of(new VentaDto.Renglon(p.id(), 3))));
        assertThat(productos.obtener(p.id()).existencia()).isEqualTo(2);
        var antes = ventas.corte(LocalDate.now()).total();

        var cancelado = ventas.cancelar(t.folio(), "Prueba");

        assertThat(cancelado.cancelada()).isTrue();
        assertThat(productos.obtener(p.id()).existencia()).isEqualTo(5);
        assertThat(ventas.corte(LocalDate.now()).total()).isEqualByComparingTo(antes.subtract(new BigDecimal("30")));
        assertThatThrownBy(() -> ventas.cancelar(t.folio(), "Otra vez")).isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void unPaseSinUsoPorMasDeLosMinutosConfiguradosYaNoSirve() {
        String token = acceso.entrarComoPersonal("4321", "prueba-inactividad", "test").getToken();
        assertThat(acceso.tieneRol(token, Rol.PERSONAL)).isTrue();

        // Simulamos que el celular lleva 2 horas sin usarse (por defecto se bloquea a los 10 minutos).
        // flush/clear: JPA guarda una copia en memoria; la limpiamos para que vuelva a leer de la base.
        em.flush();
        jdbc.update("UPDATE sesiones_acceso SET ultimo_uso = ? WHERE token = ?", LocalDateTime.now().minusHours(2), token);
        em.clear();

        assertThat(acceso.tieneRol(token, Rol.PERSONAL)).isFalse();
    }

    @Test
    void sePuedenBorrarVariosClientesDeUnaVez() {
        var a = clientes.crear(new ClienteDto.Guardar("Varios Uno", null, null, false, null));
        var b = clientes.crear(new ClienteDto.Guardar("Varios Dos", null, null, false, null));

        var r = clientes.eliminarVarios(List.of(a.id(), b.id()));

        assertThat(r.eliminados()).isEqualTo(2);
        assertThat(clientes.buscar("Varios")).isEmpty();
    }
}
