import { useEffect, useState } from "react"

/*
  NAVEGACIÓN con la parte "#" de la dirección. Ejemplos:
    http://192.168.1.50:8080/#/optica/clientes/3
    http://192.168.1.50:8080/#/optica/ordenes/nueva?cliente=3

  ¿Por qué con "#"? Porque cada cambio de pantalla queda en el HISTORIAL del navegador:
  así funcionan los botones ← → del sistema, el botón "atrás" del celular y se puede
  guardar una pantalla en favoritos. Y el servidor no necesita configuración extra.

  Uso:
    const { ruta, consulta } = useRuta()   // ruta = "/optica/clientes/3", consulta = { cliente: "3" }
    ir("/optica/armazones")                // cambiar de pantalla
*/

function leer() {
  const completo = window.location.hash.replace(/^#/, "") || "/"
  const [ruta, query = ""] = completo.split("?")
  return { ruta, consulta: Object.fromEntries(new URLSearchParams(query)) }
}

export default function useRuta() {
  const [estado, setEstado] = useState(leer)

  useEffect(() => {
    const alCambiar = () => {
      setEstado(leer())
      window.scrollTo(0, 0)
    }
    window.addEventListener("hashchange", alCambiar)
    return () => window.removeEventListener("hashchange", alCambiar)
  }, [])

  return estado
}

export function ir(ruta) {
  window.location.hash = ruta
}

/**
 * Compara una ruta con un patrón. Las partes con ":" son variables.
 *   coincide("/optica/clientes/:id", "/optica/clientes/7") → { id: "7" }
 *   coincide("/optica/clientes/:id", "/papeleria/vender")  → null
 */
export function coincide(patron, ruta) {
  const a = patron.split("/")
  const b = ruta.split("/")
  if (a.length !== b.length) return null
  const parametros = {}
  for (let i = 0; i < a.length; i++) {
    if (a[i].startsWith(":")) parametros[a[i].slice(1)] = decodeURIComponent(b[i])
    else if (a[i] !== b[i]) return null
  }
  return parametros
}
