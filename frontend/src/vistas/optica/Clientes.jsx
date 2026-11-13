import { useEffect, useState } from "react"
import { clientesApi } from "../../services/api"
import { ir } from "../../hooks/useRuta"
import FormCliente from "./FormCliente"
import Modal from "../../components/Modal"

/*
  CLIENTES de la óptica: LISTA con buscador (no cuadrícula).
  ¿Por qué lista aquí y cuadrícula en armazones? A un cliente se le busca por NOMBRE o
  CELULAR (texto); a un armazón se le reconoce por cómo SE VE (foto).

  SELECCIÓN MÚLTIPLE: con "Seleccionar" aparecen casillas para borrar varios clientes de una
  vez (los que tienen órdenes se archivan en lugar de borrarse; ver ClienteService.eliminar).
*/
export default function Clientes() {
  const [clientes, setClientes] = useState([])
  const [texto, setTexto] = useState("")
  const [nuevo, setNuevo] = useState(false)
  const [error, setError] = useState("")
  const [aviso, setAviso] = useState("")
  const [seleccionando, setSeleccionando] = useState(false)
  const [elegidos, setElegidos] = useState(new Set()) // ids marcados
  const [recarga, setRecarga] = useState(0)

  useEffect(() => {
    const espera = setTimeout(() => {
      clientesApi.buscar(texto).then(setClientes).catch((e) => setError(e.message))
    }, 250)
    return () => clearTimeout(espera)
  }, [texto, recarga])

  const hoy = new Date()
  const cumpleHoy = (f) => f && Number(f.slice(5, 7)) === hoy.getMonth() + 1 && Number(f.slice(8, 10)) === hoy.getDate()

  function alternar(id) {
    const nuevo = new Set(elegidos)
    if (nuevo.has(id)) nuevo.delete(id)
    else nuevo.add(id)
    setElegidos(nuevo)
  }

  const todosMarcados = clientes.length > 0 && clientes.every((c) => elegidos.has(c.id))

  function marcarTodos() {
    setElegidos(todosMarcados ? new Set() : new Set(clientes.map((c) => c.id)))
  }

  function terminarSeleccion() {
    setSeleccionando(false)
    setElegidos(new Set())
  }

  async function eliminarElegidos() {
    const n = elegidos.size
    if (!window.confirm(`¿Eliminar ${n} cliente${n > 1 ? "s" : ""}?\n\nLos que tengan órdenes se ARCHIVAN (sus ventas se conservan). Los demás se borran con sus graduaciones y no se puede deshacer.`)) return
    setError("")
    try {
      const r = await clientesApi.eliminarVarios([...elegidos])
      const partes = []
      if (r.eliminados) partes.push(`${r.eliminados} eliminado${r.eliminados > 1 ? "s" : ""}`)
      if (r.archivados) partes.push(`${r.archivados} archivado${r.archivados > 1 ? "s" : ""} (tenían órdenes)`)
      setAviso("Listo: " + partes.join(" y ") + ".")
      terminarSeleccion()
      setRecarga((x) => x + 1)
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <section>
      <div className="titulo-con-accion">
        <h1 className="titulo-seccion">Clientes</h1>
        <div className="acciones acciones--sin-margen">
          {seleccionando ? (
            <button className="btn btn--contorno" onClick={terminarSeleccion}>Terminar</button>
          ) : (
            <button className="btn btn--contorno" onClick={() => { setAviso(""); setSeleccionando(true) }}>Seleccionar</button>
          )}
          <button className="btn btn--verde" onClick={() => setNuevo(true)}>+ Cliente</button>
        </div>
      </div>

      {aviso && <p className="alerta alerta--exito">{aviso}</p>}

      <div className="panel">
        <input className="entrada" placeholder="Buscar por nombre o celular…" value={texto} onChange={(e) => setTexto(e.target.value)} aria-label="Buscar cliente" />
        {error && <p className="mensaje-error">{error}</p>}

        {seleccionando ? (
          <label className="casilla">
            <input type="checkbox" checked={todosMarcados} onChange={marcarTodos} />
            Marcar todos los de la lista ({clientes.length})
          </label>
        ) : (
          !texto && <p className="texto-tenue">Últimos registrados</p>
        )}

        <ul className="lista-clientes">
          {clientes.map((c) => (
            <li key={c.id}>
              <button
                className={seleccionando && elegidos.has(c.id) ? "elegido" : ""}
                onClick={() => (seleccionando ? alternar(c.id) : ir(`/optica/clientes/${c.id}`))}
                aria-pressed={seleccionando ? elegidos.has(c.id) : undefined}
              >
                {seleccionando ? (
                  <input type="checkbox" className="casilla-cliente" checked={elegidos.has(c.id)} readOnly tabIndex={-1} aria-hidden="true" />
                ) : (
                  <span className="avatar" aria-hidden="true">{c.nombre.charAt(0)}</span>
                )}
                <span className="cliente-datos">
                  <strong>{c.nombre}</strong>
                  <span className="texto-tenue cifra">
                    {c.celular ?? "sin celular"}
                    {c.edad != null ? ` · ${c.edad} años` : ""}
                  </span>
                </span>
                {cumpleHoy(c.fechaNacimiento) && <span className="insignia insignia--ambar">🎂 Cumple hoy</span>}
              </button>
            </li>
          ))}
        </ul>
        {clientes.length === 0 && <p className="texto-tenue texto-centro">No se encontraron clientes.</p>}
      </div>

      {/* Barra fija abajo mientras hay clientes marcados */}
      {seleccionando && elegidos.size > 0 && (
        <div className="barra-seleccion">
          <span>
            <strong className="cifra">{elegidos.size}</strong> seleccionado{elegidos.size > 1 ? "s" : ""}
          </span>
          <button className="btn btn--peligro" onClick={eliminarElegidos}>Eliminar seleccionados</button>
        </div>
      )}

      {nuevo && (
        <Modal titulo="Nuevo cliente" alCerrar={() => setNuevo(false)}>
          <FormCliente alGuardar={(c) => ir(`/optica/clientes/${c.id}`)} />
        </Modal>
      )}
    </section>
  )
}
