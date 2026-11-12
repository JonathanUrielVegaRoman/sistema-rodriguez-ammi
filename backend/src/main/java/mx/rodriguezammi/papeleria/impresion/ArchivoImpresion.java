package mx.rodriguezammi.papeleria.impresion;

import jakarta.persistence.*;

/** Un archivo subido por el cliente. El archivo real está en disco; aquí solo sus datos. */
@Entity
@Table(name = "archivos_impresion")
public class ArchivoImpresion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trabajo_id")
    private TrabajoImpresion trabajo;

    /** Nombre como lo tenía el cliente ("tarea final.pdf"), solo para mostrarlo. */
    @Column(name = "nombre_original", nullable = false, length = 200)
    private String nombreOriginal;

    /** Nombre aleatorio con el que se guardó en disco. null = ya se borró. */
    @Column(name = "nombre_guardado", length = 80)
    private String nombreGuardado;

    @Column(nullable = false)
    private long tamano;

    protected ArchivoImpresion() {
    }

    public ArchivoImpresion(String nombreOriginal, String nombreGuardado, long tamano) {
        this.nombreOriginal = nombreOriginal;
        this.nombreGuardado = nombreGuardado;
        this.tamano = tamano;
    }

    void setTrabajo(TrabajoImpresion trabajo) { this.trabajo = trabajo; }

    public Long getId() { return id; }
    public TrabajoImpresion getTrabajo() { return trabajo; }
    public String getNombreOriginal() { return nombreOriginal; }
    public String getNombreGuardado() { return nombreGuardado; }
    public void setNombreGuardado(String nombreGuardado) { this.nombreGuardado = nombreGuardado; }
    public long getTamano() { return tamano; }
}
