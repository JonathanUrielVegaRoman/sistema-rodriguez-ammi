import { useEffect, useState } from "react"
import { ir } from "../hooks/useRuta"
import { impresionesApi, ordenesApi, productosApi, promocionesApi } from "../services/api"
import Icono from "../components/Icono"
import { enlaceWhatsapp } from "../utils/formato"

const DIAS_PARA_RECORDAR = 3

/*
  INICIO = tablero de pendientes del día. En lugar de una pantalla vacía, muestra lo que
  requiere atención en cada negocio, con accesos directos.

  Promise.allSettled: lanza las 4 peticiones AL MISMO TIEMPO y espera a todas; si una
  falla, las demás se muestran igual.
*/
export default function Inicio() {
  const [datos, setDatos] = useState(null)

  useEffect(() => {
    Promise.allSettled([
      productosApi.porResurtir(),
      impresionesApi.cola(),
      ordenesApi.listar(["LISTA"]),
      promocionesApi.cumpleaneros(0),
    ]).then(([resurtir, cola, listas, cumple]) =>
      setDatos({
        resurtir: resurtir.value ?? [],
        cola: cola.value ?? [],
        listas: listas.value ?? [],
        cumple: cumple.value ?? [],
        error: [resurtir, cola, listas, cumple].some((r) => r.status === "rejected"),
      }),
    )
  }, [])

  const pendientesImpresion = datos?.cola.filter((t) => t.estado === "PENDIENTE").length ?? 0
  // Órdenes LISTAS desde hace 3 días o más: conviene recordarle al cliente.
  const sinRecoger = (datos?.listas ?? []).filter((o) => o.diasLista != null && o.diasLista >= DIAS_PARA_RECORDAR)
  const sinFelicitar = datos?.cumple.filter((c) => c.esHoy && !c.enviado).length ?? 0

  return (
    <section>
      <h1 className="nombre-pagina">Buen día</h1>
      <p className="descripcion-pagina">Esto es lo que hay pendiente hoy.</p>

      {datos?.error && <p className="mensaje-error">Algunos datos no se pudieron cargar. ¿Está prendida la computadora principal?</p>}

      <div className="tarjetas">
        <article className="tarjeta tarjeta--ammi">
          <span className="tarjeta-etiqueta">Papelería</span>
          <span className="tarjeta-titulo">AMMI</span>
          <div className="tarjeta-pendientes">
            <Pendiente icono="impresora" cantidad={pendientesImpresion} texto="por imprimir" alTocar={() => ir("/papeleria/impresiones")} />
            <Pendiente icono="bandeja" cantidad={datos?.resurtir.length ?? 0} texto="por resurtir" alTocar={() => ir("/papeleria/inventario?resurtir=1")} tono="ambar" />
          </div>
          <div className="tarjeta-acciones">
            <button className="btn btn--primario" onClick={() => ir("/papeleria/vender")}>Vender</button>
            <button className="btn btn--contorno" onClick={() => ir("/papeleria/impresiones")}>Impresiones</button>
          </div>
        </article>

        <article className="tarjeta tarjeta--optica">
          <span className="tarjeta-etiqueta">Óptica</span>
          <span className="tarjeta-titulo">RODRÍGUEZ</span>
          <div className="tarjeta-pendientes">
            <Pendiente icono="lentes" cantidad={datos?.listas.length ?? 0} texto="listas para entregar" alTocar={() => ir("/optica/ordenes?estado=LISTA")} />
            <Pendiente icono="regalo" cantidad={sinFelicitar} texto="cumpleaños sin felicitar" alTocar={() => ir("/optica/cumpleanos")} tono="ambar" />
          </div>
          <div className="tarjeta-acciones">
            <button className="btn btn--verde" onClick={() => ir("/optica/ordenes/nueva")}>Nueva orden</button>
            <button className="btn btn--contorno" onClick={() => ir("/optica/clientes")}>Clientes</button>
          </div>
        </article>
      </div>

      {sinRecoger.length > 0 && (
        <div className="panel">
          <h2 className="panel-titulo">Lentes listos que no han recogido</h2>
          <ul className="lista-simple">
            {sinRecoger.map((o) => (
              <li key={o.folio}>
                <a href={`#/optica/ordenes/${o.folio}`}>Folio {o.folio} · {o.clienteNombre}</a>
                <span className="insignia insignia--ambar cifra">hace {o.diasLista} días</span>
                {o.clienteCelular && (
                  <a
                    className="btn btn--verde btn--chico"
                    target="_blank"
                    rel="noopener"
                    href={enlaceWhatsapp(o.clienteCelular, `Hola ${o.clienteNombre.split(" ")[0]}, te recordamos que tus lentes están listos en Óptica RODRÍGUEZ (folio ${o.folio}). ¡Te esperamos!`)}
                  >
                    Recordar
                  </a>
                )}
              </li>
            ))}
          </ul>
        </div>
      )}

      {datos?.resurtir.length > 0 && (
        <div className="panel">
          <h2 className="panel-titulo">Por resurtir en AMMI</h2>
          <ul className="lista-simple">
            {datos.resurtir.slice(0, 6).map((p) => (
              <li key={p.id}>
                <span>
                  <span className="emoji-chico">{p.icono ?? "📦"}</span> {p.nombre}
                </span>
                <span className="insignia insignia--ambar cifra">quedan {p.existencia}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  )
}

function Pendiente({ icono, cantidad, texto, alTocar, tono }) {
  return (
    <button className={"pendiente" + (cantidad > 0 ? " pendiente--hay" + (tono ? " pendiente--" + tono : "") : "")} onClick={alTocar}>
      <Icono nombre={icono} tamano={20} />
      <strong className="cifra">{cantidad}</strong>
      <span>{texto}</span>
    </button>
  )
}
