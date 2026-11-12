package mx.rodriguezammi.papeleria.producto;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * ============================================================================
 *  ENTITY (Entidad) = el "molde" de una TABLA de la base de datos.
 * ============================================================================
 *
 * Analogía: es la FICHA de un producto en el cuaderno del almacén.
 * Cada atributo de esta clase = una columna de la tabla "productos".
 * Cada objeto Producto que guardemos = un renglón de esa tabla.
 *
 *  Tabla: productos
 *  ┌────┬──────────────────────┬──────────────┬────────┬────────────┬──────────────┬────────┐
 *  │ id │ nombre               │ codigo_barras│ precio │ existencia │ stock_minimo │ activo │
 *  ├────┼──────────────────────┼──────────────┼────────┼────────────┼──────────────┼────────┤
 *  │ 1  │ Cuaderno profesional │ 750100...    │ 45.00  │ 20         │ 5            │ true   │
 *  └────┴──────────────────────┴──────────────┴────────┴────────────┴──────────────┴────────┘
 *
 * Las anotaciones (lo que empieza con @) son "etiquetas" que le dan instrucciones a JPA:
 *   @Entity  → "esta clase es una tabla"
 *   @Id      → "este campo es el identificador único (la llave primaria)"
 *   @Column  → detalles de la columna (obligatoria, tamaño, nombre...)
 *
 * REGLA DE ORO: la Entity NUNCA se manda directo al frontend. Para eso existen los DTO
 * (ver ProductoDto.java). Así podemos cambiar la tabla sin romper la pantalla, y no
 * exponemos campos internos por accidente.
 */
@Entity
@Table(name = "productos")
public class Producto {

    /** Identificador único. La base de datos lo genera sola (1, 2, 3...). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    /** Opcional: si el producto trae código de barras, se puede escanear al vender. */
    @Column(name = "codigo_barras", unique = true, length = 50)
    private String codigoBarras;

    /** Para dinero SIEMPRE se usa BigDecimal, nunca double (double redondea mal: 0.1 + 0.2 = 0.30000000000000004). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /** Cuántas piezas hay en el local ahora mismo. */
    @Column(nullable = false)
    private int existencia;

    /** Si la existencia baja de este número, el producto aparece en "Por resurtir". */
    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo;

    /**
     * En vez de BORRAR productos, se "desactivan". ¿Por qué? Porque las ventas viejas
     * apuntan a ellos; si los borramos, se rompe el historial.
     */
    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Ícono del producto: un EMOJI (📓 ✏️ 🖨️...). Se eligió emoji en vez de foto porque
     * no hay que tomar ni cargar imágenes: viene incluido en todos los celulares y pesa casi nada.
     */
    @Column(length = 16)
    private String icono;

    /**
     * SERVICIO = no es mercancía física (impresión, copia, engargolado). No lleva inventario:
     * se puede vender siempre y nunca aparece en "por resurtir".
     * columnDefinition con "default false" permite agregar la columna aunque la tabla ya tenga datos.
     */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean servicio;

    /** JPA necesita un constructor vacío para poder crear objetos al leer de la base de datos. */
    protected Producto() {
    }

    public Producto(String nombre, String codigoBarras, BigDecimal precio, int existencia, int stockMinimo) {
        this.nombre = nombre;
        this.codigoBarras = codigoBarras;
        this.precio = precio;
        this.existencia = existencia;
        this.stockMinimo = stockMinimo;
    }

    // ---------- Comportamiento (reglas que le pertenecen al producto) ----------

    /** ¿Ya hay que resurtir? */
    public boolean necesitaResurtir() {
        return !servicio && existencia <= stockMinimo;
    }

    /** ¿Alcanza para vender esta cantidad? Los servicios siempre alcanzan. */
    public boolean alcanzaPara(int cantidad) {
        return servicio || existencia >= cantidad;
    }

    /** Resta piezas al vender. El Service revisa antes que alcance. Los servicios no se descuentan. */
    public void descontarExistencia(int cantidad) {
        if (!servicio) {
            this.existencia -= cantidad;
        }
    }

    // ---------- Getters y setters (para leer y modificar los campos) ----------

    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getExistencia() { return existencia; }
    public void setExistencia(int existencia) { this.existencia = existencia; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }

    public boolean isServicio() { return servicio; }
    public void setServicio(boolean servicio) { this.servicio = servicio; }
}
