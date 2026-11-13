import { describe, expect, it } from "vitest"
import { caja, compararMedidas, contornoLente, engordar, FORMAS, medidasDe } from "./geometriaArmazon"

/*
  Si el modelo 3D dice "Lente 52", el lente dibujado debe medir 52 mm de verdad.
  Estas pruebas lo garantizan para todas las formas.
*/
describe("contorno del lente", () => {
  for (const forma of Object.keys(FORMAS)) {
    it(`${forma}: mide exactamente el ancho y alto pedidos, de los dos lados`, () => {
      for (const lado of [1, -1]) {
        const c = caja(contornoLente(forma, 52, 40, lado))
        expect(c.ancho).toBeCloseTo(52, 6)
        expect(c.alto).toBeCloseTo(40, 6)
        expect((c.minX + c.maxX) / 2).toBeCloseTo(0, 6) // centrado
      }
    })
  }

  it("el lente izquierdo es el espejo del derecho (aviador y ojo de gato no son simétricos)", () => {
    const derecho = contornoLente("OJO_DE_GATO", 54, 38, 1)
    const izquierdo = contornoLente("OJO_DE_GATO", 54, 38, -1)
    // El punto más alto del derecho está hacia afuera (x > 0); el del izquierdo, también hacia afuera (x < 0).
    const masAlto = (puntos) => puntos.reduce((a, b) => (b[1] > a[1] ? b : a))
    expect(masAlto(derecho)[0]).toBeGreaterThan(0)
    expect(masAlto(izquierdo)[0]).toBeLessThan(0)
  })

  it("el aro (contorno engordado) es más grande que el lente en el grosor indicado", () => {
    const lente = contornoLente("REDONDO", 50, 48)
    const aro = caja(engordar(lente, 3))
    expect(aro.ancho).toBeCloseTo(56, 0) // 50 + 3 de cada lado
    expect(aro.alto).toBeCloseTo(54, 0)
  })
})

describe("medidas del armazón", () => {
  it("usa las medidas capturadas y estima el alto del lente por la forma si falta", () => {
    const m = medidasDe({ anchoLente: 50, puente: 20, varilla: 145, forma: "REDONDO" })
    expect(m).toMatchObject({ A: 50, DBL: 20, L: 145, forma: "REDONDO" })
    expect(m.B).toBe(48) // 50 × 0.96
    expect(medidasDe({ anchoLente: 50, puente: 20, altoLente: 41 }).B).toBe(41)
  })

  it("no falla si faltan datos o la forma no existe", () => {
    const m = medidasDe({ forma: "INVENTADA" })
    expect(m.forma).toBe("RECTANGULAR")
    expect(m.anchoTotal).toBeGreaterThan(100)
  })

  it("el ancho total crece con el lente y el puente", () => {
    const chico = medidasDe({ anchoLente: 48, puente: 16 }).anchoTotal
    const grande = medidasDe({ anchoLente: 54, puente: 18 }).anchoTotal
    expect(grande - chico).toBe(2 * 6 + 2) // dos lentes 6 mm más anchos + puente 2 mm más ancho
  })
})

describe("comparar dos tallas", () => {
  it("dice cuántos milímetros hay de diferencia en cada medida", () => {
    const filas = compararMedidas({ anchoLente: 52, puente: 18, varilla: 140 }, { anchoLente: 50, puente: 22, varilla: 150 })
    const por = Object.fromEntries(filas.map((f) => [f.nombre, f.diferencia]))
    expect(por["Lente"]).toBe(2)
    expect(por["Puente"]).toBe(-4)
    expect(por["Varilla"]).toBe(-10)
    expect(por["Ancho total (de sien a sien)"]).toBe(0) // 2×2 de lentes − 4 de puente
  })
})
