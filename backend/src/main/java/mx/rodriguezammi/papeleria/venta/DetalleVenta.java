package mx.rodriguezammi.papeleria.venta;

import jakarta.persistence.*;
import mx.rodriguezammi.papeleria.producto.Producto;

import java.math.BigDecimal;

/**
 * Un RENGLÓN del ticket: "2 x Cuaderno profesional  $45.00  = $90.00".
 *
 * Detalle importante: guardamos una COPIA del nombre y del precio en el momento
 * de la venta. ¿Por qué? Si mañana el cuaderno sube a $50, el ticket de hoy debe
 * seguir diciendo $45. A esto se le llama "foto" o "snapshot" del dato.
 *
 * @ManyToOne = "muchos renglones apuntan a UNA venta" y "muchos renglones pueden
 * ser del MISMO producto". LAZY = no cargues el objeto completo hasta que lo pida.
 */
@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false, length = 120)
    private String descripcion;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    protected DetalleVenta() {
    }

    public DetalleVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.descripcion = producto.getNombre();
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
        this.importe = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    void setVenta(Venta venta) { this.venta = venta; }

    public Long getId() { return id; }
    public Producto getProducto() { return producto; }
    public String getDescripcion() { return descripcion; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getImporte() { return importe; }
}
