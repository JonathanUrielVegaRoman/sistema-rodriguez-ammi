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

  useEffect(() => {
    Promise.all([clientesApi.obtener(id), clientesApi.recetas(id), clientesApi.ordenes(id)])
      .then(([c, r, o]) => {
        setCliente(c)
        setRecetas(r)
        setOrdenes(o)
      })
      .catch((e) => setError(e.message))
  }, [id])

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
          <button className="btn btn--verde" onClick={() => ir(`/optica/ordenes/nueva?cliente=${cliente.id}`)}>Nueva orden</button>
        </div>
      </div>
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
