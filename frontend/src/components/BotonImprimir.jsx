import { useState } from "react"
import { imprimirTicket } from "../services/api"
import Icono from "./Icono"

/*
  Botón "Imprimir" para tickets y notas.
  1. Intenta mandarlo DIRECTO a la impresora de tickets (sin el cuadro de Ctrl+P).
  2. Si no hay impresora de tickets configurada en Ajustes, abre la impresión del navegador.

  Uso: <BotonImprimir mandar={() => ventasApi.imprimir(folio)} texto="Imprimir ticket" />
*/
export default function BotonImprimir({ mandar, texto = "Imprimir", clase = "btn btn--primario" }) {
  const [estado, setEstado] = useState("")
  const [enviando, setEnviando] = useState(false)

  async function imprimir() {
    setEstado("")
    setEnviando(true)
    try {
      const r = await imprimirTicket(mandar)
      if (r?.impreso) setEstado(`Enviado a ${r.impresora}`)
    } catch (e) {
      setEstado(e.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <>
      <button className={clase} onClick={imprimir} disabled={enviando}>
        <Icono nombre="impresora" /> {enviando ? "Imprimiendo…" : texto}
      </button>
      {estado && <span className="texto-tenue estado-impresion">{estado}</span>}
    </>
  )
}
