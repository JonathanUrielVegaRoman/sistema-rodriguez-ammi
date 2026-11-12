package mx.rodriguezammi.papeleria.producto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * ============================================================================
 *  DTO (Data Transfer Object) = los "FORMATOS" de lo que entra y sale por la API.
 * ============================================================================
 *
 * Analogía de restaurante:
 *   - La Entity es la receta completa de la cocina (con secretos internos).
 *   - El DTO de ENTRADA es la COMANDA que llena el mesero: solo lo que el cliente pidió.
 *   - El DTO de SALIDA es el PLATILLO servido: solo lo que el cliente necesita ver.
 *
 * Agrupamos los DTO de un módulo dentro de una sola clase para tenerlos juntos.
 * Se usan así: ProductoDto.Guardar, ProductoDto.Respuesta
 *
 * Las anotaciones @NotBlank, @Positive, etc. son REGLAS DE VALIDACIÓN. Si el frontend
 * manda datos que no las cumplen, Spring rechaza la petición antes de llegar al Service
 * (y el ManejadorGlobalErrores responde un 400 con el detalle).
 */
public final class ProductoDto {

    private ProductoDto() {
    }

    /** Lo que el frontend MANDA para crear o editar un producto. Nota: no trae "id" (lo pone la BD). */
    public record Guardar(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 120)
            String nombre,

            @Size(max = 50)
            String codigoBarras,

            @NotNull(message = "El precio es obligatorio")
            @Positive(message = "El precio debe ser mayor a cero")
            BigDecimal precio,

            @PositiveOrZero(message = "La existencia no puede ser negativa")
            int existencia,

            @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
            int stockMinimo,

            @Size(max = 16)
            String icono,

            boolean servicio
    ) {
    }

    /** Lo que el backend DEVUELVE. Incluye un dato calculado: necesitaResurtir. */
    public record Respuesta(
            Long id,
            String nombre,
            String codigoBarras,
            BigDecimal precio,
            int existencia,
            int stockMinimo,
            String icono,
            boolean servicio,
            boolean necesitaResurtir
    ) {
        /** Convierte una Entity en DTO de salida (a esto se le llama "mapear"). */
        public static Respuesta desde(Producto p) {
            return new Respuesta(
                    p.getId(),
                    p.getNombre(),
                    p.getCodigoBarras(),
                    p.getPrecio(),
                    p.getExistencia(),
                    p.getStockMinimo(),
                    p.getIcono(),
                    p.isServicio(),
                    p.necesitaResurtir()
            );
        }
    }

    /** Para sumar mercancía cuando llega el resurtido. */
    public record Resurtir(
            @Positive(message = "La cantidad debe ser mayor a cero")
            int cantidad
    ) {
    }
}
