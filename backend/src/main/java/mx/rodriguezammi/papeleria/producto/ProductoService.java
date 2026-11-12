package mx.rodriguezammi.papeleria.producto;

import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
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

    private final ProductoRepository repositorio;

    /**
     * INYECCIÓN DE DEPENDENCIAS por constructor:
     * nosotros no hacemos "new ProductoRepository()"; Spring ve que lo pedimos aquí
     * y nos lo entrega ya listo.
     */
    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
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
        return ProductoDto.Respuesta.desde(repositorio.save(nuevo));
    }

    @Transactional
    public ProductoDto.Respuesta actualizar(Long id, ProductoDto.Guardar datos) {
        Producto producto = buscarEntidad(id);
        String codigo = limpiarCodigo(datos.codigoBarras());
        validarCodigoUnico(codigo, id);

        producto.setNombre(datos.nombre().trim());
        producto.setCodigoBarras(codigo);
        producto.setPrecio(datos.precio());
        producto.setExistencia(datos.existencia());
        producto.setStockMinimo(datos.stockMinimo());
        // No hace falta llamar a save(): dentro de @Transactional, JPA detecta los cambios y los guarda solo.
        return ProductoDto.Respuesta.desde(producto);
    }

    @Transactional
    public ProductoDto.Respuesta resurtir(Long id, int cantidad) {
        Producto producto = buscarEntidad(id);
        producto.setExistencia(producto.getExistencia() + cantidad);
        return ProductoDto.Respuesta.desde(producto);
    }

    /** "Borrado lógico": se oculta, pero sigue existiendo para el historial de ventas. */
    @Transactional
    public void desactivar(Long id) {
        buscarEntidad(id).setActivo(false);
    }

    /**
     * Uso interno y para otros Services (por ejemplo, VentaService).
     * Devuelve la ENTIDAD, por eso no se expone en el Controller.
     */
    public Producto buscarEntidad(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + id));
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
