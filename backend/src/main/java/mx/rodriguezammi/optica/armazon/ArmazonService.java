package mx.rodriguezammi.optica.armazon;

import mx.rodriguezammi.comun.archivos.AlmacenArchivos;
import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ArmazonService {

    public static final String CARPETA_FOTOS = "armazones";
    private static final Set<String> FORMATOS_FOTO = Set.of("jpg", "jpeg", "png", "webp");

    private final ArmazonRepository repositorio;
    private final AlmacenArchivos archivos;

    public ArmazonService(ArmazonRepository repositorio, AlmacenArchivos archivos) {
        this.repositorio = repositorio;
        this.archivos = archivos;
    }

    /**
     * Catálogo con filtros opcionales.
     * @param texto     busca en código, marca, modelo y color
     * @param ancho     talla (ancho de lente en mm), por ejemplo 52
     * @param disponibles true = solo los que tienen existencia
     */
    public List<ArmazonDto.Respuesta> buscar(String texto, Integer ancho, boolean disponibles) {
        String t = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return repositorio.findByActivoTrueOrderByCodigoAsc().stream()
                .filter(a -> t.isEmpty() || contiene(a, t))
                .filter(a -> ancho == null || ancho.equals(a.getAnchoLente()))
                .filter(a -> !disponibles || a.getExistencia() > 0)
                .map(ArmazonDto.Respuesta::desde)
                .toList();
    }

    /** Las tallas que existen en el catálogo, para mostrarlas como botones de filtro. */
    public List<Integer> tallas() {
        return repositorio.findByActivoTrueOrderByCodigoAsc().stream()
                .map(Armazon::getAnchoLente).filter(Objects::nonNull).distinct().sorted().toList();
    }

    public ArmazonDto.Respuesta obtener(Long id) {
        return ArmazonDto.Respuesta.desde(buscarEntidad(id));
    }

    @Transactional
    public ArmazonDto.Respuesta crear(ArmazonDto.Guardar d) {
        validarCodigoUnico(d.codigo(), null);
        Armazon a = new Armazon(d.codigo().trim(), d.marca(), d.modelo(), d.precio());
        copiar(d, a);
        return ArmazonDto.Respuesta.desde(repositorio.save(a));
    }

    @Transactional
    public ArmazonDto.Respuesta actualizar(Long id, ArmazonDto.Guardar d) {
        Armazon a = buscarEntidad(id);
        validarCodigoUnico(d.codigo(), id);
        copiar(d, a);
        return ArmazonDto.Respuesta.desde(a);
    }

    @Transactional
    public ArmazonDto.Respuesta cambiarFoto(Long id, MultipartFile foto) {
        Armazon a = buscarEntidad(id);
        String nueva = archivos.guardar(CARPETA_FOTOS, foto, FORMATOS_FOTO);
        archivos.borrar(CARPETA_FOTOS, a.getFotoArchivo()); // la foto anterior ya no sirve
        a.setFotoArchivo(nueva);
        return ArmazonDto.Respuesta.desde(a);
    }

    public Path rutaFoto(Long id) {
        Armazon a = buscarEntidad(id);
        if (a.getFotoArchivo() == null) {
            throw new RecursoNoEncontradoException("El armazón no tiene foto");
        }
        return archivos.ruta(CARPETA_FOTOS, a.getFotoArchivo());
    }

    @Transactional
    public void desactivar(Long id) {
        buscarEntidad(id).setActivo(false);
    }

    /** Con candado: úsalo antes de cambiar la existencia (al crear o cancelar una orden). */
    @Transactional
    public Armazon bloquearEntidad(Long id) {
        return repositorio.bloquearPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el armazón con id " + id));
    }

    public Armazon buscarEntidad(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el armazón con id " + id));
    }

    // ---------- privados ----------

    private void copiar(ArmazonDto.Guardar d, Armazon a) {
        a.setCodigo(d.codigo().trim());
        a.setMarca(d.marca());
        a.setModelo(d.modelo());
        a.setColor(d.color());
        a.setAnchoLente(d.anchoLente());
        a.setPuente(d.puente());
        a.setVarilla(d.varilla());
        a.setPrecio(d.precio());
        a.setExistencia(d.existencia());
        a.setUbicacion(d.ubicacion());
        a.setForma(d.forma());
        a.setColorHex(d.colorHex() == null || d.colorHex().isBlank() ? null : d.colorHex().toLowerCase(Locale.ROOT));
        a.setAltoLente(d.altoLente());
    }

    private boolean contiene(Armazon a, String t) {
        return (a.getCodigo() + " " + Objects.toString(a.getMarca(), "") + " " + Objects.toString(a.getModelo(), "")
                + " " + Objects.toString(a.getColor(), "")).toLowerCase(Locale.ROOT).contains(t);
    }

    private void validarCodigoUnico(String codigo, Long idActual) {
        repositorio.findByCodigoIgnoreCase(codigo.trim())
                .filter(otro -> !otro.getId().equals(idActual))
                .ifPresent(otro -> {
                    throw new ReglaDeNegocioException("Ya existe un armazón con el código " + codigo);
                });
    }
}
