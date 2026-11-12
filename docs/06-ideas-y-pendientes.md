# Ideas, pendientes y decisiones

## Decisiones tomadas (y por qué)

| Decisión | Por qué |
|---|---|
| Spring Boot + JPA, sin DAO ni servlets ni HATEOAS | El Repository ya es el DAO; lo demás es para sistemas mucho más grandes |
| PostgreSQL (relacional) | Datos con relaciones claras y necesidad de transacciones y sumas |
| Organizar por negocio/tema (`papeleria/producto`) | Todo lo de un tema junto; fácil de copiar como molde |
| React + Vite sin librerías extra (sin router, sin axios) | Menos cosas que aprender al inicio; se agregan cuando hagan falta |
| Red local, una PC como servidor | El negocio tiene una sola computadora y cada persona su celular; no hay costo de hosting |
| `ddl-auto=update` | Cómodo para empezar; ver pendiente "Flyway" |

## 🖨️ Idea: "Buzón de impresión" (reemplazo del Bluetooth)

**Problema actual:** los clientes mandan sus archivos por Bluetooth (lento, hay que emparejar).
WhatsApp no es opción: expone el número personal, los archivos se quedan en el teléfono
(confidencialidad) y genera conversaciones que hay que atender todo el día.

**Propuesta:**
1. En el mostrador hay un **código QR**. El cliente lo escanea con su celular conectado a la Wi-Fi del local.
2. Se abre una página sencilla `/imprimir`: elige su archivo, número de copias, blanco y negro o color.
3. Recibe un **turno**, por ejemplo **A-023**. No deja nombre ni teléfono.
4. En la PC aparece una **cola de impresión**: `A-023 · 3 hojas · B/N · 2 copias` → botón **Imprimir** → botón **Cobrar**, que crea la venta en AMMI.
5. Los archivos se **borran solos** al marcarlos como entregados o al terminar el día.

**Ventajas:** nadie comparte su número, no hay chats que atender, los archivos no se quedan
en ningún celular y cada impresión queda cobrada en el corte del día.

**Cuidado (seguridad):** si los clientes se conectan a la misma Wi-Fi que el sistema, el resto
del sistema **debe tener inicio de sesión** antes de abrir el buzón (ver pendiente "Login").
Mejor aún: una red de invitados o un segundo router solo para clientes. Hay que revisar qué
permite el módem del local.

## 🎂 Cumpleaños sin automatizar WhatsApp
Enviar mensajes automáticos por WhatsApp requiere la API de Meta (trámite y costo). Para empezar:
- Cada mañana el sistema genera cupones para los cumpleañeros (`@Scheduled`).
- Una pantalla "Cumpleañeros de hoy" muestra un botón por cliente que abre WhatsApp con el
  mensaje y el código ya escritos (`https://wa.me/52...?text=...`); solo hay que tocar **Enviar**.
- Solo a clientes que **aceptaron** recibir promociones (campo `aceptaPromociones`).

## Pendientes, en orden sugerido

1. **Óptica**: ejercicios 1 a 5 de `backend/.../optica/EJERCICIOS.md` y sus pantallas.
2. **Login sencillo**: 3 usuarios (mamá, papá, hija) con Spring Security. Imprescindible antes del buzón de impresión.
3. **Respaldos automáticos**: tarea de Windows que ejecute `pg_dump` cada noche y copie el archivo a una USB o a Google Drive. **Sin respaldos, si se descompone el disco duro se pierde todo.**
4. **Arranque automático**: que el sistema se encienda solo al prender la PC (tarea programada de Windows o servicio con WinSW).
5. **IP fija** de la PC en el router (reserva DHCP).
6. **Impresora de tickets**: confirmar qué impresora tienen.
   - Térmica USB conectada a la PC: imprimir desde el navegador de la PC (ya funciona, ancho de 58 mm en `app.css`).
   - Impresora Bluetooth: imprimir desde el celular es más complicado; se puede revisar más adelante (Web Bluetooth/ESC-POS).
7. **Ícono en el celular**: convertir el frontend en PWA para "Agregar a pantalla de inicio" y que parezca app.
8. **Flyway**: cuando ya haya datos reales, manejar los cambios de tablas con migraciones versionadas en vez de `ddl-auto=update`.
9. **Aviso de privacidad** (LFPDPPP): un aviso en el mostrador y una casilla "acepta promociones" al registrar clientes. La graduación es un dato de salud; conviene tratarla con cuidado.
10. **Reimpresión de tickets** y búsqueda por folio (el backend ya tiene `GET /api/papeleria/ventas/{folio}`).

## Preguntas para el cliente
- ¿En la óptica cobran **anticipo** y manejan estado del trabajo (en laboratorio / listo / entregado)?
- ¿Quién fabrica las micas: laboratorio externo o ellos mismos?
- ¿Qué tratamientos ofrecen y cuáles son los "caros" que no entran en el 2x1?
- ¿Qué impresora de tickets tienen (marca, USB o Bluetooth)?
- ¿Cobran impresiones por hoja, diferenciando blanco y negro y color?
- ¿El módem permite red de invitados?
