package mx.rodriguezammi.optica.cliente;

import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import mx.rodriguezammi.optica.orden.OrdenRepository;
import mx.rodriguezammi.optica.promocion.CuponRepository;
import mx.rodriguezammi.optica.receta.RecetaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reglas de negocio de clientes: el CRUD completo (Crear, Leer, Actualizar, Borrar).
 * Compáralo con ProductoService: es el mismo patrón.
 */
@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository repositorio;
    private final OrdenRepository ordenes;
    private final RecetaRepository recetas;
    private final CuponRepository cupones;

    public ClienteService(ClienteRepository repositorio, OrdenRepository ordenes,
                          RecetaRepository recetas, CuponRepository cupones) {
        this.repositorio = repositorio;
        this.ordenes = ordenes;
        this.recetas = recetas;
        this.cupones = cupones;
    }

    public List<ClienteDto.Respuesta> buscar(String texto) {
        List<Cliente> clientes = (texto == null || texto.isBlank())
                ? repositorio.findTop30ByActivoTrueOrderByFechaRegistroDesc()
                : repositorio.buscarActivos(texto.trim(), PageRequest.of(0, 50));
        return clientes.stream().map(ClienteDto.Respuesta::desde).toList();
    }

    public ClienteDto.Respuesta obtener(Long id) {
        return ClienteDto.Respuesta.desde(buscarEntidad(id));
    }

    @Transactional
    public ClienteDto.Respuesta crear(ClienteDto.Guardar datos) {
        Cliente cliente = new Cliente(datos.nombre().trim(), vacioANull(datos.celular()),
                datos.fechaNacimiento(), datos.aceptaPromociones());
        cliente.setNotas(vacioANull(datos.notas()));
        return ClienteDto.Respuesta.desde(repositorio.save(cliente));
    }

    @Transactional
    public ClienteDto.Respuesta actualizar(Long id, ClienteDto.Guardar datos) {
        Cliente cliente = buscarEntidad(id);
        cliente.setNombre(datos.nombre().trim());
        cliente.setCelular(vacioANull(datos.celular()));
        cliente.setFechaNacimiento(datos.fechaNacimiento());
        cliente.setAceptaPromociones(datos.aceptaPromociones());
        cliente.setNotas(vacioANull(datos.notas()));
        return ClienteDto.Respuesta.desde(cliente);
    }

    public enum ResultadoEliminar { ELIMINADO, ARCHIVADO }

    /**
     * BORRAR un cliente, con una regla para no perder dinero del historial:
     *   - Si NO tiene órdenes → se borra de verdad (con sus graduaciones y cupones).
     *   - Si SÍ tiene órdenes → se ARCHIVA: desaparece de búsquedas y cumpleaños, pero sus
     *     órdenes y pagos siguen contando en los reportes.
     */
    @Transactional
    public ResultadoEliminar eliminar(Long id) {
        Cliente cliente = buscarEntidad(id);
        if (ordenes.existsByClienteId(id)) {
            cliente.setActivo(false);
            return ResultadoEliminar.ARCHIVADO;
        }
        recetas.deleteByClienteId(id);
        cupones.deleteByClienteId(id);
        repositorio.delete(cliente);
        return ResultadoEliminar.ELIMINADO;
    }

    public record ResultadoVarios(int eliminados, int archivados) {
    }

    /**
     * Elimina VARIOS clientes de una vez (selección múltiple en la lista).
     * Aplica la misma regla a cada uno: sin órdenes se borra, con órdenes se archiva.
     * Todo en una sola transacción.
     */
    @Transactional
    public ResultadoVarios eliminarVarios(List<Long> ids) {
        int eliminados = 0;
        int archivados = 0;
        for (Long id : ids) {
            if (eliminar(id) == ResultadoEliminar.ELIMINADO) eliminados++;
            else archivados++;
        }
        return new ResultadoVarios(eliminados, archivados);
    }

    /** Devuelve un cliente archivado a la lista. */
    @Transactional
    public ClienteDto.Respuesta restaurar(Long id) {
        Cliente cliente = buscarEntidad(id);
        cliente.setActivo(true);
        return ClienteDto.Respuesta.desde(cliente);
    }

    /** Para otros Services (recetas, órdenes, cupones). */
    public Cliente buscarEntidad(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente con id " + id));
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
