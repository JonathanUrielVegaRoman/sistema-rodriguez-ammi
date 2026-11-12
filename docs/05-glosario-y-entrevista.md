# Glosario y preguntas de entrevista

Cada término con: **qué es**, **dónde está en este proyecto**, y cómo contestarlo en una entrevista.

## Glosario

| Término | Qué es | En este proyecto |
|---|---|---|
| **Frontend** | La parte que ve y toca el usuario | `frontend/` (React) |
| **Backend** | La lógica y los datos, en el servidor | `backend/` (Spring Boot) |
| **API REST** | Conjunto de rutas HTTP para que frontend y backend se comuniquen | `/api/papeleria/...` |
| **Endpoint** | Una ruta concreta de la API | `GET /api/papeleria/productos` |
| **JSON** | Formato de texto para intercambiar datos | Lo que viaja entre `api.js` y los Controllers |
| **Entity** | Clase que representa una tabla | `Producto.java` |
| **Repository** | Acceso a la base de datos | `ProductoRepository.java` |
| **Service** | Reglas de negocio | `VentaService.java` (descontar inventario) |
| **Controller** | Recibe peticiones HTTP | `ProductoController.java` |
| **DTO** | Objeto para transferir datos; separa la API de la base de datos | `ProductoDto.Guardar` / `.Respuesta` |
| **JPA / Hibernate** | Estándar / herramienta para mapear objetos a tablas (ORM) | Anotaciones `@Entity`, `@Column` |
| **ORM** | Object-Relational Mapping: objetos ↔ tablas | Hibernate |
| **Inyección de dependencias** | El framework crea y entrega los objetos que una clase necesita | Constructores de Services y Controllers |
| **Transacción** | Grupo de operaciones "todo o nada" | `@Transactional` en `VentaService.registrar` |
| **Validación** | Rechazar datos inválidos antes de procesarlos | `@NotBlank`, `@Positive` en los DTO |
| **Borrado lógico** | Marcar como inactivo en vez de borrar | `Producto.activo` |
| **Relación 1 a N** | Un registro se relaciona con muchos | Una `Venta` tiene muchos `DetalleVenta` |
| **Perfil (Spring)** | Configuración alternativa activable | Perfil `h2` para pruebas |
| **Variable de entorno** | Configuración fuera del código (contraseñas) | `DB_PASSWORD` |
| **Componente (React)** | Función que devuelve interfaz | `Ticket.jsx` |
| **Estado (React)** | Datos que al cambiar redibujan la pantalla | `carrito` en `Vender.jsx` |
| **Prueba unitaria / de integración** | Código que verifica código | `VentaServiceTest.java` |
| **Commit / Rama / Pull Request** | Foto del código / línea paralela / solicitud para unir | Ver `03-git-y-github.md` |

## Preguntas típicas de entrevista (con respuesta corta)

**1. ¿Cuál es la diferencia entre Controller, Service y Repository?**
El Controller atiende HTTP (entrada/salida), el Service contiene las reglas de negocio y el
Repository accede a la base de datos. Separarlos permite probar y reutilizar la lógica y
cambiar una capa sin afectar las otras.

**2. ¿Por qué usar DTOs en lugar de devolver la Entity?**
Para no exponer campos internos, evitar problemas de serialización con relaciones (ciclos,
carga perezosa) y poder cambiar la base de datos sin romper el contrato de la API.

**3. ¿Qué hace `@Transactional`?**
Agrupa operaciones en una transacción: si ocurre una excepción no controlada, se hace
rollback de todo. Ejemplo: registrar una venta y descontar inventario.

**4. ¿Qué es la inyección de dependencias y por qué por constructor?**
El contenedor de Spring crea los objetos y los pasa a quien los necesita. Por constructor
porque las dependencias quedan obligatorias, inmutables (`final`) y es fácil probar.

**5. ¿Por qué `BigDecimal` para dinero?**
Porque `double` usa binario y no representa exactamente decimales como 0.1; con dinero eso
genera errores de centavos.

**6. ¿Qué es el problema N+1 y cómo se evita?**
Cuando se carga una lista y luego se hace una consulta extra por cada elemento para traer sus
relaciones. Se evita con `JOIN FETCH` o `@EntityGraph` (ver `VentaRepository`).

**7. Diferencia entre PUT y POST.**
POST crea un recurso nuevo (no idempotente); PUT reemplaza uno existente en una ruta conocida
(idempotente: repetirlo deja el mismo resultado).

**8. ¿Qué códigos HTTP usas y cuándo?**
200 OK, 201 Created, 204 No Content, 400 datos inválidos, 404 no existe, 409 conflicto con
una regla de negocio, 500 error del servidor. Centralizados en `@RestControllerAdvice`.

**9. ¿Relacional o NoSQL para este sistema?**
Relacional: hay relaciones claras (cliente–recetas, venta–renglones), necesitamos
transacciones y consultas de totales. NoSQL conviene más con datos sin esquema fijo o a muy
gran escala.

**10. ¿Por qué no guardas las fotos dentro de la base de datos?**
Hacen pesada la base y sus respaldos; se guardan en disco y la base solo guarda la ruta.

**11. ¿Cómo manejas la configuración sensible?**
Variables de entorno y archivos excluidos por `.gitignore`; nunca contraseñas en el repositorio.

**12. Cuéntame de un proyecto.**
Usa esta estructura (STAR): *Situación* (dos negocios familiares sin sistema), *Tarea*
(control de ventas e inventario, clientes de óptica), *Acción* (Spring Boot + PostgreSQL +
React en red local, por módulos), *Resultado* (corte del día automático, aviso de resurtido).
