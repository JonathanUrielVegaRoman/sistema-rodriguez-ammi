package mx.rodriguezammi.optica.cliente;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *   GET  /api/optica/clientes?q=texto   buscar por nombre o celular (sin q: los últimos registrados)
 *   GET  /api/optica/clientes/{id}      uno
 *   POST /api/optica/clientes           crear
 *   PUT  /api/optica/clientes/{id}      editar
 *   DELETE /api/optica/clientes/{id}    borrar (o archivar si tiene órdenes)
 *   POST /api/optica/clientes/{id}/restaurar   sacar del archivo
 */
@RestController
@RequestMapping("/api/optica/clientes")
public class ClienteController {

    private final ClienteService servicio;

    public ClienteController(ClienteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ClienteDto.Respuesta> buscar(@RequestParam(name = "q", required = false) String texto) {
        return servicio.buscar(texto);
    }

    @GetMapping("/{id}")
    public ClienteDto.Respuesta obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDto.Respuesta crear(@Valid @RequestBody ClienteDto.Guardar datos) {
        return servicio.crear(datos);
    }

    @PutMapping("/{id}")
    public ClienteDto.Respuesta actualizar(@PathVariable Long id, @Valid @RequestBody ClienteDto.Guardar datos) {
        return servicio.actualizar(id, datos);
    }

    public record Eliminado(ClienteService.ResultadoEliminar resultado, String mensaje) {
    }

    /** Borra o archiva (si tiene órdenes). Ver ClienteService.eliminar. */
    @DeleteMapping("/{id}")
    public Eliminado eliminar(@PathVariable Long id) {
        ClienteService.ResultadoEliminar r = servicio.eliminar(id);
        return new Eliminado(r, r == ClienteService.ResultadoEliminar.ELIMINADO
                ? "Cliente eliminado"
                : "El cliente tiene órdenes, así que se archivó: ya no aparece en búsquedas, pero sus ventas se conservan");
    }

    @PostMapping("/{id}/restaurar")
    public ClienteDto.Respuesta restaurar(@PathVariable Long id) {
        return servicio.restaurar(id);
    }
}
