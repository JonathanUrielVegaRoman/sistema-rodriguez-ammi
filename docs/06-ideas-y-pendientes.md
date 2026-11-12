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
| Clientes: borrar o archivar | Borrar a alguien con órdenes rompería el historial de ventas; archivarlo lo oculta sin perder dinero |
| `ddl-auto=update` | Cómodo para empezar; ver pendiente "Flyway" |

## ✅ Ya implementado (antes eran ideas)
- **Buzón de impresión**: QR en el mostrador → el cliente sube sus archivos sin dar su número → recibe un turno →
  el personal imprime, cobra (queda como venta de papelería) y los archivos se borran. Si nadie los recoge, se borran
  solos a las 24 horas.
- **Cumpleaños**: cada mañana se generan cupones (15 %, 30 días) para quien cumple y aceptó promociones; botón de WhatsApp.
- **Acceso**: PIN del personal + contraseña del dueño.

## Ideas para después
- **Tickets directo a la impresora** (sin el cuadro de Ctrl+P): usar el mismo mecanismo del buzón
  para mandar el ticket a una impresora térmica, o abrir Chrome con `--kiosk-printing` en la PC del mostrador.
- **Cancelar o devolver una venta de la papelería** (solo el administrador), regresando la mercancía al inventario.
- **Reimprimir tickets** buscando por folio.

## Pendientes, en orden sugerido

1. **Confirmar con el cliente** las preguntas de abajo y ajustar precios de ejemplo (tratamientos, impresiones).
2. **Respaldos automáticos**: tarea de Windows que ejecute `pg_dump` cada noche y copie el archivo (y la carpeta de
   fotos `C:/sistema-rodriguez-ammi-datos/archivos/armazones`) a una USB o a Google Drive.
   **Sin respaldos, si se descompone el disco se pierde todo.**
3. **Arranque automático**: que el sistema se encienda solo al prender la PC (tarea programada de Windows o servicio con WinSW).
4. **IP fija** de la PC en el módem (reserva DHCP). Si cambia, el QR del mostrador deja de funcionar.
5. **Red de invitados** para los clientes del buzón, si el módem lo permite. Hoy el PIN protege los datos, pero
   aislar la red es una capa extra.
6. **Impresora de tickets**: confirmar cuál tienen.
   - Térmica USB en la PC: ya funciona (imprimir desde el navegador de la PC; el ticket mide 54 mm).
   - Bluetooth: imprimir desde el celular es más complicado; se revisa después (ESC/POS).
7. **Ícono en el celular (PWA)**: "Agregar a pantalla de inicio" para que parezca app.
8. **Flyway**: cuando ya haya datos reales, manejar los cambios de tablas con migraciones versionadas.
9. **Aviso de privacidad** (LFPDPPP) impreso en el mostrador de la óptica. La graduación es un dato de salud.
10. **HTTPS en la red local** (opcional): hoy la información viaja sin cifrar dentro del Wi-Fi del local.

## Preguntas para el cliente
- ¿El lente de regalo (2x1) incluye el armazón o solo las micas? *(Hoy: micas y tratamientos gratis; el armazón se cobra con el precio que se capture, que puede ser $0.)*
- ¿Qué tratamientos son "caros" y no entran en el regalo? *(Hoy: filtro de luz azul, fotocromático y polarizado.)*
- ¿Qué porcentaje y vigencia quieren para el cupón de cumpleaños? *(Hoy: 15 % por 30 días; se cambia en `application.properties`.)*
- ¿Precios reales de impresión B/N y color? *(Hoy: $2 y $6 por hoja.)*
- ¿Quién fabrica las micas: laboratorio externo o ellos mismos? (para el tiempo de entrega estimado)
- ¿Qué impresora de tickets tienen (marca, USB o Bluetooth)?
- ¿El módem permite red de invitados?
