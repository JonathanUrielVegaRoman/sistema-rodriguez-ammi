package mx.rodriguezammi.optica.orden;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *   GET  /api/optica/ordenes?estado=LISTA&estado=EN_LABORATORIO   lista (sin estado: las pendientes)
 *   GET  /api/optica/ordenes/{folio}                              detalle
 *   GET  /api/optica/clientes/{id}/ordenes                        historial de un cliente
 *   POST /api/optica/ordenes/cotizar                              calcula totales sin guardar
 *   POST /api/optica/ordenes                                      crear
 *   POST /api/optica/ordenes/{folio}/abonos                       registrar un pago
 *   PUT  /api/optica/ordenes/{folio}/estado                       LISTA, ENTREGADA, CANCELADA
 */
@RestController
public class OrdenController {

    private final OrdenService servicio;

    public OrdenController(OrdenService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/api/optica/ordenes")
    public List<OrdenDto.Resumen> listar(@RequestParam(name = "estado", required = false) List<EstadoOrden> estados) {
        return servicio.listar(estados);
    }

    @GetMapping("/api/optica/ordenes/{folio}")
    public OrdenDto.Detalle obtener(@PathVariable Long folio) {
        return servicio.obtener(folio);
    }

    @GetMapping("/api/optica/clientes/{clienteId}/ordenes")
    public List<OrdenDto.Resumen> delCliente(@PathVariable Long clienteId) {
        return servicio.delCliente(clienteId);
    }

    @PostMapping("/api/optica/ordenes/cotizar")
    public OrdenDto.Detalle cotizar(@Valid @RequestBody OrdenDto.Nueva datos) {
        return servicio.cotizar(datos);
    }

    @PostMapping("/api/optica/ordenes")
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenDto.Detalle crear(@Valid @RequestBody OrdenDto.Nueva datos) {
        return servicio.crear(datos);
    }

    @PostMapping("/api/optica/ordenes/{folio}/abonos")
    public OrdenDto.Detalle abonar(@PathVariable Long folio, @Valid @RequestBody OrdenDto.Abono abono) {
        return servicio.abonar(folio, abono);
    }

    @PutMapping("/api/optica/ordenes/{folio}/estado")
    public OrdenDto.Detalle cambiarEstado(@PathVariable Long folio, @Valid @RequestBody OrdenDto.CambioEstado cambio) {
        return servicio.cambiarEstado(folio, cambio.estado());
    }
}
