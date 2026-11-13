package mx.rodriguezammi.optica.orden;

import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.optica.armazon.Armazon;
import mx.rodriguezammi.optica.armazon.ArmazonService;
import mx.rodriguezammi.optica.cliente.Cliente;
import mx.rodriguezammi.optica.cliente.ClienteService;
import mx.rodriguezammi.optica.promocion.Cupon;
import mx.rodriguezammi.optica.promocion.CuponService;
import mx.rodriguezammi.optica.receta.Receta;
import mx.rodriguezammi.optica.receta.RecetaService;
import mx.rodriguezammi.optica.tratamiento.Tratamiento;
import mx.rodriguezammi.optica.tratamiento.TratamientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reglas del negocio para las ÓRDENES DE LENTES. Es el Service más completo del sistema;
 * léelo después de entender VentaService.
 *
 * Reglas:
 *   1. La receta (si se indica) debe ser del mismo cliente.
 *   2. Cada par de REGALO necesita un par pagado (promoción "compra uno y llévate otro"),
 *      y solo admite tratamientos marcados como "aplica en promoción".
 *   3. Si se elige un armazón del catálogo, debe haber existencia; se descuenta al crear la orden.
 *   4. El cupón debe ser válido (ver CuponService.validar) y se marca como usado.
 *   5. El anticipo no puede ser mayor al total.
 *   6. Solo se entrega (ENTREGADA) si el saldo es $0. Cancelar devuelve el armazón al inventario.
 */
@Service
@Transactional(readOnly = true)
public class OrdenService {

    private final OrdenRepository ordenes;
    private final ClienteService clientes;
    private final RecetaService recetas;
    private final ArmazonService armazones;
    private final TratamientoRepository tratamientos;
    private final CuponService cupones;

    public OrdenService(OrdenRepository ordenes, ClienteService clientes, RecetaService recetas,
                        ArmazonService armazones, TratamientoRepository tratamientos, CuponService cupones) {
        this.ordenes = ordenes;
        this.clientes = clientes;
        this.recetas = recetas;
        this.armazones = armazones;
        this.tratamientos = tratamientos;
        this.cupones = cupones;
    }

    /** Calcula cómo quedaría la orden SIN guardar nada (para mostrar el total antes de confirmar). */
    public OrdenDto.Detalle cotizar(OrdenDto.Nueva datos) {
        return OrdenDto.Detalle.desde(armar(datos));
    }

    @Transactional
    public OrdenDto.Detalle crear(OrdenDto.Nueva datos) {
        // ¿Reintento de la misma orden? (el celular la mandó dos veces por mala señal)
        String clave = vacioANull(datos.claveUnica());
        if (clave != null) {
            Optional<OrdenLentes> yaCreada = ordenes.findByClaveUnica(clave);
            if (yaCreada.isPresent()) {
                return OrdenDto.Detalle.desde(yaCreada.get());
            }
        }

        // CANDADOS antes de validar: armazones (en orden de id) y cupón. Así dos órdenes simultáneas
        // no pueden llevarse el mismo armazón ni usar el mismo cupón.
        datos.partidas().stream().map(OrdenDto.Partida::armazonId).filter(Objects::nonNull).distinct().sorted()
                .forEach(armazones::bloquearEntidad);
        if (vacioANull(datos.codigoCupon()) != null) {
            cupones.bloquear(datos.codigoCupon());
        }

        OrdenLentes orden = armar(datos);
        orden.setClaveUnica(clave);

        // Efectos: descontar armazones del inventario.
        for (PartidaLente p : orden.getPartidas()) {
            Armazon a = p.getArmazon();
            if (a != null) {
                if (a.getExistencia() <= 0) {
                    throw new ReglaDeNegocioException("No hay existencia del armazón " + a.getCodigo());
                }
                a.setExistencia(a.getExistencia() - 1);
            }
        }

        BigDecimal anticipo = datos.anticipo() == null ? BigDecimal.ZERO : datos.anticipo();
        if (anticipo.compareTo(orden.getTotal()) > 0) {
            throw new ReglaDeNegocioException("El anticipo no puede ser mayor al total ($" + orden.getTotal() + ")");
        }
        if (anticipo.signum() > 0) {
            if (datos.metodoPagoAnticipo() == null) {
                throw new ReglaDeNegocioException("Indica cómo pagó el anticipo");
            }
            orden.agregarPago(new PagoOrden(anticipo, datos.metodoPagoAnticipo(), vacioANull(datos.referenciaAnticipo()), null));
        }

        OrdenLentes guardada = ordenes.save(orden);
        if (guardada.getCupon() != null) {
            guardada.getCupon().setUsadoEnOrden(guardada.getId());
        }
        return OrdenDto.Detalle.desde(guardada);
    }

    public OrdenDto.Detalle obtener(Long folio) {
        return OrdenDto.Detalle.desde(buscarEntidad(folio));
    }

    /** Lista por estados. Sin estados = las que están "vivas" (en laboratorio o listas). */
    public List<OrdenDto.Resumen> listar(List<EstadoOrden> estados) {
        Collection<EstadoOrden> filtro = (estados == null || estados.isEmpty())
                ? EnumSet.of(EstadoOrden.EN_LABORATORIO, EstadoOrden.LISTA) : estados;
        return ordenes.findTop100ByEstadoInOrderByFechaDesc(filtro).stream().map(OrdenDto.Resumen::desde).toList();
    }

    public List<OrdenDto.Resumen> delCliente(Long clienteId) {
        return ordenes.findByClienteIdOrderByFechaDesc(clienteId).stream().map(OrdenDto.Resumen::desde).toList();
    }

    @Transactional
    public OrdenDto.Detalle abonar(Long folio, OrdenDto.Abono abono) {
        String clave = vacioANull(abono.claveUnica());
        if (clave != null) {
            Optional<PagoOrden> yaRegistrado = ordenes.pagoPorClave(clave);
            if (yaRegistrado.isPresent()) {
                return OrdenDto.Detalle.desde(yaRegistrado.get().getOrden());
            }
        }
        OrdenLentes orden = bloquearEntidad(folio);
        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new ReglaDeNegocioException("La orden está cancelada");
        }
        if (abono.monto().compareTo(orden.getSaldo()) > 0) {
            throw new ReglaDeNegocioException("El abono es mayor al saldo ($" + orden.getSaldo() + ")");
        }
        orden.agregarPago(new PagoOrden(abono.monto(), abono.metodoPago(), vacioANull(abono.referencia()), clave));
        return OrdenDto.Detalle.desde(orden);
    }

    @Transactional
    public OrdenDto.Detalle cambiarEstado(Long folio, EstadoOrden nuevo) {
        OrdenLentes orden = bloquearEntidad(folio);
        EstadoOrden actual = orden.getEstado();

        if (actual == EstadoOrden.ENTREGADA || actual == EstadoOrden.CANCELADA) {
            throw new ReglaDeNegocioException("La orden ya está " + actual.name().toLowerCase() + " y no se puede cambiar");
        }
        if (nuevo == EstadoOrden.ENTREGADA && orden.getSaldo().signum() > 0) {
            throw new ReglaDeNegocioException("Antes de entregar, liquida el saldo de $" + orden.getSaldo());
        }
        if (nuevo == EstadoOrden.CANCELADA) {
            // Los armazones regresan a la vitrina. Los pagos se quedan registrados (la devolución se hace aparte).
            // Candado a cada armazón (en orden de id) y luego se regresa una pieza por cada par.
            Map<Long, Armazon> bloqueados = new HashMap<>();
            orden.getPartidas().stream().map(PartidaLente::getArmazon).filter(Objects::nonNull)
                    .map(Armazon::getId).distinct().sorted()
                    .forEach(id -> bloqueados.put(id, armazones.bloquearEntidad(id)));
            orden.getPartidas().stream().map(PartidaLente::getArmazon).filter(Objects::nonNull)
                    .map(a -> bloqueados.get(a.getId()))
                    .forEach(a -> a.setExistencia(a.getExistencia() + 1));
        }
        orden.setEstado(nuevo);
        return OrdenDto.Detalle.desde(orden);
    }

    private OrdenLentes bloquearEntidad(Long folio) {
        return ordenes.bloquearPorId(folio)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la orden con folio " + folio));
    }

    public OrdenLentes buscarEntidad(Long folio) {
        return ordenes.findById(folio)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la orden con folio " + folio));
    }

    // ---------------- privados ----------------

    /** Construye la orden en memoria y valida reglas, sin efectos (no descuenta inventario ni usa cupones). */
    private OrdenLentes armar(OrdenDto.Nueva d) {
        Cliente cliente = clientes.buscarEntidad(d.clienteId());

        Receta receta = null;
        if (d.recetaId() != null) {
            receta = recetas.buscarEntidad(d.recetaId());
            if (!receta.getCliente().getId().equals(cliente.getId())) {
                throw new ReglaDeNegocioException("La receta elegida es de otro cliente");
            }
        }

        long regalos = d.partidas().stream().filter(OrdenDto.Partida::esRegalo).count();
        long pagados = d.partidas().size() - regalos;
        if (regalos > pagados) {
            throw new ReglaDeNegocioException("Por cada par de regalo debe haber un par pagado");
        }

        OrdenLentes orden = new OrdenLentes(cliente, receta, d.fechaEntregaEstimada(), vacioANull(d.notas()));
        for (OrdenDto.Partida p : d.partidas()) {
            orden.agregarPartida(armarPartida(p));
        }

        Cupon cupon = (d.codigoCupon() == null || d.codigoCupon().isBlank())
                ? null : cupones.validar(d.codigoCupon(), cliente.getId());
        BigDecimal manual = d.descuentoManual() == null ? BigDecimal.ZERO : d.descuentoManual();
        if (manual.signum() > 0 && (d.motivoDescuento() == null || d.motivoDescuento().isBlank())) {
            throw new ReglaDeNegocioException("Escribe el motivo del descuento especial");
        }
        orden.aplicarDescuentos(cupon, manual, vacioANull(d.motivoDescuento()));
        orden.recalcular();
        return orden;
    }

    private PartidaLente armarPartida(OrdenDto.Partida p) {
        Armazon armazon = p.armazonId() == null ? null : armazones.buscarEntidad(p.armazonId());
        String descripcion = armazon == null ? "Armazón propio del cliente"
                : armazon.getCodigo() + " · " + Objects.toString(armazon.getMarca(), "") + " " + Objects.toString(armazon.getModelo(), "");

        List<Tratamiento> elegidos = (p.tratamientoIds() == null || p.tratamientoIds().isEmpty())
                ? List.of() : tratamientos.findAllById(p.tratamientoIds());
        if (p.tratamientoIds() != null && elegidos.size() != new HashSet<>(p.tratamientoIds()).size()) {
            throw new ReglaDeNegocioException("Alguno de los tratamientos elegidos no existe");
        }
        if (p.esRegalo()) {
            elegidos.stream().filter(t -> !t.isAplicaEnPromocion()).findFirst().ifPresent(t -> {
                throw new ReglaDeNegocioException("El tratamiento \"" + t.getNombre() + "\" no entra en el lente de regalo");
            });
        }
        if (p.tipoMica() == TipoMica.SOLO_ARMAZON && (p.precioMicas().signum() > 0 || !elegidos.isEmpty())) {
            throw new ReglaDeNegocioException("Si es solo armazón, no lleva micas ni tratamientos");
        }

        String nombres = elegidos.stream().map(Tratamiento::getNombre).sorted().collect(Collectors.joining(", "));
        BigDecimal precioTratamientos = elegidos.stream().map(Tratamiento::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PartidaLente(armazon, descripcion.trim(), p.precioArmazon(), p.tipoMica(), vacioANull(p.material()),
                p.precioMicas(), nombres.isEmpty() ? null : nombres, precioTratamientos, p.esRegalo());
    }

    private String vacioANull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
