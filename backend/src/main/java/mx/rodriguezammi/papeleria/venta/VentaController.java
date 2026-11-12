package mx.rodriguezammi.papeleria.venta;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 *   POST  /api/papeleria/ventas              cobra y devuelve el ticket
 *   GET   /api/papeleria/ventas/{folio}      reimprimir un ticket
 *
 * El CORTE (cuánto se vendió en el día, semana o mes) ya no está aquí: se movió a
 * /api/admin/reportes porque solo el dueño debe verlo (ver admin/ReporteController).
 */
@RestController
@RequestMapping("/api/papeleria/ventas")
public class VentaController {

    private final VentaService servicio;

    public VentaController(VentaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VentaDto.Ticket registrar(@Valid @RequestBody VentaDto.Nueva datos) {
        return servicio.registrar(datos);
    }

    @GetMapping("/{folio}")
    public VentaDto.Ticket ticket(@PathVariable Long folio) {
        return servicio.obtenerTicket(folio);
    }
}
