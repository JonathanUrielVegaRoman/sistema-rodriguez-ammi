package mx.rodriguezammi.papeleria.impresion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import mx.rodriguezammi.papeleria.venta.MetodoPago;
import mx.rodriguezammi.papeleria.venta.VentaDto;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * PÚBLICO (clientes, sin PIN):
 *   POST /api/publico/impresiones                 subir archivos (multipart) → { turno, codigoSeguimiento }
 *   GET  /api/publico/impresiones/{codigo}        ¿ya está listo mi turno?
 *
 * PERSONAL (con PIN):
 *   GET  /api/papeleria/impresiones                          la cola
 *   GET  /api/papeleria/impresiones/{id}/archivos/{archivo}  descargar para imprimir
 *   GET  /api/papeleria/impresiones/impresoras                       impresoras instaladas en la PC
 *   GET  /api/papeleria/impresiones/{id}/archivos/{archivo}/info       páginas y si se puede imprimir directo
 *   GET  /api/papeleria/impresiones/{id}/archivos/{archivo}/vista-previa?pagina=1&color=true   PNG
 *   POST /api/papeleria/impresiones/{id}/archivos/{archivo}/imprimir   { impresora, copias, color, papel... }
 *   POST /api/papeleria/impresiones/{id}/impreso             { hojas }
 *   POST /api/papeleria/impresiones/{id}/cobrar              { metodoPago, pagaCon } → ticket
 *   POST /api/papeleria/impresiones/{id}/cancelar
 */
@RestController
public class ImpresionController {

    private final ImpresionService servicio;
    private final ImpresoraService impresora;

    public ImpresionController(ImpresionService servicio, ImpresoraService impresora) {
        this.servicio = servicio;
        this.impresora = impresora;
    }

    public record Impreso(@Min(1) int hojas) {
    }

    public record Cobro(@NotNull MetodoPago metodoPago, BigDecimal pagaCon) {
    }

    // ---------- Público ----------

    @PostMapping(value = "/api/publico/impresiones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImpresionService.Recibido subir(@RequestParam("archivos") List<MultipartFile> archivos,
                                           @RequestParam(defaultValue = "1") int copias,
                                           @RequestParam(defaultValue = "false") boolean color,
                                           @RequestParam(defaultValue = "false") boolean dobleCara,
                                           @RequestParam(required = false) String notas) {
        return servicio.recibir(archivos, copias, color, dobleCara, notas);
    }

    @GetMapping("/api/publico/impresiones/{codigo}")
    public ImpresionService.EstadoPublico consultar(@PathVariable String codigo) {
        return servicio.consultar(codigo);
    }

    // ---------- Personal ----------

    @GetMapping("/api/papeleria/impresiones")
    public List<ImpresionService.TrabajoDto> cola() {
        return servicio.cola();
    }

    @GetMapping("/api/papeleria/impresiones/{id}/archivos/{archivoId}")
    public ResponseEntity<Resource> descargar(@PathVariable Long id, @PathVariable Long archivoId) {
        ImpresionService.Descarga d = servicio.archivo(id, archivoId);
        Resource recurso = new FileSystemResource(d.ruta());
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(d.nombre()).orElse(MediaType.APPLICATION_OCTET_STREAM))
                // "inline" = el navegador lo abre (PDF, imagen) en lugar de solo descargarlo.
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(d.nombre(), StandardCharsets.UTF_8).build().toString())
                // Nunca guardar copias en caché: son archivos privados de clientes.
                .cacheControl(CacheControl.noStore())
                .body(recurso);
    }

    @GetMapping("/api/papeleria/impresiones/impresoras")
    public ImpresoraService.Impresoras impresoras() {
        return impresora.impresoras();
    }

    @GetMapping("/api/papeleria/impresiones/{id}/archivos/{archivoId}/info")
    public ImpresionService.InfoArchivo info(@PathVariable Long id, @PathVariable Long archivoId) {
        return servicio.info(id, archivoId);
    }

    @GetMapping(value = "/api/papeleria/impresiones/{id}/archivos/{archivoId}/vista-previa", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> vistaPrevia(@PathVariable Long id, @PathVariable Long archivoId,
                                              @RequestParam(defaultValue = "1") int pagina,
                                              @RequestParam(defaultValue = "true") boolean color) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(MediaType.IMAGE_PNG).body(servicio.vistaPrevia(id, archivoId, pagina, color));
    }

    @PostMapping("/api/papeleria/impresiones/{id}/archivos/{archivoId}/imprimir")
    public ImpresoraService.Resultado imprimir(@PathVariable Long id, @PathVariable Long archivoId,
                                               @RequestBody ImpresoraService.Opciones opciones) {
        return servicio.imprimirArchivo(id, archivoId, opciones);
    }

    @PostMapping("/api/papeleria/impresiones/{id}/impreso")
    public ImpresionService.TrabajoDto impreso(@PathVariable Long id, @Valid @RequestBody Impreso datos) {
        return servicio.marcarImpreso(id, datos.hojas());
    }

    @PostMapping("/api/papeleria/impresiones/{id}/cobrar")
    public VentaDto.Ticket cobrar(@PathVariable Long id, @Valid @RequestBody Cobro datos) {
        return servicio.cobrar(id, datos.metodoPago(), datos.pagaCon());
    }

    @PostMapping("/api/papeleria/impresiones/{id}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long id) {
        servicio.cancelar(id);
    }
}
