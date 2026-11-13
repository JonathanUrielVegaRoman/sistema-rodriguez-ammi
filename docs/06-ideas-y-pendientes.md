# Ideas, pendientes y decisiones

## Decisiones tomadas (y por qué)

| Decisión | Por qué |
|---|---|
| Spring Boot + JPA, sin DAO ni servlets ni HATEOAS | El Repository ya es el DAO; lo demás es para sistemas mucho más grandes |
| PostgreSQL (relacional) | Datos con relaciones claras y necesidad de transacciones y sumas |
| Organizar por negocio/tema (`papeleria/producto`) | Todo lo de un tema junto; fácil de copiar como molde |
| React + Vite sin librerías extra (solo `qrcode`) | Menos cosas que aprender; la navegación se hizo con un hook de 40 líneas (`useRuta`) |
| Red local, una PC como servidor | El negocio tiene una sola computadora y cada persona su celular; no hay costo de hosting |
| **PIN por celular** en vez de usuario y contraseña | Son familia: no tiene caso pedir contraseña a cada rato. Pero los clientes usan la misma Wi-Fi para imprimir, así que hace falta separar "personal" de "clientes" |
| **Contraseña aparte para reportes** | Pedido del dueño: las ventas del día/semana/mes son privadas |
| Acceso propio (interceptor) en vez de Spring Security | Para 2 niveles basta y se entiende leyéndolo. Si algún día hay usuarios con nombre o acceso desde internet, migrar a Spring Security |
| Emojis para la papelería, fotos para los armazones | Fotografiar cada lápiz no vale la pena; en lentes la foto sí distingue modelos parecidos |
| Armazones en **cuadrícula**; clientes en **lista** | Un armazón se reconoce por cómo se ve; un cliente se busca por nombre o celular |
| Fotos reducidas en el celular antes de subirlas | Una foto de 5 MB pasa a ~200 KB: la cuadrícula carga rápido aunque haya cientos |
| WhatsApp con enlace prellenado (no automático) | La API de WhatsApp Business requiere trámite con Meta y cuesta |
| Óptica reporta dinero que ENTRÓ (anticipos + liquidaciones) | Es lo que coincide con la caja de cada día |
| Impresión directa desde el backend (Java + PDFBox) | El backend corre en la misma PC que tiene la impresora: así se imprime desde el celular, sin abrir pestañas ni Ctrl+P. Solo PDF e imágenes; Word/Excel necesitan Office |
| Excel con Apache POI | Fechas, horas y montos como valores reales de Excel (se pueden filtrar y sumar) |
| Sesión de admin en `sessionStorage` + cierre al salir de #/admin | Pedido del dueño: si se sale de Administración, aunque sea por error, ya no se ven las ventas |
| Bloqueo por inactividad (10 min por defecto) en vez de pedir el PIN siempre | Pedirlo siempre estorba al atender; 10 minutos protege un celular olvidado sin interrumpir. Se aplica en la pantalla y también en el servidor |
| Cancelar ventas solo como administrador | Cancelar "desaparece" dinero del reporte; si cualquiera pudiera, el corte dejaría de ser confiable |
| Tickets directo a la impresora desde el backend, con respaldo a Ctrl+P | Funciona desde el celular; si no hay impresora de tickets configurada, sigue funcionando como antes |
| Clientes: borrar o archivar | Borrar a alguien con órdenes rompería el historial de ventas; archivarlo lo oculta sin perder dinero |
| Productos y armazones: **eliminar o archivar**, la misma regla que con clientes | Si nunca se vendió, se borra de verdad. Si ya aparece en un ticket o una orden, solo se oculta (y se libera su código) para no romper el historial ni los reportes |
| Las "Impresión por hoja" (código `SRV-…`) no se pueden eliminar | El buzón de impresión las usa para cobrar; sin ellas dejaría de funcionar |
| Cupón de cumpleaños válido **2 días** (el cumpleaños y el siguiente) | Pedido del dueño. Como el plazo es corto, la pantalla muestra también a quien cumplió ayer y aún no se felicitó |
| Medidas del armazón escritas `52-18-140` | El símbolo □ que traen grabado los armazones se veía en pantalla como un carácter roto |
| `ddl-auto=update` | Cómodo para empezar; ver pendiente "Flyway" |

## ✅ Ya implementado (antes eran ideas)
- **Buzón de impresión**: QR en el mostrador → el cliente sube sus archivos sin dar su número → recibe un turno →
  el personal imprime, cobra (queda como venta de papelería) y los archivos se borran. Si nadie los recoge, se borran
  solos a las 24 horas.
- **Cumpleaños**: cada mañana se generan cupones (15 %, válidos el día del cumpleaños y el siguiente) para quien cumple y aceptó promociones; botón de WhatsApp.
- **Acceso**: PIN del personal + contraseña del dueño.

## ✅ Implementado en esta ronda
- Tickets y notas directo a la impresora de tickets (Ajustes → Generales).
- Cancelar ventas de la papelería (Administración → Tickets), con motivo y regreso al inventario.
- Reimprimir tickets por folio (Vender) y desde Administración.
- Borrar varios clientes a la vez.
- Bloqueo por inactividad.

## ✅ Implementado después
- **Eliminar productos** del inventario (Inventario → Editar → Eliminar producto).
- **Eliminar armazones** (ficha del armazón → Eliminar armazón); si no tiene órdenes, se borra también su foto.
- Cupón de cumpleaños válido **dos días**, con la sección "Ayer · su cupón vence hoy".
- Medidas del armazón como `52-18-140 (lente - puente - varilla, en mm)`.

## Modelo 3D de armazones: qué es y qué sigue
Lo que hay: un modelo **paramétrico** (dibujado con medidas, forma y color) para ver tamaño y
proporciones y comparar tallas. No es un escaneo del armazón real.
Lo que se podría hacer después, de menor a mayor esfuerzo:
1. **Probador virtual**: ver el armazón sobre la cara del cliente con la cámara. Requiere HTTPS
   (los navegadores no dan acceso a la cámara en vivo sin él) y una librería de detección de rostro.
2. **Modelos escaneados** de cada armazón (fotogrametría): muy fiel, pero hay que escanear pieza
   por pieza; solo vale la pena para los modelos más vendidos.

## Pagos con transferencia y terminal
Hoy el sistema **registra el método** (Efectivo, Tarjeta, Transferencia) en cada venta y cada abono, y los
reportes y el Excel los separan. **No verifica** que el dinero haya llegado: eso se revisa en la app del banco
o en la terminal. Opciones para crecer, de menor a mayor esfuerzo:
1. ✅ Guardar una **referencia** en cada pago (clave de rastreo SPEI o número de autorización del voucher). **Ya implementado.**
2. Pantalla de **conciliación**: transferencias del día con casilla "confirmada en el banco".
3. Terminal con **API** (Mercado Pago Point, Clip, etc.): cobrar desde el sistema y recibir la confirmación sola.

## Pendientes, en orden sugerido

1. **Confirmar con el cliente** las preguntas de abajo y ajustar precios de ejemplo (tratamientos, impresiones).
2. **Respaldos automáticos**: tarea de Windows que ejecute `pg_dump` cada noche y copie el archivo (y la carpeta de
   fotos `C:/sistema-rodriguez-ammi-datos/archivos/armazones`) a una USB o a Google Drive.
   **Sin respaldos, si se descompone el disco se pierde todo.**
3. **Arranque automático**: que el sistema se encienda solo al prender la PC (tarea programada de Windows o servicio con WinSW).
4. **IP fija** de la PC en el módem (reserva DHCP). Si cambia, el QR del mostrador deja de funcionar.
5. **Red de invitados** para los clientes del buzón, si el módem lo permite. Hoy el PIN protege los datos, pero
   aislar la red es una capa extra.
6. **Impresora de tickets**: comprarla (o confirmar cuál tienen) y hacer la primera prueba real.
   - Lo que el sistema necesita: **térmica, USB, con driver para Windows**, conectada a la PC del negocio.
     Bluetooth no hace falta: el ticket se manda desde el celular al sistema y el sistema lo imprime en la PC.
   - Sugerencias (precios de noviembre de 2026, cambian seguido; revisar reputación del vendedor):
     - **Recomendada:** Xprinter 80 mm USB con autocorte, en Mercado Libre (≈ $850 a $1,500).
     - Económica: 3nStar RPT001 de 58 mm USB, en Mercado Libre (≈ $750 a $1,200); el ticket se arranca a mano.
     - Duradera: Epson TM-T20III / TM-T20IV de 80 mm, en Amazon México (≈ $3,100 a $3,700).
   - Rollos **térmicos** del mismo ancho que la impresora (58 u 80 mm). No usan tinta.
   - Al instalarla: Administración → Ajustes → Generales → elegir la impresora y el ancho del papel.
7. **Ícono en el celular (PWA)**: "Agregar a pantalla de inicio" para que parezca app.
8. **Flyway**: cuando ya haya datos reales, manejar los cambios de tablas con migraciones versionadas.
9. **Aviso de privacidad** (LFPDPPP) impreso en el mostrador de la óptica. La graduación es un dato de salud.
10. **HTTPS en la red local** (opcional): hoy la información viaja sin cifrar dentro del Wi-Fi del local.

## Preguntas para el cliente
- ¿El lente de regalo (2x1) incluye el armazón o solo las micas? *(Hoy: micas y tratamientos gratis; el armazón se cobra con el precio que se capture, que puede ser $0.)*
- ¿Qué tratamientos son "caros" y no entran en el regalo? *(Hoy: filtro de luz azul, fotocromático y polarizado.)*
- ¿Qué porcentaje quieren para el cupón de cumpleaños? *(Hoy: 15 %, válido 2 días; ambos se cambian en `application.properties`: `app.promociones.cumpleanos-porcentaje` y `app.promociones.cumpleanos-dias-validez`.)*
- ¿Precios reales de impresión B/N y color? *(Hoy: $2 y $6 por hoja.)*
- ¿Quién fabrica las micas: laboratorio externo o ellos mismos? (para el tiempo de entrega estimado)
- ¿Qué impresora de tickets tienen (marca, USB o Bluetooth)?
- ¿El módem permite red de invitados?
