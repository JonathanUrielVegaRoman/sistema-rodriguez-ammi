package mx.rodriguezammi.papeleria.impresion;

import mx.rodriguezammi.comun.exception.ReglaDeNegocioException;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas UNITARIAS: revisan funciones sueltas, sin arrancar Spring ni la base de datos.
 * Por eso corren en milisegundos. (Las otras pruebas del proyecto son "de integración".)
 */
class ImpresoraServiceTest {

    @Test
    void elRangoDePaginasSeEscribeComoEnCualquierProgramaDeImpresion() {
        assertThat(ImpresoraService.interpretarPaginas("1-3,5", 6)).containsExactly(0, 1, 2, 4); // índices desde 0
        assertThat(ImpresoraService.interpretarPaginas(" 2 , 2 , 1 ", 3)).containsExactly(0, 1);    // sin repetidos, en orden
        assertThat(ImpresoraService.interpretarPaginas("", 3)).containsExactly(0, 1, 2);           // vacío = todas
    }

    @Test
    void pedirPaginasQueNoExistenDaUnMensajeClaro() {
        assertThatThrownBy(() -> ImpresoraService.interpretarPaginas("4-9", 6))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("solo tiene 6");
        assertThatThrownBy(() -> ImpresoraService.interpretarPaginas("uno", 6))
                .isInstanceOf(ReglaDeNegocioException.class).hasMessageContaining("1-3,5");
    }

    @Test
    void unaPaginaNormalSeImprimeA200DpiYUnaGiganteSeReduceParaNoAgotarLaMemoria() {
        assertThat(ImpresoraService.dpiSeguro(PDRectangle.LETTER, 200f)).isEqualTo(200f);

        // Una "página" de 5 x 5 metros: a 200 DPI serían 1,550 megapixeles (varios GB de memoria).
        PDRectangle gigante = new PDRectangle(14173f, 14173f);
        float dpi = ImpresoraService.dpiSeguro(gigante, 200f);
        double megapixeles = Math.pow(gigante.getWidth() / 72.0 * dpi, 2) / 1_000_000;

        assertThat(dpi).isLessThan(200f);
        assertThat(megapixeles).isLessThanOrEqualTo(12.1);
    }
}
