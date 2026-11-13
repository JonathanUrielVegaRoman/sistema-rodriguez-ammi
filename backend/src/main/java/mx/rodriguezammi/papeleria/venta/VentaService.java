package mx.rodriguezammi.papeleria.venta;

import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.papeleria.inventario.InventarioService;
import mx.rodriguezammi.papeleria.inventario.MovimientoInventario;
import mx.rodriguezammi.papeleria.producto.Producto;
import mx.rodriguezammi.papeleria.producto.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final InventarioService inventario;

    public VentaService(VentaRepository ventas, ProductoService productos, InventarioService inventario) {
        this.ventas = ventas;
        this.productos = productos;
        this.inventario = inventario;
    }

    @Transactional
    public VentaDto.Ticket registrar(VentaDto.Nueva datos) {
        // 1) ¿Es un reintento del mismo cobro? (el Wi-Fi falló y el celular lo mandó otra vez)
        String clave = vacioANull(datos.claveUnica());
        if (clave != null) {
            Optional<Venta> yaRegistrada = ventas.findByClaveUnica(clave);
            if (yaRegistrada.isPresent()) {
                return VentaDto.Ticket.desde(yaRegistrada.get());
            }
        }

        Venta venta = new Venta(datos.metodoPago(), datos.pagaCon());
        venta.setReferenciaPago(vacioANull(datos.referenciaPago()));
        venta.setClaveUnica(clave);

        // 2) CANDADO a cada producto, siempre en el MISMO ORDEN (por id).
        //    El orden fijo evita un "abrazo mortal" (deadlock): si un celular bloquea A y luego B,
        //    y otro bloquea B y luego A, los dos se quedarían esperándose para siempre.
        Map<Long, Producto> bloqueados = new HashMap<>();
        datos.renglones().stream().map(VentaDto.Renglon::productoId).distinct().sorted()
                .forEach(id -> bloqueados.put(id, productos.bloquearEntidad(id)));

        for (VentaDto.Renglon renglon : datos.renglones()) {
            Producto producto = bloqueados.get(renglon.productoId());

            if (!producto.isActivo()) {
                throw new ReglaDeNegocioException(producto.getNombre() + " ya no está a la venta");
            }
            if (!producto.alcanzaPara(renglon.cantidad())) {
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

        Venta guardada = ventas.save(venta);
        for (DetalleVenta d : guardada.getDetalles()) {
            inventario.registrar(d.getProducto(), MovimientoInventario.Tipo.VENTA, -d.getCantidad(), "Ticket " + guardada.getId());
        }
        return VentaDto.Ticket.desde(guardada);
    }

    /**
     * CANCELAR una venta (devolución o cobro equivocado). Regresa la mercancía al inventario
     * y la venta deja de contar en reportes. Todo dentro de una transacción: o se hace todo o nada.
     */
    @Transactional
    public VentaDto.Ticket cancelar(Long folio, String motivo) {
        Venta venta = ventas.findWithDetallesById(folio)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el ticket con folio " + folio));
        if (venta.isCancelada()) {
            throw new ReglaDeNegocioException("El ticket " + folio + " ya estaba cancelado");
        }
        // Mismo cuidado que al vender: candado a cada producto, en orden de id.
        Map<Long, Producto> bloqueados = new HashMap<>();
        venta.getDetalles().stream().map(d -> d.getProducto().getId()).distinct().sorted()
                .forEach(id -> bloqueados.put(id, productos.bloquearEntidad(id)));
        for (DetalleVenta d : venta.getDetalles()) {
            Producto p = bloqueados.get(d.getProducto().getId());
            if (!p.isServicio()) {
                p.setExistencia(p.getExistencia() + d.getCantidad());
                inventario.registrar(p, MovimientoInventario.Tipo.CANCELACION, d.getCantidad(), "Ticket " + folio);
            }
        }
        venta.cancelar(motivo.trim());
        return VentaDto.Ticket.desde(venta);
    }

    /** Tickets de un día, incluidas las canceladas (pantalla del administrador). */
    public List<VentaDto.Ticket> delDia(LocalDate dia) {
        return ventas.findByFechaBetweenOrderByFechaAsc(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay().minusNanos(1))
                .stream().map(VentaDto.Ticket::desde).toList();
    }

    public VentaDto.Ticket obtenerTicket(Long folio) {
        return ventas.findWithDetallesById(folio)
                .map(VentaDto.Ticket::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el ticket con folio " + folio));
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    /** Corte del día: cuánto se vendió en total y por método de pago. */
    public VentaDto.CorteDelDia corte(LocalDate dia) {
        return corte(dia, dia);
    }

    /**
     * Corte de un RANGO de días (semana, mes...). Solo lo ve el administrador
     * (ver admin/ReporteController). desde y hasta se incluyen completos.
     */
    public VentaDto.CorteDelDia corte(LocalDate desde, LocalDate hasta) {
        LocalDate dia = desde;
        List<Venta> delDia = ventas.findByFechaBetweenOrderByFechaAsc(
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay().minusNanos(1));

        BigDecimal total = BigDecimal.ZERO;
        Map<MetodoPago, BigDecimal> porMetodo = new EnumMap<>(MetodoPago.class);
        for (MetodoPago m : MetodoPago.values()) {
            porMetodo.put(m, BigDecimal.ZERO);
        }

        for (Venta v : delDia) {
            if (v.isCancelada()) {
                continue; // las canceladas no suman
            }
            total = total.add(v.getTotal());
            porMetodo.merge(v.getMetodoPago(), v.getTotal(), BigDecimal::add);
        }

        List<VentaDto.Ticket> tickets = delDia.stream().map(VentaDto.Ticket::desde).toList();
        int vigentes = (int) delDia.stream().filter(v -> !v.isCancelada()).count();
        return new VentaDto.CorteDelDia(dia, vigentes, total, porMetodo, tickets);
    }
}
