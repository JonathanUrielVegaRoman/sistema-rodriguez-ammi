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
}
