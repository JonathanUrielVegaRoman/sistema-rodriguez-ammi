package mx.rodriguezammi.papeleria.producto;

import mx.rodriguezammi.comun.Eliminado;
import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.papeleria.inventario.InventarioService;
import mx.rodriguezammi.papeleria.inventario.MovimientoInventario;
import mx.rodriguezammi.papeleria.venta.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ============================================================================
 *  SERVICE = el COCINERO. Aquí viven las REGLAS DEL NEGOCIO.
 * ============================================================================
 *
 * El Controller (mesero) le pasa la comanda; el Service decide qué hacer:
 *   - ¿Ya existe otro producto con ese código de barras? → error.
 *   - ¿El producto que quieren editar existe? → si no, error 404.
 * Y le pide al Repository (almacenista) lo que necesite de la bodega.
 *
 * Por qué separar Controller y Service:
 *   Si mañana hacemos una app de escritorio o una tarea automática, reutilizamos el
 *   mismo Service. Las reglas viven en UN solo lugar.
 *
 * @Service      → Spring crea UN objeto de esta clase y se lo pasa a quien lo necesite.
 * @Transactional → "todo o nada": si algo falla a la mitad, se deshacen los cambios en la BD.
 *                  readOnly = true indica que solo lee (un poco más rápido).
 */
@Service
@Transactional(readOnly = true)
public class ProductoService {

    /** Los códigos que empiezan así son productos que el propio sistema necesita (ver DatosIniciales). */
    private static final String PREFIJO_DEL_SISTEMA = "SRV-";

    private final ProductoRepository repositorio;
    private final InventarioService inventario;
    private final VentaRepository ventas;

    /**
     * INYECCIÓN DE DEPENDENCIAS por constructor:
     * nosotros no hacemos "new ProductoRepository()"; Spring ve que lo pedimos aquí
     * y nos lo entrega ya listo.
     */
    public ProductoService(ProductoRepository repositorio, InventarioService inventario, VentaRepository ventas) {
        this.repositorio = repositorio;
        this.inventario = inventario;
        this.ventas = ventas;
    }

    public List<ProductoDto.Respuesta> listar(String busqueda) {
        List<Producto> productos = (busqueda == null || busqueda.isBlank())
                ? repositorio.findByActivoTrueOrderByNombreAsc()
                : repositorio.findByNombreContainingIgnoreCaseAndActivoTrue(busqueda.trim());

        // .stream().map(...).toList() = "a cada producto, conviértelo en DTO, y júntalos en una lista"
        return productos.stream().map(ProductoDto.Respuesta::desde).toList();
    }

    public ProductoDto.Respuesta obtener(Long id) {
        return ProductoDto.Respuesta.desde(buscarEntidad(id));
    }

    public List<ProductoDto.Respuesta> porResurtir() {
        return repositorio.buscarPorResurtir().stream().map(ProductoDto.Respuesta::desde).toList();
    }

    @Transactional
    public ProductoDto.Respuesta crear(ProductoDto.Guardar datos) {
        String codigo = limpiarCodigo(datos.codigoBarras());
        validarCodigoUnico(codigo, null);

        Producto nuevo = new Producto(datos.nombre().trim(), codigo, datos.precio(),
                datos.existencia(), datos.stockMinimo());
        nuevo.setIcono(limpiarCodigo(datos.icono()));
        nuevo.setServicio(datos.servicio());
        Producto guardado = repositorio.save(nuevo);
        inventario.registrar(guardado, MovimientoInventario.Tipo.ALTA, guardado.getExistencia(), "Alta del producto");
        return ProductoDto.Respuesta.desde(guardado);
    }

    @Transactional
    public ProductoDto.Respuesta actualizar(Long id, ProductoDto.Guardar datos) {
        Producto producto = bloquearEntidad(id);
        int existenciaAnterior = producto.getExistencia();
        String codigo = limpiarCodigo(datos.codigoBarras());
        validarCodigoUnico(codigo, id);

        producto.setNombre(datos.nombre().trim());
        producto.setCodigoBarras(codigo);
        producto.setPrecio(datos.precio());
        producto.setExistencia(datos.existencia());
        producto.setStockMinimo(datos.stockMinimo());
        producto.setIcono(limpiarCodigo(datos.icono()));
        producto.setServicio(datos.servicio());
        // Si al editar se cambió la existencia a mano, queda anotado como AJUSTE.
        inventario.registrar(producto, MovimientoInventario.Tipo.AJUSTE, producto.getExistencia() - existenciaAnterior, "Corrección manual");
        // No hace falta llamar a save(): dentro de @Transactional, JPA detecta los cambios y los guarda solo.
        return ProductoDto.Respuesta.desde(producto);
    }

    @Transactional
    public ProductoDto.Respuesta resurtir(Long id, int cantidad) {
        Producto producto = bloquearEntidad(id);
        producto.setExistencia(producto.getExistencia() + cantidad);
        inventario.registrar(producto, MovimientoInventario.Tipo.RESURTIDO, cantidad, null);
        return ProductoDto.Respuesta.desde(producto);
    }

    /**
     * ELIMINAR un producto del inventario.
     *   - Si nunca se vendió → se borra de verdad (con su historial de movimientos).
     *   - Si ya tiene ventas → se ARCHIVA: desaparece del inventario y de la pantalla de venta,
     *     pero los tickets y reportes anteriores lo conservan. Se libera su código de barras
     *     para poder usarlo en otro producto.
     *   - Los productos "del sistema" (las impresiones que cobra el buzón) no se pueden eliminar.
     */
    @Transactional
    public Eliminado eliminar(Long id) {
        Producto producto = bloquearEntidad(id);
        if (producto.getCodigoBarras() != null && producto.getCodigoBarras().startsWith(PREFIJO_DEL_SISTEMA)) {
            throw new ReglaDeNegocioException("\"" + producto.getNombre() + "\" lo usa el buzón de impresión para cobrar, así que no se puede eliminar. Sí puedes cambiarle el precio.");
        }
        if (ventas.existeVentaDeProducto(id)) {
            producto.setActivo(false);
            producto.setCodigoBarras(null);
            return Eliminado.archivado(producto.getNombre());
        }
        inventario.borrarHistorial(id);
        repositorio.delete(producto);
        return Eliminado.borrado(producto.getNombre());
    }

    /**
     * Uso interno y para otros Services (por ejemplo, VentaService).
     * Devuelve la ENTIDAD, por eso no se expone en el Controller.
     */
    public Producto buscarEntidad(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + id));
    }

    /**
     * Igual que buscarEntidad, pero con CANDADO: nadie más puede cambiar este producto hasta que
     * termine la transacción actual. Úsalo antes de modificar la existencia.
     * (Ver la explicación en ProductoRepository.bloquearPorId.)
     */
    @Transactional
    public Producto bloquearEntidad(Long id) {
        return repositorio.bloquearPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + id));
    }

    public List<InventarioService.Movimiento> historial(Long id) {
        buscarEntidad(id); // 404 si no existe
        return inventario.historial(id);
    }

    // ---------- Ayudantes privados ----------

    private String limpiarCodigo(String codigo) {
        return (codigo == null || codigo.isBlank()) ? null : codigo.trim();
    }

    private void validarCodigoUnico(String codigo, Long idActual) {
        if (codigo == null) {
            return;
        }
        repositorio.findByCodigoBarras(codigo)
                .filter(otro -> !otro.getId().equals(idActual))
                .ifPresent(otro -> {
                    throw new ReglaDeNegocioException("El código " + codigo + " ya lo tiene: " + otro.getNombre());
                });
    }
}
