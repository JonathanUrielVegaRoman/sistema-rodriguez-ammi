package mx.rodriguezammi.optica.armazon;

import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 *   GET    /api/optica/armazones?q=&ancho=52&disponibles=true   catálogo (cuadrícula)
 *   GET    /api/optica/armazones/tallas                         tallas existentes para filtrar
 *   GET    /api/optica/armazones/{id}
 *   POST   /api/optica/armazones                                crear
 *   PUT    /api/optica/armazones/{id}                           editar
 *   POST   /api/optica/armazones/{id}/foto   (multipart)        subir/cambiar foto
 *   DELETE /api/optica/armazones/{id}                           desactivar
 *   GET    /api/publico/armazones/{id}/foto                     ver la foto
 *
 * ¿Por qué la foto es "pública"? Porque la etiqueta <img src="..."> del navegador no puede
 * mandar nuestro encabezado con el pase. Una foto de un armazón no es información sensible.
 */
@RestController
public class ArmazonController {

    private final ArmazonService servicio;

    public ArmazonController(ArmazonService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/api/optica/armazones")
    public List<ArmazonDto.Respuesta> buscar(@RequestParam(required = false) String q,
                                             @RequestParam(required = false) Integer ancho,
                                             @RequestParam(defaultValue = "false") boolean disponibles) {
        return servicio.buscar(q, ancho, disponibles);
    }

    @GetMapping("/api/optica/armazones/tallas")
    public List<Integer> tallas() {
        return servicio.tallas();
    }

    @GetMapping("/api/optica/armazones/{id}")
    public ArmazonDto.Respuesta obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping("/api/optica/armazones")
    @ResponseStatus(HttpStatus.CREATED)
    public ArmazonDto.Respuesta crear(@Valid @RequestBody ArmazonDto.Guardar datos) {
        return servicio.crear(datos);
    }

    @PutMapping("/api/optica/armazones/{id}")
    public ArmazonDto.Respuesta actualizar(@PathVariable Long id, @Valid @RequestBody ArmazonDto.Guardar datos) {
        return servicio.actualizar(id, datos);
    }

    /** "multipart/form-data" es el formato que usa el navegador para mandar archivos. */
    @PostMapping(value = "/api/optica/armazones/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ArmazonDto.Respuesta foto(@PathVariable Long id, @RequestParam("foto") MultipartFile foto) {
        return servicio.cambiarFoto(id, foto);
    }

    @DeleteMapping("/api/optica/armazones/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        servicio.desactivar(id);
    }

    @GetMapping("/api/publico/armazones/{id}/foto")
    public ResponseEntity<Resource> verFoto(@PathVariable Long id) {
        Path ruta = servicio.rutaFoto(id);
        Resource recurso = new FileSystemResource(ruta);
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(recurso).orElse(MediaType.IMAGE_JPEG))
                // La URL cambia cuando cambia la foto (?v=...), así que el celular puede guardarla mucho tiempo.
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic())
                .body(recurso);
    }
}
