package mx.rodriguezammi.optica.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/** Formatos de entrada y salida de clientes (mismo patrón que ProductoDto). */
public final class ClienteDto {

    private ClienteDto() {
    }

    public record Guardar(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 120)
            String nombre,

            // Opcional, pero si viene deben ser exactamente 10 dígitos (sin espacios ni guiones).
            @Pattern(regexp = "^$|\\d{10}", message = "El celular debe tener 10 dígitos")
            String celular,

            @Past(message = "La fecha de nacimiento debe ser en el pasado")
            LocalDate fechaNacimiento,

            boolean aceptaPromociones,

            @Size(max = 500)
            String notas
    ) {
    }

    public record Respuesta(
            Long id,
            String nombre,
            String celular,
            LocalDate fechaNacimiento,
            Integer edad,
            boolean aceptaPromociones,
            String notas,
            LocalDateTime fechaRegistro,
            boolean activo
    ) {
        public static Respuesta desde(Cliente c) {
            Integer edad = c.getFechaNacimiento() == null ? null
                    : Period.between(c.getFechaNacimiento(), LocalDate.now()).getYears();
            return new Respuesta(c.getId(), c.getNombre(), c.getCelular(), c.getFechaNacimiento(), edad,
                    c.isAceptaPromociones(), c.getNotas(), c.getFechaRegistro(), c.isActivo());
        }
    }
}
