import { describe, expect, it } from "vitest"
import { dinero, dioptria, enlaceWhatsapp, fechaCorta, iso } from "./formato"
import { sugerirEmoji } from "./emojis"
import { nuevaClave } from "./clave"
import { coincide } from "../hooks/useRuta"

describe("formato", () => {
  it("el dinero siempre sale con signo de pesos y dos decimales", () => {
    expect(dinero(45)).toBe("$45.00")
    expect(dinero("1234.5")).toBe("$1,234.50")
    expect(dinero(null)).toBe("$0.00")
  })

  it("las graduaciones llevan signo y dos decimales, como en una receta", () => {
    expect(dioptria(-2.25)).toBe("−2.25")
    expect(dioptria(1.5)).toBe("+1.50")
    expect(dioptria(0)).toBe("0.00")
    expect(dioptria(null)).toBe("—")
  })

  it("las fechas no cambian de día por la zona horaria", () => {
    // Error clásico: new Date("2026-11-01") se interpreta en UTC y en México se muestra como 31 de octubre.
    expect(fechaCorta("2026-11-01")).toMatch(/^1 nov/)
    expect(iso(new Date(2026, 0, 5))).toBe("2026-01-05")
  })

  it("el enlace de WhatsApp lleva la lada de México y el mensaje codificado", () => {
    const enlace = enlaceWhatsapp("5512345678", "¡Hola! Tus lentes están listos")
    expect(enlace.startsWith("https://wa.me/525512345678?text=")).toBe(true)
    expect(enlace).not.toContain(" ")
  })
})

describe("íconos de la papelería", () => {
  it("sugiere un emoji según el nombre, sin importar mayúsculas", () => {
    expect(sugerirEmoji("Cuaderno profesional")).toBe("📓")
    expect(sugerirEmoji("LÁPIZ del 2")).toBe("✏️")
    expect(sugerirEmoji("Algo que no conoce")).toBeNull()
  })
})

describe("clave única de cada cobro", () => {
  it("son 32 caracteres y no se repiten", () => {
    const claves = new Set(Array.from({ length: 500 }, nuevaClave))
    expect(claves.size).toBe(500)
    expect([...claves][0]).toMatch(/^[0-9a-f]{32}$/)
  })
})

describe("rutas de la aplicación", () => {
  it("reconoce las partes variables de una dirección", () => {
    expect(coincide("/optica/clientes/:id", "/optica/clientes/7")).toEqual({ id: "7" })
    expect(coincide("/optica/ordenes/:folio", "/optica/ordenes/15")).toEqual({ folio: "15" })
  })

  it("no confunde direcciones parecidas", () => {
    expect(coincide("/optica/clientes/:id", "/optica/clientes")).toBeNull()
    expect(coincide("/optica/clientes/:id", "/papeleria/vender/7")).toBeNull()
    expect(coincide("/", "/")).toEqual({})
  })
})
