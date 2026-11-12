package mx.rodriguezammi.optica.promocion;

import jakarta.persistence.*;
import mx.rodriguezammi.optica.cliente.Cliente;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Código de descuento. Ejemplo: "CUMPLE-7K3P9" = 15 % de descuento, válido 30 días, un solo uso.
 *
 * Si tiene cliente, SOLO ese cliente lo puede usar (así no se comparte el regalo de cumpleaños).
 * Si no tiene cliente, lo puede usar cualquiera (por ejemplo, una promoción de temporada).
 */
@Entity
@Table(name = "cupones")
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCupon tipo;

    /** Porcentaje de descuento sobre el total de la orden (1 a 100). */
    @Column(nullable = false)
    private int porcentaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDateTime creado = LocalDateTime.now();

    @Column(name = "vigente_hasta", nullable = false)
    private LocalDate vigenteHasta;

    /** Se marca cuando el personal ya envió el WhatsApp de felicitación. */
    @Column(nullable = false)
    private boolean enviado;

    /** Folio de la orden donde se usó. null = todavía no se usa. */
    @Column(name = "usado_en_orden")
    private Long usadoEnOrden;

    @Column(length = 120)
    private String nota;

    protected Cupon() {
    }

    public Cupon(String codigo, TipoCupon tipo, int porcentaje, Cliente cliente, LocalDate vigenteHasta) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.porcentaje = porcentaje;
        this.cliente = cliente;
        this.vigenteHasta = vigenteHasta;
    }

    public boolean usado() {
        return usadoEnOrden != null;
    }

    public boolean vencido() {
        return LocalDate.now().isAfter(vigenteHasta);
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public TipoCupon getTipo() { return tipo; }
    public int getPorcentaje() { return porcentaje; }
    public Cliente getCliente() { return cliente; }
    public LocalDateTime getCreado() { return creado; }
    public LocalDate getVigenteHasta() { return vigenteHasta; }
    public boolean isEnviado() { return enviado; }
    public void setEnviado(boolean enviado) { this.enviado = enviado; }
    public Long getUsadoEnOrden() { return usadoEnOrden; }
    public void setUsadoEnOrden(Long usadoEnOrden) { this.usadoEnOrden = usadoEnOrden; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
}
