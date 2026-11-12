# Cómo se usa (guía para la familia)

> Esta guía es para quien atiende el mostrador. Se puede imprimir y dejar junto a la computadora.

## Primera vez en cada celular
1. Conectarse a la Wi-Fi del local.
2. Abrir Chrome y escribir la dirección de la computadora (por ejemplo `http://192.168.1.50:8080`).
3. Escribir el **PIN del personal**. Listo: ese celular ya no lo vuelve a pedir.
4. (Recomendado) Menú de Chrome ⋮ → **Agregar a pantalla de inicio**, para abrirlo como una app.

## Cómo moverse
```
 [←] [→]  🏠   [ Papelería | Óptica ]   ☀ 🖥 🌙   🔒
            │          │                   │       └─ Administración (solo el dueño)
            │          │                   └─ Tema: claro / como el celular / oscuro
            │          └─ Cambiar de negocio sin pasar por el inicio
            └─ Inicio: lo pendiente de hoy en los dos negocios
```
- **← →** regresan y avanzan entre pantallas (también funciona el botón "atrás" del celular).
- Debajo aparecen las secciones del negocio en el que estás.

## Papelería AMMI
| Para… | Ve a… | Pasos |
|---|---|---|
| Vender | **Vender** | Tocar productos → ajustar cantidades → método de pago → **Cobrar** → **Imprimir ticket** |
| Dar de alta mercancía | **Inventario → + Producto** | Nombre (el ícono se sugiere solo), precio, existencia y "avisar cuando queden" |
| Cuando llega mercancía | **Inventario → Resurtir** | Escribir cuántas piezas llegaron |
| Imprimir archivos de un cliente | **Impresiones** | Tocar el archivo → ver la **vista previa** y elegir opciones (copias, color, papel, orientación, páginas) → **Imprimir** (sale directo en la impresora de la PC, también desde el celular) → las hojas se anotan solas → **Ya lo imprimí** → **Cobrar y entregar**. Word y Excel se abren para imprimirlos con su programa |
| Poner el QR en el mostrador | **Impresiones → Cartel con QR** | Imprimir y pegar a la vista |

## Óptica RODRÍGUEZ
| Para… | Ve a… | Pasos |
|---|---|---|
| Cliente nuevo | **Clientes → + Cliente** | Nombre, celular, cumpleaños y **preguntarle** si acepta felicitación por WhatsApp |
| Borrar un cliente | Ficha → **Editar** → **Eliminar/Archivar** | Sin órdenes se borra; con órdenes se **archiva** (sus ventas se conservan) y se puede restaurar |
| Capturar graduación | Ficha del cliente → **+ Nueva graduación** | OD = ojo derecho, OI = ojo izquierdo |
| Armazón nuevo | **Armazones → + Armazón** | Código, medidas de la varilla, precio → **Tomar foto** (fondo blanco, de frente) |
| Encontrar un armazón | **Armazones** | Buscar por código o tocar la **talla** (ej. 52 mm) para ver solo esa medida |
| Vender lentes | Ficha del cliente → **Nueva orden** | Graduación → elegir armazón → mica y tratamientos → cupón/descuento → anticipo → **Crear orden** |
| Promoción 2x1 | En la orden → **+ Par de regalo (2x1)** | Micas gratis; los tratamientos caros aparecen tachados |
| Llegaron los lentes | **Órdenes → En laboratorio** → la orden | **Marcar como lista** → **Avisar por WhatsApp** |
| Entregar | La orden | Registrar el abono del saldo → **Entregar al cliente** |
| Felicitar cumpleaños | **Cumpleaños** | Botón **WhatsApp** (el mensaje y el código ya van escritos) → Enviar → **Ya lo envié** |

## Solo el dueño (🔒)
- **Reportes**: hoy, ayer, semana, mes, mes pasado o fechas a elegir. Cuánto entró por negocio y por método de pago, lo más vendido y lo que falta por cobrar en la óptica.
- **Excel**: botón *Excel del periodo* o *Excel de todo el historial*. Trae cada venta con **fecha y hora**, los pagos de la óptica y las órdenes, listo para filtrar y sumar.
- **Ajustes**: precios de tratamientos y cuáles entran en el 2x1, cupones de descuento especial, y **cerrar sesión en todos los celulares** si se pierde uno.
- La sesión de administrador **se cierra sola** en cuanto se sale de Administración (con ←, con el menú o por error) o al cerrar la pestaña. Para volver a ver las ventas se pide la contraseña otra vez.

---

# Decisiones de diseño (por qué se ve así)

**¿Cuadrícula para la óptica?** Sí, **para los armazones** y las órdenes: la foto es lo que distingue
un modelo de otro más rápido que leer un código. Pero **no para los clientes**: a una persona se le
busca escribiendo su nombre o celular, y una lista con buscador es más rápida que una cuadrícula de
iniciales. Para que la cuadrícula funcione bien:
- Fotos **parejas**: mismo fondo blanco, armazón de frente, misma distancia.
- La **talla** (52□18 140) va encima de la foto, porque hay modelos iguales en distinto tamaño.
- En celular se muestran **2 por renglón**; en computadora, los que quepan.

**¿Emojis o imágenes en la papelería?** Emojis: no hay que tomar fotos, no tardan en cargar y se
reconocen al instante. Las imágenes se reservaron para los lentes, donde sí hacen la diferencia.

**¿Por qué PIN si son familia?** No es por desconfianza entre ustedes: es porque los **clientes** se
conectan a la misma Wi-Fi para el buzón de impresión. Sin PIN, cualquier cliente podría abrir la lista
de clientes de la óptica con sus celulares y graduaciones. El PIN se escribe **una sola vez** por celular.

**¿Por qué los reportes tienen otra contraseña?** Porque así lo pidió el dueño: el día a día lo
manejan todos, pero cuánto se vende es información privada.
