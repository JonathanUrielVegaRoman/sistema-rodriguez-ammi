package mx.rodriguezammi.optica.promocion;

import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import mx.rodriguezammi.optica.cliente.Cliente;
import mx.rodriguezammi.optica.cliente.ClienteRepository;
import mx.rodriguezammi.optica.cliente.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Cupones de cumpleaños y descuentos especiales.
 *
 * Flujo de cumpleaños:
 *   1. Todos los días a las 8:00 (y también al abrir la pantalla de Cumpleaños, por si la
 *      computadora se prendió tarde) se generan cupones para quien cumple HOY.
 *   2. La pantalla muestra un botón por cliente que abre WhatsApp con el mensaje ya escrito.
 *   3. El personal toca "Enviar" en WhatsApp y marca el cupón como enviado.
 *   4. Cuando el cliente viene, se escribe el código al crear su orden y se descuenta.
 *
 * No se automatiza el envío de WhatsApp porque requiere la API de Meta (trámite y costo).
 */
@Service
@Transactional(readOnly = true)
public class CuponService {

    private static final Logger log = LoggerFactory.getLogger(CuponService.class);
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sin O/0 ni I/1 para no confundir
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-MX"));

    private final CuponRepository cupones;
    private final ClienteRepository clientes;
    private final ClienteService clienteService;
    private final int porcentajeCumple;
    private final int diasVigencia;
    private final SecureRandom aleatorio = new SecureRandom();

    public CuponService(CuponRepository cupones, ClienteRepository clientes, ClienteService clienteService,
                        @Value("${app.promociones.cumpleanos-porcentaje:15}") int porcentajeCumple,
                        @Value("${app.promociones.cumpleanos-dias-vigencia:30}") int diasVigencia) {
        this.cupones = cupones;
        this.clientes = clientes;
        this.clienteService = clienteService;
        this.porcentajeCumple = porcentajeCumple;
        this.diasVigencia = diasVigencia;
    }

    // ---------------- Cumpleaños ----------------

    public record Cumpleanero(
            Long clienteId, String nombre, String celular, LocalDate fecha, int cumpleAnos,
            boolean esHoy, String codigo, Integer porcentaje, LocalDate vigenteHasta,
            boolean enviado, Long cuponId, String enlaceWhatsapp
    ) {
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "America/Mexico_City")
    @Transactional
    public void generarCuponesDeHoy() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDeAnio = hoy.withDayOfYear(1).atStartOfDay();
        for (Cliente c : clientes.cumpleanerosDel(hoy.getMonthValue(), hoy.getDayOfMonth())) {
            if (!cupones.existsByClienteIdAndTipoAndCreadoAfter(c.getId(), TipoCupon.CUMPLEANOS, inicioDeAnio)) {
                cupones.save(new Cupon(codigoNuevo("CUMPLE"), TipoCupon.CUMPLEANOS, porcentajeCumple, c, hoy.plusDays(diasVigencia)));
                log.info("Cupón de cumpleaños generado para {}", c.getNombre());
            }
        }
    }

    /** Cumpleañeros de hoy y de los próximos días (para avisar con tiempo). */
    @Transactional
    public List<Cumpleanero> cumpleaneros(int diasAdelante) {
        generarCuponesDeHoy();
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDeAnio = hoy.withDayOfYear(1).atStartOfDay();
        List<Cumpleanero> lista = new ArrayList<>();

        for (int i = 0; i <= diasAdelante; i++) {
            LocalDate dia = hoy.plusDays(i);
            for (Cliente c : clientes.cumpleanerosDel(dia.getMonthValue(), dia.getDayOfMonth())) {
                int anos = Period.between(c.getFechaNacimiento(), dia).getYears();
                Cupon cupon = i == 0
                        ? cupones.findFirstByClienteIdAndTipoAndCreadoAfterOrderByCreadoDesc(c.getId(), TipoCupon.CUMPLEANOS, inicioDeAnio).orElse(null)
                        : null;
                lista.add(new Cumpleanero(c.getId(), c.getNombre(), c.getCelular(), dia, anos, i == 0,
                        cupon == null ? null : cupon.getCodigo(),
                        cupon == null ? null : cupon.getPorcentaje(),
                        cupon == null ? null : cupon.getVigenteHasta(),
                        cupon != null && cupon.isEnviado(),
                        cupon == null ? null : cupon.getId(),
                        cupon == null || c.getCelular() == null ? null : enlaceWhatsapp(c, cupon)));
            }
        }
        return lista;
    }

    @Transactional
    public void marcarEnviado(Long cuponId) {
        cupones.findById(cuponId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cupón " + cuponId))
                .setEnviado(true);
    }

    // ---------------- Descuentos especiales (los crea el dueño) ----------------

    public record CuponResumen(Long id, String codigo, TipoCupon tipo, int porcentaje, String cliente,
                               LocalDate vigenteHasta, boolean enviado, Long usadoEnOrden, String nota) {
        static CuponResumen desde(Cupon c) {
            return new CuponResumen(c.getId(), c.getCodigo(), c.getTipo(), c.getPorcentaje(),
                    c.getCliente() == null ? null : c.getCliente().getNombre(),
                    c.getVigenteHasta(), c.isEnviado(), c.getUsadoEnOrden(), c.getNota());
        }
    }

    public List<CuponResumen> recientes() {
        return cupones.findTop100ByOrderByCreadoDesc().stream().map(CuponResumen::desde).toList();
    }

    @Transactional
    public CuponResumen crearDescuento(Long clienteId, int porcentaje, int diasVigencia, String nota) {
        if (porcentaje < 1 || porcentaje > 100) {
            throw new ReglaDeNegocioException("El porcentaje debe estar entre 1 y 100");
        }
        Cliente cliente = clienteId == null ? null : clienteService.buscarEntidad(clienteId);
        Cupon cupon = new Cupon(codigoNuevo("DESC"), TipoCupon.DESCUENTO, porcentaje, cliente, LocalDate.now().plusDays(diasVigencia));
        cupon.setNota(nota);
        return CuponResumen.desde(cupones.save(cupon));
    }

    // ---------------- Uso al crear una orden ----------------

    /** Revisa que el cupón exista, esté vigente, no se haya usado y sea de ese cliente. */
    public Cupon validar(String codigo, Long clienteId) {
        Cupon cupon = cupones.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new ReglaDeNegocioException("El código " + codigo + " no existe"));
        if (cupon.usado()) {
            throw new ReglaDeNegocioException("El código " + codigo + " ya se usó en la orden " + cupon.getUsadoEnOrden());
        }
        if (cupon.vencido()) {
            throw new ReglaDeNegocioException("El código " + codigo + " venció el " + cupon.getVigenteHasta());
        }
        if (cupon.getCliente() != null && !cupon.getCliente().getId().equals(clienteId)) {
            throw new ReglaDeNegocioException("El código " + codigo + " es de otro cliente");
        }
        return cupon;
    }

    // ---------------- privados ----------------

    private String codigoNuevo(String prefijo) {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder(prefijo).append('-');
            for (int i = 0; i < 5; i++) {
                sb.append(LETRAS.charAt(aleatorio.nextInt(LETRAS.length())));
            }
            codigo = sb.toString();
        } while (cupones.existsByCodigo(codigo));
        return codigo;
    }

    private String enlaceWhatsapp(Cliente c, Cupon cupon) {
        String primerNombre = c.getNombre().split(" ")[0];
        String mensaje = "¡Feliz cumpleaños, " + primerNombre + "! 🎉 En Óptica RODRÍGUEZ queremos celebrarte: "
                + "tienes " + cupon.getPorcentaje() + "% de descuento en tus lentes con el código "
                + cupon.getCodigo() + ". Válido hasta el " + cupon.getVigenteHasta().format(FECHA) + ". ¡Te esperamos!";
        return "https://wa.me/52" + c.getCelular() + "?text=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
