package mx.rodriguezammi.papeleria.venta;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
     *
     * referenciaPago: clave de rastreo de la transferencia o autorización de la terminal (opcional).
     * claveUnica: la genera el celular por cada cobro, para no cobrar doble si reintenta (opcional).
     */
    public record Nueva(
            @NotNull(message = "Indica el método de pago")
            MetodoPago metodoPago,

            BigDecimal pagaCon,

            @NotEmpty(message = "La venta no tiene productos")
            @Size(max = 200, message = "Demasiados renglones en una sola venta")
            List<@Valid Renglon> renglones,

            @Size(max = 60, message = "La referencia es demasiado larga")
            String referenciaPago,

            @Size(max = 40)
            String claveUnica
    ) {
        /** Le dice a Jackson (quien convierte el JSON) que use este constructor completo. */
        @JsonCreator
        public Nueva {
        }

        /** Atajo para ventas en efectivo sin referencia (lo usan las pruebas y el buzón). */
        public Nueva(MetodoPago metodoPago, BigDecimal pagaCon, List<Renglon> renglones) {
            this(metodoPago, pagaCon, renglones, null, null);
        }
    }

    public record Renglon(
            @NotNull Long productoId,
            @Positive(message = "La cantidad debe ser mayor a cero")
            @Max(value = 10000, message = "La cantidad es demasiado grande") int cantidad
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
            BigDecimal cambio,
            boolean cancelada,
            String motivoCancelacion,
            String referenciaPago
    ) {
        public static Ticket desde(Venta v) {
            List<RenglonTicket> renglones = v.getDetalles().stream()
                    .map(d -> new RenglonTicket(d.getDescripcion(), d.getCantidad(), d.getPrecioUnitario(), d.getImporte()))
                    .toList();
            return new Ticket(v.getId(), v.getFecha(), v.getMetodoPago(), renglones,
                    v.getTotal(), v.getPagaCon(), v.getCambio(), v.isCancelada(), v.getMotivoCancelacion(), v.getReferenciaPago());
        }
    }

    public record RenglonTicket(String descripcion, int cantidad, BigDecimal precioUnitario, BigDecimal importe) {
    }

    /** Motivo obligatorio al cancelar una venta (queda registrado). */
    public record Cancelacion(@NotBlank(message = "Escribe el motivo de la cancelación") @Size(max = 200) String motivo) {
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
