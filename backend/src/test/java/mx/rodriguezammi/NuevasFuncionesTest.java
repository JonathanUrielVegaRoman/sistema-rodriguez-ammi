package mx.rodriguezammi;

import mx.rodriguezammi.admin.ExcelService;
import mx.rodriguezammi.optica.cliente.ClienteDto;
import mx.rodriguezammi.optica.cliente.ClienteService;
import mx.rodriguezammi.papeleria.producto.ProductoDto;
import mx.rodriguezammi.papeleria.producto.ProductoService;
import mx.rodriguezammi.papeleria.venta.MetodoPago;
import mx.rodriguezammi.papeleria.venta.VentaDto;
import mx.rodriguezammi.papeleria.venta.VentaService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas de: eliminar/archivar clientes y el Excel de ventas. */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class NuevasFuncionesTest {

    @Autowired ClienteService clientes;
    @Autowired ProductoService productos;
    @Autowired VentaService ventas;
    @Autowired ExcelService excel;

    @Test
    void unClienteSinOrdenesSeEliminaYDesapareceDeLaBusqueda() {
        var c = clientes.crear(new ClienteDto.Guardar("Cliente Para Borrar", null, null, false, null));

        assertThat(clientes.eliminar(c.id())).isEqualTo(ClienteService.ResultadoEliminar.ELIMINADO);
        assertThat(clientes.buscar("Para Borrar")).isEmpty();
    }

    @Test
    void elExcelTraeLasVentasConFechaYHora() throws Exception {
        var p = productos.crear(new ProductoDto.Guardar("Producto Excel", null, new BigDecimal("12.50"), 10, 1, null, false));
        ventas.registrar(new VentaDto.Nueva(MetodoPago.EFECTIVO, null, List.of(new VentaDto.Renglon(p.id(), 2))));

        byte[] archivo = excel.generar(LocalDate.now(), LocalDate.now());

        try (var libro = new XSSFWorkbook(new ByteArrayInputStream(archivo))) {
            assertThat(libro.getSheetName(0)).isEqualTo("Resumen");
            Sheet papeleria = libro.getSheet("Papelería");
            Row encabezado = papeleria.getRow(0);
            assertThat(encabezado.getCell(1).getStringCellValue()).isEqualTo("Fecha");
            assertThat(encabezado.getCell(2).getStringCellValue()).isEqualTo("Hora");
            boolean encontrado = false;
            for (Row r : papeleria) {
                if (r.getRowNum() > 0 && r.getCell(5) != null && "Producto Excel".equals(r.getCell(5).getStringCellValue())) {
                    encontrado = true;
                    assertThat(r.getCell(8).getNumericCellValue()).isEqualTo(25.0);
                    assertThat(r.getCell(2).getLocalDateTimeCellValue()).isNotNull(); // la hora es una fecha real de Excel
                }
            }
            assertThat(encontrado).isTrue();
        }
    }
}
