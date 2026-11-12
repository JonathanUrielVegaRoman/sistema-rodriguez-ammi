package mx.rodriguezammi.optica.cliente;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cliente de la Óptica RODRÍGUEZ.
 *
 * Datos personales: nombre, celular y fecha de nacimiento. Por ley (LFPDPPP en México)
 * hay que avisarle al cliente para qué se usan (aviso de privacidad) y pedir su permiso
 * para mandarle promociones; por eso existe el campo "aceptaPromociones".
 *
 * ESTADO: solo Entity + Repository. El Service, el Controller y los DTO quedan como
 * EJERCICIO (ver optica/EJERCICIOS.md), siguiendo el ejemplo de papeleria/producto.
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    /** 10 dígitos. Se usa para mandar el mensaje de cumpleaños por WhatsApp. */
    @Column(length = 15)
    private String celular;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "acepta_promociones", nullable = false)
    private boolean aceptaPromociones;

    @Column(length = 500)
    private String notas;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    protected Cliente() {
    }

    public Cliente(String nombre, String celular, LocalDate fechaNacimiento, boolean aceptaPromociones) {
        this.nombre = nombre;
        this.celular = celular;
        this.fechaNacimiento = fechaNacimiento;
        this.aceptaPromociones = aceptaPromociones;
    }

    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public boolean isAceptaPromociones() { return aceptaPromociones; }
    public void setAceptaPromociones(boolean aceptaPromociones) { this.aceptaPromociones = aceptaPromociones; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}
