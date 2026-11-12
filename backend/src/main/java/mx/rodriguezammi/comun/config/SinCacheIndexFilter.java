package mx.rodriguezammi.comun.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Evita que los celulares se queden con una versión VIEJA del sistema después de actualizarlo.
 *
 * index.html es la "portada" que dice qué archivos .js y .css cargar. Si el celular la guarda
 * en caché, seguiría pidiendo los archivos viejos. Con "no-cache" el navegador siempre pregunta
 * por la portada nueva (es muy ligera); los .js/.css sí se guardan, porque su nombre cambia
 * en cada versión (ej. index-D3wRb_lv.css).
 */
@Component
public class SinCacheIndexFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String ruta = peticion.getRequestURI();
        if ("/".equals(ruta) || "/index.html".equals(ruta)) {
            respuesta.setHeader("Cache-Control", "no-cache");
        }
        cadena.doFilter(peticion, respuesta);
    }
}
