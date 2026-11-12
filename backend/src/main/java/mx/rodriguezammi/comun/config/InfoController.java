package mx.rodriguezammi.comun.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GET /api/publico/info → las direcciones de esta computadora en la red local.
 *
 * Sirve para armar el código QR del mostrador ("escanea para imprimir"): el QR debe
 * apuntar a la IP de la PC del negocio (por ejemplo http://192.168.1.50:8080), no a "localhost".
 */
@RestController
public class InfoController {

    private final int puerto;

    public InfoController(@Value("${server.port:8080}") int puerto) {
        this.puerto = puerto;
    }

    public record Info(List<String> direcciones) {
    }

    @GetMapping("/api/publico/info")
    public Info info() throws SocketException {
        List<String> direcciones = new ArrayList<>();
        for (NetworkInterface red : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!red.isUp() || red.isLoopback() || red.isVirtual()) {
                continue;
            }
            Collections.list(red.getInetAddresses()).stream()
                    .filter(d -> d instanceof Inet4Address && d.isSiteLocalAddress()) // 192.168.x.x, 10.x.x.x...
                    .forEach(d -> direcciones.add("http://" + d.getHostAddress() + ":" + puerto));
        }
        return new Info(direcciones);
    }
}
