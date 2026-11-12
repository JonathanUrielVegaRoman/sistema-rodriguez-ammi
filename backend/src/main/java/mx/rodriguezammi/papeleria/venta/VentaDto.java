package mx.rodriguezammi.papeleria.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Formatos de entrada y salida del módulo de ventas (ver ProductoDto para la explicación de qué es un DTO). */
public final class VentaDto {

    private VentaDto() {
    }

    // ---------------- ENTRADA ----------------

    /**
     * Lo que manda el frontend al cobrar. Ejemplo en JSON:
     * {
     *   "metodoPago": "EFECTIVO",
     *   "pagaCon": 100,
     *   "renglones": [ { "productoId": 1, "cantidad": 2 }, { "productoId": 3, "cantidad": 1 } ]
     * }
     * Nota: el frontend NO manda precios. El precio lo toma el backend de la base de datos;
     * así nadie puede "cobrar barato" modificando la petición.
     */
    public record Nueva(
            @NotNull(message = "Indica el método de pago")
            MetodoPago metodoPago,

            BigDecimal pagaCon,

            @NotEmpty(message = "La venta no tiene productos")
            List<@Valid Renglon> renglones
    ) {
    }

    public record Renglon(
            @NotNull Long productoId,
            @Positive(message = "La cantidad debe ser mayor a cero") int cantidad
    ) {
    }

    // ---------------- SALIDA ----------------

    /** El ticket tal cual se imprime. */
    public record Ticket(
            Long folio,
            LocalDateTime fecha,
            MetodoPago metodoPago,
            List<RenglonTicket> renglones,
            BigDecimal total,
            BigDecimal pagaCon,
            BigDecimal cambio
    ) {
        public static Ticket desde(Venta v) {
            List<RenglonTicket> renglones = v.getDetalles().stream()
                    .map(d -> new RenglonTicket(d.getDescripcion(), d.getCantidad(), d.getPrecioUnitario(), d.getImporte()))
                    .toList();
            return new Ticket(v.getId(), v.getFecha(), v.getMetodoPago(), renglones,
                    v.getTotal(), v.getPagaCon(), v.getCambio());
        }
    }

    public record RenglonTicket(String descripcion, int cantidad, BigDecimal precioUnitario, BigDecimal importe) {
    }

    /** Resumen para el "corte de caja" del día. */
    public record CorteDelDia(
            LocalDate fecha,
            int numeroVentas,
            BigDecimal total,
            Map<MetodoPago, BigDecimal> totalPorMetodo,
            List<Ticket> ventas
    ) {
    }
}
