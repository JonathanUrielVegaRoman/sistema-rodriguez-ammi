package mx.rodriguezammi.optica.tratamiento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Catálogo de tratamientos.
 *   GET  /api/optica/tratamientos          los activos (para armar una orden)
 *   GET  /api/admin/tratamientos           todos (para el dueño)
 *   POST /api/admin/tratamientos           crear
 *   PUT  /api/admin/tratamientos/{id}      editar precio, nombre, si entra en promoción, activo
 *
 * Nota para aprender: este módulo es tan pequeño que el Controller habla directo con el
 * Repository (sin Service). Es válido cuando NO hay reglas de negocio que proteger; en cuanto
 * aparezcan reglas, se mueven a un TratamientoService.
 */
@RestController
public class TratamientoController {

    private final TratamientoRepository repositorio;

    public TratamientoController(TratamientoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public record Guardar(
            @NotBlank @Size(max = 60) String nombre,
            @NotNull @PositiveOrZero BigDecimal precio,
            boolean aplicaEnPromocion,
            boolean activo
    ) {
    }

    public record Respuesta(Long id, String nombre, BigDecimal precio, boolean aplicaEnPromocion, boolean activo) {
        static Respuesta desde(Tratamiento t) {
            return new Respuesta(t.getId(), t.getNombre(), t.getPrecio(), t.isAplicaEnPromocion(), t.isActivo());
        }
    }

    @GetMapping("/api/optica/tratamientos")
    public List<Respuesta> activos() {
        return repositorio.findByActivoTrueOrderByNombreAsc().stream().map(Respuesta::desde).toList();
    }

    @GetMapping("/api/admin/tratamientos")
    public List<Respuesta> todos() {
        return repositorio.findAllByOrderByNombreAsc().stream().map(Respuesta::desde).toList();
    }

    @PostMapping("/api/admin/tratamientos")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Respuesta crear(@Valid @RequestBody Guardar d) {
        if (repositorio.existsByNombreIgnoreCase(d.nombre().trim())) {
            throw new ReglaDeNegocioException("Ya existe el tratamiento " + d.nombre());
        }
        Tratamiento t = new Tratamiento(d.nombre().trim(), d.precio(), d.aplicaEnPromocion());
        t.setActivo(d.activo());
        return Respuesta.desde(repositorio.save(t));
    }

    @PutMapping("/api/admin/tratamientos/{id}")
    @Transactional
    public Respuesta actualizar(@PathVariable Long id, @Valid @RequestBody Guardar d) {
        Tratamiento t = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tratamiento " + id));
        t.setNombre(d.nombre().trim());
        t.setPrecio(d.precio());
        t.setAplicaEnPromocion(d.aplicaEnPromocion());
        t.setActivo(d.activo());
        return Respuesta.desde(t);
    }
}
