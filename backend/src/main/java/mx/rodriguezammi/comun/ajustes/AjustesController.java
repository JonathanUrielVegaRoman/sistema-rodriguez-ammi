package mx.rodriguezammi.comun.ajustes;

import org.springframework.web.bind.annotation.*;

/**
 *   GET /api/admin/ajustes    ajustes generales (solo el dueño)
 *   PUT /api/admin/ajustes    guardarlos
 */
@RestController
@RequestMapping("/api/admin/ajustes")
public class AjustesController {

    private final AjustesService servicio;

    public AjustesController(AjustesService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public AjustesService.Generales obtener() {
        return servicio.generales();
    }

    @PutMapping
    public AjustesService.Generales guardar(@RequestBody AjustesService.Generales datos) {
        return servicio.guardar(datos);
    }
}
