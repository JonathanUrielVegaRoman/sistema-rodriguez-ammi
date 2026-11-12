package mx.rodriguezammi.optica.receta;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Rutas ANIDADAS: la receta "vive dentro" del cliente, y la ruta lo refleja.
 *
 *   GET  /api/optica/clientes/{clienteId}/recetas               historial (la más nueva primero)
 *   POST /api/optica/clientes/{clienteId}/recetas               nueva graduación
 *   PUT  /api/optica/clientes/{clienteId}/recetas/{recetaId}    corregir una graduación
 */
@RestController
@RequestMapping("/api/optica/clientes/{clienteId}/recetas")
public class RecetaController {

    private final RecetaService servicio;

    public RecetaController(RecetaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<RecetaDto.Respuesta> historial(@PathVariable Long clienteId) {
        return servicio.historial(clienteId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecetaDto.Respuesta crear(@PathVariable Long clienteId, @Valid @RequestBody RecetaDto.Guardar datos) {
        return servicio.crear(clienteId, datos);
    }

    @PutMapping("/{recetaId}")
    public RecetaDto.Respuesta actualizar(@PathVariable Long clienteId, @PathVariable Long recetaId,
                                          @Valid @RequestBody RecetaDto.Guardar datos) {
        return servicio.actualizar(clienteId, recetaId, datos);
    }
}
