package mx.rodriguezammi.papeleria.venta;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 *   POST  /api/papeleria/ventas              cobra y devuelve el ticket
 *   GET   /api/papeleria/ventas/{folio}      reimprimir un ticket
 *   GET   /api/papeleria/ventas/corte        corte de hoy
 *   GET   /api/papeleria/ventas/corte?dia=2026-11-11   corte de otro día
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

    @GetMapping("/corte")
    public VentaDto.CorteDelDia corte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {
        return servicio.corte(dia != null ? dia : LocalDate.now());
    }

    @GetMapping("/{folio}")
    public VentaDto.Ticket ticket(@PathVariable Long folio) {
        return servicio.obtenerTicket(folio);
    }
}
