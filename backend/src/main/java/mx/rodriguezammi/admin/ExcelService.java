package mx.rodriguezammi.admin;

import mx.rodriguezammi.optica.armazon.Armazon;
import mx.rodriguezammi.optica.armazon.ArmazonRepository;
import mx.rodriguezammi.optica.orden.OrdenLentes;
import mx.rodriguezammi.papeleria.producto.Producto;
import mx.rodriguezammi.papeleria.producto.ProductoRepository;
import mx.rodriguezammi.optica.orden.OrdenRepository;
import mx.rodriguezammi.optica.orden.PagoOrden;
import mx.rodriguezammi.optica.orden.PartidaLente;
import mx.rodriguezammi.papeleria.venta.DetalleVenta;
import mx.rodriguezammi.papeleria.venta.MetodoPago;
import mx.rodriguezammi.papeleria.venta.Venta;
import mx.rodriguezammi.papeleria.venta.VentaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * REPORTE EN EXCEL (.xlsx) de todas las ventas, para el administrador.
 *
 * Hojas del archivo:
 *   1. Resumen          → totales del periodo por negocio y método de pago
 *   2. Papelería        → un renglón por producto vendido, con FECHA y HORA del ticket
 *   3. Óptica pagos     → cada anticipo/abono que entró, con fecha y hora
 *   4. Óptica órdenes   → cada orden con su total, lo pagado y lo que falta
 *   5. Canceladas       → ventas de papelería canceladas (no suman en las demás hojas)
 *
 * Las fechas y montos se guardan como fechas y números REALES de Excel (no como texto),
 * así el dueño puede filtrar, ordenar y sumar en Excel sin problemas.
 * Librería: Apache POI (ver pom.xml).
 */
@Service
@Transactional(readOnly = true)
public class ExcelService {

    private static final Locale ES = Locale.forLanguageTag("es-MX");

    private final VentaRepository ventas;
    private final OrdenRepository ordenes;
    private final ProductoRepository productos;
    private final ArmazonRepository armazones;

    public ExcelService(VentaRepository ventas, OrdenRepository ordenes, ProductoRepository productos, ArmazonRepository armazones) {
        this.ventas = ventas;
        this.ordenes = ordenes;
        this.productos = productos;
        this.armazones = armazones;
    }

    /**
     * INVENTARIO en Excel: existencias y valor de la mercancía de los dos negocios.
     * Sirve para resurtir, para un conteo físico ("lo que dice el sistema vs. lo que hay")
     * y para saber cuánto dinero hay invertido en mercancía.
     */
    public byte[] inventario() {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos e = new Estilos(libro);

            Sheet papeleria = libro.createSheet("Papelería");
            String[] cols = {"Producto", "Código de barras", "Precio", "Existencia", "Avisar en", "¿Resurtir?", "Valor a precio de venta", "Conteo físico"};
            int f = encabezados(papeleria, cols, e);
            for (Producto p : productos.findByActivoTrueOrderByNombreAsc()) {
                if (p.isServicio()) {
                    continue; // los servicios (impresiones) no son mercancía
                }
                Row r = papeleria.createRow(f++);
                texto(r, 0, p.getNombre(), e.normal);
                texto(r, 1, p.getCodigoBarras(), e.normal);
                dinero(r, 2, p.getPrecio(), e.dinero);
                numero(r, 3, p.getExistencia(), e.normal);
                numero(r, 4, p.getStockMinimo(), e.normal);
                texto(r, 5, p.necesitaResurtir() ? "SÍ" : "", e.normal);
                formula(r, 6, "C" + f + "*D" + f, e.dinero);
            }
            filaTotal(papeleria, f, e, 0, new int[]{3}, new int[]{6});
            cerrar(papeleria, cols.length, f, 36, 18, 12, 12, 11, 11, 22, 14);

            Sheet optica = libro.createSheet("Armazones");
            String[] cols2 = {"Código", "Marca", "Modelo", "Color", "Medidas", "Ubicación", "Precio", "Existencia", "Valor a precio de venta", "Conteo físico"};
            f = encabezados(optica, cols2, e);
            for (Armazon a : armazones.findByActivoTrueOrderByCodigoAsc()) {
                Row r = optica.createRow(f++);
                texto(r, 0, a.getCodigo(), e.normal);
                texto(r, 1, a.getMarca(), e.normal);
                texto(r, 2, a.getModelo(), e.normal);
                texto(r, 3, a.getColor(), e.normal);
                texto(r, 4, a.getMedidas(), e.normal);
                texto(r, 5, a.getUbicacion(), e.normal);
                dinero(r, 6, a.getPrecio(), e.dinero);
                numero(r, 7, a.getExistencia(), e.normal);
                formula(r, 8, "G" + f + "*H" + f, e.dinero);
            }
            filaTotal(optica, f, e, 0, new int[]{7}, new int[]{8});
            cerrar(optica, cols2.length, f, 16, 16, 18, 12, 14, 22, 12, 12, 22, 14);

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public byte[] generar(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay().minusNanos(1);
        List<Venta> todas = ventas.findByFechaBetweenOrderByFechaAsc(inicio, fin);
        List<Venta> delPeriodo = todas.stream().filter(v -> !v.isCancelada()).toList();
        List<Venta> canceladas = todas.stream().filter(Venta::isCancelada).toList();
        List<PagoOrden> pagos = ordenes.pagosEntre(inicio, fin);
        List<OrdenLentes> ordenesPeriodo = ordenes.findByFechaBetweenOrderByFechaAsc(inicio, fin);

        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos e = new Estilos(libro);
            hojaResumen(libro, e, desde, hasta, delPeriodo, pagos);
            hojaPapeleria(libro, e, delPeriodo);
            hojaPagosOptica(libro, e, pagos);
            hojaOrdenes(libro, e, ordenesPeriodo);
            hojaCanceladas(libro, e, canceladas);
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    // ---------------- Hojas ----------------

    private void hojaResumen(Workbook libro, Estilos e, LocalDate desde, LocalDate hasta, List<Venta> v, List<PagoOrden> pagos) {
        Sheet hoja = libro.createSheet("Resumen");
        int f = 0;
        Row titulo = hoja.createRow(f++);
        texto(titulo, 0, "Reporte de ventas — Óptica RODRÍGUEZ y Papelería AMMI", e.titulo);
        Row periodo = hoja.createRow(f++);
        texto(periodo, 0, "Periodo", e.encabezado);
        fecha(periodo, 1, desde.atStartOfDay(), e.fecha);
        fecha(periodo, 2, hasta.atStartOfDay(), e.fecha);
        Row generado = hoja.createRow(f++);
        texto(generado, 0, "Generado", e.encabezado);
        fecha(generado, 1, LocalDateTime.now(), e.fechaHora);
        f++;

        Row cab = hoja.createRow(f++);
        String[] columnas = {"Método de pago", "Papelería AMMI", "Óptica RODRÍGUEZ", "Total"};
        for (int i = 0; i < columnas.length; i++) texto(cab, i, columnas[i], e.encabezado);
        int primera = f;
        for (MetodoPago m : MetodoPago.values()) {
            BigDecimal pap = v.stream().filter(x -> x.getMetodoPago() == m).map(Venta::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal opt = pagos.stream().filter(x -> x.getMetodoPago() == m).map(PagoOrden::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
            Row r = hoja.createRow(f++);
            texto(r, 0, nombre(m), e.normal);
            dinero(r, 1, pap, e.dinero);
            dinero(r, 2, opt, e.dinero);
            formula(r, 3, "B" + f + "+C" + f, e.dinero);
        }
        Row total = hoja.createRow(f);
        texto(total, 0, "TOTAL", e.total);
        for (int c = 1; c <= 3; c++) {
            String col = String.valueOf((char) ('A' + c));
            formula(total, c, "SUM(" + col + (primera + 1) + ":" + col + f + ")", e.totalDinero);
        }
        f += 2;
        Row nota = hoja.createRow(f);
        texto(nota, 0, "Óptica = dinero que entró (anticipos y liquidaciones) en el periodo. Tickets de papelería: " + v.size(), e.normal);
        anchos(hoja, 22, 18, 20, 16);
    }

    private void hojaPapeleria(Workbook libro, Estilos e, List<Venta> lista) {
        Sheet hoja = libro.createSheet("Papelería");
        String[] cols = {"Folio", "Fecha", "Hora", "Día", "Método de pago", "Producto", "Cantidad", "Precio unitario", "Importe", "Total del ticket", "Referencia de pago"};
        int f = encabezados(hoja, cols, e);
        for (Venta v : lista) {
            boolean primero = true;
            for (DetalleVenta d : v.getDetalles()) {
                Row r = hoja.createRow(f++);
                numero(r, 0, v.getId(), e.normal);
                fecha(r, 1, v.getFecha(), e.fecha);
                fecha(r, 2, v.getFecha(), e.hora);
                texto(r, 3, v.getFecha().getDayOfWeek().getDisplayName(TextStyle.FULL, ES), e.normal);
                texto(r, 4, nombre(v.getMetodoPago()), e.normal);
                texto(r, 5, d.getDescripcion(), e.normal);
                numero(r, 6, d.getCantidad(), e.normal);
                dinero(r, 7, d.getPrecioUnitario(), e.dinero);
                dinero(r, 8, d.getImporte(), e.dinero);
                // El total del ticket solo en su primer renglón, para que la suma de la columna no se duplique.
                if (primero) dinero(r, 9, v.getTotal(), e.dinero);
                texto(r, 10, v.getReferenciaPago(), e.normal);
                primero = false;
            }
        }
        filaTotal(hoja, f, e, 5, new int[]{6}, new int[]{8, 9});
        cerrar(hoja, cols.length, f, 8, 10, 8, 12, 16, 34, 10, 15, 13, 16, 24);
    }

    private void hojaPagosOptica(Workbook libro, Estilos e, List<PagoOrden> pagos) {
        Sheet hoja = libro.createSheet("Óptica pagos");
        String[] cols = {"Fecha", "Hora", "Día", "Folio de orden", "Cliente", "Método de pago", "Monto", "Referencia de pago"};
        int f = encabezados(hoja, cols, e);
        for (PagoOrden p : pagos) {
            Row r = hoja.createRow(f++);
            fecha(r, 0, p.getFecha(), e.fecha);
            fecha(r, 1, p.getFecha(), e.hora);
            texto(r, 2, p.getFecha().getDayOfWeek().getDisplayName(TextStyle.FULL, ES), e.normal);
            numero(r, 3, p.getOrden().getId(), e.normal);
            texto(r, 4, p.getOrden().getCliente().getNombre(), e.normal);
            texto(r, 5, nombre(p.getMetodoPago()), e.normal);
            dinero(r, 6, p.getMonto(), e.dinero);
            texto(r, 7, p.getReferencia(), e.normal);
        }
        filaTotal(hoja, f, e, 5, new int[]{}, new int[]{6});
        cerrar(hoja, cols.length, f, 12, 8, 12, 14, 30, 16, 14, 24);
    }

    private void hojaOrdenes(Workbook libro, Estilos e, List<OrdenLentes> lista) {
        Sheet hoja = libro.createSheet("Óptica órdenes");
        String[] cols = {"Folio", "Fecha", "Hora", "Cliente", "Estado", "Lentes", "Subtotal", "Descuento", "Total", "Pagado", "Saldo"};
        int f = encabezados(hoja, cols, e);
        for (OrdenLentes o : lista) {
            Row r = hoja.createRow(f++);
            numero(r, 0, o.getId(), e.normal);
            fecha(r, 1, o.getFecha(), e.fecha);
            fecha(r, 2, o.getFecha(), e.hora);
            texto(r, 3, o.getCliente().getNombre(), e.normal);
            texto(r, 4, switch (o.getEstado()) {
                case EN_LABORATORIO -> "En laboratorio";
                case LISTA -> "Lista";
                case ENTREGADA -> "Entregada";
                case CANCELADA -> "Cancelada";
            }, e.normal);
            texto(r, 5, o.getPartidas().stream()
                    .map(p -> p.getDescripcionArmazon() + (p.isEsRegalo() ? " (regalo 2x1)" : ""))
                    .collect(Collectors.joining(" + ")), e.normal);
            dinero(r, 6, o.getSubtotal(), e.dinero);
            dinero(r, 7, o.getDescuentoCupon().add(o.getDescuentoManual()), e.dinero);
            dinero(r, 8, o.getTotal(), e.dinero);
            dinero(r, 9, o.getPagado(), e.dinero);
            dinero(r, 10, o.getSaldo(), e.dinero);
        }
        filaTotal(hoja, f, e, 5, new int[]{}, new int[]{6, 7, 8, 9, 10});
        cerrar(hoja, cols.length, f, 8, 12, 8, 28, 15, 40, 13, 13, 13, 13, 13);
    }

    /** Ventas canceladas: NO están en las demás hojas ni en los totales; aquí se listan para control. */
    private void hojaCanceladas(Workbook libro, Estilos e, List<Venta> lista) {
        Sheet hoja = libro.createSheet("Canceladas");
        String[] cols = {"Folio", "Fecha de venta", "Hora", "Total", "Cancelada el", "Motivo"};
        int f = encabezados(hoja, cols, e);
        for (Venta v : lista) {
            Row r = hoja.createRow(f++);
            numero(r, 0, v.getId(), e.normal);
            fecha(r, 1, v.getFecha(), e.fecha);
            fecha(r, 2, v.getFecha(), e.hora);
            dinero(r, 3, v.getTotal(), e.dinero);
            if (v.getFechaCancelacion() != null) fecha(r, 4, v.getFechaCancelacion(), e.fechaHora);
            texto(r, 5, v.getMotivoCancelacion(), e.normal);
        }
        if (f == 1) texto(hoja.createRow(1), 0, "Sin ventas canceladas en el periodo", e.normal);
        cerrar(hoja, cols.length, f, 8, 14, 8, 13, 18, 40);
    }

    // ---------------- Ayudantes ----------------

    private int encabezados(Sheet hoja, String[] cols, Estilos e) {
        Row cab = hoja.createRow(0);
        cab.setHeightInPoints(20);
        for (int i = 0; i < cols.length; i++) texto(cab, i, cols[i], e.encabezado);
        return 1;
    }

    /** Renglón final con SUMA de las columnas indicadas. */
    private void filaTotal(Sheet hoja, int f, Estilos e, int colEtiqueta, int[] colsNumero, int[] colsDinero) {
        if (f == 1) {
            texto(hoja.createRow(1), 0, "Sin movimientos en el periodo", e.normal);
            return;
        }
        Row r = hoja.createRow(f);
        texto(r, colEtiqueta, "TOTAL", e.total);
        for (int c : colsNumero) formula(r, c, "SUM(" + col(c) + "2:" + col(c) + f + ")", e.total);
        for (int c : colsDinero) formula(r, c, "SUM(" + col(c) + "2:" + col(c) + f + ")", e.totalDinero);
    }

    /** Congela el encabezado, agrega filtros y fija anchos de columna (en caracteres). */
    private void cerrar(Sheet hoja, int columnas, int ultimaFila, int... anchos) {
        hoja.createFreezePane(0, 1);
        if (ultimaFila > 1) {
            hoja.setAutoFilter(new CellRangeAddress(0, ultimaFila - 1, 0, columnas - 1));
        }
        anchos(hoja, anchos);
    }

    private void anchos(Sheet hoja, int... anchos) {
        for (int i = 0; i < anchos.length; i++) hoja.setColumnWidth(i, anchos[i] * 256);
    }

    private static String col(int c) {
        return String.valueOf((char) ('A' + c));
    }

    private static String nombre(MetodoPago m) {
        return switch (m) {
            case EFECTIVO -> "Efectivo";
            case TARJETA -> "Tarjeta";
            case TRANSFERENCIA -> "Transferencia";
        };
    }

    private void texto(Row r, int c, String v, CellStyle s) {
        Cell celda = r.createCell(c);
        celda.setCellValue(v == null ? "" : v);
        celda.setCellStyle(s);
    }

    private void numero(Row r, int c, long v, CellStyle s) {
        Cell celda = r.createCell(c);
        celda.setCellValue(v);
        celda.setCellStyle(s);
    }

    private void dinero(Row r, int c, BigDecimal v, CellStyle s) {
        Cell celda = r.createCell(c);
        celda.setCellValue(v == null ? 0 : v.doubleValue());
        celda.setCellStyle(s);
    }

    private void fecha(Row r, int c, LocalDateTime v, CellStyle s) {
        Cell celda = r.createCell(c);
        celda.setCellValue(v);
        celda.setCellStyle(s);
    }

    private void formula(Row r, int c, String f, CellStyle s) {
        Cell celda = r.createCell(c);
        celda.setCellFormula(f);
        celda.setCellStyle(s);
    }

    /** Formatos de celda reutilizables (Excel limita cuántos estilos distintos puede tener un archivo). */
    private static class Estilos {
        final CellStyle titulo, encabezado, normal, dinero, fecha, hora, fechaHora, total, totalDinero;

        Estilos(Workbook libro) {
            DataFormat formato = libro.createDataFormat();

            Font negrita = libro.createFont();
            negrita.setBold(true);
            Font grande = libro.createFont();
            grande.setBold(true);
            grande.setFontHeightInPoints((short) 14);
            Font blanca = libro.createFont();
            blanca.setBold(true);
            blanca.setColor(IndexedColors.WHITE.getIndex());

            titulo = libro.createCellStyle();
            titulo.setFont(grande);

            encabezado = libro.createCellStyle();
            encabezado.setFont(blanca);
            encabezado.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            encabezado.setVerticalAlignment(VerticalAlignment.CENTER);

            normal = libro.createCellStyle();

            dinero = libro.createCellStyle();
            dinero.setDataFormat(formato.getFormat("$#,##0.00"));

            fecha = libro.createCellStyle();
            fecha.setDataFormat(formato.getFormat("dd/mm/yyyy"));

            hora = libro.createCellStyle();
            hora.setDataFormat(formato.getFormat("hh:mm"));

            fechaHora = libro.createCellStyle();
            fechaHora.setDataFormat(formato.getFormat("dd/mm/yyyy hh:mm"));

            total = libro.createCellStyle();
            total.setFont(negrita);
            total.setBorderTop(BorderStyle.THIN);

            totalDinero = libro.createCellStyle();
            totalDinero.cloneStyleFrom(total);
            totalDinero.setDataFormat(formato.getFormat("$#,##0.00"));
        }
    }
}
