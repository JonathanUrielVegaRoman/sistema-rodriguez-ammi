package mx.rodriguezammi.admin;

import mx.rodriguezammi.papeleria.venta.VentaDto;
import mx.rodriguezammi.papeleria.venta.VentaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
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
 *   GET /api/admin/reportes/excel?desde=...&hasta=...             descarga .xlsx (sin fechas: todo)
 */
@RestController
public class ReporteController {

    private final ReporteService reportes;
    private final VentaService ventas;
    private final ExcelService excel;

    public ReporteController(ReporteService reportes, VentaService ventas, ExcelService excel) {
        this.reportes = reportes;
        this.ventas = ventas;
        this.excel = excel;
    }

    @GetMapping("/api/admin/reportes")
    public ReporteService.Reporte reporte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        return reportes.generar(desde != null ? desde : hoy, hasta != null ? hasta : hoy);
    }

    /**
     * Descarga el Excel. Sin fechas = TODO el historial desde la primera venta.
     *   GET /api/admin/reportes/excel?desde=2026-11-01&hasta=2026-11-30
     */
    @GetMapping("/api/admin/reportes/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate inicio = desde != null ? desde : LocalDate.of(2000, 1, 1);
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        String nombre = desde == null ? "ventas-historial-completo.xlsx" : "ventas-" + inicio + "-a-" + fin + ".xlsx";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .cacheControl(CacheControl.noStore())
                .body(excel.generar(inicio, fin));
    }

    /** Excel con las existencias y el valor del inventario de los dos negocios. */
    @GetMapping("/api/admin/reportes/inventario-excel")
    public ResponseEntity<byte[]> inventarioExcel() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("inventario-" + LocalDate.now() + ".xlsx").build().toString())
                .cacheControl(CacheControl.noStore())
                .body(excel.inventario());
    }

    @GetMapping("/api/admin/reportes/corte")
    public VentaDto.CorteDelDia corte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {
        return ventas.corte(dia != null ? dia : LocalDate.now());
    }
}
