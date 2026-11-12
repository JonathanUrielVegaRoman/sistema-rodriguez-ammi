package mx.rodriguezammi.optica.receta;

import jakarta.persistence.*;
import mx.rodriguezammi.optica.cliente.Cliente;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * GRADUACIÓN de un cliente en una fecha. Un cliente tiene VARIAS recetas a lo largo
 * de los años; así se puede ver si su vista empeoró (historial).
 *
 * Vocabulario óptico (para que el programador entienda los campos):
 *   OD = ojo derecho, OI = ojo izquierdo
 *   Esfera (ESF)   = miopía (-) o hipermetropía (+), en dioptrías. Ej: -2.25
 *   Cilindro (CIL) = astigmatismo, en dioptrías. Ej: -0.75
 *   Eje            = orientación del astigmatismo, de 0 a 180 grados
 *   Adición (ADD)  = graduación extra para ver de cerca (bifocales/progresivos)
 *   DIP            = distancia interpupilar en milímetros (entre los centros de las pupilas)
 *
 * Se usa BigDecimal porque las graduaciones van de 0.25 en 0.25 y no queremos redondeos raros.
 *
 * ESTADO: solo Entity + Repository (ver optica/EJERCICIOS.md).
 */
@Entity
@Table(name = "recetas")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDate fecha;

    // ----- Ojo derecho -----
    @Column(name = "od_esfera", precision = 5, scale = 2)
    private BigDecimal odEsfera;
    @Column(name = "od_cilindro", precision = 5, scale = 2)
    private BigDecimal odCilindro;
    @Column(name = "od_eje")
    private Integer odEje;
    @Column(name = "od_adicion", precision = 5, scale = 2)
    private BigDecimal odAdicion;

    // ----- Ojo izquierdo -----
    @Column(name = "oi_esfera", precision = 5, scale = 2)
    private BigDecimal oiEsfera;
    @Column(name = "oi_cilindro", precision = 5, scale = 2)
    private BigDecimal oiCilindro;
    @Column(name = "oi_eje")
    private Integer oiEje;
    @Column(name = "oi_adicion", precision = 5, scale = 2)
    private BigDecimal oiAdicion;

    /** Distancia interpupilar en mm. */
    @Column(precision = 4, scale = 1)
    private BigDecimal dip;

    @Column(length = 500)
    private String observaciones;

    protected Receta() {
    }

    public Receta(Cliente cliente, LocalDate fecha) {
        this.cliente = cliente;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public BigDecimal getOdEsfera() { return odEsfera; }
    public void setOdEsfera(BigDecimal v) { this.odEsfera = v; }
    public BigDecimal getOdCilindro() { return odCilindro; }
    public void setOdCilindro(BigDecimal v) { this.odCilindro = v; }
    public Integer getOdEje() { return odEje; }
    public void setOdEje(Integer v) { this.odEje = v; }
    public BigDecimal getOdAdicion() { return odAdicion; }
    public void setOdAdicion(BigDecimal v) { this.odAdicion = v; }

    public BigDecimal getOiEsfera() { return oiEsfera; }
    public void setOiEsfera(BigDecimal v) { this.oiEsfera = v; }
    public BigDecimal getOiCilindro() { return oiCilindro; }
    public void setOiCilindro(BigDecimal v) { this.oiCilindro = v; }
    public Integer getOiEje() { return oiEje; }
    public void setOiEje(Integer v) { this.oiEje = v; }
    public BigDecimal getOiAdicion() { return oiAdicion; }
    public void setOiAdicion(BigDecimal v) { this.oiAdicion = v; }

    public BigDecimal getDip() { return dip; }
    public void setDip(BigDecimal dip) { this.dip = dip; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
