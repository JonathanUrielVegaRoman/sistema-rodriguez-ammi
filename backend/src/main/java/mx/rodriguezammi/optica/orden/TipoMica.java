package mx.rodriguezammi.optica.orden;

/**
 * MONOFOCAL  → una sola graduación (ver de lejos o de cerca).
 * BIFOCAL    → dos zonas: lejos arriba, cerca abajo (con línea visible).
 * PROGRESIVO → cambia gradualmente de lejos a cerca (sin línea).
 * SOLO_ARMAZON → el cliente solo compra el armazón (sin micas).
 */
public enum TipoMica {
    MONOFOCAL,
    BIFOCAL,
    PROGRESIVO,
    SOLO_ARMAZON
}
