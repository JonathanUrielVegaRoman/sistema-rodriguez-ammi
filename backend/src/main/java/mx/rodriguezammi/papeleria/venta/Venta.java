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

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

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
}
