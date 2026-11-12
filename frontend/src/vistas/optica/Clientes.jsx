import { useEffect, useState } from "react"
import { clientesApi } from "../../services/api"
import { ir } from "../../hooks/useRuta"
import FormCliente from "./FormCliente"
import Modal from "../../components/Modal"

/*
  CLIENTES de la óptica: LISTA con buscador (no cuadrícula).
  ¿Por qué lista aquí y cuadrícula en armazones? A un cliente se le busca por NOMBRE o
  CELULAR (texto); a un armazón se le reconoce por cómo SE VE (foto).
*/
export default function Clientes() {
  const [clientes, setClientes] = useState([])
  const [texto, setTexto] = useState("")
  const [nuevo, setNuevo] = useState(false)
  const [error, setError] = useState("")

  useEffect(() => {
    const espera = setTimeout(() => {
      clientesApi.buscar(texto).then(setClientes).catch((e) => setError(e.message))
    }, 250)
    return () => clearTimeout(espera)
  }, [texto])

  const hoy = new Date()
  const cumpleHoy = (f) => f && Number(f.slice(5, 7)) === hoy.getMonth() + 1 && Number(f.slice(8, 10)) === hoy.getDate()

  return (
    <section>
      <div className="titulo-con-accion">
        <h1 className="titulo-seccion">Clientes</h1>
        <button className="btn btn--verde" onClick={() => setNuevo(true)}>+ Cliente</button>
      </div>

      <div className="panel">
        <input className="entrada" placeholder="Buscar por nombre o celular…" value={texto} onChange={(e) => setTexto(e.target.value)} aria-label="Buscar cliente" />
        {error && <p className="mensaje-error">{error}</p>}
        {!texto && <p className="texto-tenue">Últimos registrados</p>}
        <ul className="lista-clientes">
          {clientes.map((c) => (
            <li key={c.id}>
              <button onClick={() => ir(`/optica/clientes/${c.id}`)}>
                <span className="avatar" aria-hidden="true">{c.nombre.charAt(0)}</span>
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

      {nuevo && (
        <Modal titulo="Nuevo cliente" alCerrar={() => setNuevo(false)}>
          <FormCliente alGuardar={(c) => ir(`/optica/clientes/${c.id}`)} />
        </Modal>
      )}
    </section>
  )
}
