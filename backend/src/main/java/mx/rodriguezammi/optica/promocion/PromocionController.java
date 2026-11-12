package mx.rodriguezammi.optica.promocion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *   GET  /api/optica/cumpleaneros?dias=7          hoy + próximos días, con enlace de WhatsApp
 *   POST /api/optica/cupones/{id}/enviado         marcar que ya se mandó el mensaje
 *   GET  /api/admin/cupones                       últimos cupones (dueño)
 *   POST /api/admin/cupones                       crear un descuento especial (dueño)
 */
@RestController
public class PromocionController {

    private final CuponService servicio;

    public PromocionController(CuponService servicio) {
        this.servicio = servicio;
    }

    public record NuevoDescuento(
            Long clienteId,
            @Min(1) @Max(100) int porcentaje,
            @Min(1) @Max(365) int diasVigencia,
            @Size(max = 120) String nota
    ) {
    }

    @GetMapping("/api/optica/cumpleaneros")
    public List<CuponService.Cumpleanero> cumpleaneros(@RequestParam(defaultValue = "7") int dias) {
        return servicio.cumpleaneros(Math.min(Math.max(dias, 0), 31));
    }

    @PostMapping("/api/optica/cupones/{id}/enviado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enviado(@PathVariable Long id) {
        servicio.marcarEnviado(id);
    }

    @GetMapping("/api/admin/cupones")
    public List<CuponService.CuponResumen> recientes() {
        return servicio.recientes();
    }

    @PostMapping("/api/admin/cupones")
    @ResponseStatus(HttpStatus.CREATED)
    public CuponService.CuponResumen crear(@Valid @RequestBody NuevoDescuento d) {
        return servicio.crearDescuento(d.clienteId(), d.porcentaje(), d.diasVigencia(), d.nota());
    }
}
