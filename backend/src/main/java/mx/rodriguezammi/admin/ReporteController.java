package mx.rodriguezammi.admin;

import mx.rodriguezammi.papeleria.venta.VentaDto;
import mx.rodriguezammi.papeleria.venta.VentaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Todo lo que empieza con /api/admin/ lo protege el AccesoInterceptor: solo entra
 * quien escribió la contraseña del administrador.
 *
 *   GET /api/admin/reportes?desde=2026-11-01&hasta=2026-11-30   resumen de ambos negocios
 *   GET /api/admin/reportes/corte?dia=2026-11-11                 tickets de la papelería de un día
 */
@RestController
public class ReporteController {

    private final ReporteService reportes;
    private final VentaService ventas;

    public ReporteController(ReporteService reportes, VentaService ventas) {
        this.reportes = reportes;
        this.ventas = ventas;
    }

    @GetMapping("/api/admin/reportes")
    public ReporteService.Reporte reporte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        return reportes.generar(desde != null ? desde : hoy, hasta != null ? hasta : hoy);
    }

    @GetMapping("/api/admin/reportes/corte")
    public VentaDto.CorteDelDia corte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {
        return ventas.corte(dia != null ? dia : LocalDate.now());
    }
}
