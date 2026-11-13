# Calidad: qué se revisó, qué se encontró y cómo se comprueba

> "Calidad" no es tener muchas pantallas. Es que el sistema **no pierda dinero ni datos cuando
> pasa lo inesperado**: dos personas cobrando a la vez, el Wi-Fi que falla, un archivo raro.
> Este documento cuenta qué se buscó, qué fallas reales aparecieron y qué prueba las vigila ahora.

## 1. Fallas reales que se encontraron (y ya no existen)

| # | Qué podía pasar | Cómo se demostró | Cómo se corrigió | Prueba que lo vigila |
|---|---|---|---|---|
| 1 | **Dos celulares vendían la misma última pieza.** Los dos leían "queda 1" y los dos cobraban. | Se escribió la prueba primero y **falló**: esperaba 1 venta, hubo 2. | Candado en la base de datos (`SELECT … FOR UPDATE`) al vender, cancelar, resurtir y ajustar. | `VentasSimultaneasTest` (15 rondas). También probado contra PostgreSQL real: 4 cobros a la vez → 1 cobrado, 3 rechazados. |
| 2 | Lo mismo con **armazones**, **cupones** (usar el mismo dos veces) y **abonos** (pasarse del saldo). | Por analogía con el #1: es el mismo patrón de código. No se demostró aparte. | Candados en armazón, cupón y orden. | Todavía no tiene prueba de simultaneidad propia (pendiente). |
| 3 | **Cobro doble por mala señal.** El servidor cobraba, la respuesta se perdía, la persona tocaba "Cobrar" otra vez. | Análisis del flujo. | Cada cobro lleva una clave única; si llega repetida, se devuelve el mismo ticket. | `RobustezTest.elMismoCobroEnviadoDosVeces…` |
| 4 | **Una foto o PDF enorme en el buzón podía tumbar todo el sistema** (se quedaba sin memoria), ventas incluidas. El buzón lo usa cualquier cliente. | Análisis: una imagen de 200 megapixeles ocupa ~800 MB al abrirse. | Las imágenes se abren reducidas, los PDF con tope de resolución, y se rechaza lo absurdo. | `ImpresoraServiceTest`, `RobustezTest.unArchivoQueDiceSerImagen…` |
| 5 | Un cliente podía **llenar el disco** mandando archivos sin parar. | Análisis. | Máximo 6 envíos por dispositivo cada 10 minutos y 40 turnos abiertos. | `RobustezTest.elBuzonNoAceptaMasDeSeis…` |
| 6 | El reporte de "por cobrar en la óptica" **solo sumaba las primeras 100 órdenes**. | Lectura del código. | Ahora suma todas. | — |
| 7 | Si una pantalla fallaba, **todo quedaba en blanco**. | Lectura del código. | Red de seguridad (`ErrorBoundary`) con mensaje y botón para continuar. | — |

## 2. Lo que se endureció aunque no se pudo demostrar que fallara

- **El guardia de acceso** ahora decide con la ruta del Controller, no con el texto de la
  dirección. Se probaron 8 direcciones "disfrazadas" (`/api/publico/../admin/reportes`, etc.)
  contra el servidor real: todas responden 404 o 401.
- **Candados siempre en el mismo orden** (por id), para que dos ventas con los mismos productos
  en distinto orden no se bloqueen entre sí.
- **Aviso al dueño** si el sistema sigue con el PIN y la contraseña de ejemplo.
- **Bitácora en archivo** (`C:\sistema-rodriguez-ammi-datos\logs\sistema.log`, 14 días) para
  investigar "ayer no dejó cobrar".

## 3. Las pruebas automáticas

```bash
cd backend  && ./mvnw test     # 33 pruebas (≈ 1 minuto)
cd frontend && npm test        # 20 pruebas (≈ 1 segundo)
```

| Archivo | Qué cuida |
|---|---|
| `VentaServiceTest` | Vender descuenta inventario, calcula total y cambio, corte del día |
| `VentasSimultaneasTest` | La última pieza solo se vende una vez |
| `OrdenServiceTest` | Promoción 2x1, cupones de un solo uso, entrega solo si está pagada, cancelación |
| `CancelacionEInactividadTest` | Cancelar regresa mercancía; el PIN se vuelve a pedir tras inactividad; borrar varios clientes |
| `RobustezTest` | Cobros repetidos, historial de inventario, límites del buzón, direcciones disfrazadas |
| `AccesoTest` | Sin PIN no se ve nada; con PIN no se ven reportes; con contraseña sí |
| `NuevasFuncionesTest` | Eliminar clientes; el Excel trae fecha y hora reales |
| `EliminarYCumpleanosTest` | Eliminar productos y armazones (borrar si nunca se vendió, archivar si ya se vendió, el código queda libre, las impresiones del buzón no se eliminan); medidas `52-18-140`; el cupón de cumpleaños vale dos días |
| `ImpresoraServiceTest` | Rangos de páginas ("1-3,5") y tope de memoria al imprimir |
| `geometriaArmazon.test.js` | El modelo 3D mide lo que dicen las medidas, en las 6 formas |
| `utilidades.test.js` | Dinero, graduaciones, fechas sin error de zona horaria, rutas |

Además, **GitHub corre todas las pruebas solo** en cada push (`.github/workflows/ci.yml`).
Una ✗ roja junto a un commit significa "esto rompió algo: no lo lleves al negocio".

### Si el editor marca archivos en rojo

El editor (VS Code, IntelliJ) guarda su propia "foto" del proyecto. Cuando se crean o cambian
archivos **desde fuera del editor** (por ejemplo con `git pull` o con una IA), esa foto se queda
vieja y marca errores que no existen: típicamente "no encuentro la clase X" en el archivo que usa
una clase nueva, y de rebote en los archivos que usan a ese.

Quien manda es Maven, no el color del editor:

```bash
cd backend && ./mvnw compile
```

Si termina sin errores, el código está bien y solo hay que refrescar el editor:

- **VS Code:** `Ctrl+Shift+P` → `Java: Clean Java Language Server Workspace` → *Reload and delete*.
- **IntelliJ:** clic derecho en `pom.xml` → *Maven → Reload project* (o *File → Invalidate Caches*).

Si `./mvnw compile` **sí** marca error, entonces es real: el mensaje dice archivo y línea.

## 4. Lo que NO está comprobado (honestidad ante todo)

- **Impresoras reales.** La impresión directa (archivos y tickets) solo se probó con impresoras
  virtuales. Falta probar con la térmica y la de documentos del negocio.
- **Respaldos automáticos.** No existen (decisión del dueño por ahora). Es el mayor riesgo que
  queda: si falla el disco, se pierde todo. Hay respaldos manuales en
  `C:\sistema-rodriguez-ammi-datos\respaldos`.
- **La red viaja sin cifrar** (HTTP) dentro del Wi-Fi del local. Aceptable en red propia; con
  HTTPS además se podría instalar como app y usar la cámara en vivo.
- **El modelo 3D es aproximado**: se dibuja con las medidas, no es un escaneo del armazón.
- **Cambios a las tablas** siguen con `ddl-auto=update`. Cuando haya datos reales conviene Flyway.
- **Celulares reales.** Se probó en el navegador simulando tamaño de celular, no en los
  teléfonos de la familia.
