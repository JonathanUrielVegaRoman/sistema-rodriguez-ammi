package mx.rodriguezammi.optica.tratamiento;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Tratamiento que se le aplica a la mica: antirreflejante, filtro de luz azul, fotocromático...
 *
 * "aplicaEnPromocion" resuelve la regla del cliente:
 *   "En la compra de un lente se le regala otro, pero SIN tratamientos caros".
 * El dueño decide (en Administración) cuáles tratamientos sí entran en el lente de regalo.
 */
@Entity
@Table(name = "tratamientos")
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "aplica_en_promocion", nullable = false)
    private boolean aplicaEnPromocion;

    @Column(nullable = false)
    private boolean activo = true;

    protected Tratamiento() {
    }

    public Tratamiento(String nombre, BigDecimal precio, boolean aplicaEnPromocion) {
        this.nombre = nombre;
        this.precio = precio;
        this.aplicaEnPromocion = aplicaEnPromocion;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public boolean isAplicaEnPromocion() { return aplicaEnPromocion; }
    public void setAplicaEnPromocion(boolean aplicaEnPromocion) { this.aplicaEnPromocion = aplicaEnPromocion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
