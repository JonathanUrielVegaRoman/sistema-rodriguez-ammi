package mx.rodriguezammi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PUNTO DE ARRANQUE del backend. Es el "interruptor general".
 *
 * Al ejecutar este main(), Spring Boot:
 *   1. Busca todas las clases dentro de mx.rodriguezammi (y subcarpetas) que tengan
 *      anotaciones como @RestController, @Service, @Repository, @Entity...
 *   2. Las crea y las "conecta" entre sí automáticamente (a esto se le llama
 *      INYECCIÓN DE DEPENDENCIAS: tú no haces "new ProductoService()", Spring lo hace por ti).
 *   3. Se conecta a la base de datos y crea/actualiza las tablas.
 *   4. Levanta un servidor web en el puerto 8080 (http://localhost:8080).
 *
 * @EnableScheduling permite tareas automáticas con horario (por ejemplo, revisar
 * cumpleaños cada mañana). Aún no hay ninguna, pero queda listo.
 */
@SpringBootApplication
@EnableScheduling
public class SistemaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaApplication.class, args);
    }
}
