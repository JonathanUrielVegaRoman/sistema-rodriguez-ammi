import { useEffect, useState } from "react"
import { ordenesApi } from "../../services/api"
import { ir } from "../../hooks/useRuta"
import TarjetaOrden from "../../components/TarjetaOrden"

/*
  ÓRDENES DE LENTES agrupadas por estado (pestañas).
  La pestaña se recuerda en la dirección (#/optica/ordenes?estado=LISTA) para que el botón
  "regresar" vuelva a la misma pestaña.
*/
const PESTANAS = [
  ["EN_LABORATORIO", "En laboratorio"],
  ["LISTA", "Listas"],
  ["ENTREGADA", "Entregadas"],
  ["CANCELADA", "Canceladas"],
]

export default function Ordenes({ consulta }) {
  const estado = consulta?.estado ?? "EN_LABORATORIO"
  const [ordenes, setOrdenes] = useState([])
  const [error, setError] = useState("")

  useEffect(() => {
    ordenesApi.listar([estado]).then(setOrdenes).catch((e) => setError(e.message))
  }, [estado])

  return (
    <section>
      <div className="titulo-con-accion">
        <h1 className="titulo-seccion">Órdenes de lentes</h1>
        <button className="btn btn--verde" onClick={() => ir("/optica/ordenes/nueva")}>+ Nueva orden</button>
      </div>

      <div className="pestanas" role="tablist">
        {PESTANAS.map(([valor, texto]) => (
          <button key={valor} role="tab" aria-selected={estado === valor} className={"pestana" + (estado === valor ? " pestana--activa" : "")} onClick={() => ir(`/optica/ordenes?estado=${valor}`)}>
            {texto}
          </button>
        ))}
      </div>

      {error && <p className="mensaje-error">{error}</p>}
      {ordenes.length === 0 && <div className="panel vacio">No hay órdenes en esta pestaña.</div>}
      <div className="cuadricula-ordenes">
        {ordenes.map((o) => (
          <TarjetaOrden key={o.folio} orden={o} />
        ))}
      </div>
    </section>
  )
}
