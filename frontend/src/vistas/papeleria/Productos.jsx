import { useEffect, useState } from "react"
import { productosApi } from "../../services/api"
import { dinero, fechaHora } from "../../utils/formato"

const NOMBRES_MOVIMIENTO = { ALTA: "Alta", VENTA: "Venta", CANCELACION: "Cancelación", RESURTIDO: "Resurtido", AJUSTE: "Corrección manual" }
import { EMOJI_GENERICO, sugerirEmoji } from "../../utils/emojis"
import SelectorEmoji from "../../components/SelectorEmoji"
import Modal from "../../components/Modal"

/*
  INVENTARIO de la papelería: lista con ícono, alta, edición y resurtido.

  Patrón muy común en React:
    - "productos" guarda lo que vino del backend.
    - "formulario" guarda lo que el usuario está escribiendo.
    - "editando": null = cerrado, "nuevo" = alta, número = id del producto que se edita.
*/
const VACIO = { nombre: "", codigoBarras: "", precio: "", existencia: 0, stockMinimo: 0, icono: "", servicio: false }

export default function Productos({ consulta }) {
  const [productos, setProductos] = useState([])
  const [busqueda, setBusqueda] = useState("")
  const [soloResurtir, setSoloResurtir] = useState(consulta?.resurtir === "1")
  const [formulario, setFormulario] = useState(VACIO)
  const [editando, setEditando] = useState(null)
  const [error, setError] = useState("")
  const [aviso, setAviso] = useState("")
  const [historial, setHistorial] = useState(null) // { producto, movimientos } del producto abierto

  function cargar() {
    productosApi.listar().then(setProductos).catch((e) => setError(e.message))
  }

  useEffect(cargar, [])

  function cambiarCampo(e) {
    const { name, value, type, checked } = e.target
    const nuevo = { ...formulario, [name]: type === "checkbox" ? checked : value }
    // Si aún no eligió ícono, sugerimos uno según el nombre ("cuaderno" → 📓).
    if (name === "nombre" && !formulario.iconoElegido) nuevo.icono = sugerirEmoji(value) ?? ""
    setFormulario(nuevo)
  }

  function abrirNuevo() {
    setError("")
    setFormulario(VACIO)
    setEditando("nuevo")
  }

  function editar(p) {
    setError("")
    setEditando(p.id)
    setFormulario({
      nombre: p.nombre, codigoBarras: p.codigoBarras ?? "", precio: p.precio, existencia: p.existencia,
      stockMinimo: p.stockMinimo, icono: p.icono ?? "", servicio: p.servicio, iconoElegido: true,
    })
  }

  async function guardar(e) {
    e.preventDefault() // evita que el navegador recargue la página al enviar el formulario
    setError("")
    const datos = {
      nombre: formulario.nombre,
      codigoBarras: formulario.codigoBarras,
      precio: Number(formulario.precio),
      existencia: Number(formulario.existencia),
      stockMinimo: Number(formulario.stockMinimo),
      icono: formulario.icono || null,
      servicio: formulario.servicio,
    }
    try {
      if (editando === "nuevo") await productosApi.crear(datos)
      else await productosApi.actualizar(editando, datos)
      setEditando(null)
      cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  async function resurtir(p) {
    const cantidad = Number(window.prompt(`¿Cuántas piezas llegaron de "${p.nombre}"?`))
    if (!cantidad || cantidad <= 0) return
    try {
      await productosApi.resurtir(p.id, cantidad)
      cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  async function eliminar() {
    if (!window.confirm(`¿Eliminar "${formulario.nombre}" del inventario?`)) return
    setError("")
    try {
      const r = await productosApi.desactivar(editando)
      setAviso(r?.mensaje ?? "")
      setEditando(null)
      cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  // Historial: cada entrada y salida del producto (ver MovimientoInventario.java en el backend).
  async function verHistorial(p) {
    setError("")
    try {
      setHistorial({ producto: p, movimientos: await productosApi.movimientos(p.id) })
    } catch (err) {
      setError(err.message)
    }
  }

  const texto = busqueda.trim().toLowerCase()
  const visibles = productos
    .filter((p) => !soloResurtir || p.necesitaResurtir)
    .filter((p) => !texto || p.nombre.toLowerCase().includes(texto))

  return (
    <section>
      <div className="titulo-con-accion">
        <h1 className="titulo-seccion">Inventario</h1>
        <button className="btn btn--primario" onClick={abrirNuevo}>+ Producto</button>
      </div>

      {aviso && <p className="alerta alerta--exito">{aviso}</p>}

      <div className="panel">
        <div className="filtros">
          <input className="entrada" placeholder="Buscar…" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} />
          <label className="casilla">
            <input type="checkbox" checked={soloResurtir} onChange={(e) => setSoloResurtir(e.target.checked)} />
            Solo por resurtir
          </label>
        </div>
        {error && !editando && <p className="mensaje-error">{error}</p>}

        <ul className="lista-productos">
          {visibles.map((p) => (
            <li key={p.id} className="producto">
              <span className="producto-emoji" aria-hidden="true">{p.icono ?? EMOJI_GENERICO}</span>
              <div className="producto-datos">
                <strong>{p.nombre}</strong>
                <div className="texto-tenue cifra">
                  {dinero(p.precio)}
                  {p.servicio ? " · servicio (sin inventario)" : ` · aviso en ${p.stockMinimo}`}
                </div>
              </div>
              {!p.servicio && (
                <span className={"insignia cifra " + (p.necesitaResurtir ? "insignia--ambar" : "insignia--verde")}>{p.existencia} pzas</span>
              )}
              <div className="producto-acciones">
                {!p.servicio && <button className="btn btn--texto" onClick={() => resurtir(p)}>Resurtir</button>}
                {!p.servicio && <button className="btn btn--texto" onClick={() => verHistorial(p)}>Historial</button>}
                <button className="btn btn--texto" onClick={() => editar(p)}>Editar</button>
              </div>
            </li>
          ))}
        </ul>
        {visibles.length === 0 && <p className="texto-tenue texto-centro">No hay productos que mostrar.</p>}
      </div>

      {historial && (
        <Modal titulo={`Historial · ${historial.producto.nombre}`} alCerrar={() => setHistorial(null)}>
          <p className="texto-tenue">Últimos movimientos, del más reciente al más antiguo. Hoy hay {historial.producto.existencia} piezas.</p>
          {historial.movimientos.length === 0 && <p className="texto-tenue">Sin movimientos registrados todavía.</p>}
          <div className="tabla-desplazable">
            <table className="tabla cifra">
              <thead>
                <tr><th>Cuándo</th><th>Qué pasó</th><th>Cantidad</th><th>Quedaron</th></tr>
              </thead>
              <tbody>
                {historial.movimientos.map((m, i) => (
                  <tr key={i}>
                    <th>{fechaHora(m.fecha)}</th>
                    <td>{NOMBRES_MOVIMIENTO[m.tipo]}{m.nota && m.tipo !== "ALTA" ? ` · ${m.nota}` : ""}</td>
                    <td className={m.cantidad > 0 ? "texto-verde" : "texto-ambar"}>{m.cantidad > 0 ? "+" : "−"}{Math.abs(m.cantidad)}</td>
                    <td>{m.existenciaResultante}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Modal>
      )}

      {editando && (
        <Modal titulo={editando === "nuevo" ? "Nuevo producto" : "Editar producto"} alCerrar={() => setEditando(null)}>
          <form className="formulario" onSubmit={guardar}>
            <div className="campo-con-emoji">
              <SelectorEmoji valor={formulario.icono} alCambiar={(icono) => setFormulario({ ...formulario, icono, iconoElegido: true })} />
              <label className="campo">
                <span className="etiqueta">Nombre</span>
                <input className="entrada" name="nombre" value={formulario.nombre} onChange={cambiarCampo} required autoFocus />
              </label>
            </div>
            <div className="rejilla">
              <label className="campo">
                <span className="etiqueta">Precio</span>
                <input className="entrada" name="precio" type="number" min="0" step="0.5" inputMode="decimal" value={formulario.precio} onChange={cambiarCampo} required />
              </label>
              <label className="campo">
                <span className="etiqueta">Código de barras (opcional)</span>
                <input className="entrada" name="codigoBarras" value={formulario.codigoBarras} onChange={cambiarCampo} />
              </label>
              {!formulario.servicio && (
                <>
                  <label className="campo">
                    <span className="etiqueta">Existencia</span>
                    <input className="entrada" name="existencia" type="number" min="0" inputMode="numeric" value={formulario.existencia} onChange={cambiarCampo} />
                  </label>
                  <label className="campo">
                    <span className="etiqueta">Avisar cuando queden</span>
                    <input className="entrada" name="stockMinimo" type="number" min="0" inputMode="numeric" value={formulario.stockMinimo} onChange={cambiarCampo} />
                  </label>
                </>
              )}
            </div>
            <label className="casilla">
              <input type="checkbox" name="servicio" checked={formulario.servicio} onChange={cambiarCampo} />
              Es un servicio (copias, engargolado…): no lleva inventario
            </label>
            {error && <p className="mensaje-error">{error}</p>}
            <button className="btn btn--primario btn--bloque" type="submit">
              {editando === "nuevo" ? "Agregar producto" : "Guardar cambios"}
            </button>
          </form>
          {editando !== "nuevo" && (
            <div className="zona-peligro">
              <p className="texto-tenue">
                Si el producto nunca se vendió, se borra por completo. Si ya tiene ventas, solo se quita del
                inventario y los tickets anteriores se conservan.
              </p>
              <button className="btn btn--peligro" type="button" onClick={eliminar}>Eliminar producto</button>
            </div>
          )}
        </Modal>
      )}
    </section>
  )
}
