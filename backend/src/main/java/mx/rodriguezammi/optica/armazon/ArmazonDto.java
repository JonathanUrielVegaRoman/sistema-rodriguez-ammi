package mx.rodriguezammi.optica.armazon;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public final class ArmazonDto {

    private ArmazonDto() {
    }

    public record Guardar(
            @NotBlank(message = "El código es obligatorio") @Size(max = 40) String codigo,
            @Size(max = 60) String marca,
            @Size(max = 80) String modelo,
            @Size(max = 40) String color,
            @Min(value = 30, message = "Ancho de lente fuera de rango") @Max(70) Integer anchoLente,
            @Min(value = 10, message = "Puente fuera de rango") @Max(30) Integer puente,
            @Min(value = 110, message = "Varilla fuera de rango") @Max(160) Integer varilla,
            @NotNull(message = "El precio es obligatorio") @PositiveOrZero BigDecimal precio,
            @PositiveOrZero int existencia,
            @Size(max = 80) String ubicacion
    ) {
    }

    /**
     * fotoUrl incluye "?v=..." con el nombre del archivo: si cambia la foto, cambia la URL
     * y el celular descarga la nueva (en vez de mostrar la vieja guardada en caché).
     */
    public record Respuesta(
            Long id, String codigo, String marca, String modelo, String color,
            Integer anchoLente, Integer puente, Integer varilla, String medidas,
            BigDecimal precio, int existencia, String ubicacion, String fotoUrl
    ) {
        public static Respuesta desde(Armazon a) {
            String foto = a.getFotoArchivo() == null ? null
                    : "/api/publico/armazones/" + a.getId() + "/foto?v=" + a.getFotoArchivo().substring(0, 8);
            return new Respuesta(a.getId(), a.getCodigo(), a.getMarca(), a.getModelo(), a.getColor(),
                    a.getAnchoLente(), a.getPuente(), a.getVarilla(), a.getMedidas(),
                    a.getPrecio(), a.getExistencia(), a.getUbicacion(), foto);
        }
    }
}
