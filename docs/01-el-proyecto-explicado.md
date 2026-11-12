# El proyecto explicado (para quien empieza)

> Objetivo de este documento: que puedas **dibujar en una hoja** cómo funciona el sistema,
> aunque todavía no sepas escribir el código. Ese dibujo es tu plano de arquitecto.

---

## 1. La gran idea: un restaurante

Todo sistema web se parece a un restaurante. Memoriza esta tabla; la vas a usar siempre.

| En el restaurante | En el sistema | Archivo de ejemplo |
|---|---|---|
| El **comedor** y la **carta** (lo que ve el cliente) | **Frontend** (React) | `frontend/src/vistas/papeleria/Vender.jsx` |
| La **comanda** que se llena | **DTO de entrada** | `ProductoDto.Guardar` |
| El **mesero**: toma el pedido, lo lleva a cocina, regresa con el plato | **Controller** | `ProductoController.java` |
| El **cocinero**: sabe las recetas y las reglas ("no hay pescado, no lo sirvas") | **Service** | `ProductoService.java` |
| El **almacenista**: el único que entra a la bodega | **Repository** | `ProductoRepository.java` |
| La **ficha** de cada ingrediente en el inventario | **Entity** | `Producto.java` |
| La **bodega** | **Base de datos** (PostgreSQL) | tabla `productos` |
| El **platillo** servido (solo lo que el cliente debe ver) | **DTO de salida** | `ProductoDto.Respuesta` |
| El **gerente** que atiende cualquier queja | **Manejador global de errores** | `ManejadorGlobalErrores.java` |

**Regla de oro:** cada quien hace SOLO su trabajo. El mesero no cocina; el cocinero no
atiende mesas; nadie más que el almacenista entra a la bodega.

---

## 2. El viaje de una petición: "cobrar 2 cuadernos"

```
 📱 Celular (Vender.jsx)
   │  El usuario toca "Cobrar $90.00"
   │
   │  POST /api/papeleria/ventas
   │  { "metodoPago": "EFECTIVO", "pagaCon": 100,
   │    "renglones": [ { "productoId": 1, "cantidad": 2 } ] }
   ▼
 🧑‍🍳 VentaController.registrar()        ← MESERO: recibe y valida el formato (@Valid)
   ▼
 👨‍🍳 VentaService.registrar()            ← COCINERO: aplica reglas
   │   • ¿existe el producto 1?               (si no → 404)
   │   • ¿hay al menos 2 piezas?             (si no → 409 "No hay suficiente existencia")
   │   • descuenta 2 piezas del inventario
   │   • arma el ticket y calcula total y cambio
   ▼
 📦 ProductoRepository / VentaRepository  ← ALMACENISTA: lee y guarda
   ▼
 🗄️ PostgreSQL                            ← BODEGA: tablas productos, ventas, detalle_venta
   │
   ▲  (el camino de regreso)
   │
 📱 Recibe el TICKET en JSON → lo dibuja → botón "Imprimir"
```

Ejercicio: abre los archivos en ese orden y sigue el recorrido con el dedo. Es la mejor
forma de "leer" un proyecto.

---

## 3. ¿Qué es cada cosa? (versión corta)

**HTTP / API REST.** El frontend y el backend se hablan con "mensajes" HTTP. Cada mensaje
tiene un **verbo** y una **ruta**:

| Verbo | Significa | Ejemplo |
|---|---|---|
| GET | Dame / leer | `GET /api/papeleria/productos` |
| POST | Crea algo nuevo | `POST /api/papeleria/ventas` |
| PUT | Reemplaza / edita | `PUT /api/papeleria/productos/3` |
| DELETE | Borra | `DELETE /api/papeleria/productos/3` |

**JSON.** El "idioma" de los datos que viajan. Es texto con llaves:
`{ "nombre": "Cuaderno", "precio": 45 }`.

**Anotaciones (`@Algo`).** Etiquetas que le dan instrucciones a Spring:
`@Entity` "esto es una tabla", `@Service` "esto tiene reglas de negocio",
`@GetMapping` "este método responde a un GET".

**Inyección de dependencias.** En vez de que tú hagas `new ProductoService()`, lo pides en
el constructor y Spring te lo entrega. Es como pedir "necesito un cocinero" y que el
restaurante te asigne uno ya contratado.

**Transacción (`@Transactional`).** "Todo o nada". Si al cobrar 3 productos el tercero no
alcanza, se deshace todo: ni ticket ni inventario descontado.

---

## 4. ¿Por qué esta estructura y no otra?

Organizamos por **negocio** (papelería, óptica) y dentro por **tema** (producto, venta):

```
papeleria/producto/   ← TODO lo de productos está junto
  Producto.java              (Entity)
  ProductoRepository.java    (Repository)
  ProductoDto.java           (DTOs)
  ProductoService.java       (Service)
  ProductoController.java    (Controller)
```

La alternativa (todas las entities en una carpeta, todos los controllers en otra) obliga a
saltar entre 5 carpetas para entender una sola cosa. Así, para hacer "Clientes" basta con
copiar la carpeta `producto/` y adaptarla.

**¿Y los DAO, servlets y assemblers?**
- **Servlets**: Spring Boot los usa por dentro; nunca los escribimos.
- **DAO**: el `Repository` de Spring Data **es** el DAO.
- **Assemblers / HATEOAS**: sirven para APIs públicas muy grandes. Aquí no.

Menos piezas = menos cosas que entender = menos errores.

---

## 5. El frontend en 3 ideas

1. **Componente**: una función de JavaScript que devuelve lo que se ve (JSX, parecido a HTML).
2. **Estado** (`useState`): datos que, al cambiar, redibujan la pantalla.
   Ejemplo: el carrito en `Vender.jsx`.
3. **Efecto** (`useEffect`): "cuando aparezca la pantalla, pide datos al backend".

Todas las llamadas al backend están en `frontend/src/services/api.js`. Las pantallas
nunca hablan con el backend directamente; le piden a `api.js`.

---

## 6. Ruta de aprendizaje sugerida

| Semana | Qué hacer | Cómo saber que lo lograste |
|---|---|---|
| 1 | Instalar todo, correr el sistema, vender algo, crear una orden de lentes, ver el reporte | Puedes explicar con el dibujo del punto 2 qué pasó |
| 2 | Git básico: clonar, rama, commit, push, pull request | Subiste un cambio pequeño (por ejemplo, el texto del ticket) |
| 3 | Leer `papeleria/producto` completo, con comentarios | Puedes decir qué hace cada archivo sin mirarlo |
| 4 | Leer `optica/cliente` y compararlo con `papeleria/producto` (mismo patrón) | Puedes señalar qué cambia y qué se repite |
| 5 | **Ejercicio 1** de `optica/EJERCICIOS.md` con ayuda de IA | El campo nuevo aparece en el celular |
| 6–7 | JavaScript básico + ejercicios 2 y 3 | Ves tu cambio en la pantalla de Inicio |
| 8+ | Ejercicios 4 y 5, y pendientes de `06-ideas-y-pendientes.md` | Lo usa la familia de verdad |

Para practicar la API sin frontend usa la extensión **Thunder Client** de VS Code
(o Postman): ahí escribes `GET http://localhost:8080/api/papeleria/productos` y ves el JSON.
