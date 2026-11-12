package mx.rodriguezammi.optica.orden;

import jakarta.persistence.*;
import mx.rodriguezammi.optica.armazon.Armazon;

import java.math.BigDecimal;

/**
 * Un PAR de lentes dentro de una orden. Una orden puede tener varios pares; así se maneja
 * la promoción "compra uno y llévate otro": el segundo par se marca como REGALO.
 *
 * Igual que en el ticket de la papelería, se guardan COPIAS (snapshot) de nombres y precios:
 * si mañana sube el precio de un tratamiento, la orden de hoy no cambia.
 *
 * Regla del regalo: las micas y los tratamientos del par de regalo cuestan $0, pero solo se
 * permiten tratamientos marcados como "aplica en promoción" (los no caros). El armazón del
 * par de regalo se cobra con el precio que capture el personal (puede ser $0).
 */
@Entity
@Table(name = "partidas_lente")
public class PartidaLente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id")
    private OrdenLentes orden;

    /** null = el cliente trajo su propio armazón. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "armazon_id")
    private Armazon armazon;

    @Column(name = "descripcion_armazon", nullable = false, length = 160)
    private String descripcionArmazon;

    @Column(name = "precio_armazon", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioArmazon;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_mica", nullable = false, length = 20)
    private TipoMica tipoMica;

    /** CR-39, policarbonato, alto índice... (texto libre). */
    @Column(length = 60)
    private String material;

    @Column(name = "precio_micas", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMicas;

    /** Nombres de los tratamientos, separados por coma (copia). */
    @Column(length = 300)
    private String tratamientos;

    @Column(name = "precio_tratamientos", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTratamientos;

    @Column(name = "es_regalo", nullable = false)
    private boolean esRegalo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    protected PartidaLente() {
    }

    public PartidaLente(Armazon armazon, String descripcionArmazon, BigDecimal precioArmazon, TipoMica tipoMica,
                        String material, BigDecimal precioMicas, String tratamientos, BigDecimal precioTratamientos,
                        boolean esRegalo) {
        this.armazon = armazon;
        this.descripcionArmazon = descripcionArmazon;
        this.precioArmazon = precioArmazon;
        this.tipoMica = tipoMica;
        this.material = material;
        this.precioMicas = precioMicas;
        this.tratamientos = tratamientos;
        this.precioTratamientos = precioTratamientos;
        this.esRegalo = esRegalo;
        // En el par de regalo, micas y tratamientos no se cobran.
        this.subtotal = esRegalo ? precioArmazon : precioArmazon.add(precioMicas).add(precioTratamientos);
    }

    void setOrden(OrdenLentes orden) { this.orden = orden; }

    public Long getId() { return id; }
    public Armazon getArmazon() { return armazon; }
    public String getDescripcionArmazon() { return descripcionArmazon; }
    public BigDecimal getPrecioArmazon() { return precioArmazon; }
    public TipoMica getTipoMica() { return tipoMica; }
    public String getMaterial() { return material; }
    public BigDecimal getPrecioMicas() { return precioMicas; }
    public String getTratamientos() { return tratamientos; }
    public BigDecimal getPrecioTratamientos() { return precioTratamientos; }
    public boolean isEsRegalo() { return esRegalo; }
    public BigDecimal getSubtotal() { return subtotal; }
}
