package mx.rodriguezammi.comun.config;

import mx.rodriguezammi.comun.acceso.AccesoInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web: aquí "contratamos" al guardia (AccesoInterceptor) y le decimos
 * qué puertas vigilar: todo lo que empiece con /api/.
 * Las pantallas (index.html, .js, .css) no pasan por el guardia: se pueden descargar,
 * pero sin pase no pueden pedir ningún dato.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AccesoInterceptor guardia;

    public WebConfig(AccesoInterceptor guardia) {
        this.guardia = guardia;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(guardia).addPathPatterns("/api/**");
    }
}
