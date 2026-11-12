package mx.rodriguezammi.optica.orden;

/**
 * Vida de una orden de lentes:
 *
 *   EN_LABORATORIO ──► LISTA ──► ENTREGADA
 *         │              │
 *         └──────────────┴──► CANCELADA
 *
 * Al pasar a LISTA, la pantalla ofrece avisarle al cliente por WhatsApp.
 * Para pasar a ENTREGADA, la orden debe estar pagada por completo.
 */
public enum EstadoOrden {
    EN_LABORATORIO,
    LISTA,
    ENTREGADA,
    CANCELADA
}
