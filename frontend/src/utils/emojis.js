/*
  ÍCONOS DE LA PAPELERÍA con emojis.

  ¿Por qué emojis y no fotos? Para la papelería no vale la pena tomar foto a cada lápiz:
  el emoji ya viene en todos los celulares, no hay que cargarlo y se reconoce al instante.
  (Se ven un poco distintos en Android, iPhone y Windows, pero siempre se entienden.)

  Las fotos se dejan para los ARMAZONES, donde sí hacen la diferencia.
*/

export const EMOJIS_PAPELERIA = [
  "📓", "📒", "📔", "📕", "📗", "📘", "📙", "📚", "📄", "📃", "📑", "🗒️",
  "✏️", "🖊️", "🖋️", "🖍️", "🖌️", "✒️", "📝", "📏", "📐", "✂️", "📎", "🖇️",
  "📌", "📍", "🧷", "📁", "📂", "🗂️", "✉️", "📦", "🏷️", "🧴", "🧽", "🧮",
  "🎨", "🎒", "🎁", "🎈", "🎀", "🪀", "🧩", "🎲", "🖨️", "💾", "🔋", "🧾",
]

/** Palabras clave → emoji sugerido al escribir el nombre del producto. */
const SUGERENCIAS = [
  [["cuaderno"], "📓"],
  [["libreta", "block"], "📒"],
  [["agenda"], "📔"],
  [["libro"], "📚"],
  [["hoja", "papel", "cartulina"], "📄"],
  [["lapiz", "lápiz", "sacapuntas"], "✏️"],
  [["pluma", "boligrafo", "bolígrafo"], "🖊️"],
  [["color", "crayon", "crayón", "marcador", "plumon", "plumón"], "🖍️"],
  [["pincel", "pintura", "acuarela"], "🎨"],
  [["regla", "escuadra"], "📏"],
  [["transportador", "compas", "compás"], "📐"],
  [["tijera"], "✂️"],
  [["clip", "grapa", "engrapadora"], "📎"],
  [["chincheta", "tachuela"], "📌"],
  [["seguro"], "🧷"],
  [["folder", "carpeta"], "📁"],
  [["sobre"], "✉️"],
  [["etiqueta"], "🏷️"],
  [["pegamento", "resistol", "silicon", "silicón"], "🧴"],
  [["borrador", "goma"], "🧽"],
  [["calculadora"], "🧮"],
  [["mochila"], "🎒"],
  [["regalo", "envoltura", "moño"], "🎁"],
  [["globo"], "🎈"],
  [["listón", "liston"], "🎀"],
  [["rompecabezas"], "🧩"],
  [["impres", "copia"], "🖨️"],
  [["usb", "memoria"], "💾"],
  [["pila", "bateria", "batería"], "🔋"],
]

export const EMOJI_GENERICO = "📦"

export function sugerirEmoji(nombre) {
  const texto = (nombre ?? "").toLowerCase()
  const hallado = SUGERENCIAS.find(([palabras]) => palabras.some((p) => texto.includes(p)))
  return hallado ? hallado[1] : null
}
