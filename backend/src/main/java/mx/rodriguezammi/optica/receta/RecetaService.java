package mx.rodriguezammi.optica.receta;

import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.optica.cliente.Cliente;
import mx.rodriguezammi.optica.cliente.ClienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecetaService {

    private final RecetaRepository repositorio;
    private final ClienteService clientes;

    public RecetaService(RecetaRepository repositorio, ClienteService clientes) {
        this.repositorio = repositorio;
        this.clientes = clientes;
    }

    public List<RecetaDto.Respuesta> historial(Long clienteId) {
        clientes.buscarEntidad(clienteId); // 404 si el cliente no existe
        return repositorio.findByClienteIdOrderByFechaDesc(clienteId).stream().map(RecetaDto.Respuesta::desde).toList();
    }

    @Transactional
    public RecetaDto.Respuesta crear(Long clienteId, RecetaDto.Guardar datos) {
        Cliente cliente = clientes.buscarEntidad(clienteId);
        Receta receta = new Receta(cliente, datos.fecha());
        copiar(datos, receta);
        return RecetaDto.Respuesta.desde(repositorio.save(receta));
    }

    @Transactional
    public RecetaDto.Respuesta actualizar(Long clienteId, Long recetaId, RecetaDto.Guardar datos) {
        Receta receta = buscarEntidad(recetaId);
        if (!receta.getCliente().getId().equals(clienteId)) {
            throw new ReglaDeNegocioException("La receta no pertenece a ese cliente");
        }
        receta.setFecha(datos.fecha());
        copiar(datos, receta);
        return RecetaDto.Respuesta.desde(receta);
    }

    public Receta buscarEntidad(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la receta con id " + id));
    }

    private void copiar(RecetaDto.Guardar d, Receta r) {
        r.setOdEsfera(d.odEsfera());
        r.setOdCilindro(d.odCilindro());
        r.setOdEje(d.odEje());
        r.setOdAdicion(d.odAdicion());
        r.setOiEsfera(d.oiEsfera());
        r.setOiCilindro(d.oiCilindro());
        r.setOiEje(d.oiEje());
        r.setOiAdicion(d.oiAdicion());
        r.setDip(d.dip());
        r.setObservaciones(d.observaciones());
    }
}
