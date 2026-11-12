package mx.rodriguezammi.comun.archivos;

import mx.rodriguezammi.comun.exception.RecursoNoEncontradoException;
import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Guarda y lee archivos en una carpeta del disco (fuera de la base de datos).
 *
 * Por qué fuera de la base de datos: una foto pesa ~300 KB; mil fotos dentro de PostgreSQL
 * harían la base y sus respaldos enormes. Así, la base solo guarda el NOMBRE del archivo.
 *
 * Seguridad: nunca usamos el nombre que manda el usuario para guardar (podría traer
 * trucos como "../../Windows/archivo"); generamos uno nuevo y aleatorio.
 *
 * Carpetas:  {app.archivos.carpeta}/armazones/   y   {app.archivos.carpeta}/impresiones/
 */
@Service
public class AlmacenArchivos {

    private final Path raiz;

    public AlmacenArchivos(@Value("${app.archivos.carpeta}") String carpeta) {
        this.raiz = Path.of(carpeta).toAbsolutePath().normalize();
    }

    /**
     * Guarda el archivo y devuelve el nombre con el que quedó (por ejemplo "a3f9...e1.jpg").
     * @param permitidas extensiones aceptadas, en minúsculas y sin punto ("jpg", "pdf"...)
     */
    public String guardar(String subcarpeta, MultipartFile archivo, Set<String> permitidas) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaDeNegocioException("No se recibió ningún archivo");
        }
        String extension = extension(archivo.getOriginalFilename());
        if (!permitidas.contains(extension)) {
            throw new ReglaDeNegocioException("Tipo de archivo no permitido (." + extension + "). Se aceptan: " + String.join(", ", permitidas));
        }
        String nombre = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        try (InputStream entrada = archivo.getInputStream()) {
            Path destino = carpeta(subcarpeta).resolve(nombre);
            Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
            return nombre;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo", e);
        }
    }

    public Path ruta(String subcarpeta, String nombre) {
        Path ruta = carpeta(subcarpeta).resolve(nombre).normalize();
        if (!ruta.startsWith(raiz) || !Files.exists(ruta)) {
            throw new RecursoNoEncontradoException("El archivo ya no existe");
        }
        return ruta;
    }

    public void borrar(String subcarpeta, String nombre) {
        if (nombre == null) {
            return;
        }
        try {
            Files.deleteIfExists(carpeta(subcarpeta).resolve(nombre).normalize());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo borrar el archivo", e);
        }
    }

    public static String extension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            return "";
        }
        return nombreOriginal.substring(nombreOriginal.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private Path carpeta(String subcarpeta) {
        try {
            Path carpeta = raiz.resolve(subcarpeta);
            Files.createDirectories(carpeta);
            return carpeta;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta de archivos", e);
        }
    }
}
