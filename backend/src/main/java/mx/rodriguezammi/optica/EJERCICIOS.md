# Óptica RODRÍGUEZ — ejercicios para completar

La papelería ya está completa y sirve de **molde**. La óptica tiene las tablas
(Entities) y los Repositories; falta lo demás. Cada ejercicio se hace copiando el
patrón de `papeleria/producto/` y adaptándolo.

> Tip: pídele a la IA *"Siguiendo exactamente el patrón de ProductoDto, ProductoService y
> ProductoController, crea ClienteDto, ClienteService y ClienteController para la entidad
> Cliente"*. Luego **lee** lo que generó y compáralo con Producto, línea por línea.

## Ejercicio 1 — Clientes (fácil)
- `ClienteDto` con `Guardar` (nombre obligatorio, celular de 10 dígitos con `@Pattern(regexp = "\\d{10}")`) y `Respuesta`.
- `ClienteService`: listar/buscar por nombre, obtener, crear, actualizar.
- `ClienteController` en `/api/optica/clientes`.
- ✅ Listo cuando: puedes crear un cliente desde Postman/Thunder Client y verlo en la lista.

## Ejercicio 2 — Recetas (medio)
- Rutas anidadas: `GET /api/optica/clientes/{id}/recetas` y `POST /api/optica/clientes/{id}/recetas`.
- Validar que el eje esté entre 0 y 180 (`@Min(0) @Max(180)`).
- ✅ Listo cuando: un cliente puede tener varias recetas y se listan de la más nueva a la más vieja.

## Ejercicio 3 — Armazones con foto (medio)
- CRUD normal en `/api/optica/armazones`.
- Subir foto: `POST /api/optica/armazones/{id}/foto` recibiendo `@RequestParam("foto") MultipartFile foto`.
  Guardar el archivo en la carpeta `app.archivos.carpeta` (ver application.properties) con un nombre
  único (por ejemplo, `armazon-15.jpg`) y anotar ese nombre en `fotoArchivo`.
- Servir la foto: `GET /api/optica/armazones/{id}/foto`.
- ✅ Listo cuando: desde el celular tomas la foto y aparece en la lista.

## Ejercicio 4 — Orden de venta de lentes (difícil)
Nueva entidad `OrdenLentes` (o `VentaOptica`): cliente, receta, armazón, tipo de mica
(material), tratamientos, precio, descuento, anticipo, saldo y **estado**
(`EN_LABORATORIO`, `LISTO`, `ENTREGADO`). Usa un `enum` como en `MetodoPago`.
Antes de programarla, **confirmar con el cliente** cómo trabajan hoy (¿cobran anticipo? ¿quién hace las micas?).

## Ejercicio 5 — Cumpleaños y cupones (difícil)
- Entidad `Cupon`: código único, cliente, tipo (`CUMPLEANOS`, `DOS_POR_UNO`, `DESCUENTO`), porcentaje, vigencia, usado.
- Tarea automática diaria:
  ```java
  @Scheduled(cron = "0 0 8 * * *", zone = "America/Mexico_City") // todos los días a las 8:00
  public void generarCuponesDeCumpleanos() { ... clienteRepository.cumpleanerosDel(mes, dia) ... }
  ```
- Endpoint `GET /api/optica/cumpleaneros-hoy` que devuelva cada cliente con su cupón y un enlace
  `https://wa.me/52XXXXXXXXXX?text=...` (mensaje ya escrito). En pantalla, un botón por cliente que
  abre WhatsApp; la persona solo presiona "Enviar".
