// Funciones pequeñas para mostrar datos bonitos. Se reutilizan en todas las pantallas.

const pesos = new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" })

/** 45 → "$45.00" */
export function dinero(valor) {
  return pesos.format(Number(valor ?? 0))
}

/** "2026-11-11T10:31:00" → "11/11/2026 10:31" */
export function fechaHora(texto) {
  return new Date(texto).toLocaleString("es-MX", { dateStyle: "short", timeStyle: "short" })
}

/** Fecha de hoy en formato "2026-11-11" (el que usan los <input type="date">). */
export function hoyISO() {
  const d = new Date()
  const mes = String(d.getMonth() + 1).padStart(2, "0")
  const dia = String(d.getDate()).padStart(2, "0")
  return `${d.getFullYear()}-${mes}-${dia}`
}

export const NOMBRES_PAGO = {
  EFECTIVO: "Efectivo",
  TARJETA: "Tarjeta",
  TRANSFERENCIA: "Transferencia",
}
