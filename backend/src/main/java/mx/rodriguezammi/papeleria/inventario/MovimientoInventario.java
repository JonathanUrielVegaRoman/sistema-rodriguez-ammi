package mx.rodriguezammi.papeleria.inventario;

import jakarta.persistence.*;
import mx.rodriguezammi.papeleria.producto.Producto;

import java.time.LocalDateTime;

/**
 * Un MOVIMIENTO de inventario: cada vez que la existencia de un producto cambia, se anota
 * aquí qué pasó, cuánto y cómo quedó. Es el "kárdex" de la papelería.
 *
 *   ┌──────────────┬─────────────┬──────────┬────────┬──────────────────┐
 *   │ fecha        │ tipo        │ cantidad │ quedó  │ nota             │
 *   ├──────────────┼─────────────┼──────────┼────────┼──────────────────┤
 *   │ 12/11 10:02  │ RESURTIDO   │   +24    │   27   │                  │
 *   │ 12/11 10:31  │ VENTA       │    -2    │   25   │ Ticket 41        │
 *   │ 12/11 11:15  │ CANCELACION │    +2    │   27   │ Ticket 41        │
 *   └──────────────┴─────────────┴──────────┴────────┴──────────────────┘
 *
 * Sirve para contestar "¿por qué el sistema dice que hay 3 si yo cuento 5?": se revisa el
 * historial en lugar de adivinar. Los movimientos nunca se editan ni se borran.
 */
@Entity
@Table(name = "movimientos_inventario", indexes = @Index(name = "idx_movimientos_producto", columnList = "producto_id, fecha"))
public class MovimientoInventario {

    public enum Tipo { ALTA, VENTA, CANCELACION, RESURTIDO, AJUSTE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    /** Con signo: +24 entraron, -2 salieron. */
    @Column(nullable = false)
    private int cantidad;

    @Column(name = "existencia_resultante", nullable = false)
    private int existenciaResultante;

    @Column(length = 120)
    private String nota;

    protected MovimientoInventario() {
    }

    public MovimientoInventario(Producto producto, Tipo tipo, int cantidad, String nota) {
        this.producto = producto;
        this.fecha = LocalDateTime.now();
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.existenciaResultante = producto.getExistencia();
        this.nota = nota;
    }

    public Long getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public Tipo getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public int getExistenciaResultante() { return existenciaResultante; }
    public String getNota() { return nota; }
}
