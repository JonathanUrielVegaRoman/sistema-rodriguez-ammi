package mx.rodriguezammi.comun.impresion;

import mx.rodriguezammi.comun.ajustes.AjustesService;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.*;
import java.awt.print.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * IMPRIME TICKETS directo en la impresora de tickets (térmica de 58 u 80 mm), sin el cuadro
 * de diálogo del navegador (Ctrl+P). La impresora y el ancho se eligen en Administración → Ajustes.
 *
 * Si no hay impresora de tickets configurada, devuelve "no impreso" y la pantalla usa la
 * impresión normal del navegador como respaldo.
 *
 * Un ticket es una lista de RENGLONES; cada renglón puede tener texto a la izquierda y a la
 * derecha (por ejemplo "2 x Cuaderno ......... $90.00"). Aquí se dibujan con Java 2D.
 */
@Service
public class ImpresorTickets {

    private static final Logger log = LoggerFactory.getLogger(ImpresorTickets.class);
    private static final NumberFormat PESOS = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX"));

    public enum Estilo { NORMAL, NEGRITA, TITULO, CENTRADO, PEQUENO, SEPARADOR }

    public record Renglon(String izquierda, String derecha, Estilo estilo) {
        public static Renglon texto(String t) { return new Renglon(t, null, Estilo.NORMAL); }
        public static Renglon par(String izq, String der) { return new Renglon(izq, der, Estilo.NORMAL); }
        public static Renglon negrita(String izq, String der) { return new Renglon(izq, der, Estilo.NEGRITA); }
        public static Renglon titulo(String t) { return new Renglon(t, null, Estilo.TITULO); }
        public static Renglon centrado(String t) { return new Renglon(t, null, Estilo.CENTRADO); }
        public static Renglon pequeno(String t) { return new Renglon(t, null, Estilo.PEQUENO); }
        public static Renglon separador() { return new Renglon(null, null, Estilo.SEPARADOR); }
    }

    public record Resultado(boolean impreso, String impresora) {
    }

    private final AjustesService ajustes;

    public ImpresorTickets(AjustesService ajustes) {
        this.ajustes = ajustes;
    }

    public static String dinero(BigDecimal valor) {
        return PESOS.format(valor == null ? BigDecimal.ZERO : valor);
    }

    public Resultado imprimir(String nombreTrabajo, List<Renglon> renglones) {
        String nombre = ajustes.impresoraTickets();
        if (nombre == null) {
            return new Resultado(false, null); // sin impresora: la pantalla imprime con el navegador
        }
        PrintService impresora = Arrays.stream(PrintServiceLookup.lookupPrintServices(null, null))
                .filter(s -> s.getName().equals(nombre)).findFirst()
                .orElseThrow(() -> new ReglaDeNegocioException("No se encontró la impresora de tickets \"" + nombre + "\". Revisa Ajustes."));

        double mm = 72 / 25.4;
        double ancho = ajustes.anchoTicketMm() * mm;
        double margen = 2 * mm;
        float tamano = ajustes.anchoTicketMm() == 80 ? 9f : 7.5f;
        Font normal = new Font(Font.SANS_SERIF, Font.PLAIN, Math.round(tamano));
        Dibujo dibujo = new Dibujo(renglones, normal, ancho - 2 * margen);

        PrinterJob trabajo = PrinterJob.getPrinterJob();
        try {
            trabajo.setPrintService(impresora);
            trabajo.setJobName(nombreTrabajo);
            Paper papel = new Paper();
            double alto = dibujo.altoTotal() + 2 * margen + 10 * mm; // un poco de espacio al final para cortar
            papel.setSize(ancho, alto);
            papel.setImageableArea(margen, margen, ancho - 2 * margen, alto - 2 * margen);
            PageFormat formato = trabajo.defaultPage();
            formato.setOrientation(PageFormat.PORTRAIT);
            formato.setPaper(papel);
            trabajo.setPrintable(dibujo, formato);
            trabajo.print();
            return new Resultado(true, impresora.getName());
        } catch (PrinterException e) {
            log.error("Error al imprimir ticket", e);
            throw new ReglaDeNegocioException("La impresora de tickets no respondió: " + e.getMessage());
        }
    }

    /** Dibuja los renglones de arriba hacia abajo, partiendo los textos largos en varias líneas. */
    private static class Dibujo implements Printable {
        private final List<Renglon> renglones;
        private final Font base;
        private final double ancho;

        Dibujo(List<Renglon> renglones, Font base, double ancho) {
            this.renglones = renglones;
            this.base = base;
            this.ancho = ancho;
        }

        private Font fuente(Estilo e) {
            return switch (e) {
                case TITULO -> base.deriveFont(Font.BOLD, base.getSize2D() * 1.4f);
                case NEGRITA -> base.deriveFont(Font.BOLD);
                case PEQUENO -> base.deriveFont(base.getSize2D() * 0.85f);
                default -> base;
            };
        }

        /** Alto aproximado (se usa para calcular el largo del papel). */
        double altoTotal() {
            // Un "lienzo" invisible de 1x1 solo para medir el tamaño de las letras.
            Graphics2D medidor = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB).createGraphics();
            double total = 0;
            for (Renglon r : renglones) {
                FontMetrics m = medidor.getFontMetrics(fuente(r.estilo()));
                int lineas = r.estilo() == Estilo.SEPARADOR ? 1 : partir(r, m).size();
                total += lineas * m.getHeight();
            }
            return total;
        }

        @Override
        public int print(Graphics grafico, PageFormat formato, int pagina) {
            if (pagina > 0) {
                return NO_SUCH_PAGE;
            }
            Graphics2D g = (Graphics2D) grafico;
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.BLACK);
            double x0 = formato.getImageableX();
            double y = formato.getImageableY();

            for (Renglon r : renglones) {
                Font f = fuente(r.estilo());
                g.setFont(f);
                FontMetrics m = g.getFontMetrics();
                if (r.estilo() == Estilo.SEPARADOR) {
                    int medio = (int) (y + m.getHeight() / 2.0);
                    g.drawLine((int) x0, medio, (int) (x0 + ancho), medio);
                    y += m.getHeight();
                    continue;
                }
                List<String> lineas = partir(r, m);
                for (int i = 0; i < lineas.size(); i++) {
                    String texto = lineas.get(i);
                    int base = (int) (y + m.getAscent());
                    boolean centrado = r.estilo() == Estilo.TITULO || r.estilo() == Estilo.CENTRADO;
                    int x = centrado ? (int) (x0 + (ancho - m.stringWidth(texto)) / 2) : (int) x0;
                    g.drawString(texto, x, base);
                    // El texto de la derecha (precio) va en la última línea, pegado al margen derecho.
                    if (r.derecha() != null && i == lineas.size() - 1) {
                        g.drawString(r.derecha(), (int) (x0 + ancho - m.stringWidth(r.derecha())), base);
                    }
                    y += m.getHeight();
                }
            }
            return PAGE_EXISTS;
        }

        /** Parte el texto izquierdo en líneas que quepan, dejando lugar para el texto derecho. */
        private List<String> partir(Renglon r, FontMetrics m) {
            String texto = r.izquierda() == null ? "" : r.izquierda();
            double disponible = ancho - (r.derecha() == null ? 0 : m.stringWidth(r.derecha()) + m.stringWidth("  "));
            List<String> lineas = new ArrayList<>();
            StringBuilder actual = new StringBuilder();
            for (String palabra : texto.split(" ")) {
                String prueba = actual.isEmpty() ? palabra : actual + " " + palabra;
                if (m.stringWidth(prueba) <= disponible || actual.isEmpty()) {
                    actual = new StringBuilder(prueba);
                } else {
                    lineas.add(actual.toString());
                    actual = new StringBuilder(palabra);
                }
            }
            lineas.add(actual.toString());
            return lineas;
        }
    }
}
