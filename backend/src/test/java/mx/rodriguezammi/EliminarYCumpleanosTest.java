package mx.rodriguezammi;

import mx.rodriguezammi.comun.Eliminado;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.optica.armazon.ArmazonDto;
import mx.rodriguezammi.optica.armazon.ArmazonService;
import mx.rodriguezammi.optica.cliente.ClienteDto;
import mx.rodriguezammi.optica.cliente.ClienteService;
import mx.rodriguezammi.optica.orden.OrdenDto;
import mx.rodriguezammi.optica.orden.OrdenService;
import mx.rodriguezammi.optica.orden.TipoMica;
import mx.rodriguezammi.optica.promocion.CuponService;
import mx.rodriguezammi.papeleria.producto.ProductoDto;
import mx.rodriguezammi.papeleria.producto.ProductoRepository;
import mx.rodriguezammi.papeleria.producto.ProductoService;
import mx.rodriguezammi.papeleria.venta.MetodoPago;
import mx.rodriguezammi.papeleria.venta.VentaDto;
import mx.rodriguezammi.papeleria.venta.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas de: eliminar productos y armazones, y la validez de dos días del cupón de cumpleaños. */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class EliminarYCumpleanosTest {

    @Autowired ProductoService productos;
    @Autowired ProductoRepository productoRepository;
    @Autowired VentaService ventas;
    @Autowired ArmazonService armazones;
    @Autowired ClienteService clientes;
    @Autowired OrdenService ordenes;
    @Autowired CuponService cupones;

    @Test
    void unProductoQueNuncaSeVendioSeBorraPorCompleto() {
        var p = productos.crear(new ProductoDto.Guardar("Producto sin ventas", "7500000000001", new BigDecimal("9"), 4, 1, null, false));

        assertThat(productos.eliminar(p.id()).resultado()).isEqualTo(Eliminado.Resultado.ELIMINADO);
        assertThat(productoRepository.findById(p.id())).isEmpty();
    }

    @Test
    void unProductoConVentasSeArchivaYSuTicketSeConserva() {
        var p = productos.crear(new ProductoDto.Guardar("Producto vendido", "7500000000002", new BigDecimal("9"), 4, 1, null, false));
        var ticket = ventas.registrar(new VentaDto.Nueva(MetodoPago.EFECTIVO, null, List.of(new VentaDto.Renglon(p.id(), 1))));

        assertThat(productos.eliminar(p.id()).resultado()).isEqualTo(Eliminado.Resultado.ARCHIVADO);

        assertThat(productos.listar("Producto vendido")).isEmpty();                       // ya no aparece para vender
        assertThat(ventas.obtenerTicket(ticket.folio()).renglones()).hasSize(1);          // el ticket sigue completo
        // Su código de barras quedó libre para otro producto.
        productos.crear(new ProductoDto.Guardar("Producto nuevo", "7500000000002", new BigDecimal("5"), 1, 0, null, false));
    }

    @Test
    void losProductosQueUsaElBuzonNoSePuedenEliminar() {
        Long idImpresion = productoRepository.findByCodigoBarras("SRV-IMP-BN").orElseThrow().getId();

        assertThatThrownBy(() -> productos.eliminar(idImpresion))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("buzón de impresión");
    }

    @Test
    void unArmazonSinOrdenesSeBorraYUnoVendidoSeArchivaLiberandoSuCodigo() {
        var libre = armazones.crear(new ArmazonDto.Guardar("ELIM-1", "M", "Mod", "Negro", 52, 18, 140, new BigDecimal("500"), 1, null));
        var vendido = armazones.crear(new ArmazonDto.Guardar("ELIM-2", "M", "Mod", "Negro", 50, 18, 140, new BigDecimal("500"), 1, null));
        Long cliente = clientes.crear(new ClienteDto.Guardar("Cliente Armazón", null, null, false, null)).id();
        ordenes.crear(new OrdenDto.Nueva(cliente, null, null,
                List.of(new OrdenDto.Partida(vendido.id(), new BigDecimal("500"), TipoMica.SOLO_ARMAZON, null, BigDecimal.ZERO, List.of(), false)),
                null, null, null, null, null, null));

        assertThat(armazones.eliminar(libre.id()).resultado()).isEqualTo(Eliminado.Resultado.ELIMINADO);
        assertThat(armazones.eliminar(vendido.id()).resultado()).isEqualTo(Eliminado.Resultado.ARCHIVADO);

        assertThat(armazones.buscar("ELIM", null, false)).isEmpty();
        // El código del archivado se puede volver a usar.
        armazones.crear(new ArmazonDto.Guardar("ELIM-2", "M", "Mod", "Negro", 50, 18, 140, new BigDecimal("500"), 1, null));
    }

    @Test
    void lasMedidasSeMuestranConGuiones() {
        var a = armazones.crear(new ArmazonDto.Guardar("MED-1", "M", "Mod", "Negro", 52, 18, 140, new BigDecimal("500"), 1, null));
        assertThat(a.medidas()).isEqualTo("52-18-140");
    }

    @Test
    void elCuponDeCumpleanosValeElDiaDelCumpleanosYElSiguiente() {
        LocalDate hoy = LocalDate.now();
        Long id = clientes.crear(new ClienteDto.Guardar("Cumpleañera De Prueba", "5511223344", hoy.minusYears(30), true, null)).id();

        var ella = cupones.cumpleaneros(0).stream().filter(c -> c.clienteId().equals(id)).findFirst().orElseThrow();

        assertThat(ella.esHoy()).isTrue();
        assertThat(ella.vigenteHasta()).isEqualTo(hoy.plusDays(1));
        assertThat(java.net.URLDecoder.decode(ella.enlaceWhatsapp(), java.nio.charset.StandardCharsets.UTF_8)).contains("Válido hoy y mañana");
    }
}
