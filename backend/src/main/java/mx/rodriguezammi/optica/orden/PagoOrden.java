package mx.rodriguezammi.optica.orden;

import jakarta.persistence.*;
import mx.rodriguezammi.papeleria.venta.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Un pago (abono) a una orden: el anticipo al encargar los lentes y la liquidación al
 * entregarlos. Guardar cada pago con su fecha permite que el reporte diga cuánto dinero
 * ENTRÓ cada día, aunque la orden se haya hecho otro día.
 *
 * Reutilizamos MetodoPago de la papelería (EFECTIVO, TARJETA, TRANSFERENCIA).
 */
@Entity
@Table(name = "pagos_orden")
public class PagoOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id")
    private OrdenLentes orden;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    protected PagoOrden() {
    }

    public PagoOrden(BigDecimal monto, MetodoPago metodoPago) {
        this.fecha = LocalDateTime.now();
        this.monto = monto;
        this.metodoPago = metodoPago;
    }

    void setOrden(OrdenLentes orden) { this.orden = orden; }

    public Long getId() { return id; }
    public OrdenLentes getOrden() { return orden; }
    public LocalDateTime getFecha() { return fecha; }
    public BigDecimal getMonto() { return monto; }
    public MetodoPago getMetodoPago() { return metodoPago; }
}
