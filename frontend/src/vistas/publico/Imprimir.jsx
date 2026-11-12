import { useEffect, useState } from "react"
import { impresionesApi } from "../../services/api"
import Icono from "../../components/Icono"
import SelectorTema from "../../components/SelectorTema"

/*
  BUZÓN DE IMPRESIÓN — pantalla PÚBLICA para los CLIENTES de la papelería.
  Llegan aquí escaneando el código QR del mostrador (#/imprimir).

  1. Eligen sus archivos (PDF, Word, fotos...), copias, color y doble cara.
  2. Reciben un TURNO grande ("A-007") para decirlo en el mostrador.
  3. La pantalla revisa cada 10 segundos y avisa cuando ya está impreso.

  No piden nombre ni teléfono. El código de seguimiento se guarda solo en SU celular
  (localStorage), así que si recarga la página sigue viendo su turno.
*/

const CLAVE = "mis-turnos"
const ACEPTA = ".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.jpg,.jpeg,.png,.txt"

function turnosGuardados() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE) ?? "[]")
  } catch {
    return []
  }
}

function guardarTurnos(lista) {
  try {
    localStorage.setItem(CLAVE, JSON.stringify(lista.slice(-5)))
  } catch {
    // sin almacenamiento: funciona igual mientras no recargue
  }
}

export default function Imprimir() {
  const [archivos, setArchivos] = useState([])
  const [copias, setCopias] = useState(1)
  const [color, setColor] = useState(false)
  const [dobleCara, setDobleCara] = useState(false)
  const [notas, setNotas] = useState("")
  const [error, setError] = useState("")
  const [enviando, setEnviando] = useState(false)
  const [turnos, setTurnos] = useState(turnosGuardados) // [{ turno, codigo, estado }]

  // Revisar el estado de mis turnos cada 10 segundos.
  useEffect(() => {
    if (turnos.length === 0) return
    const revisar = () =>
      Promise.all(
        turnos.map((t) =>
          impresionesApi
            .consultar(t.codigo)
            .then((r) => ({ ...t, estado: r.estado }))
            .catch(() => null), // el turno ya no existe: se quita
        ),
      ).then((actualizados) => {
        const vigentes = actualizados.filter(Boolean)
        guardarTurnos(vigentes)
        setTurnos((anteriores) => (JSON.stringify(anteriores) === JSON.stringify(vigentes) ? anteriores : vigentes))
      })
    revisar()
    const reloj = setInterval(revisar, 10_000)
    return () => clearInterval(reloj)
  }, [turnos])

  async function enviar(e) {
    e.preventDefault()
    setError("")
    setEnviando(true)
    try {
      const formulario = new FormData()
      archivos.forEach((a) => formulario.append("archivos", a))
      formulario.append("copias", copias)
      formulario.append("color", color)
      formulario.append("dobleCara", dobleCara)
      formulario.append("notas", notas)
      const r = await impresionesApi.subir(formulario)
      const nuevos = [...turnos, { turno: r.turno, codigo: r.codigoSeguimiento, estado: "PENDIENTE" }]
      guardarTurnos(nuevos)
      setTurnos(nuevos)
      setArchivos([])
      setNotas("")
      setCopias(1)
      e.target.reset()
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  const activos = turnos.filter((t) => t.estado === "PENDIENTE" || t.estado === "IMPRESO")

  return (
    <div className="publico">
      <SelectorTema flotante />
      <header className="publico-encabezado">
        <Icono nombre="impresora" tamano={30} />
        <div>
          <h1>Papelería AMMI</h1>
          <p>Manda tus archivos para imprimir</p>
        </div>
      </header>

      {activos.map((t) => (
        <div key={t.codigo} className={"turno" + (t.estado === "IMPRESO" ? " turno--listo" : "")}>
          <span className="turno-etiqueta">Tu turno</span>
          <strong className="turno-numero">{t.turno}</strong>
          <span className="turno-estado">
            {t.estado === "IMPRESO" ? "¡Listo! Pasa al mostrador a recogerlo." : "En espera. Dile tu turno a quien te atienda."}
          </span>
        </div>
      ))}

      <form className="panel formulario" onSubmit={enviar}>
        <label className="campo">
          <span className="etiqueta">Archivos (máximo 5, hasta 20 MB cada uno)</span>
          <input className="entrada" type="file" multiple accept={ACEPTA} onChange={(e) => setArchivos([...e.target.files].slice(0, 5))} required />
        </label>
        {archivos.length > 0 && (
          <ul className="lista-simple">
            {archivos.map((a) => (
              <li key={a.name}>
                <span>{a.name}</span>
                <span className="texto-tenue cifra">{(a.size / 1024 / 1024).toFixed(1)} MB</span>
              </li>
            ))}
          </ul>
        )}

        <div className="rejilla">
          <label className="campo">
            <span className="etiqueta">Copias</span>
            <input className="entrada" type="number" min="1" max="100" inputMode="numeric" value={copias} onChange={(e) => setCopias(e.target.value)} />
          </label>
          <div className="campo">
            <span className="etiqueta">Tipo de impresión</span>
            <div className="segmentado">
              <button type="button" className={!color ? "activo" : ""} onClick={() => setColor(false)}>Blanco y negro</button>
              <button type="button" className={color ? "activo" : ""} onClick={() => setColor(true)}>Color</button>
            </div>
          </div>
        </div>

        <label className="casilla">
          <input type="checkbox" checked={dobleCara} onChange={(e) => setDobleCara(e.target.checked)} />
          Imprimir por ambos lados
        </label>

        <label className="campo">
          <span className="etiqueta">Indicaciones (opcional)</span>
          <input className="entrada" value={notas} maxLength={300} onChange={(e) => setNotas(e.target.value)} placeholder="Ej. solo las páginas 3 a 5" />
        </label>

        {error && <p className="mensaje-error">{error}</p>}
        <button className="btn btn--primario btn--bloque" disabled={enviando || archivos.length === 0}>
          {enviando ? "Enviando…" : "Enviar a imprimir"}
        </button>
      </form>

      <p className="aviso-barra">
        <Icono nombre="candado" />
        <span>
          Tu privacidad: no pedimos nombre ni teléfono. Tus archivos solo los ve el personal de la papelería y se
          borran en cuanto te entregamos tus impresiones (o en 24 horas como máximo).
        </span>
      </p>
    </div>
  )
}
