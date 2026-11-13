/*
  Campo para anotar la REFERENCIA de un pago que no es en efectivo:
    - Transferencia → clave de rastreo (o los últimos dígitos) que aparece en el comprobante.
    - Tarjeta       → número de autorización que imprime la terminal en el voucher.

  Es opcional, pero conviene llenarlo: después permite encontrar ese pago en el estado de
  cuenta del banco y aclarar un "sí te transferí". Sale en el ticket y en el Excel.

  Con efectivo no se muestra nada.
*/
const TEXTOS = {
  TRANSFERENCIA: { etiqueta: "Clave de rastreo de la transferencia (opcional)", ejemplo: "Ej. últimos 6 dígitos" },
  TARJETA: { etiqueta: "Autorización del voucher (opcional)", ejemplo: "Ej. 045812" },
}

export default function CampoReferencia({ metodo, valor, alCambiar }) {
  const texto = TEXTOS[metodo]
  if (!texto) return null
  return (
    <label className="campo">
      <span className="etiqueta">{texto.etiqueta}</span>
      <input className="entrada cifra" value={valor} maxLength={60} placeholder={texto.ejemplo} onChange={(e) => alCambiar(e.target.value)} />
    </label>
  )
}
