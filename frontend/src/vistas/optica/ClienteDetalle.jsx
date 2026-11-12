import { useEffect, useState } from "react"
import { clientesApi } from "../../services/api"
import { ir } from "../../hooks/useRuta"
import { fechaCorta } from "../../utils/formato"
import FormCliente from "./FormCliente"
import FormReceta from "./FormReceta"
import TablaReceta from "../../components/TablaReceta"
import TarjetaOrden from "../../components/TarjetaOrden"
import Modal from "../../components/Modal"

/*
  FICHA DEL CLIENTE: sus datos, su historial de graduaciones y sus órdenes.
  Recibe "id" de la dirección: #/optica/clientes/7  → id = "7"
*/
export default function ClienteDetalle({ id }) {
  const [cliente, setCliente] = useState(null)
  const [recetas, setRecetas] = useState([])
  const [ordenes, setOrdenes] = useState([])
  const [editando, setEditando] = useState(false)
  const [nuevaReceta, setNuevaReceta] = useState(false)
  const [error, setError] = useState("")
  const [aviso, setAviso] = useState("")

  useEffect(() => {
    Promise.all([clientesApi.obtener(id), clientesApi.recetas(id), clientesApi.ordenes(id)])
      .then(([c, r, o]) => {
        setCliente(c)
        setRecetas(r)
        setOrdenes(o)
      })
      .catch((e) => setError(e.message))
  }, [id])

  async function eliminar() {
    const texto = ordenes.length > 0 ? `¿Archivar a ${cliente.nombre}?` : `¿Eliminar a ${cliente.nombre} y sus graduaciones? No se puede deshacer.`
    if (!window.confirm(texto)) return
    try {
      const r = await clientesApi.eliminar(cliente.id)
      if (r.resultado === "ELIMINADO") {
        ir("/optica/clientes")
      } else {
        setCliente({ ...cliente, activo: false })
        setEditando(false)
        setAviso(r.mensaje)
      }
    } catch (e) {
      setError(e.message)
    }
  }

  async function restaurar() {
    try {
      setCliente(await clientesApi.restaurar(cliente.id))
      setAviso("")
    } catch (e) {
      setError(e.message)
    }
  }

  if (error) return <p className="mensaje-error">{error}</p>
  if (!cliente) return <p className="texto-tenue">Cargando…</p>

  return (
    <section>
      <div className="titulo-con-accion">
        <div>
          <h1 className="titulo-seccion">{cliente.nombre}</h1>
          <p className="texto-tenue cifra">
            {cliente.celular ?? "sin celular"}
            {cliente.fechaNacimiento && ` · nació el ${fechaCorta(cliente.fechaNacimiento)} (${cliente.edad} años)`}
            {!cliente.aceptaPromociones && " · no acepta promociones"}
          </p>
        </div>
        <div className="acciones acciones--sin-margen">
          <button className="btn btn--contorno" onClick={() => setEditando(true)}>Editar</button>
          {cliente.activo && <button className="btn btn--verde" onClick={() => ir(`/optica/ordenes/nueva?cliente=${cliente.id}`)}>Nueva orden</button>}
        </div>
      </div>
      {!cliente.activo && (
        <div className="aviso-barra">
          <span>Cliente archivado: no aparece en búsquedas ni recibe felicitaciones. Sus órdenes se conservan.</span>
          <button className="btn btn--texto" onClick={restaurar}>Restaurar</button>
        </div>
      )}
      {aviso && <p className="alerta alerta--exito">{aviso}</p>}
      {cliente.notas && <p className="aviso-barra">{cliente.notas}</p>}

      <div className="dos-columnas">
        <div className="panel">
          <div className="titulo-con-accion">
            <h2 className="panel-titulo">Graduaciones</h2>
            <button className="btn btn--texto" onClick={() => setNuevaReceta(true)}>+ Nueva graduación</button>
          </div>
          {recetas.length === 0 && <p className="texto-tenue">Aún no tiene graduaciones registradas.</p>}
          {recetas.map((r, i) => (
            <div key={r.id} className={i > 0 ? "receta-anterior" : ""}>
              {i === 1 && <p className="etiqueta">Anteriores</p>}
              <TablaReceta receta={r} />
            </div>
          ))}
        </div>

        <div className="panel">
          <h2 className="panel-titulo">Órdenes</h2>
          {ordenes.length === 0 && <p className="texto-tenue">Sin órdenes todavía.</p>}
          <div className="lista-ordenes">
            {ordenes.map((o) => (
              <TarjetaOrden key={o.folio} orden={o} />
            ))}
          </div>
        </div>
      </div>

      {editando && (
        <Modal titulo="Editar cliente" alCerrar={() => setEditando(false)}>
          <FormCliente inicial={cliente} alGuardar={(c) => { setCliente(c); setEditando(false) }} />
          {cliente.activo && (
            <div className="zona-peligro">
              <p className="texto-tenue">
                {ordenes.length > 0
                  ? "Tiene órdenes: se archivará (deja de aparecer en búsquedas, pero sus ventas se conservan)."
                  : "No tiene órdenes: se borrará junto con sus graduaciones. No se puede deshacer."}
              </p>
              <button className="btn btn--peligro" onClick={eliminar}>
                {ordenes.length > 0 ? "Archivar cliente" : "Eliminar cliente"}
              </button>
            </div>
          )}
        </Modal>
      )}
      {nuevaReceta && (
        <Modal titulo="Nueva graduación" alCerrar={() => setNuevaReceta(false)} ancho>
          <FormReceta clienteId={cliente.id} alGuardar={(r) => { setRecetas([r, ...recetas]); setNuevaReceta(false) }} />
        </Modal>
      )}
    </section>
  )
}
