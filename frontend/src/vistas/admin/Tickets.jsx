import { useEffect, useState } from "react"
import { adminApi, ventasApi } from "../../services/api"
import { dinero, fechaHora, hoyISO, NOMBRES_PAGO } from "../../utils/formato"
import PuertaAdmin from "../../components/PuertaAdmin"
import Ticket from "../../components/Ticket"
import BotonImprimir from "../../components/BotonImprimir"
import Modal from "../../components/Modal"

/*
  TICKETS DE LA PAPELERÍA (solo el dueño): ver los tickets de cualquier día, reimprimirlos y
  CANCELAR uno (devolución o cobro equivocado). Al cancelar:
    - la mercancía regresa al inventario,
    - la venta deja de contar en reportes y en el Excel (aparece en la hoja "Canceladas"),
    - queda registrado el motivo y la hora.
*/
export default function Tickets() {
  return (
    <PuertaAdmin>
      <ContenidoTickets />
    </PuertaAdmin>
  )
}

function ContenidoTickets() {
  const [dia, setDia] = useState(hoyISO())
  const [tickets, setTickets] = useState([])
  const [abierto, setAbierto] = useState(null)
  const [motivo, setMotivo] = useState("")
  const [error, setError] = useState("")

  function cargar() {
    adminApi.ventasDelDia(dia).then(setTickets).catch((e) => setError(e.message))
  }

  useEffect(cargar, [dia])

  async function cancelar() {
    if (!window.confirm(`¿Cancelar el ticket ${abierto.folio} por ${dinero(abierto.total)}? La mercancía regresa al inventario.`)) return
    setError("")
    try {
      const t = await adminApi.cancelarVenta(abierto.folio, motivo)
      setAbierto(t)
      setMotivo("")
      cargar()
    } catch (e) {
      setError(e.message)
    }
  }

  const vigentes = tickets.filter((t) => !t.cancelada)
  const total = vigentes.reduce((s, t) => s + Number(t.total), 0)

  return (
    <section>
      <h1 className="titulo-seccion">Tickets de la papelería</h1>

      <div className="panel filtros-reporte">
        <input className="entrada" type="date" value={dia} max={hoyISO()} onChange={(e) => setDia(e.target.value)} aria-label="Día" />
        <span className="texto-tenue cifra">
          {vigentes.length} {vigentes.length === 1 ? "venta" : "ventas"} · {dinero(total)}
          {tickets.length > vigentes.length && ` · ${tickets.length - vigentes.length} cancelada(s)`}
        </span>
      </div>

      {error && !abierto && <p className="mensaje-error">{error}</p>}

      <div className="panel">
        {tickets.length === 0 && <p className="texto-tenue">Sin tickets este día.</p>}
        <div className="tabla-desplazable">
          <table className="tabla tabla--clic">
            <thead>
              <tr><th>Folio</th><th>Hora</th><th>Pago</th><th>Productos</th><th>Total</th></tr>
            </thead>
            <tbody>
              {tickets.map((t) => (
                <tr key={t.folio} onClick={() => { setError(""); setAbierto(t) }} className={t.cancelada ? "fila-cancelada" : ""}>
                  <th className="cifra">{t.folio}</th>
                  <td className="cifra">{fechaHora(t.fecha).split(", ")[1]}</td>
                  <td>{NOMBRES_PAGO[t.metodoPago]}</td>
                  <td>{t.renglones.reduce((s, r) => s + r.cantidad, 0)} pzas</td>
                  <td className="cifra">{t.cancelada ? "Cancelado" : dinero(t.total)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {abierto && (
        <Modal titulo={`Ticket ${abierto.folio}`} alCerrar={() => setAbierto(null)}>
          {abierto.cancelada && <p className="mensaje-error">Cancelado: {abierto.motivoCancelacion}</p>}
          <Ticket ticket={abierto} />
          <div className="acciones">
            <BotonImprimir mandar={() => ventasApi.imprimir(abierto.folio)} texto="Reimprimir" clase="btn btn--contorno" />
          </div>
          {!abierto.cancelada && (
            <div className="zona-peligro">
              <label className="campo">
                <span className="etiqueta">Motivo de la cancelación (obligatorio)</span>
                <input className="entrada" value={motivo} maxLength={200} onChange={(e) => setMotivo(e.target.value)} placeholder="Ej. el cliente devolvió el producto" />
              </label>
              {error && <p className="mensaje-error">{error}</p>}
              <button className="btn btn--peligro" disabled={!motivo.trim()} onClick={cancelar}>Cancelar este ticket</button>
            </div>
          )}
        </Modal>
      )}
    </section>
  )
}
