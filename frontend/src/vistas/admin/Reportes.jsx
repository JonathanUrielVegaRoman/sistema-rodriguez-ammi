import { useEffect, useState } from "react"
import { reportesApi } from "../../services/api"
import { dinero, fechaCorta, iso, NOMBRES_PAGO } from "../../utils/formato"
import PuertaAdmin from "../../components/PuertaAdmin"

/*
  REPORTES PRIVADOS (solo el dueño): cuánto entró por día, semana o mes, en cada negocio.

  - Papelería: ventas cobradas (tickets).
  - Óptica: dinero que ENTRÓ (anticipos y liquidaciones), no el total de las órdenes.
*/

function periodo(clave) {
  const hoy = new Date()
  const d = (y, m, dia) => new Date(y, m, dia)
  switch (clave) {
    case "ayer": {
      const a = d(hoy.getFullYear(), hoy.getMonth(), hoy.getDate() - 1)
      return [iso(a), iso(a)]
    }
    case "semana": {
      const lunes = d(hoy.getFullYear(), hoy.getMonth(), hoy.getDate() - ((hoy.getDay() + 6) % 7))
      return [iso(lunes), iso(hoy)]
    }
    case "mes":
      return [iso(d(hoy.getFullYear(), hoy.getMonth(), 1)), iso(hoy)]
    case "mesPasado":
      return [iso(d(hoy.getFullYear(), hoy.getMonth() - 1, 1)), iso(d(hoy.getFullYear(), hoy.getMonth(), 0))]
    default:
      return [iso(hoy), iso(hoy)]
  }
}

const PERIODOS = [
  ["hoy", "Hoy"],
  ["ayer", "Ayer"],
  ["semana", "Esta semana"],
  ["mes", "Este mes"],
  ["mesPasado", "Mes pasado"],
]

export default function Reportes() {
  return (
    <PuertaAdmin>
      <ContenidoReportes />
    </PuertaAdmin>
  )
}

function ContenidoReportes() {
  const [clave, setClave] = useState("hoy")
  const [[desde, hasta], setRango] = useState(periodo("hoy"))
  const [reporte, setReporte] = useState(null)
  const [error, setError] = useState("")

  useEffect(() => {
    setError("")
    reportesApi.generar(desde, hasta).then(setReporte).catch((e) => setError(e.message))
  }, [desde, hasta])

  function elegir(c) {
    setClave(c)
    setRango(periodo(c))
  }

  return (
    <section>
      <h1 className="titulo-seccion">Reportes de ventas</h1>

      {/* Filtros en un solo renglón */}
      <div className="panel filtros-reporte">
        <div className="chips" role="group" aria-label="Periodo">
          {PERIODOS.map(([c, t]) => (
            <button key={c} className={"chip" + (clave === c ? " chip--activo" : "")} onClick={() => elegir(c)}>{t}</button>
          ))}
        </div>
        <div className="fila">
          <input className="entrada" type="date" value={desde} max={hasta} onChange={(e) => { setClave(""); setRango([e.target.value, hasta]) }} aria-label="Desde" />
          <span className="texto-tenue">a</span>
          <input className="entrada" type="date" value={hasta} min={desde} onChange={(e) => { setClave(""); setRango([desde, e.target.value]) }} aria-label="Hasta" />
        </div>
      </div>

      {error && <p className="mensaje-error">{error}</p>}

      {reporte && (
        <>
          <div className="cifras">
            <div className="cifra-tarjeta cifra-tarjeta--principal">
              <span className="etiqueta">Entró en total</span>
              <strong className="cifra">{dinero(reporte.granTotal)}</strong>
              <span className="texto-tenue">{desde === hasta ? fechaCorta(desde) : `${fechaCorta(desde)} – ${fechaCorta(hasta)}`}</span>
            </div>
            <div className="cifra-tarjeta">
              <span className="etiqueta"><i className="muestra muestra--papeleria" /> Papelería AMMI</span>
              <strong className="cifra">{dinero(reporte.papeleria.total)}</strong>
              <span className="texto-tenue cifra">{reporte.papeleria.numeroVentas} {reporte.papeleria.numeroVentas === 1 ? "venta" : "ventas"} · promedio {dinero(reporte.papeleria.ticketPromedio)}</span>
            </div>
            <div className="cifra-tarjeta">
              <span className="etiqueta"><i className="muestra muestra--optica" /> Óptica RODRÍGUEZ</span>
              <strong className="cifra">{dinero(reporte.optica.cobrado)}</strong>
              <span className="texto-tenue cifra">{reporte.optica.ordenesNuevas} {reporte.optica.ordenesNuevas === 1 ? "orden nueva" : "órdenes nuevas"} ({dinero(reporte.optica.valorOrdenesNuevas)})</span>
            </div>
            <div className="cifra-tarjeta">
              <span className="etiqueta">Por cobrar en la óptica</span>
              <strong className="cifra">{dinero(reporte.optica.saldoPendienteTotal)}</strong>
              <span className="texto-tenue cifra">{reporte.optica.ordenesPendientes} órdenes sin entregar</span>
            </div>
          </div>

          {reporte.porDia.length > 1 && <GraficaPorDia dias={reporte.porDia} />}

          <div className="dos-columnas">
            <div className="panel">
              <h2 className="panel-titulo">Por método de pago</h2>
              <table className="tabla cifra">
                <thead>
                  <tr><th></th><th>Papelería</th><th>Óptica</th></tr>
                </thead>
                <tbody>
                  {Object.keys(NOMBRES_PAGO).map((m) => (
                    <tr key={m}>
                      <th>{NOMBRES_PAGO[m]}</th>
                      <td>{dinero(reporte.papeleria.porMetodo[m])}</td>
                      <td>{dinero(reporte.optica.porMetodo[m])}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="panel">
              <h2 className="panel-titulo">Lo más vendido en la papelería</h2>
              {reporte.papeleria.masVendidos.length === 0 && <p className="texto-tenue">Sin ventas en el periodo.</p>}
              <table className="tabla cifra">
                <tbody>
                  {reporte.papeleria.masVendidos.map((p) => (
                    <tr key={p.nombre}>
                      <th>{p.nombre}</th>
                      <td>{p.cantidad} pzas</td>
                      <td>{dinero(p.importe)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </section>
  )
}

/*
  Gráfica de barras apiladas por día: papelería (azul) + óptica (verde).
  Pasar el dedo o el mouse sobre un día muestra sus cifras. Debajo, la misma
  información como tabla (para quien no distingue colores o prefiere números).
*/
function GraficaPorDia({ dias }) {
  const [activo, setActivo] = useState(null)
  const maximo = Math.max(1, ...dias.map((d) => Number(d.papeleria) + Number(d.optica)))
  const etiquetaCada = Math.ceil(dias.length / 10) // no más de ~10 fechas en el eje
  const elegido = activo !== null ? dias[activo] : null

  return (
    <div className="panel">
      <div className="fila fila--entre">
        <h2 className="panel-titulo">Por día</h2>
        <div className="leyenda">
          <span><i className="muestra muestra--papeleria" /> Papelería</span>
          <span><i className="muestra muestra--optica" /> Óptica</span>
        </div>
      </div>
      <p className="texto-tenue cifra grafica-detalle" aria-live="polite">
        {elegido
          ? `${fechaCorta(elegido.fecha)}: papelería ${dinero(elegido.papeleria)} · óptica ${dinero(elegido.optica)} · total ${dinero(Number(elegido.papeleria) + Number(elegido.optica))}`
          : "Toca una barra para ver el detalle del día."}
      </p>
      <div className="grafica" onMouseLeave={() => setActivo(null)}>
        <span className="grafica-maximo cifra">{dinero(maximo)}</span>
        {dias.map((d, i) => {
          const p = (Number(d.papeleria) / maximo) * 100
          const o = (Number(d.optica) / maximo) * 100
          return (
            <button
              key={d.fecha}
              className={"grafica-columna" + (activo === i ? " grafica-columna--activa" : "")}
              onMouseEnter={() => setActivo(i)}
              onFocus={() => setActivo(i)}
              onClick={() => setActivo(i)}
              aria-label={`${fechaCorta(d.fecha)}: papelería ${dinero(d.papeleria)}, óptica ${dinero(d.optica)}`}
            >
              <span className="grafica-barras">
                {o > 0 && <span className="grafica-barra grafica-barra--optica" style={{ height: o + "%" }} />}
                {p > 0 && <span className="grafica-barra grafica-barra--papeleria" style={{ height: p + "%" }} />}
              </span>
              <span className="grafica-fecha cifra">{i % etiquetaCada === 0 ? Number(d.fecha.slice(8, 10)) : ""}</span>
            </button>
          )
        })}
      </div>
      <details>
        <summary className="btn btn--texto">Ver como tabla</summary>
        <table className="tabla cifra">
          <thead>
            <tr><th>Día</th><th>Papelería</th><th>Óptica</th><th>Total</th></tr>
          </thead>
          <tbody>
            {dias.map((d) => (
              <tr key={d.fecha}>
                <th>{fechaCorta(d.fecha)}</th>
                <td>{dinero(d.papeleria)}</td>
                <td>{dinero(d.optica)}</td>
                <td>{dinero(Number(d.papeleria) + Number(d.optica))}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </details>
    </div>
  )
}
