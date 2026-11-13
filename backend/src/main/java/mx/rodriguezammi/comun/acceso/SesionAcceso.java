package mx.rodriguezammi.comun.acceso;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Un "pase" (token) entregado a un dispositivo después de escribir el PIN o la contraseña.
 *
 * Analogía: la pulsera de un evento. Te la ponen una vez en la entrada y después
 * solo la muestras. El celular guarda el token y lo manda en cada petición.
 *
 * Se guarda en la base de datos para que los celulares sigan "dentro" aunque se
 * reinicie el servidor, y para poder cerrar todas las sesiones si se pierde un celular.
 */
@Entity
@Table(name = "sesiones_acceso")
public class SesionAcceso {

    /** El token mismo: 43 caracteres aleatorios, imposibles de adivinar. */
    @Id
    @Column(length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private LocalDateTime creada;

    @Column(nullable = false)
    private LocalDateTime expira;

    /**
     * Última vez que este dispositivo usó el sistema. Si pasa más tiempo que los "minutos de
     * inactividad" (Ajustes), el pase deja de servir y se vuelve a pedir el PIN.
     */
    @Column(name = "ultimo_uso")
    private LocalDateTime ultimoUso;

    /** Qué dispositivo es (navegador), para reconocerlo en la lista. */
    @Column(length = 200)
    private String dispositivo;

    protected SesionAcceso() {
    }

    public SesionAcceso(String token, Rol rol, LocalDateTime expira, String dispositivo) {
        this.token = token;
        this.rol = rol;
        this.creada = LocalDateTime.now();
        this.ultimoUso = this.creada;
        this.expira = expira;
        this.dispositivo = dispositivo;
    }

    public boolean vigente() {
        return LocalDateTime.now().isBefore(expira);
    }

    /** ¿Lleva más de N minutos sin usarse? */
    public boolean inactivaPorMasDe(int minutos) {
        LocalDateTime referencia = ultimoUso != null ? ultimoUso : creada;
        return referencia.plusMinutes(minutos).isBefore(LocalDateTime.now());
    }

    /** Marca "se usó ahora". Solo escribe si pasaron más de 30 s, para no escribir en la BD en cada clic. */
    public void tocar() {
        LocalDateTime ahora = LocalDateTime.now();
        if (ultimoUso == null || ultimoUso.plusSeconds(30).isBefore(ahora)) {
            ultimoUso = ahora;
        }
    }

    public LocalDateTime getUltimoUso() { return ultimoUso; }

    public String getToken() { return token; }
    public Rol getRol() { return rol; }
    public LocalDateTime getCreada() { return creada; }
    public LocalDateTime getExpira() { return expira; }
    public String getDispositivo() { return dispositivo; }
}
