import { useEffect, useMemo, useState } from "react"
import { productosApi, ventasApi } from "../../services/api"
import { dinero, NOMBRES_PAGO } from "../../utils/formato"
import { EMOJI_GENERICO } from "../../utils/emojis"
import Ticket from "../../components/Ticket"
import BotonImprimir from "../../components/BotonImprimir"
import CampoReferencia from "../../components/CampoReferencia"
import { nuevaClave } from "../../utils/clave"

/*
  PUNTO DE VENTA de la papelería.

  Flujo:
    1. Tocar un producto en la cuadrícula (o buscarlo / escanear su código) → se agrega al carrito.
    2. Ajustar cantidades.
    3. Elegir método de pago (y con cuánto paga, si es efectivo).
    4. Cobrar → el backend descuenta inventario y devuelve el TICKET → imprimir.

  El "carrito" vive solo en la pantalla (estado de React) hasta que se cobra.
*/
export default function Vender() {
  const [productos, setProductos] = useState([])
  const [busqueda, setBusqueda] = useState("")
  const [carrito, setCarrito] = useState([]) // [{ producto, cantidad }]
  const [metodoPago, setMetodoPago] = useState("EFECTIVO")
  const [pagaCon, setPagaCon] = useState("")
  const [referencia, setReferencia] = useState("") // clave de rastreo / autorización (si no es efectivo)
  // Clave única de ESTE cobro: cambia cuando cambia el carrito o el método. Si el Wi-Fi falla y
  // se toca "Cobrar" dos veces con el mismo carrito, el servidor lo cobra una sola vez.
  const claveCobro = useMemo(() => nuevaClave(), [carrito, metodoPago])
  const [error, setError] = useState("")
  const [ticket, setTicket] = useState(null)
  const [cobrando, setCobrando] = useState(false)
  const [reimpresion, setReimpresion] = useState(false) // true = se está viendo un ticket viejo
  const [folioBuscado, setFolioBuscado] = useState("")

  // Cargamos el catálogo; se recarga después de cada venta para ver existencias al día.
  useEffect(() => {
    productosApi.listar().then(setProductos).catch((e) => setError(e.message))
  }, [ticket])

  const texto = busqueda.trim().toLowerCase()
  const visibles = texto
    ? productos.filter((p) => p.nombre.toLowerCase().includes(texto) || p.codigoBarras === busqueda.trim())
    : productos

  const total = carrito.reduce((suma, r) => suma + r.producto.precio * r.cantidad, 0)
  const cambio = pagaCon ? Number(pagaCon) - total : 0

  function agregar(producto) {
    setCarrito((actual) => {
      const existente = actual.find((r) => r.producto.id === producto.id)
      if (existente) {
        return actual.map((r) => (r.producto.id === producto.id ? { ...r, cantidad: r.cantidad + 1 } : r))
      }
      return [...actual, { producto, cantidad: 1 }]
    })
    setBusqueda("")
  }

  function cambiarCantidad(id, cantidad) {
    setCarrito((actual) =>
      cantidad <= 0 ? actual.filter((r) => r.producto.id !== id) : actual.map((r) => (r.producto.id === id ? { ...r, cantidad } : r)),
    )
  }

  // Con lector de código de barras: el lector "teclea" el código y presiona Enter.
  function alPresionarTecla(e) {
    if (e.key === "Enter" && texto && visibles.length === 1) agregar(visibles[0])
  }

  async function cobrar() {
    setError("")
    setCobrando(true)
    try {
      const nuevoTicket = await ventasApi.registrar({
        metodoPago,
        pagaCon: metodoPago === "EFECTIVO" && pagaCon ? Number(pagaCon) : null,
        referenciaPago: metodoPago !== "EFECTIVO" ? referencia.trim() || null : null,
        claveUnica: claveCobro,
        renglones: carrito.map((r) => ({ productoId: r.producto.id, cantidad: r.cantidad })),
      })
      setTicket(nuevoTicket)
      setCarrito([])
      setPagaCon("")
      setReferencia("")
    } catch (e) {
      setError(e.message)
    } finally {
      setCobrando(false)
    }
  }

  // REIMPRIMIR: buscar un ticket anterior por su folio (viene impreso en el ticket).
  async function buscarTicket(e) {
    e.preventDefault()
    setError("")
    try {
      setTicket(await ventasApi.ticket(folioBuscado))
      setReimpresion(true)
      setFolioBuscado("")
    } catch (err) {
      setError(err.message)
    }
  }

  if (ticket) {
    return (
      <section className="angosto">
        {ticket.cancelada ? (
          <p className="mensaje-error">Ticket CANCELADO: {ticket.motivoCancelacion}</p>
        ) : (
          <p className="alerta alerta--exito">{reimpresion ? `Ticket folio ${ticket.folio}` : "Venta registrada"}</p>
        )}
        <Ticket ticket={ticket} />
        <div className="acciones">
          <BotonImprimir mandar={() => ventasApi.imprimir(ticket.folio)} texto="Imprimir ticket" />
          <button className="btn btn--contorno" onClick={() => { setTicket(null); setReimpresion(false) }}>Nueva venta</button>
        </div>
      </section>
    )
  }

  return (
    <section>
      <div className="dos-columnas dos-columnas--venta">
        <div className="panel">
          <input
            className="entrada"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            onKeyDown={alPresionarTecla}
            placeholder="Buscar o escanear código…"
            aria-label="Buscar producto"
          />
          <div className="mosaico">
            {visibles.map((p) => {
              const agotado = !p.servicio && p.existencia <= 0
              return (
                <button key={p.id} className="mosaico-pieza" onClick={() => agregar(p)} disabled={agotado}>
                  <span className="mosaico-emoji">{p.icono ?? EMOJI_GENERICO}</span>
                  <span className="mosaico-nombre">{p.nombre}</span>
                  <span className="mosaico-precio cifra">{dinero(p.precio)}</span>
                  {!p.servicio && (
                    <span className={"mosaico-existencia cifra" + (p.necesitaResurtir ? " mosaico-existencia--baja" : "")}>
                      {agotado ? "agotado" : `hay ${p.existencia}`}
                    </span>
                  )}
                </button>
              )
            })}
          </div>
          {texto && visibles.length === 0 && <p className="texto-tenue">Sin resultados</p>}
        </div>

        <div className="panel panel--pegado">
          <h2 className="panel-titulo">Carrito</h2>
          {carrito.length === 0 && <p className="texto-tenue">Toca un producto para agregarlo.</p>}
          {/* Cada renglón es una rejilla de 4 columnas fijas: ícono | nombre | cantidad | importe.
              Así los botones y los precios quedan alineados aunque los nombres midan distinto. */}
          <ul className="carrito">
            {carrito.map((r) => (
              <li key={r.producto.id} className="carrito-renglon">
                <span className="carrito-emoji" aria-hidden="true">{r.producto.icono ?? EMOJI_GENERICO}</span>
                <span className="carrito-nombre">
                  {r.producto.nombre}
                  <small className="texto-tenue cifra">{dinero(r.producto.precio)} c/u</small>
                </span>
                <span className="cantidad">
                  <button className="btn btn--contorno btn--mini" onClick={() => cambiarCantidad(r.producto.id, r.cantidad - 1)} aria-label="Quitar uno">−</button>
                  <span className="cifra cantidad-numero">{r.cantidad}</span>
                  <button className="btn btn--contorno btn--mini" onClick={() => cambiarCantidad(r.producto.id, r.cantidad + 1)} aria-label="Agregar uno">+</button>
                </span>
                <span className="cifra carrito-importe">{dinero(r.producto.precio * r.cantidad)}</span>
              </li>
            ))}
          </ul>

          <p className="total">
            Total <strong className="cifra">{dinero(total)}</strong>
          </p>

          <div className="formulario">
            <div className="segmentado">
              {Object.entries(NOMBRES_PAGO).map(([valor, nombre]) => (
                <button key={valor} type="button" className={metodoPago === valor ? "activo" : ""} onClick={() => setMetodoPago(valor)}>
                  {nombre}
                </button>
              ))}
            </div>
            <CampoReferencia metodo={metodoPago} valor={referencia} alCambiar={setReferencia} />
            {metodoPago === "EFECTIVO" && (
              <label className="campo">
                <span className="etiqueta">Paga con (opcional)</span>
                <input className="entrada" type="number" min="0" step="0.5" inputMode="decimal" value={pagaCon} onChange={(e) => setPagaCon(e.target.value)} />
                {pagaCon && cambio >= 0 && <span className="texto-tenue">Cambio: {dinero(cambio)}</span>}
              </label>
            )}
            {error && <p className="mensaje-error">{error}</p>}
            <button className="btn btn--primario btn--bloque" onClick={cobrar} disabled={carrito.length === 0 || cobrando}>
              {cobrando ? "Cobrando…" : `Cobrar ${dinero(total)}`}
            </button>
          </div>

          <form className="fila reimprimir" onSubmit={buscarTicket}>
            <input className="entrada cifra" inputMode="numeric" placeholder="Folio de un ticket anterior" value={folioBuscado} onChange={(e) => setFolioBuscado(e.target.value.replace(/\D/g, ""))} aria-label="Folio del ticket" />
            <button className="btn btn--contorno" disabled={!folioBuscado}>Reimprimir</button>
          </form>
        </div>
      </div>
    </section>
  )
}
