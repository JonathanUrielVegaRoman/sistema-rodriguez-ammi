// Funciones pequeñas para mostrar datos bonitos. Se reutilizan en todas las pantallas.

const pesos = new Intl.NumberFormat("es-MX", { style: "currency", currency: "MXN" })

/** 45 → "$45.00" */
export function dinero(valor) {
  return pesos.format(Number(valor ?? 0))
}

/** "2026-11-11T10:31:00" → "11/11/26, 10:31" */
export function fechaHora(texto) {
  return new Date(texto).toLocaleString("es-MX", { dateStyle: "short", timeStyle: "short" })
}

/** "2026-11-11" → "11 nov 2026". Se arma a mano para evitar que la zona horaria cambie el día. */
export function fechaCorta(texto) {
  if (!texto) return ""
  const [a, m, d] = texto.slice(0, 10).split("-").map(Number)
  return new Date(a, m - 1, d).toLocaleDateString("es-MX", { day: "numeric", month: "short", year: "numeric" })
}

/** Fecha en formato "2026-11-11" (el que usan los <input type="date">). */
export function iso(d) {
  const mes = String(d.getMonth() + 1).padStart(2, "0")
  const dia = String(d.getDate()).padStart(2, "0")
  return `${d.getFullYear()}-${mes}-${dia}`
}

export function hoyISO() {
  return iso(new Date())
}

/** Graduación con signo: -2.25 → "−2.25", 1.5 → "+1.50", null → "—" */
export function dioptria(valor) {
  if (valor === null || valor === undefined || valor === "") return "—"
  const n = Number(valor)
  return (n > 0 ? "+" : n < 0 ? "−" : "") + Math.abs(n).toFixed(2)
}

export const NOMBRES_PAGO = {
  EFECTIVO: "Efectivo",
  TARJETA: "Tarjeta",
  TRANSFERENCIA: "Transferencia",
}

export const NOMBRES_ESTADO_ORDEN = {
  EN_LABORATORIO: "En laboratorio",
  LISTA: "Lista para entregar",
  ENTREGADA: "Entregada",
  CANCELADA: "Cancelada",
}

export const NOMBRES_MICA = {
  MONOFOCAL: "Monofocal",
  BIFOCAL: "Bifocal",
  PROGRESIVO: "Progresivo",
  SOLO_ARMAZON: "Solo armazón",
}

/** Enlace de WhatsApp con mensaje ya escrito (el número se escribe con lada de México). */
export function enlaceWhatsapp(celular, mensaje) {
  return `https://wa.me/52${celular}?text=${encodeURIComponent(mensaje)}`
}
