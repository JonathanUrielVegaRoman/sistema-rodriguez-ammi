package mx.rodriguezammi.papeleria.venta;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Una VENTA = un TICKET. Tiene varios renglones (DetalleVenta).
 *
 * RELACIÓN "uno a muchos":
 *
 *   ventas                         detalle_venta
 *   ┌────┬────────┬───────┐        ┌────┬──────────┬──────────┬──────────┐
 *   │ id │ fecha  │ total │        │ id │ venta_id │ producto │ cantidad │
 *   ├────┼────────┼───────┤        ├────┼──────────┼──────────┼──────────┤
 *   │ 7  │ 10:31  │ 95.00 │ ◄──┬── │ 1  │ 7        │ Cuaderno │ 2        │
 *   └────┴────────┴───────┘    └── │ 2  │ 7        │ Pluma    │ 1        │
 *                                  └────┴──────────┴──────────┴──────────┘
 *
 * cascade = ALL      → al guardar la venta, se guardan también sus renglones.
 * orphanRemoval      → si quitamos un renglón de la lista, se borra de la BD.
 * mappedBy = "venta" → la columna venta_id vive en la tabla de DetalleVenta.
 */
@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    /** Con cuánto pagó el cliente (solo en efectivo), para calcular el cambio. */
    @Column(name = "paga_con", precision = 10, scale = 2)
    private BigDecimal pagaCon;

    /**
     * Referencia del pago cuando NO es efectivo: la clave de rastreo de la transferencia (SPEI)
     * o el número de autorización del voucher de la terminal. Sirve para aclarar un pago
     * comparándolo con el estado de cuenta del banco.
     */
    @Column(name = "referencia_pago", length = 60)
    private String referenciaPago;

    /**
     * CLAVE ÚNICA que genera el celular para cada intento de cobro. Si el Wi-Fi falla y el
     * celular manda el mismo cobro dos veces, la segunda vez encontramos esta clave y devolvemos
     * el ticket que ya existía, en lugar de cobrar doble. (A esto se le llama "idempotencia".)
     */
    @Column(name = "clave_unica", unique = true, length = 40)
    private String claveUnica;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    /**
     * Venta CANCELADA (devolución o error al cobrar). No se borra: queda registrada con su
     * motivo, pero ya no cuenta en los reportes y la mercancía regresó al inventario.
     * Solo el administrador puede cancelar (ver admin/AdminVentasController).
     */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean cancelada;

    @Column(name = "motivo_cancelacion", length = 200)
    private String motivoCancelacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    protected Venta() {
    }

    public Venta(MetodoPago metodoPago, BigDecimal pagaCon) {
        this.fecha = LocalDateTime.now();
        this.metodoPago = metodoPago;
        this.pagaCon = pagaCon;
    }

    /** Agrega un renglón y recalcula el total. Así el total nunca queda desfasado. */
    public void agregarDetalle(DetalleVenta detalle) {
        detalle.setVenta(this);
        detalles.add(detalle);
        total = total.add(detalle.getImporte());
    }

    public void cancelar(String motivo) {
        this.cancelada = true;
        this.motivoCancelacion = motivo;
        this.fechaCancelacion = LocalDateTime.now();
    }

    public BigDecimal getCambio() {
        if (pagaCon == null) {
            return BigDecimal.ZERO;
        }
        return pagaCon.subtract(total);
    }

    public Long getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getPagaCon() { return pagaCon; }
    public List<DetalleVenta> getDetalles() { return detalles; }
    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }
    public String getClaveUnica() { return claveUnica; }
    public void setClaveUnica(String claveUnica) { this.claveUnica = claveUnica; }
    public boolean isCancelada() { return cancelada; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public LocalDateTime getFechaCancelacion() { return fechaCancelacion; }
}
