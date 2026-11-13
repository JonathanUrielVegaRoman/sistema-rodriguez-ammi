/*
  GEOMETRÍA DE UN ARMAZÓN a partir de sus medidas (todo en milímetros).

  Aquí NO hay nada de 3D todavía: solo matemáticas que convierten "52-18-140, cuadrado"
  en el contorno del lente y las dimensiones del armazón. El componente Armazon3D.jsx toma
  estos contornos y los "extruye" (les da grosor) para dibujar el modelo.

  Se separó así para poder PROBARLO sin navegador (ver geometriaArmazon.test.js): si el
  contorno de un lente de 52 mm no mide 52 mm, la prueba falla.

  Medidas de un armazón (las tres primeras vienen grabadas en la varilla):

        ├─── A ───┤├DBL┤├─── A ───┤
        ╭─────────╮     ╭─────────╮   ┬
        │         ├─────┤         │   B   A   = ancho del lente      (anchoLente)
        ╰─────────╯     ╰─────────╯   ┴   B   = alto del lente       (altoLente, opcional)
        ├──────── ancho total ────────┤   DBL = puente               (puente)
                                          L   = largo de la varilla  (varilla)
*/

/** Cada forma: su nombre, qué tan alto es el lente respecto a su ancho, y qué tan "cuadradas" son sus esquinas. */
export const FORMAS = {
  RECTANGULAR: { nombre: "Rectangular", proporcion: 0.62, esquinas: 5 },
  CUADRADO: { nombre: "Cuadrado", proporcion: 0.84, esquinas: 4.5 },
  REDONDO: { nombre: "Redondo", proporcion: 0.96, esquinas: 2 },
  OVALADO: { nombre: "Ovalado", proporcion: 0.72, esquinas: 2.2 },
  AVIADOR: { nombre: "Aviador", proporcion: 0.88, esquinas: 2.6 },
  OJO_DE_GATO: { nombre: "Ojo de gato", proporcion: 0.7, esquinas: 3 },
}

export const FORMA_POR_DEFECTO = "RECTANGULAR"

/** Grosor del aro y espacio de la bisagra a cada lado (aproximados). */
const ARO_ACETATO = 3.6
const ARO_METAL = 1.2
const BISAGRA = 3

/**
 * Completa las medidas que falten con valores típicos y calcula el ancho total.
 * Devuelve todo lo que el modelo necesita.
 */
export function medidasDe(armazon) {
  const forma = FORMAS[armazon?.forma] ? armazon.forma : FORMA_POR_DEFECTO
  const A = Number(armazon?.anchoLente) || 52
  const B = Number(armazon?.altoLente) || Math.round(A * FORMAS[forma].proporcion)
  const DBL = Number(armazon?.puente) || 18
  const L = Number(armazon?.varilla) || 140
  const metalico = forma === "AVIADOR"
  const aro = metalico ? ARO_METAL : ARO_ACETATO
  return {
    forma,
    A,
    B,
    DBL,
    L,
    metalico,
    aro,
    profundidad: metalico ? 1.6 : 4.4,
    // De sien a sien: dos lentes + puente + (aro y bisagra) de cada lado.
    anchoTotal: Math.round(2 * A + DBL + 2 * (aro + BISAGRA)),
  }
}

/**
 * Contorno del lente como lista de puntos [x, y], centrado en (0, 0), que mide EXACTAMENTE
 * A de ancho por B de alto.
 *   lado = +1 → lente del lado derecho del dibujo (su orilla "de afuera" es la derecha)
 *   lado = -1 → el otro lente (espejo)
 *
 * Base: una "superelipse" (un óvalo cuyas esquinas se pueden hacer más cuadradas).
 * Aviador y ojo de gato le aplican después su deformación característica.
 */
export function contornoLente(forma, A, B, lado = 1, puntos = 96) {
  const { esquinas } = FORMAS[forma] ?? FORMAS[FORMA_POR_DEFECTO]
  const a = A / 2
  const b = B / 2
  const contorno = []

  for (let i = 0; i < puntos; i++) {
    const t = (i / puntos) * Math.PI * 2
    const c = Math.cos(t)
    const s = Math.sin(t)
    let x = a * Math.sign(c) * Math.abs(c) ** (2 / esquinas)
    let y = b * Math.sign(s) * Math.abs(s) ** (2 / esquinas)
    const afuera = (x * lado) / a // -1 = orilla junto a la nariz, +1 = orilla de la sien

    if (forma === "AVIADOR" && y < 0) {
      // Gota: la parte de abajo se angosta y "cae" hacia la mejilla (hacia afuera).
      const bajo = -y / b
      x = x * (1 - 0.16 * bajo) + lado * a * 0.1 * bajo
    }
    if (forma === "OJO_DE_GATO" && y > 0 && afuera > 0) {
      // La esquina superior de afuera se levanta.
      y += b * 0.3 * afuera ** 2
    }
    contorno.push([x, y])
  }
  return ajustarA(contorno, A, B)
}

/** Reescala y centra una lista de puntos para que mida exactamente ancho x alto. */
function ajustarA(puntos, ancho, alto) {
  const { minX, maxX, minY, maxY } = caja(puntos)
  const ex = ancho / (maxX - minX)
  const ey = alto / (maxY - minY)
  const cx = (minX + maxX) / 2
  const cy = (minY + maxY) / 2
  return puntos.map(([x, y]) => [(x - cx) * ex, (y - cy) * ey])
}

/** La caja que encierra a los puntos. */
export function caja(puntos) {
  let minX = Infinity
  let maxX = -Infinity
  let minY = Infinity
  let maxY = -Infinity
  for (const [x, y] of puntos) {
    if (x < minX) minX = x
    if (x > maxX) maxX = x
    if (y < minY) minY = y
    if (y > maxY) maxY = y
  }
  return { minX, maxX, minY, maxY, ancho: maxX - minX, alto: maxY - minY }
}

/**
 * Devuelve el mismo contorno pero "engordado" una distancia hacia afuera (para dibujar el aro
 * alrededor del lente). Cada punto se mueve en la dirección perpendicular a la orilla.
 * El contorno debe recorrerse en sentido antihorario (como lo entrega contornoLente).
 */
export function engordar(puntos, distancia) {
  const n = puntos.length
  return puntos.map(([x, y], i) => {
    const [ax, ay] = puntos[(i - 1 + n) % n]
    const [bx, by] = puntos[(i + 1) % n]
    // La orilla va de "a" a "b"; su perpendicular hacia afuera (sentido antihorario) es (dy, -dx).
    const dx = bx - ax
    const dy = by - ay
    const largo = Math.hypot(dx, dy) || 1
    return [x + (dy / largo) * distancia, y - (dx / largo) * distancia]
  })
}

/**
 * Compara dos armazones y describe las diferencias en palabras sencillas.
 * Es justo lo que pidió el cliente: distinguir modelos parecidos de distinto tamaño.
 */
export function compararMedidas(uno, otro) {
  const a = medidasDe(uno)
  const b = medidasDe(otro)
  const filas = [
    ["Ancho total (de sien a sien)", a.anchoTotal, b.anchoTotal],
    ["Lente", a.A, b.A],
    ["Alto del lente", a.B, b.B],
    ["Puente", a.DBL, b.DBL],
    ["Varilla", a.L, b.L],
  ]
  return filas.map(([nombre, este, elOtro]) => ({ nombre, este, otro: elOtro, diferencia: este - elOtro }))
}
