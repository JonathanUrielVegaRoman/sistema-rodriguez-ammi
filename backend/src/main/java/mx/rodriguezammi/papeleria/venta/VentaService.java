package mx.rodriguezammi.papeleria.venta;

import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.papeleria.producto.Producto;
import mx.rodriguezammi.papeleria.producto.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Reglas del negocio para VENDER en la papelería.
 *
 * Este Service es un buen ejemplo de por qué existe @Transactional:
 * registrar una venta implica VARIOS cambios (crear el ticket, crear renglones,
 * descontar existencia de cada producto). Si el tercer producto no alcanza,
 * se cancela TODO; no queremos un ticket a medias ni inventario descontado a medias.
 *
 * Fíjate también: un Service puede usar OTRO Service (aquí, ProductoService).
 */
@Service
@Transactional(readOnly = true)
public class VentaService {

    private final VentaRepository ventas;
    private final ProductoService productos;

    public VentaService(VentaRepository ventas, ProductoService productos) {
        this.ventas = ventas;
        this.productos = productos;
    }

    @Transactional
    public VentaDto.Ticket registrar(VentaDto.Nueva datos) {
        Venta venta = new Venta(datos.metodoPago(), datos.pagaCon());

        for (VentaDto.Renglon renglon : datos.renglones()) {
            Producto producto = productos.buscarEntidad(renglon.productoId());

            if (!producto.isActivo()) {
                throw new ReglaDeNegocioException(producto.getNombre() + " ya no está a la venta");
            }
            if (producto.getExistencia() < renglon.cantidad()) {
                throw new ReglaDeNegocioException("No hay suficiente existencia de " + producto.getNombre()
                        + " (quedan " + producto.getExistencia() + ")");
            }

            producto.descontarExistencia(renglon.cantidad());
            venta.agregarDetalle(new DetalleVenta(producto, renglon.cantidad()));
        }

        if (datos.metodoPago() == MetodoPago.EFECTIVO && datos.pagaCon() != null
                && datos.pagaCon().compareTo(venta.getTotal()) < 0) {
            throw new ReglaDeNegocioException("El pago ($" + datos.pagaCon() + ") no cubre el total ($" + venta.getTotal() + ")");
        }

        return VentaDto.Ticket.desde(ventas.save(venta));
    }

    public VentaDto.Ticket obtenerTicket(Long folio) {
        return ventas.findWithDetallesById(folio)
                .map(VentaDto.Ticket::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el ticket con folio " + folio));
    }

    /** Corte del día: cuánto se vendió en total y por método de pago. */
    public VentaDto.CorteDelDia corte(LocalDate dia) {
        List<Venta> delDia = ventas.findByFechaBetweenOrderByFechaAsc(
                dia.atStartOfDay(), dia.plusDays(1).atStartOfDay().minusNanos(1));

        BigDecimal total = BigDecimal.ZERO;
        Map<MetodoPago, BigDecimal> porMetodo = new EnumMap<>(MetodoPago.class);
        for (MetodoPago m : MetodoPago.values()) {
            porMetodo.put(m, BigDecimal.ZERO);
        }

        for (Venta v : delDia) {
            total = total.add(v.getTotal());
            porMetodo.merge(v.getMetodoPago(), v.getTotal(), BigDecimal::add);
        }

        List<VentaDto.Ticket> tickets = delDia.stream().map(VentaDto.Ticket::desde).toList();
        return new VentaDto.CorteDelDia(dia, delDia.size(), total, porMetodo, tickets);
    }
}
