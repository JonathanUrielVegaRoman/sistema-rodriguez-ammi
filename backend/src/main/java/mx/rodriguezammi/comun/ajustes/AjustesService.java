package mx.rodriguezammi.comun.ajustes;

import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lee y guarda los ajustes del sistema. Si un ajuste nunca se ha guardado, se usa su
 * valor POR DEFECTO (así el sistema funciona desde el primer día sin configurar nada).
 */
@Service
@Transactional(readOnly = true)
public class AjustesService {

    public static final String MINUTOS_INACTIVIDAD = "minutos_inactividad";
    public static final String IMPRESORA_TICKETS = "impresora_tickets";
    public static final String ANCHO_TICKET_MM = "ancho_ticket_mm";

    /** Recomendado: 10 minutos. Suficiente para atender sin estorbar, y protege un celular olvidado en el mostrador. */
    public static final int MINUTOS_POR_DEFECTO = 10;

    private final AjusteRepository repositorio;

    public AjustesService(AjusteRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Lo que se muestra y edita en la pantalla de Ajustes del dueño. */
    public record Generales(int minutosInactividad, String impresoraTickets, int anchoTicketMm) {
    }

    public Generales generales() {
        return new Generales(minutosInactividad(), impresoraTickets(), anchoTicketMm());
    }

    @Transactional
    public Generales guardar(Generales g) {
        if (g.minutosInactividad() < 1 || g.minutosInactividad() > 480) {
            throw new ReglaDeNegocioException("Los minutos de inactividad deben estar entre 1 y 480");
        }
        if (g.anchoTicketMm() != 58 && g.anchoTicketMm() != 80) {
            throw new ReglaDeNegocioException("El ancho del ticket debe ser 58 u 80 mm");
        }
        escribir(MINUTOS_INACTIVIDAD, String.valueOf(g.minutosInactividad()));
        escribir(IMPRESORA_TICKETS, g.impresoraTickets() == null || g.impresoraTickets().isBlank() ? null : g.impresoraTickets());
        escribir(ANCHO_TICKET_MM, String.valueOf(g.anchoTicketMm()));
        return generales();
    }

    public int minutosInactividad() {
        return entero(MINUTOS_INACTIVIDAD, MINUTOS_POR_DEFECTO);
    }

    /** null = no hay impresora de tickets configurada (se usa la impresión del navegador). */
    public String impresoraTickets() {
        return leer(IMPRESORA_TICKETS);
    }

    public int anchoTicketMm() {
        return entero(ANCHO_TICKET_MM, 58);
    }

    // ---------- privados ----------

    private String leer(String clave) {
        return repositorio.findById(clave).map(Ajuste::getValor).orElse(null);
    }

    private int entero(String clave, int porDefecto) {
        try {
            String v = leer(clave);
            return v == null ? porDefecto : Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    private void escribir(String clave, String valor) {
        repositorio.findById(clave).ifPresentOrElse(a -> a.setValor(valor), () -> repositorio.save(new Ajuste(clave, valor)));
    }
}
