package mx.rodriguezammi.optica.orden;

import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.optica.armazon.ArmazonDto;
import mx.rodriguezammi.optica.armazon.ArmazonService;
import mx.rodriguezammi.optica.cliente.ClienteDto;
import mx.rodriguezammi.optica.cliente.ClienteService;
import mx.rodriguezammi.optica.promocion.CuponService;
import mx.rodriguezammi.optica.tratamiento.Tratamiento;
import mx.rodriguezammi.optica.tratamiento.TratamientoRepository;
import mx.rodriguezammi.papeleria.venta.MetodoPago;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas de las reglas de las órdenes de lentes (promoción 2x1, cupones, entrega, cancelación). */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class OrdenServiceTest {

    @Autowired OrdenService ordenes;
    @Autowired ClienteService clientes;
    @Autowired ArmazonService armazones;
    @Autowired TratamientoRepository tratamientos;
    @Autowired CuponService cupones;

    Long clienteId;
    Long armazonId;
    Tratamiento barato;   // entra en promoción
    Tratamiento caro;     // NO entra en promoción

    @BeforeEach
    void preparar() {
        clienteId = clientes.crear(new ClienteDto.Guardar("Cliente Prueba", "5500000000", null, true, null)).id();
        armazonId = armazones.crear(new ArmazonDto.Guardar("PRUEBA-1", "Marca", "Modelo", "Negro",
                52, 18, 140, new BigDecimal("1000"), 2, null)).id();
        barato = tratamientos.save(new Tratamiento("Barato prueba", new BigDecimal("100"), true));
        caro = tratamientos.save(new Tratamiento("Caro prueba", new BigDecimal("900"), false));
    }

    private OrdenDto.Partida par(Long armazon, BigDecimal precioArmazon, List<Long> trats, boolean regalo) {
        return new OrdenDto.Partida(armazon, precioArmazon, TipoMica.MONOFOCAL, "CR-39", new BigDecimal("500"), trats, regalo);
    }

    private OrdenDto.Nueva orden(List<OrdenDto.Partida> partidas, String cupon, BigDecimal anticipo) {
        return new OrdenDto.Nueva(clienteId, null, null, partidas, cupon, null, null, anticipo,
                anticipo == null ? null : MetodoPago.EFECTIVO, null);
    }

    @Test
    void elParDeRegaloNoCobraMicasNiTratamientos() {
        var detalle = ordenes.crear(orden(List.of(
                par(armazonId, new BigDecimal("1000"), List.of(caro.getId()), false),   // 1000 + 500 + 900
                par(null, BigDecimal.ZERO, List.of(barato.getId()), true)),              // regalo: $0
                null, new BigDecimal("400")));

        assertThat(detalle.total()).isEqualByComparingTo("2400");
        assertThat(detalle.saldo()).isEqualByComparingTo("2000");
        assertThat(armazones.obtener(armazonId).existencia()).isEqualTo(1); // se descontó del inventario
    }

    @Test
    void elRegaloNoAdmiteTratamientosCaros() {
        assertThatThrownBy(() -> ordenes.crear(orden(List.of(
                par(armazonId, new BigDecimal("1000"), List.of(), false),
                par(null, BigDecimal.ZERO, List.of(caro.getId()), true)), null, null)))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("no entra en el lente de regalo");
    }

    @Test
    void noHayRegaloSinUnParPagado() {
        assertThatThrownBy(() -> ordenes.crear(orden(List.of(par(null, BigDecimal.ZERO, List.of(), true)), null, null)))
                .isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void elCuponDescuentaYSoloSeUsaUnaVez() {
        String codigo = cupones.crearDescuento(clienteId, 10, 30, "prueba").codigo();

        var detalle = ordenes.crear(orden(List.of(par(armazonId, new BigDecimal("1000"), List.of(), false)), codigo, null));
        assertThat(detalle.descuentoCupon()).isEqualByComparingTo("150"); // 10 % de 1500
        assertThat(detalle.total()).isEqualByComparingTo("1350");

        assertThatThrownBy(() -> ordenes.crear(orden(List.of(par(null, new BigDecimal("0"), List.of(), false)), codigo, null)))
                .hasMessageContaining("ya se usó");
    }

    @Test
    void soloSeEntregaSiEstaPagadaYCancelarDevuelveElArmazon() {
        var detalle = ordenes.crear(orden(List.of(par(armazonId, new BigDecimal("1000"), List.of(), false)), null, new BigDecimal("500")));

        assertThatThrownBy(() -> ordenes.cambiarEstado(detalle.folio(), EstadoOrden.ENTREGADA))
                .hasMessageContaining("liquida el saldo");

        ordenes.abonar(detalle.folio(), new OrdenDto.Abono(new BigDecimal("1000"), MetodoPago.TARJETA));
        assertThat(ordenes.cambiarEstado(detalle.folio(), EstadoOrden.ENTREGADA).estado()).isEqualTo(EstadoOrden.ENTREGADA);

        var otra = ordenes.crear(orden(List.of(par(armazonId, new BigDecimal("1000"), List.of(), false)), null, null));
        assertThat(armazones.obtener(armazonId).existencia()).isZero();
        ordenes.cambiarEstado(otra.folio(), EstadoOrden.CANCELADA);
        assertThat(armazones.obtener(armazonId).existencia()).isEqualTo(1);
    }
}
