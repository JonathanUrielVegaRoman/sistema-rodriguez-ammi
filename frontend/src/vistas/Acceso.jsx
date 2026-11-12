import { useState } from "react"
import { accesoApi } from "../services/api"
import { sesion } from "../services/sesion"
import Icono from "../components/Icono"
import SelectorTema from "../components/SelectorTema"

/*
  ENTRADA DEL PERSONAL con PIN.
  Se pide UNA sola vez por celular: después el celular queda "recordado" por un año.

  ¿Por qué pedir PIN si son familia? Porque los CLIENTES se conectan a la misma Wi-Fi para el
  buzón de impresión. Sin PIN, cualquier cliente podría abrir la lista de clientes de la óptica
  (con celulares y graduaciones). El PIN separa "personal" de "clientes" sin estorbar en el día a día.
*/
export default function Acceso({ alEntrar }) {
  const [pin, setPin] = useState("")
  const [error, setError] = useState("")
  const [enviando, setEnviando] = useState(false)

  async function entrar(e) {
    e.preventDefault()
    setError("")
    setEnviando(true)
    try {
      const pase = await accesoApi.personal(pin)
      sesion.guardarPersonal(pase.token)
      alEntrar()
    } catch (err) {
      setError(err.message)
      setPin("")
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="pantalla-acceso">
      <SelectorTema flotante />
      <form className="panel acceso" onSubmit={entrar}>
        <div className="acceso-logo">
          <Icono nombre="lentes" tamano={34} />
        </div>
        <h1 className="seccion-titulo">Rodríguez · AMMI</h1>
        <p className="seccion-descripcion">Escribe el PIN del personal. Este celular quedará recordado.</p>
        <input
          className="entrada entrada--pin"
          type="password"
          inputMode="numeric"
          autoComplete="off"
          maxLength={12}
          value={pin}
          onChange={(e) => setPin(e.target.value)}
          placeholder="••••"
          aria-label="PIN"
          autoFocus
        />
        {error && <p className="mensaje-error">{error}</p>}
        <button className="btn btn--primario btn--bloque" disabled={!pin || enviando}>
          {enviando ? "Entrando…" : "Entrar"}
        </button>
        <p className="texto-tenue texto-centro">
          ¿Eres cliente y quieres imprimir? <a href="#/imprimir">Toca aquí</a>
        </p>
      </form>
    </div>
  )
}
