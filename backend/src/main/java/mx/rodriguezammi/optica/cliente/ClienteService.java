package mx.rodriguezammi.optica.cliente;

import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Reglas de negocio de clientes. Compáralo con ProductoService: es el mismo patrón. */
@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository repositorio;

    public ClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<ClienteDto.Respuesta> buscar(String texto) {
        List<Cliente> clientes = (texto == null || texto.isBlank())
                ? repositorio.findTop30ByOrderByFechaRegistroDesc()
                : repositorio.findTop50ByNombreContainingIgnoreCaseOrCelularContainingOrderByNombreAsc(texto.trim(), texto.trim());
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

    /** Para otros Services (recetas, órdenes, cupones). */
    public Cliente buscarEntidad(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente con id " + id));
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
