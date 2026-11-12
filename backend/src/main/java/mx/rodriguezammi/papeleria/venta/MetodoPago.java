package mx.rodriguezammi.papeleria.venta;

/**
 * ENUM = una lista CERRADA de opciones. Evita errores de dedo como "efectivo", "Efectivo", "EFVO".
 * En la base de datos se guarda el texto (EFECTIVO, TARJETA, TRANSFERENCIA).
 */
public enum MetodoPago {
    EFECTIVO,
    TARJETA,
    TRANSFERENCIA
}
