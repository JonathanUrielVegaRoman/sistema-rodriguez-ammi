package mx.rodriguezammi.admin;

import jakarta.validation.Valid;
import mx.rodriguezammi.papeleria.venta.VentaDto;
import mx.rodriguezammi.papeleria.venta.VentaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * TICKETS DE LA PAPELERÍA para el administrador.
 *
 *   GET  /api/admin/ventas?dia=2026-11-12          tickets del día (incluye cancelados)
 *   POST /api/admin/ventas/{folio}/cancelar         { "motivo": "Se cobró dos veces" }
 *
 * ¿Por qué solo el administrador? Porque cancelar una venta "desaparece" dinero de los reportes:
 * si cualquiera pudiera hacerlo, el corte dejaría de ser confiable.
 * El personal sí puede REIMPRIMIR un ticket por folio (GET /api/papeleria/ventas/{folio}).
 */
@RestController
@RequestMapping("/api/admin/ventas")
public class AdminVentasController {

    private final VentaService ventas;

    public AdminVentasController(VentaService ventas) {
        this.ventas = ventas;
    }

    @GetMapping
    public List<VentaDto.Ticket> delDia(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {
        return ventas.delDia(dia != null ? dia : LocalDate.now());
    }

    @PostMapping("/{folio}/cancelar")
    public VentaDto.Ticket cancelar(@PathVariable Long folio, @Valid @RequestBody VentaDto.Cancelacion datos) {
        return ventas.cancelar(folio, datos.motivo());
    }
}
