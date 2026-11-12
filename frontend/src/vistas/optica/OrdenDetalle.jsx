import { useEffect, useState } from "react"
import { ordenesApi } from "../../services/api"
import { ir } from "../../hooks/useRuta"
import { dinero, enlaceWhatsapp, fechaCorta, fechaHora, NOMBRES_ESTADO_ORDEN, NOMBRES_MICA, NOMBRES_PAGO } from "../../utils/formato"
import TablaReceta from "../../components/TablaReceta"
import Icono from "../../components/Icono"

/*
  DETALLE DE UNA ORDEN: qué lentes son, cuánto se debe y en qué estado van.

  Acciones según el estado:
    EN_LABORATORIO → "Ya llegaron (marcar lista)"
    LISTA          → "Avisar por WhatsApp", "Entregar" (solo si ya pagó todo)
    Siempre        → "Registrar abono", "Imprimir nota", "Cancelar"
*/
export default function OrdenDetalle({ folio }) {
  const [orden, setOrden] = useState(null)
  const [error, setError] = useState("")
  const [abono, setAbono] = useState("")
  const [metodo, setMetodo] = useState("EFECTIVO")

  useEffect(() => {
    ordenesApi.obtener(folio).then(setOrden).catch((e) => setError(e.message))
  }, [folio])

  async function accion(promesa) {
    setError("")
    try {
      setOrden(await promesa)
    } catch (e) {
      setError(e.message)
    }
  }

  if (!orden) return error ? <p className="mensaje-error">{error}</p> : <p className="texto-tenue">Cargando…</p>

  const abierta = orden.estado === "EN_LABORATORIO" || orden.estado === "LISTA"
  const debe = Number(orden.saldo) > 0
  const mensajeListo =
    `Hola ${orden.clienteNombre.split(" ")[0]}, tus lentes ya están listos en Óptica RODRÍGUEZ (folio ${orden.folio}).` +
    (debe ? ` Saldo pendiente: ${dinero(orden.saldo)}.` : "") + " ¡Te esperamos!"

  return (
    <section>
      <div className="titulo-con-accion no-imprimir">
        <div>
          <h1 className="titulo-seccion">Orden folio {orden.folio}</h1>
          <p className="texto-tenue">
            <a href={`#/optica/clientes/${orden.clienteId}`}>{orden.clienteNombre}</a> · {fechaHora(orden.fecha)}
            {orden.fechaEntregaEstimada && ` · entrega estimada ${fechaCorta(orden.fechaEntregaEstimada)}`}
          </p>
        </div>
        <span className={"insignia insignia--grande estado--" + orden.estado}>{NOMBRES_ESTADO_ORDEN[orden.estado]}</span>
      </div>

      {error && <p className="mensaje-error no-imprimir">{error}</p>}

      <div className="dos-columnas no-imprimir">
        <div>
          <div className="panel">
            <h2 className="panel-titulo">Lentes</h2>
            {orden.partidas.map((p, i) => (
              <div key={i} className={"par-armazon" + (p.esRegalo ? " par--regalo" : "")}>
                <div className="par-armazon-foto par-armazon-foto--grande">
                  {p.fotoUrl ? <img src={p.fotoUrl} alt="" /> : <Icono nombre="lentes" tamano={32} />}
                </div>
                <div className="par-armazon-datos">
                  <strong>{p.descripcionArmazon} {p.esRegalo && <span className="insignia insignia--verde">Regalo 2x1</span>}</strong>
                  {p.medidas && <span className="texto-tenue cifra">{p.medidas}</span>}
                  <span>{NOMBRES_MICA[p.tipoMica]}{p.material ? ` · ${p.material}` : ""}</span>
                  {p.tratamientos && <span className="texto-tenue">{p.tratamientos}</span>}
                  <span className="cifra">{dinero(p.subtotal)}</span>
                </div>
              </div>
            ))}
            {orden.notas && <p className="aviso-barra">{orden.notas}</p>}
          </div>
          {orden.receta && (
            <div className="panel">
              <h2 className="panel-titulo">Graduación</h2>
              <TablaReceta receta={orden.receta} />
            </div>
          )}
        </div>

        <div>
          <div className="panel">
            <h2 className="panel-titulo">Cuenta</h2>
            <dl className="totales cifra">
              <div className="totales-renglon"><dt>Subtotal</dt><dd>{dinero(orden.subtotal)}</dd></div>
              {Number(orden.descuentoCupon) > 0 && <div className="totales-renglon texto-verde"><dt>Cupón {orden.codigoCupon}</dt><dd>−{dinero(orden.descuentoCupon)}</dd></div>}
              {Number(orden.descuentoManual) > 0 && <div className="totales-renglon texto-verde"><dt>{orden.motivoDescuento}</dt><dd>−{dinero(orden.descuentoManual)}</dd></div>}
              <div className="totales-renglon totales-total"><dt>Total</dt><dd>{dinero(orden.total)}</dd></div>
              {orden.pagos.map((p, i) => (
                <div key={i} className="totales-renglon texto-tenue"><dt>{fechaHora(p.fecha)} · {NOMBRES_PAGO[p.metodoPago]}</dt><dd>−{dinero(p.monto)}</dd></div>
              ))}
              <div className={"totales-renglon totales-total" + (debe ? " texto-ambar" : " texto-verde")}><dt>Saldo</dt><dd>{dinero(orden.saldo)}</dd></div>
            </dl>

            {abierta && debe && (
              <div className="formulario">
                <div className="fila">
                  <input className="entrada cifra" type="number" min="0" inputMode="decimal" placeholder="Monto del abono" value={abono} onChange={(e) => setAbono(e.target.value)} />
                  <button className="btn btn--contorno" onClick={() => setAbono(String(orden.saldo))}>Todo</button>
                </div>
                <div className="segmentado">
                  {Object.entries(NOMBRES_PAGO).map(([v, t]) => (
                    <button key={v} type="button" className={metodo === v ? "activo" : ""} onClick={() => setMetodo(v)}>{t}</button>
                  ))}
                </div>
                <button className="btn btn--primario" disabled={!abono} onClick={() => { accion(ordenesApi.abonar(orden.folio, Number(abono), metodo)); setAbono("") }}>
                  Registrar abono
                </button>
              </div>
            )}
          </div>

          <div className="panel">
            <h2 className="panel-titulo">Estado</h2>
            <div className="acciones acciones--sin-margen acciones--columna">
              {orden.estado === "EN_LABORATORIO" && (
                <button className="btn btn--primario" onClick={() => accion(ordenesApi.estado(orden.folio, "LISTA"))}>Ya llegaron: marcar como lista</button>
              )}
              {orden.estado === "LISTA" && orden.clienteCelular && (
                <a className="btn btn--verde" href={enlaceWhatsapp(orden.clienteCelular, mensajeListo)} target="_blank" rel="noopener">
                  <Icono nombre="mensaje" /> Avisar por WhatsApp
                </a>
              )}
              {orden.estado === "LISTA" && (
                <button className="btn btn--primario" disabled={debe} title={debe ? "Primero liquida el saldo" : ""} onClick={() => accion(ordenesApi.estado(orden.folio, "ENTREGADA"))}>
                  Entregar al cliente
                </button>
              )}
              <button className="btn btn--contorno" onClick={() => window.print()}><Icono nombre="impresora" /> Imprimir nota</button>
              {abierta && (
                <button className="btn btn--texto btn--peligro-texto" onClick={() => window.confirm("¿Cancelar la orden? El armazón regresa al inventario. Las devoluciones de dinero se hacen aparte.") && accion(ordenesApi.estado(orden.folio, "CANCELADA"))}>
                  Cancelar orden
                </button>
              )}
              <button className="btn btn--texto" onClick={() => ir("/optica/ordenes")}>Ver todas las órdenes</button>
            </div>
          </div>
        </div>
      </div>

      <NotaOrden orden={orden} />
    </section>
  )
}

/* Nota impresa para el cliente (solo se ve al imprimir; mismo ancho que el ticket de la papelería). */
function NotaOrden({ orden }) {
  return (
    <article className="ticket solo-imprimir">
      <header className="ticket-encabezado">
        <strong>ÓPTICA RODRÍGUEZ</strong>
        <span>Folio {orden.folio}</span>
        <span>{fechaHora(orden.fecha)}</span>
        <span>{orden.clienteNombre}</span>
      </header>
      <table className="ticket-tabla">
        <tbody>
          {orden.partidas.map((p, i) => (
            <tr key={i}>
              <td>
                {p.descripcionArmazon}
                <br />
                <small>{NOMBRES_MICA[p.tipoMica]} {p.material ?? ""}{p.tratamientos ? ` · ${p.tratamientos}` : ""}{p.esRegalo ? " · REGALO 2x1" : ""}</small>
              </td>
              <td className="cifra">{dinero(p.subtotal)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <dl className="ticket-totales">
        <dt>TOTAL</dt>
        <dd className="cifra">{dinero(orden.total)}</dd>
        <dt>Pagado</dt>
        <dd className="cifra">{dinero(orden.pagado)}</dd>
        <dt>Saldo</dt>
        <dd className="cifra">{dinero(orden.saldo)}</dd>
      </dl>
      {orden.fechaEntregaEstimada && <p className="ticket-pie">Entrega estimada: {fechaCorta(orden.fechaEntregaEstimada)}</p>}
      <footer className="ticket-pie">Presente esta nota al recoger sus lentes.</footer>
    </article>
  )
}
