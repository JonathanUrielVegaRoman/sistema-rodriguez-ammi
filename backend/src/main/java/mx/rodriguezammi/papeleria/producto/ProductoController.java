package mx.rodriguezammi.papeleria.producto;

import jakarta.validation.Valid;
import mx.rodriguezammi.comun.Eliminado;
import mx.rodriguezammi.papeleria.inventario.InventarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================================
 *  CONTROLLER = el MESERO. Recibe pedidos (peticiones HTTP) y entrega respuestas.
 * ============================================================================
 *
 * NO toma decisiones del negocio: solo recibe, le pasa el pedido al Service y
 * devuelve lo que el Service le dé. Si ves un "if" de reglas de negocio aquí,
 * probablemente debería estar en el Service.
 *
 * Cada método = una "ruta" (endpoint) de la API REST:
 *
 *   Verbo   Ruta                                   Qué hace
 *   ─────   ────────────────────────────────────   ─────────────────────────────
 *   GET     /api/papeleria/productos               lista (opcional ?q=texto)
 *   GET     /api/papeleria/productos/{id}          uno solo
 *   GET     /api/papeleria/productos/por-resurtir  los que se están agotando
 *   GET     /api/papeleria/productos/{id}/movimientos   historial de entradas y salidas
 *   POST    /api/papeleria/productos               crea uno nuevo
 *   PUT     /api/papeleria/productos/{id}          edita uno existente
 *   POST    /api/papeleria/productos/{id}/resurtir suma existencia
 *   DELETE  /api/papeleria/productos/{id}          lo elimina (o lo archiva si ya tiene ventas)
 *
 * REST en una frase: "usar los verbos de HTTP (GET leer, POST crear, PUT editar,
 * DELETE borrar) sobre rutas que representan COSAS (sustantivos), no acciones."
 *
 * @RestController → las respuestas se convierten automáticamente a JSON.
 * @RequestMapping → prefijo común de todas las rutas de esta clase.
 * @Valid          → aplica las reglas de validación del DTO antes de entrar al método.
 */
@RestController
@RequestMapping("/api/papeleria/productos")
public class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ProductoDto.Respuesta> listar(@RequestParam(name = "q", required = false) String busqueda) {
        return servicio.listar(busqueda);
    }

    @GetMapping("/{id}")
    public ProductoDto.Respuesta obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    /** Historial de entradas y salidas de un producto (los últimos 50 movimientos). */
    @GetMapping("/{id}/movimientos")
    public List<InventarioService.Movimiento> movimientos(@PathVariable Long id) {
        return servicio.historial(id);
    }

    @GetMapping("/por-resurtir")
    public List<ProductoDto.Respuesta> porResurtir() {
        return servicio.porResurtir();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoDto.Respuesta crear(@Valid @RequestBody ProductoDto.Guardar datos) {
        return servicio.crear(datos);
    }

    @PutMapping("/{id}")
    public ProductoDto.Respuesta actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDto.Guardar datos) {
        return servicio.actualizar(id, datos);
    }

    @PostMapping("/{id}/resurtir")
    public ProductoDto.Respuesta resurtir(@PathVariable Long id, @Valid @RequestBody ProductoDto.Resurtir datos) {
        return servicio.resurtir(id, datos.cantidad());
    }

    /** Elimina (o archiva, si ya tiene ventas). Ver ProductoService.eliminar. */
    @DeleteMapping("/{id}")
    public Eliminado eliminar(@PathVariable Long id) {
        return servicio.eliminar(id);
    }
}
