package mx.rodriguezammi.optica.orden;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import mx.rodriguezammi.optica.armazon.ArmazonDto;
import mx.rodriguezammi.optica.receta.RecetaDto;
import mx.rodriguezammi.papeleria.venta.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class OrdenDto {

    private OrdenDto() {
    }

    // ---------------- ENTRADA ----------------

    /**
     * Lo que manda la pantalla "Nueva orden". Ejemplo:
     * {
     *   "clienteId": 3, "recetaId": 8, "fechaEntregaEstimada": "2026-11-20",
     *   "partidas": [
     *     { "armazonId": 12, "precioArmazon": 1200, "tipoMica": "MONOFOCAL", "material": "CR-39",
     *       "precioMicas": 800, "tratamientoIds": [1, 2], "esRegalo": false },
     *     { "armazonId": null, "precioArmazon": 0, "tipoMica": "MONOFOCAL", "material": "CR-39",
     *       "precioMicas": 0, "tratamientoIds": [1], "esRegalo": true }
     *   ],
     *   "codigoCupon": "CUMPLE-7K3P9", "descuentoManual": 0, "motivoDescuento": null,
     *   "anticipo": 1000, "metodoPagoAnticipo": "EFECTIVO", "notas": "Entregar en estuche rojo"
     * }
     */
    public record Nueva(
            @NotNull(message = "Elige al cliente") Long clienteId,
            Long recetaId,
            LocalDate fechaEntregaEstimada,
            @NotEmpty(message = "Agrega al menos un par de lentes")
            @Size(max = 10, message = "Máximo 10 pares por orden") List<@Valid Partida> partidas,
            @Size(max = 20) String codigoCupon,
            @PositiveOrZero BigDecimal descuentoManual,
            @Size(max = 120) String motivoDescuento,
            @PositiveOrZero BigDecimal anticipo,
            MetodoPago metodoPagoAnticipo,
            @Size(max = 500) String notas,
            @Size(max = 60) String referenciaAnticipo,
            @Size(max = 40) String claveUnica
    ) {
        @JsonCreator
        public Nueva {
        }

        /** Atajo sin referencia ni clave única (lo usan las pruebas). */
        public Nueva(Long clienteId, Long recetaId, LocalDate fechaEntregaEstimada, List<Partida> partidas,
                     String codigoCupon, BigDecimal descuentoManual, String motivoDescuento,
                     BigDecimal anticipo, MetodoPago metodoPagoAnticipo, String notas) {
            this(clienteId, recetaId, fechaEntregaEstimada, partidas, codigoCupon, descuentoManual, motivoDescuento,
                    anticipo, metodoPagoAnticipo, notas, null, null);
        }
    }

    public record Partida(
            Long armazonId,
            @NotNull @PositiveOrZero BigDecimal precioArmazon,
            @NotNull(message = "Elige el tipo de mica") TipoMica tipoMica,
            @Size(max = 60) String material,
            @NotNull @PositiveOrZero BigDecimal precioMicas,
            List<Long> tratamientoIds,
            boolean esRegalo
    ) {
    }

    public record Abono(
            @NotNull @Positive(message = "El abono debe ser mayor a cero") BigDecimal monto,
            @NotNull MetodoPago metodoPago,
            @Size(max = 60) String referencia,
            @Size(max = 40) String claveUnica
    ) {
        @JsonCreator
        public Abono {
        }

        public Abono(BigDecimal monto, MetodoPago metodoPago) {
            this(monto, metodoPago, null, null);
        }
    }

    public record CambioEstado(@NotNull EstadoOrden estado) {
    }

    // ---------------- SALIDA ----------------

    public record Detalle(
            Long folio, LocalDateTime fecha, EstadoOrden estado,
            Long clienteId, String clienteNombre, String clienteCelular,
            RecetaDto.Respuesta receta, LocalDate fechaEntregaEstimada,
            List<PartidaRespuesta> partidas,
            BigDecimal subtotal, String codigoCupon, Integer porcentajeCupon, BigDecimal descuentoCupon,
            BigDecimal descuentoManual, String motivoDescuento,
            BigDecimal total, BigDecimal pagado, BigDecimal saldo,
            List<PagoRespuesta> pagos, String notas, LocalDateTime fechaLista
    ) {
        public static Detalle desde(OrdenLentes o) {
            return new Detalle(o.getId(), o.getFecha(), o.getEstado(),
                    o.getCliente().getId(), o.getCliente().getNombre(), o.getCliente().getCelular(),
                    o.getReceta() == null ? null : RecetaDto.Respuesta.desde(o.getReceta()),
                    o.getFechaEntregaEstimada(),
                    o.getPartidas().stream().map(PartidaRespuesta::desde).toList(),
                    o.getSubtotal(),
                    o.getCupon() == null ? null : o.getCupon().getCodigo(),
                    o.getCupon() == null ? null : o.getCupon().getPorcentaje(),
                    o.getDescuentoCupon(), o.getDescuentoManual(), o.getMotivoDescuento(),
                    o.getTotal(), o.getPagado(), o.getSaldo(),
                    o.getPagos().stream().map(PagoRespuesta::desde).toList(), o.getNotas(), o.getFechaLista());
        }
    }

    public record PartidaRespuesta(
            Long armazonId, String descripcionArmazon, String fotoUrl, String medidas, BigDecimal precioArmazon,
            TipoMica tipoMica, String material, BigDecimal precioMicas,
            String tratamientos, BigDecimal precioTratamientos, boolean esRegalo, BigDecimal subtotal
    ) {
        static PartidaRespuesta desde(PartidaLente p) {
            ArmazonDto.Respuesta a = p.getArmazon() == null ? null : ArmazonDto.Respuesta.desde(p.getArmazon());
            return new PartidaRespuesta(a == null ? null : a.id(), p.getDescripcionArmazon(),
                    a == null ? null : a.fotoUrl(), a == null ? null : a.medidas(), p.getPrecioArmazon(),
                    p.getTipoMica(), p.getMaterial(), p.getPrecioMicas(),
                    p.getTratamientos(), p.getPrecioTratamientos(), p.isEsRegalo(), p.getSubtotal());
        }
    }

    public record PagoRespuesta(LocalDateTime fecha, BigDecimal monto, MetodoPago metodoPago, String referencia) {
        static PagoRespuesta desde(PagoOrden p) {
            return new PagoRespuesta(p.getFecha(), p.getMonto(), p.getMetodoPago(), p.getReferencia());
        }
    }

    /** Para las listas: lo justo para reconocer la orden de un vistazo (con la foto del armazón). */
    public record Resumen(
            Long folio, LocalDateTime fecha, EstadoOrden estado, Long clienteId, String clienteNombre,
            String clienteCelular, String fotoUrl, String descripcion, BigDecimal total, BigDecimal saldo,
            LocalDate fechaEntregaEstimada, Integer diasLista
    ) {
        public static Resumen desde(OrdenLentes o) {
            PartidaLente primera = o.getPartidas().isEmpty() ? null : o.getPartidas().get(0);
            String foto = primera == null || primera.getArmazon() == null ? null
                    : ArmazonDto.Respuesta.desde(primera.getArmazon()).fotoUrl();
            String descripcion = primera == null ? "" : primera.getDescripcionArmazon()
                    + (o.getPartidas().size() > 1 ? " + " + (o.getPartidas().size() - 1) + " par(es) más" : "");
            return new Resumen(o.getId(), o.getFecha(), o.getEstado(), o.getCliente().getId(), o.getCliente().getNombre(),
                    o.getCliente().getCelular(), foto, descripcion, o.getTotal(), o.getSaldo(), o.getFechaEntregaEstimada(),
                    diasLista(o));
        }

        /** Días que lleva LISTA sin que la recojan (null si no aplica). */
        private static Integer diasLista(OrdenLentes o) {
            if (o.getEstado() != EstadoOrden.LISTA || o.getFechaLista() == null) {
                return null;
            }
            return (int) java.time.temporal.ChronoUnit.DAYS.between(o.getFechaLista().toLocalDate(), LocalDate.now());
        }
    }
}
