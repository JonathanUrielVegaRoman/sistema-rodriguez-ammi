package mx.rodriguezammi.papeleria.impresion;

import mx.rodriguezammi.comun.archivos.AlmacenArchivos;
import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.papeleria.producto.Producto;
import mx.rodriguezammi.papeleria.producto.ProductoRepository;
import mx.rodriguezammi.papeleria.venta.MetodoPago;
import mx.rodriguezammi.papeleria.venta.VentaDto;
import mx.rodriguezammi.papeleria.venta.VentaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * BUZÓN DE IMPRESIÓN: reglas del negocio.
 *
 *   Cliente (sin cuenta)                         Personal (con PIN)
 *   ─────────────────────                        ─────────────────────────────
 *   1. Escanea el QR del mostrador
 *   2. Sube archivos, copias, color         ──►  3. Lo ve en la COLA con su turno
 *      → recibe turno "A-007"                    4. Descarga y lo imprime
 *   5. Ve en su celular "¡Listo!"           ◄──  5. Marca IMPRESO y anota hojas
 *                                                6. COBRAR → crea una venta de papelería,
 *                                                   imprime ticket y BORRA los archivos
 */
@Service
@Transactional(readOnly = true)
public class ImpresionService {

    private static final Logger log = LoggerFactory.getLogger(ImpresionService.class);

    public static final String CARPETA = "impresiones";
    /** Códigos de barras internos de los productos-servicio (se crean solos en DatosIniciales). */
    public static final String CODIGO_BN = "SRV-IMP-BN";
    public static final String CODIGO_COLOR = "SRV-IMP-COLOR";

    private static final int MAX_ARCHIVOS = 5;
    private static final Set<String> PERMITIDOS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "jpg", "jpeg", "png", "txt");

    private final TrabajoImpresionRepository trabajos;
    private final AlmacenArchivos archivos;
    private final ProductoRepository productos;
    private final VentaService ventas;
    private final int horasRetencion;
    private final SecureRandom aleatorio = new SecureRandom();

    public ImpresionService(TrabajoImpresionRepository trabajos, AlmacenArchivos archivos, ProductoRepository productos,
                            VentaService ventas, @Value("${app.impresiones.horas-retencion:24}") int horasRetencion) {
        this.trabajos = trabajos;
        this.archivos = archivos;
        this.productos = productos;
        this.ventas = ventas;
        this.horasRetencion = horasRetencion;
    }

    // ---------------- DTOs ----------------

    public record Recibido(String turno, String codigoSeguimiento) {
    }

    public record EstadoPublico(String turno, EstadoImpresion estado, LocalDateTime fecha, int archivos) {
    }

    public record ArchivoDto(Long id, String nombre, long tamano, boolean disponible) {
    }

    public record TrabajoDto(Long id, String turno, LocalDateTime fecha, int copias, boolean color, boolean dobleCara,
                             String notas, EstadoImpresion estado, Integer hojas, Long ventaFolio, List<ArchivoDto> archivos) {
        static TrabajoDto desde(TrabajoImpresion t) {
            return new TrabajoDto(t.getId(), t.getTurno(), t.getFecha(), t.getCopias(), t.isColor(), t.isDobleCara(),
                    t.getNotas(), t.getEstado(), t.getHojas(), t.getVentaFolio(),
                    t.getArchivos().stream().map(a -> new ArchivoDto(a.getId(), a.getNombreOriginal(), a.getTamano(),
                            a.getNombreGuardado() != null)).toList());
        }
    }

    public record Descarga(Path ruta, String nombre) {
    }

    // ---------------- Lado del cliente (público) ----------------

    @Transactional
    public Recibido recibir(List<MultipartFile> subidos, int copias, boolean color, boolean dobleCara, String notas) {
        if (subidos == null || subidos.isEmpty()) {
            throw new ReglaDeNegocioException("Elige al menos un archivo");
        }
        if (subidos.size() > MAX_ARCHIVOS) {
            throw new ReglaDeNegocioException("Máximo " + MAX_ARCHIVOS + " archivos por turno");
        }
        if (copias < 1 || copias > 100) {
            throw new ReglaDeNegocioException("Las copias deben ser entre 1 y 100");
        }

        long deHoy = trabajos.countByFechaAfter(LocalDate.now().atStartOfDay());
        String turno = "A-" + String.format("%03d", deHoy + 1);
        String notasLimpias = notas == null || notas.isBlank() ? null : notas.trim().substring(0, Math.min(300, notas.trim().length()));
        TrabajoImpresion trabajo = new TrabajoImpresion(turno, codigoSecreto(), copias, color, dobleCara, notasLimpias);

        for (MultipartFile f : subidos) {
            String guardado = archivos.guardar(CARPETA, f, PERMITIDOS);
            String original = f.getOriginalFilename() == null ? "archivo" : f.getOriginalFilename();
            trabajo.agregarArchivo(new ArchivoImpresion(original.substring(0, Math.min(200, original.length())), guardado, f.getSize()));
        }
        trabajos.save(trabajo);
        return new Recibido(turno, trabajo.getCodigoSeguimiento());
    }

    public EstadoPublico consultar(String codigo) {
        TrabajoImpresion t = trabajos.findByCodigoSeguimiento(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No encontramos tu turno"));
        return new EstadoPublico(t.getTurno(), t.getEstado(), t.getFecha(), t.getArchivos().size());
    }

    // ---------------- Lado del personal ----------------

    public List<TrabajoDto> cola() {
        return trabajos.findByEstadoInOrderByFechaAsc(EnumSet.of(EstadoImpresion.PENDIENTE, EstadoImpresion.IMPRESO))
                .stream().map(TrabajoDto::desde).toList();
    }

    public Descarga archivo(Long trabajoId, Long archivoId) {
        ArchivoImpresion a = buscar(trabajoId).getArchivos().stream()
                .filter(x -> x.getId().equals(archivoId)).findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe ese archivo"));
        if (a.getNombreGuardado() == null) {
            throw new RecursoNoEncontradoException("El archivo ya se borró por privacidad");
        }
        return new Descarga(archivos.ruta(CARPETA, a.getNombreGuardado()), a.getNombreOriginal());
    }

    @Transactional
    public TrabajoDto marcarImpreso(Long id, int hojas) {
        if (hojas < 1) {
            throw new ReglaDeNegocioException("Indica cuántas hojas se imprimieron");
        }
        TrabajoImpresion t = buscar(id);
        exigirAbierto(t);
        t.setHojas(hojas);
        t.setEstado(EstadoImpresion.IMPRESO);
        return TrabajoDto.desde(t);
    }

    /** Cobra las hojas como una venta normal de papelería, entrega y borra los archivos. */
    @Transactional
    public VentaDto.Ticket cobrar(Long id, MetodoPago metodo, java.math.BigDecimal pagaCon) {
        TrabajoImpresion t = buscar(id);
        exigirAbierto(t);
        if (t.getHojas() == null) {
            throw new ReglaDeNegocioException("Primero marca el turno como impreso e indica las hojas");
        }
        String codigo = t.isColor() ? CODIGO_COLOR : CODIGO_BN;
        Producto servicio = productos.findByCodigoBarras(codigo)
                .orElseThrow(() -> new ReglaDeNegocioException("Falta el producto de servicio " + codigo + " en el inventario"));

        VentaDto.Ticket ticket = ventas.registrar(new VentaDto.Nueva(metodo, pagaCon,
                List.of(new VentaDto.Renglon(servicio.getId(), t.getHojas()))));
        t.setVentaFolio(ticket.folio());
        t.setEstado(EstadoImpresion.ENTREGADO);
        borrarArchivos(t);
        return ticket;
    }

    @Transactional
    public void cancelar(Long id) {
        TrabajoImpresion t = buscar(id);
        exigirAbierto(t);
        t.setEstado(EstadoImpresion.CANCELADO);
        borrarArchivos(t);
    }

    /** Cada hora: borra archivos de clientes más viejos que las horas de retención (privacidad). */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void limpiarViejos() {
        List<TrabajoImpresion> viejos = trabajos.findByArchivosBorradosFalseAndFechaBefore(LocalDateTime.now().minusHours(horasRetencion));
        for (TrabajoImpresion t : viejos) {
            if (t.getEstado() == EstadoImpresion.PENDIENTE || t.getEstado() == EstadoImpresion.IMPRESO) {
                t.setEstado(EstadoImpresion.CANCELADO);
            }
            borrarArchivos(t);
        }
        if (!viejos.isEmpty()) {
            log.info("Buzón de impresión: se borraron los archivos de {} turno(s) vencido(s)", viejos.size());
        }
    }

    // ---------------- privados ----------------

    private TrabajoImpresion buscar(Long id) {
        return trabajos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe el turno " + id));
    }

    private void exigirAbierto(TrabajoImpresion t) {
        if (t.getEstado() == EstadoImpresion.ENTREGADO || t.getEstado() == EstadoImpresion.CANCELADO) {
            throw new ReglaDeNegocioException("El turno " + t.getTurno() + " ya está cerrado");
        }
    }

    private void borrarArchivos(TrabajoImpresion t) {
        for (ArchivoImpresion a : t.getArchivos()) {
            archivos.borrar(CARPETA, a.getNombreGuardado());
            a.setNombreGuardado(null);
        }
        t.setArchivosBorrados(true);
    }

    private String codigoSecreto() {
        byte[] bytes = new byte[18];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
