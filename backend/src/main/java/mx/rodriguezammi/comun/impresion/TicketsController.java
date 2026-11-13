package mx.rodriguezammi.comun.impresion;

import mx.rodriguezammi.optica.orden.OrdenDto;
import mx.rodriguezammi.optica.orden.OrdenService;
import mx.rodriguezammi.papeleria.venta.MetodoPago;
import mx.rodriguezammi.papeleria.venta.VentaDto;
import mx.rodriguezammi.papeleria.venta.VentaService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Arma el contenido de cada ticket y lo manda a la impresora de tickets.
 *
 *   POST /api/papeleria/ventas/{folio}/imprimir   ticket de la papelería
 *   POST /api/optica/ordenes/{folio}/imprimir     nota de la óptica
 *
 * Respuesta: { "impreso": true, "impresora": "POS-58" }
 * Si impreso = false (no hay impresora configurada), la pantalla usa Ctrl+P como respaldo.
 */
@RestController
public class TicketsController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ImpresorTickets impresor;
    private final VentaService ventas;
    private final OrdenService ordenes;

    public TicketsController(ImpresorTickets impresor, VentaService ventas, OrdenService ordenes) {
        this.impresor = impresor;
        this.ventas = ventas;
        this.ordenes = ordenes;
    }

    @PostMapping("/api/papeleria/ventas/{folio}/imprimir")
    public ImpresorTickets.Resultado ticketPapeleria(@PathVariable Long folio) {
        VentaDto.Ticket t = ventas.obtenerTicket(folio);
        List<ImpresorTickets.Renglon> r = new ArrayList<>();
        r.add(ImpresorTickets.Renglon.titulo("PAPELERÍA AMMI"));
        r.add(ImpresorTickets.Renglon.centrado("Folio " + t.folio() + "  ·  " + t.fecha().format(FECHA)));
        if (t.cancelada()) {
            r.add(ImpresorTickets.Renglon.titulo("*** CANCELADO ***"));
        }
        r.add(ImpresorTickets.Renglon.separador());
        for (VentaDto.RenglonTicket d : t.renglones()) {
            r.add(ImpresorTickets.Renglon.par(d.cantidad() + " x " + d.descripcion(), ImpresorTickets.dinero(d.importe())));
            if (d.cantidad() > 1) {
                r.add(ImpresorTickets.Renglon.pequeno("    " + ImpresorTickets.dinero(d.precioUnitario()) + " c/u"));
            }
        }
        r.add(ImpresorTickets.Renglon.separador());
        r.add(ImpresorTickets.Renglon.negrita("TOTAL", ImpresorTickets.dinero(t.total())));
        r.add(ImpresorTickets.Renglon.par("Pago", nombre(t.metodoPago())));
        if (t.referenciaPago() != null) {
            r.add(ImpresorTickets.Renglon.par("Ref.", t.referenciaPago()));
        }
        if (t.pagaCon() != null) {
            r.add(ImpresorTickets.Renglon.par("Recibido", ImpresorTickets.dinero(t.pagaCon())));
            r.add(ImpresorTickets.Renglon.par("Cambio", ImpresorTickets.dinero(t.cambio())));
        }
        r.add(ImpresorTickets.Renglon.separador());
        r.add(ImpresorTickets.Renglon.centrado("¡Gracias por su compra!"));
        return impresor.imprimir("Ticket AMMI " + t.folio(), r);
    }

    @PostMapping("/api/optica/ordenes/{folio}/imprimir")
    public ImpresorTickets.Resultado notaOptica(@PathVariable Long folio) {
        OrdenDto.Detalle o = ordenes.obtener(folio);
        List<ImpresorTickets.Renglon> r = new ArrayList<>();
        r.add(ImpresorTickets.Renglon.titulo("ÓPTICA RODRÍGUEZ"));
        r.add(ImpresorTickets.Renglon.centrado("Folio " + o.folio() + "  ·  " + o.fecha().format(FECHA)));
        r.add(ImpresorTickets.Renglon.centrado(o.clienteNombre()));
        r.add(ImpresorTickets.Renglon.separador());
        for (OrdenDto.PartidaRespuesta p : o.partidas()) {
            r.add(ImpresorTickets.Renglon.par(p.descripcionArmazon() + (p.esRegalo() ? " (REGALO 2x1)" : ""), ImpresorTickets.dinero(p.subtotal())));
            String mica = p.tipoMica().name().charAt(0) + p.tipoMica().name().substring(1).toLowerCase().replace("_", " ")
                    + (p.material() != null ? " " + p.material() : "") + (p.tratamientos() != null ? " · " + p.tratamientos() : "");
            r.add(ImpresorTickets.Renglon.pequeno("  " + mica));
        }
        r.add(ImpresorTickets.Renglon.separador());
        if (o.descuentoCupon().signum() > 0) {
            r.add(ImpresorTickets.Renglon.par("Cupón " + o.codigoCupon(), "-" + ImpresorTickets.dinero(o.descuentoCupon())));
        }
        if (o.descuentoManual().signum() > 0) {
            r.add(ImpresorTickets.Renglon.par("Descuento", "-" + ImpresorTickets.dinero(o.descuentoManual())));
        }
        r.add(ImpresorTickets.Renglon.negrita("TOTAL", ImpresorTickets.dinero(o.total())));
        r.add(ImpresorTickets.Renglon.par("Pagado", ImpresorTickets.dinero(o.pagado())));
        r.add(ImpresorTickets.Renglon.negrita("SALDO", ImpresorTickets.dinero(o.saldo())));
        if (o.fechaEntregaEstimada() != null) {
            r.add(ImpresorTickets.Renglon.separador());
            r.add(ImpresorTickets.Renglon.centrado("Entrega estimada: " + o.fechaEntregaEstimada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        }
        r.add(ImpresorTickets.Renglon.centrado("Presente esta nota al recoger sus lentes."));
        return impresor.imprimir("Nota óptica " + o.folio(), r);
    }

    private static String nombre(MetodoPago m) {
        return switch (m) {
            case EFECTIVO -> "Efectivo";
            case TARJETA -> "Tarjeta";
            case TRANSFERENCIA -> "Transferencia";
        };
    }
}
