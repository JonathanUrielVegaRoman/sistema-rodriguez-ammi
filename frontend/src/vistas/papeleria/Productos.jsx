import { useEffect, useState } from "react"
import { productosApi } from "../../services/api"
import { dinero } from "../../utils/formato"

/*
  INVENTARIO de la papelería: lista, alta, edición y resurtido.

  Patrón muy común en React:
    - "productos" guarda lo que vino del backend.
    - "formulario" guarda lo que el usuario está escribiendo.
    - "editando" dice si el formulario es para crear (null) o para editar (id).
*/
const VACIO = { nombre: "", codigoBarras: "", precio: "", existencia: 0, stockMinimo: 0 }

export default function Productos() {
  const [productos, setProductos] = useState([])
  const [busqueda, setBusqueda] = useState("")
  const [soloResurtir, setSoloResurtir] = useState(false)
  const [formulario, setFormulario] = useState(VACIO)
  const [editando, setEditando] = useState(null)
  const [error, setError] = useState("")

  function cargar() {
    productosApi.listar().then(setProductos).catch((e) => setError(e.message))
  }

  useEffect(cargar, [])

  // Un solo manejador para todos los campos: usa el atributo "name" del input.
  function cambiarCampo(e) {
    setFormulario({ ...formulario, [e.target.name]: e.target.value })
  }

  async function guardar(e) {
    e.preventDefault() // evita que el navegador recargue la página al enviar el formulario
    setError("")
    const datos = {
      ...formulario,
      precio: Number(formulario.precio),
      existencia: Number(formulario.existencia),
      stockMinimo: Number(formulario.stockMinimo),
    }
    try {
      if (editando) await productosApi.actualizar(editando, datos)
      else await productosApi.crear(datos)
      setFormulario(VACIO)
      setEditando(null)
      cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  function editar(p) {
    setEditando(p.id)
    setFormulario({ nombre: p.nombre, codigoBarras: p.codigoBarras ?? "", precio: p.precio, existencia: p.existencia, stockMinimo: p.stockMinimo })
    window.scrollTo({ top: 0, behavior: "smooth" })
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

  const texto = busqueda.trim().toLowerCase()
  const visibles = productos
    .filter((p) => !soloResurtir || p.necesitaResurtir)
    .filter((p) => !texto || p.nombre.toLowerCase().includes(texto))

  return (
    <section>
      <h1 className="nombre-pagina">Inventario</h1>
      <p className="descripcion-pagina">Papelería AMMI</p>

      <form className="panel formulario" onSubmit={guardar}>
        <h2 className="panel-titulo">{editando ? "Editar producto" : "Nuevo producto"}</h2>
        <div className="rejilla">
          <label className="campo campo--ancho">
            <span className="etiqueta">Nombre</span>
            <input className="entrada" name="nombre" value={formulario.nombre} onChange={cambiarCampo} required />
          </label>
          <label className="campo">
            <span className="etiqueta">Código de barras (opcional)</span>
            <input className="entrada" name="codigoBarras" value={formulario.codigoBarras} onChange={cambiarCampo} />
          </label>
          <label className="campo">
            <span className="etiqueta">Precio</span>
            <input className="entrada" name="precio" type="number" min="0" step="0.5" inputMode="decimal" value={formulario.precio} onChange={cambiarCampo} required />
          </label>
          <label className="campo">
            <span className="etiqueta">Existencia</span>
            <input className="entrada" name="existencia" type="number" min="0" inputMode="numeric" value={formulario.existencia} onChange={cambiarCampo} />
          </label>
          <label className="campo">
            <span className="etiqueta">Avisar cuando queden</span>
            <input className="entrada" name="stockMinimo" type="number" min="0" inputMode="numeric" value={formulario.stockMinimo} onChange={cambiarCampo} />
          </label>
        </div>
        {error && <p className="mensaje-error">{error}</p>}
        <div className="acciones">
          <button className="btn btn--primario" type="submit">
            {editando ? "Guardar cambios" : "Agregar producto"}
          </button>
          {editando && (
            <button className="btn btn--contorno" type="button" onClick={() => { setEditando(null); setFormulario(VACIO) }}>
              Cancelar
            </button>
          )}
        </div>
      </form>

      <div className="panel">
        <div className="filtros">
          <input className="entrada" placeholder="Buscar…" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} />
          <label className="casilla">
            <input type="checkbox" checked={soloResurtir} onChange={(e) => setSoloResurtir(e.target.checked)} />
            Solo por resurtir
          </label>
        </div>

        <ul className="lista-productos">
          {visibles.map((p) => (
            <li key={p.id} className="producto">
              <div>
                <strong>{p.nombre}</strong>
                <div className="texto-tenue cifra">
                  {dinero(p.precio)} · aviso en {p.stockMinimo}
                </div>
              </div>
              <span className={"insignia cifra " + (p.necesitaResurtir ? "insignia--ambar" : "insignia--verde")}>{p.existencia} pzas</span>
              <div className="producto-acciones">
                <button className="btn btn--texto" onClick={() => resurtir(p)}>Resurtir</button>
                <button className="btn btn--texto" onClick={() => editar(p)}>Editar</button>
              </div>
            </li>
          ))}
        </ul>
        {visibles.length === 0 && <p className="texto-tenue texto-centro">No hay productos que mostrar.</p>}
      </div>
    </section>
  )
}
