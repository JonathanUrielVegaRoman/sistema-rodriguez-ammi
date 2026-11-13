package mx.rodriguezammi.comun.ajustes;

import jakarta.persistence.*;

/**
 * Un AJUSTE del sistema que el dueño puede cambiar desde la pantalla de Ajustes,
 * sin tocar código ni reiniciar. Se guarda como pareja clave → valor:
 *
 *   ajustes
 *   ┌────────────────────────┬──────────────────────────┐
 *   │ clave                  │ valor                    │
 *   ├────────────────────────┼──────────────────────────┤
 *   │ minutos_inactividad    │ 10                       │
 *   │ impresora_tickets      │ POS-58                   │
 *   │ ancho_ticket_mm        │ 58                       │
 *   └────────────────────────┴──────────────────────────┘
 */
@Entity
@Table(name = "ajustes")
public class Ajuste {

    @Id
    @Column(length = 60)
    private String clave;

    @Column(length = 200)
    private String valor;

    protected Ajuste() {
    }

    public Ajuste(String clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public String getClave() { return clave; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}
