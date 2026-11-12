# Óptica RODRÍGUEZ — ejercicios para aprender

La óptica ya está completa (clientes, graduaciones, armazones con foto, órdenes, promociones).
Estos ejercicios son **cambios pequeños y reales** para practicar el patrón
Entity → Repository → DTO → Service → Controller → pantalla, sin miedo a romper nada.

> Antes de cada ejercicio: `git switch -c ejercicio-N`. Si algo sale mal: `git restore .`

## Ejercicio 1 — Ocupación del cliente (fácil, solo agregar un campo)
Agregar el campo `ocupacion` (texto, opcional) al cliente.
- Backend: `Cliente.java` (atributo + getter/setter), `ClienteDto.Guardar` y `ClienteDto.Respuesta`, `ClienteService` (crear y actualizar).
- Frontend: `vistas/optica/FormCliente.jsx` (un campo más) y `ClienteDetalle.jsx` (mostrarlo).
- ✅ Listo cuando: lo capturas en el celular y aparece en la ficha.
- 💡 Aprendes: cómo viaja un dato por TODAS las capas.

## Ejercicio 2 — Órdenes que se entregan hoy (fácil)
Endpoint `GET /api/optica/ordenes/entregas-hoy`: órdenes LISTA o EN_LABORATORIO cuya
`fechaEntregaEstimada` es hoy.
- Repository: un método con nombre tipo `findByFechaEntregaEstimadaAndEstadoIn(...)`.
- Service y Controller: copia el patrón de `listar`.
- ✅ Listo cuando: lo pruebas en Thunder Client.
- 💡 Aprendes: consultas derivadas del nombre del método.

## Ejercicio 3 — Mostrarlo en Inicio (medio)
En `vistas/Inicio.jsx`, agregar un `Pendiente` "entregas para hoy" usando el endpoint del ejercicio 2
(primero agrégalo en `services/api.js`).
- 💡 Aprendes: cómo una pantalla pide datos con `useEffect`.

## Ejercicio 4 — Prueba automática (medio)
En `OrdenServiceTest.java`, escribir una prueba: "no se puede abonar más que el saldo".
- 💡 Aprendes: el patrón Dado / Cuando / Entonces y `assertThatThrownBy`.

## Ejercicio 5 — Segunda foto del armazón (difícil)
Permitir una foto LATERAL además de la frontal (`fotoLateral` en `Armazon.java`, otro endpoint
de subida y mostrarla en la ficha).
- 💡 Aprendes: archivos, multipart y reutilizar `AlmacenArchivos`.
