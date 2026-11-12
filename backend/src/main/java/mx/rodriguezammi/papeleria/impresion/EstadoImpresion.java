package mx.rodriguezammi.papeleria.impresion;

/**
 * PENDIENTE → el cliente ya subió sus archivos y espera en el mostrador.
 * IMPRESO   → ya se imprimió; falta cobrar y entregar.
 * ENTREGADO → se cobró y entregó. Los archivos se BORRAN en ese momento.
 * CANCELADO → el cliente se fue o se equivocó de archivo. También se borran.
 */
public enum EstadoImpresion {
    PENDIENTE,
    IMPRESO,
    ENTREGADO,
    CANCELADO
}
