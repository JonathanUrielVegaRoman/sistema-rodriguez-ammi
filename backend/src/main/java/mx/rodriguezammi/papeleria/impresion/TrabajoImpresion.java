package mx.rodriguezammi.papeleria.impresion;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Un "pedido" del BUZÓN DE IMPRESIÓN: uno o varios archivos que un cliente subió desde su celular.
 *
 * Problema que resuelve: antes los clientes mandaban sus archivos por Bluetooth (lento) y por
 * WhatsApp no conviene (expone el número personal y obliga a atender chats todo el día).
 *
 * Privacidad:
 *   - El cliente NO deja nombre ni teléfono: recibe un TURNO ("A-007") para identificarse.
 *   - Recibe además un código secreto para ver desde su celular si ya está listo.
 *   - Los archivos se borran al entregar, o solos después de unas horas (LimpiezaImpresiones).
 */
@Entity
@Table(name = "trabajos_impresion")
public class TrabajoImpresion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Lo que se dice en voz alta: "¿Turno A-007?". Se reinicia cada día. */
    @Column(nullable = false, length = 10)
    private String turno;

    /** Código secreto para que SOLO ese cliente consulte su estado. */
    @Column(name = "codigo_seguimiento", nullable = false, unique = true, length = 40)
    private String codigoSeguimiento;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private int copias;

    @Column(nullable = false)
    private boolean color;

    @Column(name = "doble_cara", nullable = false)
    private boolean dobleCara;

    @Column(length = 300)
    private String notas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoImpresion estado = EstadoImpresion.PENDIENTE;

    /** Hojas que imprimió el personal (para cobrar). */
    private Integer hojas;

    /** Folio de la venta de papelería con la que se cobró. */
    @Column(name = "venta_folio")
    private Long ventaFolio;

    @Column(name = "archivos_borrados", nullable = false)
    private boolean archivosBorrados;

    @OneToMany(mappedBy = "trabajo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ArchivoImpresion> archivos = new ArrayList<>();

    protected TrabajoImpresion() {
    }

    public TrabajoImpresion(String turno, String codigoSeguimiento, int copias, boolean color, boolean dobleCara, String notas) {
        this.turno = turno;
        this.codigoSeguimiento = codigoSeguimiento;
        this.fecha = LocalDateTime.now();
        this.copias = copias;
        this.color = color;
        this.dobleCara = dobleCara;
        this.notas = notas;
    }

    public void agregarArchivo(ArchivoImpresion archivo) {
        archivo.setTrabajo(this);
        archivos.add(archivo);
    }

    public Long getId() { return id; }
    public String getTurno() { return turno; }
    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public LocalDateTime getFecha() { return fecha; }
    public int getCopias() { return copias; }
    public boolean isColor() { return color; }
    public boolean isDobleCara() { return dobleCara; }
    public String getNotas() { return notas; }
    public EstadoImpresion getEstado() { return estado; }
    public void setEstado(EstadoImpresion estado) { this.estado = estado; }
    public Integer getHojas() { return hojas; }
    public void setHojas(Integer hojas) { this.hojas = hojas; }
    public Long getVentaFolio() { return ventaFolio; }
    public void setVentaFolio(Long ventaFolio) { this.ventaFolio = ventaFolio; }
    public boolean isArchivosBorrados() { return archivosBorrados; }
    public void setArchivosBorrados(boolean archivosBorrados) { this.archivosBorrados = archivosBorrados; }
    public List<ArchivoImpresion> getArchivos() { return archivos; }
}
