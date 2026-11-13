package mx.rodriguezammi.optica.armazon;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * ARMAZÓN (montura) en exhibición o bodega.
 *
 * Las 3 medidas vienen grabadas en la varilla. En el armazón aparecen como "52□18 140" (el
 * cuadrito es un símbolo del fabricante); en pantalla se muestran como "52-18-140":
 *   anchoLente = 52 mm  (ancho de cada mica)
 *   puente     = 18 mm  (espacio entre micas, sobre la nariz)
 *   varilla    = 140 mm (largo de la patita)
 * Dos armazones del mismo modelo pueden tener tallas distintas; por eso se guardan.
 *
 * La FOTO no se guarda dentro de la base de datos (la haría pesada y lenta);
 * se guarda como archivo en una carpeta y aquí solo anotamos su nombre (fotoArchivo).
 *
 * Se muestra en CUADRÍCULA con foto (ver frontend/src/vistas/optica/Armazones.jsx).
 */
@Entity
@Table(name = "armazones")
public class Armazon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** El código que ya traen físicamente los armazones. */
    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(length = 60)
    private String marca;

    @Column(length = 80)
    private String modelo;

    @Column(length = 40)
    private String color;

    @Column(name = "ancho_lente")
    private Integer anchoLente;

    private Integer puente;

    private Integer varilla;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private int existencia;

    @Column(name = "foto_archivo", length = 200)
    private String fotoArchivo;

    /** Dónde está físicamente: "Vitrina 2, charola de arriba". Junto con la foto, ayuda a encontrarlo rápido. */
    @Column(length = 80)
    private String ubicacion;

    @Column(nullable = false)
    private boolean activo = true;

    // ----- Datos para el modelo 3D (todos opcionales) -----

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FormaArmazon forma;

    /** Color para dibujarlo, en formato web: "#1a1a1a" (negro), "#8b5a2b" (carey)... */
    @Column(name = "color_hex", length = 7)
    private String colorHex;

    /** Alto del lente en mm (la medida "B"). No viene en la varilla; si falta, se estima según la forma. */
    @Column(name = "alto_lente")
    private Integer altoLente;

    protected Armazon() {
    }

    public Armazon(String codigo, String marca, String modelo, BigDecimal precio) {
        this.codigo = codigo;
        this.marca = marca;
        this.modelo = modelo;
        this.precio = precio;
    }

    /** Texto tipo "52-18-140" (lente - puente - varilla) para mostrar en pantalla. */
    public String getMedidas() {
        if (anchoLente == null || puente == null) {
            return null;
        }
        return anchoLente + "-" + puente + (varilla != null ? "-" + varilla : "");
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getAnchoLente() { return anchoLente; }
    public void setAnchoLente(Integer anchoLente) { this.anchoLente = anchoLente; }
    public Integer getPuente() { return puente; }
    public void setPuente(Integer puente) { this.puente = puente; }
    public Integer getVarilla() { return varilla; }
    public void setVarilla(Integer varilla) { this.varilla = varilla; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public int getExistencia() { return existencia; }
    public void setExistencia(int existencia) { this.existencia = existencia; }
    public String getFotoArchivo() { return fotoArchivo; }
    public void setFotoArchivo(String fotoArchivo) { this.fotoArchivo = fotoArchivo; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public FormaArmazon getForma() { return forma; }
    public void setForma(FormaArmazon forma) { this.forma = forma; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public Integer getAltoLente() { return altoLente; }
    public void setAltoLente(Integer altoLente) { this.altoLente = altoLente; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
