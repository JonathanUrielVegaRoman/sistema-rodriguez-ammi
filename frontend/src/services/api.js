/*
  TODAS las llamadas al backend pasan por aquí.

  Ventaja: si mañana cambia la dirección del backend o la forma de entrar, se cambia en UN
  solo archivo y no en cada pantalla.

  fetch() es la función nativa del navegador para hacer peticiones HTTP.
  "async/await": la petición tarda (viaja por la red), así que "esperamos" la respuesta
  sin congelar la pantalla.
*/
import { sesion, EVENTO_SIN_ACCESO } from "./sesion"

const BASE = "/api"

/** Encabezados con los pases de este dispositivo (ver backend: AccesoInterceptor.java). */
function encabezadosDeAcceso() {
  const h = {}
  if (sesion.personal()) h["X-Token-Personal"] = sesion.personal()
  if (sesion.admin()) h["X-Token-Admin"] = sesion.admin()
  return h
}

async function peticion(ruta, { metodo = "GET", cuerpo, formulario } = {}) {
  const headers = encabezadosDeAcceso()
  let body
  if (formulario) {
    body = formulario // FormData: el navegador pone solo el Content-Type correcto para archivos
  } else if (cuerpo !== undefined) {
    headers["Content-Type"] = "application/json"
    body = JSON.stringify(cuerpo)
  }

  let respuesta
  try {
    respuesta = await fetch(BASE + ruta, { method: metodo, headers, body })
  } catch {
    throw new Error("No hay conexión con el sistema. ¿Está prendida la computadora principal?")
  }

  // 401 = el pase no sirve (venció o lo cerraron). Avisamos a la app para pedir PIN o contraseña.
  if (respuesta.status === 401 && !ruta.startsWith("/publico/acceso")) {
    if (ruta.startsWith("/admin")) sesion.olvidarAdmin()
    else sesion.olvidarPersonal()
    window.dispatchEvent(new CustomEvent(EVENTO_SIN_ACCESO, { detail: ruta.startsWith("/admin") ? "admin" : "personal" }))
  }

  // 204 = "todo bien, pero no hay nada que devolver".
  if (respuesta.status === 204) return null

  const datos = await respuesta.json().catch(() => null)

  if (!respuesta.ok) {
    // El backend siempre manda errores con la forma de ErrorRespuesta.java: { mensaje, detalles }
    const mensaje = datos?.mensaje ?? "Ocurrió un error (" + respuesta.status + ")"
    const detalles = datos?.detalles?.length ? "\n" + datos.detalles.join("\n") : ""
    throw new Error(mensaje + detalles)
  }
  return datos
}

/**
  Pide un archivo protegido (con los pases) y lo devuelve como "blob" (datos binarios).
  Se usa para imágenes de vista previa, archivos del buzón y el Excel.
*/
async function obtenerBlob(ruta) {
  const respuesta = await fetch(BASE + ruta, { headers: encabezadosDeAcceso() })
  if (!respuesta.ok) {
    const datos = await respuesta.json().catch(() => null)
    throw new Error(datos?.mensaje ?? "No se pudo obtener el archivo")
  }
  return { blob: await respuesta.blob(), disposicion: respuesta.headers.get("Content-Disposition") }
}

/** Descarga un archivo protegido con su nombre (ej. el Excel de ventas). */
async function descargar(ruta, nombrePorDefecto) {
  const { blob, disposicion } = await obtenerBlob(ruta)
  const nombre = /filename="?([^";]+)"?/.exec(disposicion ?? "")?.[1] ?? nombrePorDefecto
  const url = URL.createObjectURL(blob)
  const enlace = document.createElement("a")
  enlace.href = url
  enlace.download = nombre
  document.body.appendChild(enlace)
  enlace.click()
  enlace.remove()
  setTimeout(() => URL.revokeObjectURL(url), 60_000)
}

/** Abre un archivo protegido en otra pestaña (para Word/Excel del buzón, que no se imprimen directo). */
async function abrirArchivo(ruta) {
  const { blob } = await obtenerBlob(ruta)
  const url = URL.createObjectURL(blob)
  const enlace = document.createElement("a")
  enlace.href = url
  enlace.target = "_blank"
  enlace.rel = "noopener"
  document.body.appendChild(enlace)
  enlace.click()
  enlace.remove()
  setTimeout(() => URL.revokeObjectURL(url), 60_000)
}

const consulta = (objeto) => {
  const p = new URLSearchParams()
  Object.entries(objeto).forEach(([k, v]) => {
    if (Array.isArray(v)) v.forEach((x) => p.append(k, x))
    else if (v !== undefined && v !== null && v !== "") p.append(k, v)
  })
  const texto = p.toString()
  return texto ? "?" + texto : ""
}

// ---------------- Acceso ----------------

export const accesoApi = {
  estado: () => peticion("/publico/acceso/estado"),
  personal: (clave) => peticion("/publico/acceso/personal", { metodo: "POST", cuerpo: { clave } }),
  admin: (clave) => peticion("/publico/acceso/admin", { metodo: "POST", cuerpo: { clave } }),
  salirAdmin: () => peticion("/publico/acceso/salir", { metodo: "POST" }),
  bloquear: () => peticion("/publico/acceso/bloquear", { metodo: "POST" }),
  cerrarPersonal: () => peticion("/admin/acceso/cerrar-personal", { metodo: "POST" }),
  clavesDeEjemplo: () => peticion("/admin/acceso/claves-de-ejemplo"),
  info: () => peticion("/publico/info"),
}

/** Cierra la sesión del administrador en el servidor y la olvida en este dispositivo. */
export async function cerrarSesionAdmin() {
  await accesoApi.salirAdmin().catch(() => {})
  sesion.olvidarAdmin()
}

// ---------------- Papelería ----------------

export const productosApi = {
  listar: (busqueda = "") => peticion("/papeleria/productos" + consulta({ q: busqueda })),
  porResurtir: () => peticion("/papeleria/productos/por-resurtir"),
  crear: (producto) => peticion("/papeleria/productos", { metodo: "POST", cuerpo: producto }),
  actualizar: (id, producto) => peticion(`/papeleria/productos/${id}`, { metodo: "PUT", cuerpo: producto }),
  resurtir: (id, cantidad) => peticion(`/papeleria/productos/${id}/resurtir`, { metodo: "POST", cuerpo: { cantidad } }),
  desactivar: (id) => peticion(`/papeleria/productos/${id}`, { metodo: "DELETE" }),
  movimientos: (id) => peticion(`/papeleria/productos/${id}/movimientos`),
}

export const ventasApi = {
  registrar: (venta) => peticion("/papeleria/ventas", { metodo: "POST", cuerpo: venta }),
  ticket: (folio) => peticion(`/papeleria/ventas/${folio}`),
  /** Manda el ticket a la impresora de tickets. Responde { impreso: false } si no hay una configurada. */
  imprimir: (folio) => peticion(`/papeleria/ventas/${folio}/imprimir`, { metodo: "POST" }),
}

export const impresionesApi = {
  // Público (clientes)
  subir: (formulario) => peticion("/publico/impresiones", { metodo: "POST", formulario }),
  consultar: (codigo) => peticion(`/publico/impresiones/${encodeURIComponent(codigo)}`),
  // Personal
  cola: () => peticion("/papeleria/impresiones"),
  abrirArchivo: (id, archivoId) => abrirArchivo(`/papeleria/impresiones/${id}/archivos/${archivoId}`),
  impresoras: () => peticion("/papeleria/impresiones/impresoras"),
  info: (id, archivoId) => peticion(`/papeleria/impresiones/${id}/archivos/${archivoId}/info`),
  /** Devuelve una URL temporal con la imagen de la vista previa. */
  vistaPrevia: async (id, archivoId, pagina, color) => {
    const { blob } = await obtenerBlob(`/papeleria/impresiones/${id}/archivos/${archivoId}/vista-previa` + consulta({ pagina, color }))
    return URL.createObjectURL(blob)
  },
  imprimir: (id, archivoId, opciones) => peticion(`/papeleria/impresiones/${id}/archivos/${archivoId}/imprimir`, { metodo: "POST", cuerpo: opciones }),
  impreso: (id, hojas) => peticion(`/papeleria/impresiones/${id}/impreso`, { metodo: "POST", cuerpo: { hojas } }),
  cobrar: (id, metodoPago, pagaCon, referencia) => peticion(`/papeleria/impresiones/${id}/cobrar`, { metodo: "POST", cuerpo: { metodoPago, pagaCon, referencia } }),
  cancelar: (id) => peticion(`/papeleria/impresiones/${id}/cancelar`, { metodo: "POST" }),
}

// ---------------- Óptica ----------------

export const clientesApi = {
  buscar: (q = "") => peticion("/optica/clientes" + consulta({ q })),
  obtener: (id) => peticion(`/optica/clientes/${id}`),
  crear: (c) => peticion("/optica/clientes", { metodo: "POST", cuerpo: c }),
  actualizar: (id, c) => peticion(`/optica/clientes/${id}`, { metodo: "PUT", cuerpo: c }),
  eliminar: (id) => peticion(`/optica/clientes/${id}`, { metodo: "DELETE" }),
  restaurar: (id) => peticion(`/optica/clientes/${id}/restaurar`, { metodo: "POST" }),
  eliminarVarios: (ids) => peticion("/optica/clientes/eliminar", { metodo: "POST", cuerpo: { ids } }),
  recetas: (id) => peticion(`/optica/clientes/${id}/recetas`),
  crearReceta: (id, r) => peticion(`/optica/clientes/${id}/recetas`, { metodo: "POST", cuerpo: r }),
  ordenes: (id) => peticion(`/optica/clientes/${id}/ordenes`),
}

export const armazonesApi = {
  buscar: (filtros = {}) => peticion("/optica/armazones" + consulta(filtros)),
  tallas: () => peticion("/optica/armazones/tallas"),
  crear: (a) => peticion("/optica/armazones", { metodo: "POST", cuerpo: a }),
  actualizar: (id, a) => peticion(`/optica/armazones/${id}`, { metodo: "PUT", cuerpo: a }),
  desactivar: (id) => peticion(`/optica/armazones/${id}`, { metodo: "DELETE" }),
  subirFoto: (id, archivo) => {
    const f = new FormData()
    f.append("foto", archivo, "foto.jpg")
    return peticion(`/optica/armazones/${id}/foto`, { metodo: "POST", formulario: f })
  },
}

export const ordenesApi = {
  listar: (estados = []) => peticion("/optica/ordenes" + consulta({ estado: estados })),
  obtener: (folio) => peticion(`/optica/ordenes/${folio}`),
  cotizar: (o) => peticion("/optica/ordenes/cotizar", { metodo: "POST", cuerpo: o }),
  crear: (o) => peticion("/optica/ordenes", { metodo: "POST", cuerpo: o }),
  abonar: (folio, abono) => peticion(`/optica/ordenes/${folio}/abonos`, { metodo: "POST", cuerpo: abono }),
  estado: (folio, estado) => peticion(`/optica/ordenes/${folio}/estado`, { metodo: "PUT", cuerpo: { estado } }),
  imprimir: (folio) => peticion(`/optica/ordenes/${folio}/imprimir`, { metodo: "POST" }),
}

export const tratamientosApi = {
  activos: () => peticion("/optica/tratamientos"),
  todos: () => peticion("/admin/tratamientos"),
  crear: (t) => peticion("/admin/tratamientos", { metodo: "POST", cuerpo: t }),
  actualizar: (id, t) => peticion(`/admin/tratamientos/${id}`, { metodo: "PUT", cuerpo: t }),
}

export const promocionesApi = {
  cumpleaneros: (dias = 7) => peticion("/optica/cumpleaneros" + consulta({ dias })),
  marcarEnviado: (cuponId) => peticion(`/optica/cupones/${cuponId}/enviado`, { metodo: "POST" }),
  cupones: () => peticion("/admin/cupones"),
  crearCupon: (c) => peticion("/admin/cupones", { metodo: "POST", cuerpo: c }),
}

// ---------------- Administración ----------------

export const reportesApi = {
  generar: (desde, hasta) => peticion("/admin/reportes" + consulta({ desde, hasta })),
  corte: (dia) => peticion("/admin/reportes/corte" + consulta({ dia })),
  /** Sin fechas = todo el historial. */
  excel: (desde, hasta) => descargar("/admin/reportes/excel" + consulta({ desde, hasta }), "ventas.xlsx"),
  inventarioExcel: () => descargar("/admin/reportes/inventario-excel", "inventario.xlsx"),
}

export const adminApi = {
  ventasDelDia: (dia) => peticion("/admin/ventas" + consulta({ dia })),
  cancelarVenta: (folio, motivo) => peticion(`/admin/ventas/${folio}/cancelar`, { metodo: "POST", cuerpo: { motivo } }),
  ajustes: () => peticion("/admin/ajustes"),
  guardarAjustes: (a) => peticion("/admin/ajustes", { metodo: "PUT", cuerpo: a }),
}

/**
 * Imprime un ticket: primero intenta mandarlo directo a la impresora de tickets (backend).
 * Si no hay una configurada, usa la impresión del navegador (Ctrl+P) como respaldo.
 *   await imprimirTicket(() => ventasApi.imprimir(folio))
 */
export async function imprimirTicket(mandar) {
  const r = await mandar()
  if (!r?.impreso) window.print()
  return r
}
