package mx.rodriguezammi.admin;

import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.optica.orden.EstadoOrden;
import mx.rodriguezammi.optica.orden.OrdenLentes;
import mx.rodriguezammi.optica.orden.OrdenRepository;
import mx.rodriguezammi.optica.orden.PagoOrden;
import mx.rodriguezammi.papeleria.venta.DetalleVenta;
import mx.rodriguezammi.papeleria.venta.MetodoPago;
import mx.rodriguezammi.papeleria.venta.Venta;
import mx.rodriguezammi.papeleria.venta.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * REPORTES PRIVADOS para el dueño (solo con contraseña de administrador).
 *
 * Responde: ¿cuánto se vendió hoy / esta semana / este mes, en cada negocio y por método de pago?
 *
 * Óptica: se reporta el dinero que ENTRÓ (pagos: anticipos y liquidaciones) en el periodo,
 * no el total de las órdenes. Ejemplo: una orden de $3,000 con anticipo de $1,000 el lunes
 * y liquidación de $2,000 el viernes suma $1,000 al lunes y $2,000 al viernes.
 */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    private static final int MAX_DIAS = 400;

    private final VentaRepository ventas;
    private final OrdenRepository ordenes;

    public ReporteService(VentaRepository ventas, OrdenRepository ordenes) {
        this.ventas = ventas;
        this.ordenes = ordenes;
    }

    public record PorDia(LocalDate fecha, BigDecimal papeleria, BigDecimal optica) {
    }

    public record ProductoVendido(String nombre, int cantidad, BigDecimal importe) {
    }

    public record Papeleria(BigDecimal total, int numeroVentas, BigDecimal ticketPromedio,
                            Map<MetodoPago, BigDecimal> porMetodo, List<ProductoVendido> masVendidos) {
    }

    public record Optica(BigDecimal cobrado, Map<MetodoPago, BigDecimal> porMetodo, int ordenesNuevas,
                         BigDecimal valorOrdenesNuevas, BigDecimal saldoPendienteTotal, int ordenesPendientes) {
    }

    public record Reporte(LocalDate desde, LocalDate hasta, BigDecimal granTotal,
                         Papeleria papeleria, Optica optica, List<PorDia> porDia) {
    }

    public Reporte generar(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new ReglaDeNegocioException("La fecha final es anterior a la inicial");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > MAX_DIAS) {
            throw new ReglaDeNegocioException("El periodo máximo es de " + MAX_DIAS + " días");
        }
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay().minusNanos(1);

        // Un renglón por día del periodo (aunque sea $0), para la gráfica.
        Map<LocalDate, BigDecimal[]> dias = new TreeMap<>();
        for (LocalDate d = desde; !d.isAfter(hasta); d = d.plusDays(1)) {
            dias.put(d, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }

        // ---------- Papelería ----------
        List<Venta> delPeriodo = ventas.findByFechaBetweenOrderByFechaAsc(inicio, fin).stream()
                .filter(v -> !v.isCancelada()).toList(); // las canceladas no cuentan
        BigDecimal totalPapeleria = BigDecimal.ZERO;
        Map<MetodoPago, BigDecimal> metodoPapeleria = ceros();
        Map<String, ProductoVendido> productos = new HashMap<>();
        for (Venta v : delPeriodo) {
            totalPapeleria = totalPapeleria.add(v.getTotal());
            metodoPapeleria.merge(v.getMetodoPago(), v.getTotal(), BigDecimal::add);
            dias.get(v.getFecha().toLocalDate())[0] = dias.get(v.getFecha().toLocalDate())[0].add(v.getTotal());
            for (DetalleVenta d : v.getDetalles()) {
                productos.merge(d.getDescripcion(), new ProductoVendido(d.getDescripcion(), d.getCantidad(), d.getImporte()),
                        (a, b) -> new ProductoVendido(a.nombre(), a.cantidad() + b.cantidad(), a.importe().add(b.importe())));
            }
        }
        List<ProductoVendido> top = productos.values().stream()
                .sorted(Comparator.comparing(ProductoVendido::importe).reversed()).limit(10).toList();
        BigDecimal promedio = delPeriodo.isEmpty() ? BigDecimal.ZERO
                : totalPapeleria.divide(BigDecimal.valueOf(delPeriodo.size()), 2, java.math.RoundingMode.HALF_UP);

        // ---------- Óptica ----------
        BigDecimal cobrado = BigDecimal.ZERO;
        Map<MetodoPago, BigDecimal> metodoOptica = ceros();
        for (PagoOrden p : ordenes.pagosEntre(inicio, fin)) {
            cobrado = cobrado.add(p.getMonto());
            metodoOptica.merge(p.getMetodoPago(), p.getMonto(), BigDecimal::add);
            dias.get(p.getFecha().toLocalDate())[1] = dias.get(p.getFecha().toLocalDate())[1].add(p.getMonto());
        }
        List<OrdenLentes> nuevas = ordenes.findByFechaBetweenOrderByFechaAsc(inicio, fin).stream()
                .filter(o -> o.getEstado() != EstadoOrden.CANCELADA).toList();
        BigDecimal valorNuevas = nuevas.stream().map(OrdenLentes::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<OrdenLentes> pendientes = ordenes.findByEstadoIn(EnumSet.of(EstadoOrden.EN_LABORATORIO, EstadoOrden.LISTA));
        BigDecimal saldo = pendientes.stream().map(OrdenLentes::getSaldo).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PorDia> porDia = dias.entrySet().stream()
                .map(e -> new PorDia(e.getKey(), e.getValue()[0], e.getValue()[1])).toList();

        return new Reporte(desde, hasta, totalPapeleria.add(cobrado),
                new Papeleria(totalPapeleria, delPeriodo.size(), promedio, metodoPapeleria, top),
                new Optica(cobrado, metodoOptica, nuevas.size(), valorNuevas, saldo, pendientes.size()),
                porDia);
    }

    private Map<MetodoPago, BigDecimal> ceros() {
        Map<MetodoPago, BigDecimal> m = new EnumMap<>(MetodoPago.class);
        for (MetodoPago metodo : MetodoPago.values()) {
            m.put(metodo, BigDecimal.ZERO);
        }
        return m;
    }
}
