import { useEffect, useState } from "react"
import { promocionesApi } from "../../services/api"
import { fechaCorta } from "../../utils/formato"
import Icono from "../../components/Icono"

/*
  CUMPLEAÑOS: quién cumple hoy (con su cupón y botón de WhatsApp) y quién cumple en los
  próximos 7 días.

  El envío NO es automático (eso requiere la API de WhatsApp Business, con trámite y costo):
  el botón abre WhatsApp con el mensaje ya escrito; solo hay que tocar "Enviar" y luego
  marcarlo aquí como enviado.

  Solo aparecen clientes que ACEPTARON recibir promociones.
*/
export default function Cumpleanos() {
  const [lista, setLista] = useState([])
  const [error, setError] = useState("")

  function cargar() {
    promocionesApi.cumpleaneros(7).then(setLista).catch((e) => setError(e.message))
  }

  useEffect(cargar, [])

  async function marcar(cuponId) {
    try {
      await promocionesApi.marcarEnviado(cuponId)
      cargar()
    } catch (e) {
      setError(e.message)
    }
  }

  const hoy = lista.filter((c) => c.esHoy)
  const proximos = lista.filter((c) => !c.esHoy)

  return (
    <section>
      <h1 className="titulo-seccion">Cumpleaños</h1>
      {error && <p className="mensaje-error">{error}</p>}

      <div className="panel">
        <h2 className="panel-titulo">🎂 Hoy</h2>
        {hoy.length === 0 && <p className="texto-tenue">Nadie cumple años hoy.</p>}
        <ul className="lista-cumple">
          {hoy.map((c) => (
            <li key={c.clienteId}>
              <div>
                <a href={`#/optica/clientes/${c.clienteId}`}><strong>{c.nombre}</strong></a>
                <div className="texto-tenue">
                  Cumple {c.cumpleAnos} · cupón <strong className="cifra">{c.codigo}</strong> ({c.porcentaje}% hasta {fechaCorta(c.vigenteHasta)})
                </div>
              </div>
              {c.enviado ? (
                <span className="insignia insignia--verde"><Icono nombre="check" tamano={14} /> Felicitado</span>
              ) : c.enlaceWhatsapp ? (
                <div className="acciones acciones--sin-margen">
                  <a className="btn btn--verde btn--chico" href={c.enlaceWhatsapp} target="_blank" rel="noopener">
                    <Icono nombre="mensaje" /> WhatsApp
                  </a>
                  <button className="btn btn--texto" onClick={() => marcar(c.cuponId)}>Ya lo envié</button>
                </div>
              ) : (
                <span className="texto-tenue">Sin celular registrado</span>
              )}
            </li>
          ))}
        </ul>
      </div>

      <div className="panel">
        <h2 className="panel-titulo">Próximos 7 días</h2>
        {proximos.length === 0 && <p className="texto-tenue">Sin cumpleaños próximos.</p>}
        <ul className="lista-simple">
          {proximos.map((c) => (
            <li key={c.clienteId + c.fecha}>
              <a href={`#/optica/clientes/${c.clienteId}`}>{c.nombre}</a>
              <span className="texto-tenue">{fechaCorta(c.fecha)} · cumple {c.cumpleAnos}</span>
            </li>
          ))}
        </ul>
      </div>
    </section>
  )
}
