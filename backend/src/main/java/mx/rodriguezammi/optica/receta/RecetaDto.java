package mx.rodriguezammi.optica.receta;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Formatos de la graduación. Rangos razonables para atrapar errores de dedo
 * (por ejemplo, escribir 225 en lugar de -2.25).
 */
public final class RecetaDto {

    private RecetaDto() {
    }

    public record Guardar(
            @NotNull(message = "Indica la fecha del examen") @PastOrPresent LocalDate fecha,

            @DecimalMin("-30.00") @DecimalMax("30.00") BigDecimal odEsfera,
            @DecimalMin("-10.00") @DecimalMax("10.00") BigDecimal odCilindro,
            @Min(0) @Max(180) Integer odEje,
            @DecimalMin("0.00") @DecimalMax("4.00") BigDecimal odAdicion,

            @DecimalMin("-30.00") @DecimalMax("30.00") BigDecimal oiEsfera,
            @DecimalMin("-10.00") @DecimalMax("10.00") BigDecimal oiCilindro,
            @Min(0) @Max(180) Integer oiEje,
            @DecimalMin("0.00") @DecimalMax("4.00") BigDecimal oiAdicion,

            @DecimalMin("40.0") @DecimalMax("80.0") BigDecimal dip,

            @Size(max = 500) String observaciones
    ) {
    }

    public record Respuesta(
            Long id, Long clienteId, LocalDate fecha,
            BigDecimal odEsfera, BigDecimal odCilindro, Integer odEje, BigDecimal odAdicion,
            BigDecimal oiEsfera, BigDecimal oiCilindro, Integer oiEje, BigDecimal oiAdicion,
            BigDecimal dip, String observaciones
    ) {
        public static Respuesta desde(Receta r) {
            return new Respuesta(r.getId(), r.getCliente().getId(), r.getFecha(),
                    r.getOdEsfera(), r.getOdCilindro(), r.getOdEje(), r.getOdAdicion(),
                    r.getOiEsfera(), r.getOiCilindro(), r.getOiEje(), r.getOiAdicion(),
                    r.getDip(), r.getObservaciones());
        }
    }
}
