package mx.rodriguezammi.optica.orden;

import jakarta.persistence.*;
import mx.rodriguezammi.optica.cliente.Cliente;
import mx.rodriguezammi.optica.promocion.Cupon;
import mx.rodriguezammi.optica.receta.Receta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ORDEN DE LENTES (la "nota" de la óptica). Reúne todo lo que pidió el cliente:
 *
 *   OrdenLentes (folio 15, Ana López, receta del 10/11/2026)
 *     ├── PartidaLente 1: armazón RB-2140 + micas monofocales + antirreflejante   $2,350
 *     ├── PartidaLente 2: armazón propio + micas monofocales (REGALO 2x1)         $0
 *     ├── Cupón CUMPLE-7K3P9 (15 %)                                              −$352.50
 *     ├── PagoOrden: anticipo $1,000 (efectivo)
 *     └── Estado: EN_LABORATORIO     Saldo: $997.50
 *
 * Los totales se calculan en recalcular(): nunca se confía en totales que mande el frontend.
 */
@Entity
@Table(name = "ordenes_lentes")
public class OrdenLentes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /** Graduación usada. Puede ser null si solo compra armazón. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id")
    private Receta receta;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "fecha_entrega_estimada")
    private LocalDate fechaEntregaEstimada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrden estado = EstadoOrden.EN_LABORATORIO;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PartidaLente> partidas = new ArrayList<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha ASC")
    private List<PagoOrden> pagos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cupon_id")
    private Cupon cupon;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "descuento_cupon", nullable = false, precision = 10, scale = 2)
    private BigDecimal descuentoCupon = BigDecimal.ZERO;

    /** "Descuento especial" que decide el personal en el momento (en pesos). */
    @Column(name = "descuento_manual", nullable = false, precision = 10, scale = 2)
    private BigDecimal descuentoManual = BigDecimal.ZERO;

    @Column(name = "motivo_descuento", length = 120)
    private String motivoDescuento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 500)
    private String notas;

    /** Cuándo se marcó como LISTA. Sirve para avisar de lentes que llevan días sin recogerse. */
    @Column(name = "fecha_lista")
    private LocalDateTime fechaLista;

    /** Para no crear la orden dos veces si el celular reintenta (ver Venta.claveUnica). */
    @Column(name = "clave_unica", unique = true, length = 40)
    private String claveUnica;

    protected OrdenLentes() {
    }

    public OrdenLentes(Cliente cliente, Receta receta, LocalDate fechaEntregaEstimada, String notas) {
        this.cliente = cliente;
        this.receta = receta;
        this.fecha = LocalDateTime.now();
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.notas = notas;
    }

    // ---------------- Comportamiento ----------------

    public void agregarPartida(PartidaLente partida) {
        partida.setOrden(this);
        partidas.add(partida);
    }

    public void agregarPago(PagoOrden pago) {
        pago.setOrden(this);
        pagos.add(pago);
    }

    public void aplicarDescuentos(Cupon cupon, BigDecimal descuentoManual, String motivo) {
        this.cupon = cupon;
        this.descuentoManual = descuentoManual == null ? BigDecimal.ZERO : descuentoManual;
        this.motivoDescuento = motivo;
    }

    /** Suma partidas, aplica el porcentaje del cupón y el descuento manual. El total nunca baja de $0. */
    public void recalcular() {
        subtotal = partidas.stream().map(PartidaLente::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        descuentoCupon = cupon == null ? BigDecimal.ZERO
                : subtotal.multiply(BigDecimal.valueOf(cupon.getPorcentaje())).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal resultado = subtotal.subtract(descuentoCupon).subtract(descuentoManual);
        total = resultado.max(BigDecimal.ZERO);
    }

    public BigDecimal getPagado() {
        return pagos.stream().map(PagoOrden::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSaldo() {
        return total.subtract(getPagado());
    }

    // ---------------- Getters ----------------

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Receta getReceta() { return receta; }
    public LocalDateTime getFecha() { return fecha; }
    public LocalDate getFechaEntregaEstimada() { return fechaEntregaEstimada; }
    public EstadoOrden getEstado() { return estado; }
    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
        if (estado == EstadoOrden.LISTA) {
            this.fechaLista = LocalDateTime.now();
        }
    }
    public LocalDateTime getFechaLista() { return fechaLista; }
    public String getClaveUnica() { return claveUnica; }
    public void setClaveUnica(String claveUnica) { this.claveUnica = claveUnica; }
    public List<PartidaLente> getPartidas() { return partidas; }
    public List<PagoOrden> getPagos() { return pagos; }
    public Cupon getCupon() { return cupon; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDescuentoCupon() { return descuentoCupon; }
    public BigDecimal getDescuentoManual() { return descuentoManual; }
    public String getMotivoDescuento() { return motivoDescuento; }
    public BigDecimal getTotal() { return total; }
    public String getNotas() { return notas; }
}
