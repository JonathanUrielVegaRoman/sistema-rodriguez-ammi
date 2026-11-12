import { useEffect, useState } from "react"
import { ventasApi } from "../../services/api"
import { dinero, fechaHora, hoyISO, NOMBRES_PAGO } from "../../utils/formato"

/*
  CORTE DEL DÍA: cuánto se vendió y cómo se pagó.
  useEffect con [dia] = "vuelve a pedir el corte cada vez que cambie el día elegido".
*/
export default function Corte() {
  const [dia, setDia] = useState(hoyISO())
  const [corte, setCorte] = useState(null)
  const [error, setError] = useState("")

  useEffect(() => {
    setError("")
    ventasApi.corte(dia).then(setCorte).catch((e) => setError(e.message))
  }, [dia])

  return (
    <section>
      <h1 className="nombre-pagina">Corte del día</h1>
      <p className="descripcion-pagina">Papelería AMMI</p>

      <div className="panel">
        <label className="campo">
          <span className="etiqueta">Día</span>
          <input className="entrada" type="date" value={dia} onChange={(e) => setDia(e.target.value)} />
        </label>
      </div>

      {error && <p className="mensaje-error">{error}</p>}

      {corte && (
        <>
          <div className="cifras">
            <div className="cifra-tarjeta cifra-tarjeta--principal">
              <span className="etiqueta">Total vendido</span>
              <strong className="cifra">{dinero(corte.total)}</strong>
              <span className="texto-tenue">{corte.numeroVentas} ventas</span>
            </div>
            {Object.entries(corte.totalPorMetodo).map(([metodo, monto]) => (
              <div className="cifra-tarjeta" key={metodo}>
                <span className="etiqueta">{NOMBRES_PAGO[metodo]}</span>
                <strong className="cifra">{dinero(monto)}</strong>
              </div>
            ))}
          </div>

          <div className="panel">
            <h2 className="panel-titulo">Ventas</h2>
            {corte.ventas.length === 0 && <p className="texto-tenue">Sin ventas este día.</p>}
            <ul className="lista-simple">
              {corte.ventas.map((v) => (
                <li key={v.folio}>
                  <span>
                    Folio {v.folio} · {fechaHora(v.fecha)}
                  </span>
                  <span className="texto-tenue">{NOMBRES_PAGO[v.metodoPago]}</span>
                  <span className="cifra">{dinero(v.total)}</span>
                </li>
              ))}
            </ul>
          </div>
        </>
      )}
    </section>
  )
}
