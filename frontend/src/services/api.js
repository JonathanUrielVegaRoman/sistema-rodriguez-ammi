/*
  TODAS las llamadas al backend pasan por aquí.

  Ventaja: si mañana cambia la dirección del backend, o hay que agregar un login,
  se cambia en UN solo archivo y no en cada pantalla.

  fetch() es la función nativa del navegador para hacer peticiones HTTP.
  "async/await": la petición tarda (viaja por la red), así que "esperamos" la respuesta
  sin congelar la pantalla.
*/

const BASE = "/api"

async function peticion(ruta, { metodo = "GET", cuerpo } = {}) {
  const respuesta = await fetch(BASE + ruta, {
    method: metodo,
    headers: cuerpo ? { "Content-Type": "application/json" } : undefined,
    body: cuerpo ? JSON.stringify(cuerpo) : undefined,
  })

  // 204 = "todo bien, pero no hay nada que devolver" (por ejemplo, al desactivar).
  if (respuesta.status === 204) return null

  const datos = await respuesta.json().catch(() => null)

  if (!respuesta.ok) {
    // El backend siempre manda errores con la forma de ErrorRespuesta.java: { mensaje, detalles }
    const mensaje = datos?.mensaje ?? "No se pudo conectar con el sistema"
    const detalles = datos?.detalles?.length ? "\n" + datos.detalles.join("\n") : ""
    throw new Error(mensaje + detalles)
  }
  return datos
}

// ---------------- Papelería ----------------

export const productosApi = {
  listar: (busqueda = "") => peticion("/papeleria/productos" + (busqueda ? "?q=" + encodeURIComponent(busqueda) : "")),
  porResurtir: () => peticion("/papeleria/productos/por-resurtir"),
  crear: (producto) => peticion("/papeleria/productos", { metodo: "POST", cuerpo: producto }),
  actualizar: (id, producto) => peticion(`/papeleria/productos/${id}`, { metodo: "PUT", cuerpo: producto }),
  resurtir: (id, cantidad) => peticion(`/papeleria/productos/${id}/resurtir`, { metodo: "POST", cuerpo: { cantidad } }),
  desactivar: (id) => peticion(`/papeleria/productos/${id}`, { metodo: "DELETE" }),
}

export const ventasApi = {
  registrar: (venta) => peticion("/papeleria/ventas", { metodo: "POST", cuerpo: venta }),
  ticket: (folio) => peticion(`/papeleria/ventas/${folio}`),
  corte: (dia) => peticion("/papeleria/ventas/corte" + (dia ? "?dia=" + dia : "")),
}
