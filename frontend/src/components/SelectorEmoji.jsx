import { useState } from "react"
import { EMOJIS_PAPELERIA, EMOJI_GENERICO } from "../utils/emojis"

/*
  Botón que muestra el emoji actual y, al tocarlo, abre una paleta para elegir otro.
  Props: valor (emoji actual), alCambiar(emoji).
*/
export default function SelectorEmoji({ valor, alCambiar }) {
  const [abierto, setAbierto] = useState(false)

  return (
    <div className="selector-emoji">
      <button type="button" className="selector-emoji-actual" onClick={() => setAbierto(!abierto)} aria-label="Elegir ícono" aria-expanded={abierto}>
        {valor || EMOJI_GENERICO}
      </button>
      {abierto && (
        <div className="selector-emoji-paleta" role="listbox">
          {EMOJIS_PAPELERIA.map((e) => (
            <button
              key={e}
              type="button"
              role="option"
              aria-selected={e === valor}
              className={e === valor ? "elegido" : ""}
              onClick={() => {
                alCambiar(e)
                setAbierto(false)
              }}
            >
              {e}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
