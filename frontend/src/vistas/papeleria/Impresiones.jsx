import { useEffect, useState } from "react"
import { impresionesApi } from "../../services/api"
import { fechaHora, NOMBRES_PAGO } from "../../utils/formato"
import { ir } from "../../hooks/useRuta"
import Ticket from "../../components/Ticket"
import Icono from "../../components/Icono"

/*
  COLA DEL BUZÓN DE IMPRESIÓN (lado del personal).

  Cada tarjeta es un turno que mandó un cliente desde su celular:
    1. "Abrir" cada archivo → se abre en otra pestaña → Imprimir (Ctrl+P).
    2. "Ya lo imprimí" → anotar cuántas hojas salieron en total.
    3. "Cobrar y entregar" → se registra como venta de papelería, sale el ticket
       y LOS ARCHIVOS SE BORRAN (privacidad del cliente).

  La lista se actualiza sola cada 15 segundos para ver los turnos nuevos.
*/
export default function Impresiones() {
  const [cola, setCola] = useState([])
  const [error, setError] = useState("")
  const [ticket, setTicket] = useState(null)

  function cargar() {
    impresionesApi.cola().then(setCola).catch((e) => setError(e.message))
  }

  useEffect(() => {
    cargar()
    const reloj = setInterval(cargar, 15_000)
    return () => clearInterval(reloj)
  }, [])

  async function accion(promesa) {
    setError("")
    try {
      const r = await promesa
      cargar()
      return r
    } catch (e) {
      setError(e.message)
    }
  }

  if (ticket) {
    return (
      <section className="angosto">
        <p className="alerta alerta--exito">Cobrado. Los archivos del cliente se borraron.</p>
        <Ticket ticket={ticket} />
        <div className="acciones">
          <button className="btn btn--primario" onClick={() => window.print()}>Imprimir ticket</button>
          <button className="btn btn--contorno" onClick={() => setTicket(null)}>Volver a la cola</button>
        </div>
      </section>
    )
  }

  return (
    <section>
      <div className="titulo-con-accion">
        <h1 className="titulo-seccion">Impresiones</h1>
        <button className="btn btn--contorno" onClick={() => ir("/papeleria/cartel")}>
          <Icono nombre="imagen" /> Cartel con QR
        </button>
      </div>
      <p className="texto-tenue">Los clientes mandan sus archivos escaneando el QR del mostrador. Esta lista se actualiza sola.</p>

      {error && <p className="mensaje-error">{error}</p>}
      {cola.length === 0 && <div className="panel vacio">No hay turnos en espera.</div>}

      <div className="cuadricula-turnos">
        {cola.map((t) => (
          <TarjetaTurno key={t.id} t={t} accion={accion} alCobrar={setTicket} />
        ))}
      </div>
    </section>
  )
}

function TarjetaTurno({ t, accion, alCobrar }) {
  const [hojas, setHojas] = useState(t.hojas ?? "")
  const [metodo, setMetodo] = useState("EFECTIVO")

  async function cobrar() {
    const ticket = await accion(impresionesApi.cobrar(t.id, metodo, null))
    if (ticket) alCobrar(ticket)
  }

  return (
    <article className={"panel turno-tarjeta" + (t.estado === "IMPRESO" ? " turno-tarjeta--impreso" : "")}>
      <div className="turno-cabecera">
        <strong className="turno-chico">{t.turno}</strong>
        <span className={"insignia " + (t.estado === "IMPRESO" ? "insignia--verde" : "insignia--ambar")}>
          {t.estado === "IMPRESO" ? "Impreso" : "En espera"}
        </span>
      </div>
      <p className="texto-tenue">
        {fechaHora(t.fecha)} · {t.copias} {t.copias === 1 ? "copia" : "copias"} · {t.color ? "Color" : "B/N"}
        {t.dobleCara ? " · doble cara" : ""}
      </p>
      {t.notas && <p className="aviso-barra">{t.notas}</p>}

      <ul className="lista-archivos">
        {t.archivos.map((a) => (
          <li key={a.id}>
            <button className="btn btn--contorno btn--chico" disabled={!a.disponible} onClick={() => accion(impresionesApi.abrirArchivo(t.id, a.id))}>
              <Icono nombre="documento" /> {a.nombre}
            </button>
          </li>
        ))}
      </ul>

      {t.estado === "PENDIENTE" ? (
        <div className="fila">
          <input className="entrada" type="number" min="1" inputMode="numeric" placeholder="Hojas impresas" value={hojas} onChange={(e) => setHojas(e.target.value)} />
          <button className="btn btn--primario" disabled={!hojas} onClick={() => accion(impresionesApi.impreso(t.id, Number(hojas)))}>
            Ya lo imprimí
          </button>
        </div>
      ) : (
        <>
          <p>
            <strong className="cifra">{t.hojas}</strong> hojas {t.color ? "a color" : "en B/N"}
          </p>
          <div className="segmentado">
            {Object.entries(NOMBRES_PAGO).map(([valor, nombre]) => (
              <button key={valor} type="button" className={metodo === valor ? "activo" : ""} onClick={() => setMetodo(valor)}>
                {nombre}
              </button>
            ))}
          </div>
          <button className="btn btn--verde btn--bloque" onClick={cobrar}>Cobrar y entregar</button>
        </>
      )}
      <button
        className="btn btn--texto btn--peligro-texto"
        onClick={() => window.confirm(`¿Cancelar el turno ${t.turno}? Sus archivos se borrarán.`) && accion(impresionesApi.cancelar(t.id))}
      >
        Cancelar turno
      </button>
    </article>
  )
}

