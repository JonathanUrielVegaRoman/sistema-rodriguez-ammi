import { useEffect, useState } from "react"
import { productosApi, ventasApi } from "../../services/api"
import { dinero, NOMBRES_PAGO } from "../../utils/formato"
import { EMOJI_GENERICO } from "../../utils/emojis"
import Ticket from "../../components/Ticket"

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
  const [error, setError] = useState("")
  const [ticket, setTicket] = useState(null)
  const [cobrando, setCobrando] = useState(false)

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
        renglones: carrito.map((r) => ({ productoId: r.producto.id, cantidad: r.cantidad })),
      })
      setTicket(nuevoTicket)
      setCarrito([])
      setPagaCon("")
    } catch (e) {
      setError(e.message)
    } finally {
      setCobrando(false)
    }
  }

  if (ticket) {
    return (
      <section className="angosto">
        <p className="alerta alerta--exito">Venta registrada</p>
        <Ticket ticket={ticket} />
        <div className="acciones">
          <button className="btn btn--primario" onClick={() => window.print()}>Imprimir ticket</button>
          <button className="btn btn--contorno" onClick={() => setTicket(null)}>Nueva venta</button>
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
          <ul className="lista-simple">
            {carrito.map((r) => (
              <li key={r.producto.id}>
                <span>
                  <span className="emoji-chico">{r.producto.icono ?? EMOJI_GENERICO}</span> {r.producto.nombre}
                </span>
                <span className="cantidad">
                  <button className="btn btn--contorno btn--mini" onClick={() => cambiarCantidad(r.producto.id, r.cantidad - 1)} aria-label="Quitar uno">−</button>
                  <span className="cifra">{r.cantidad}</span>
                  <button className="btn btn--contorno btn--mini" onClick={() => cambiarCantidad(r.producto.id, r.cantidad + 1)} aria-label="Agregar uno">+</button>
                </span>
                <span className="cifra">{dinero(r.producto.precio * r.cantidad)}</span>
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
        </div>
      </div>
    </section>
  )
}
