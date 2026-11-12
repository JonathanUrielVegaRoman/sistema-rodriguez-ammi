import { useEffect, useState } from "react"
import { productosApi } from "../services/api"

/*
  Pantalla de bienvenida: dos tarjetas (una por negocio) y un aviso de lo que se está agotando.

  useEffect(() => { ... }, []) = "ejecuta esto UNA vez, cuando la pantalla aparece".
  Aquí lo usamos para pedirle al backend los productos por resurtir.
*/
export default function Inicio({ irA }) {
  const [porResurtir, setPorResurtir] = useState([])
  const [sinConexion, setSinConexion] = useState(false)

  useEffect(() => {
    productosApi
      .porResurtir()
      .then(setPorResurtir)
      .catch(() => setSinConexion(true))
  }, [])

  return (
    <section>
      <h1 className="nombre-pagina">Buen día</h1>
      <p className="descripcion-pagina">¿En qué negocio vas a trabajar?</p>

      {sinConexion && (
        <p className="mensaje-error">No hay conexión con el servidor. ¿Está prendida la computadora principal?</p>
      )}

      <div className="tarjetas">
        <button className="tarjeta tarjeta--ammi" onClick={() => irA("vender")}>
          <span className="tarjeta-etiqueta">Papelería</span>
          <span className="tarjeta-titulo">AMMI</span>
          <span className="tarjeta-texto">Vender, imprimir ticket, inventario y corte del día</span>
        </button>
        <button className="tarjeta tarjeta--optica" onClick={() => irA("optica")}>
          <span className="tarjeta-etiqueta">Óptica</span>
          <span className="tarjeta-titulo">RODRÍGUEZ</span>
          <span className="tarjeta-texto">Clientes, graduaciones, armazones y promociones</span>
        </button>
      </div>

      {porResurtir.length > 0 && (
        <div className="panel">
          <h2 className="panel-titulo">Por resurtir en AMMI ({porResurtir.length})</h2>
          <ul className="lista-simple">
            {porResurtir.map((p) => (
              <li key={p.id}>
                <span>{p.nombre}</span>
                <span className="insignia insignia--ambar cifra">quedan {p.existencia}</span>
              </li>
            ))}
          </ul>
          <button className="btn btn--texto" onClick={() => irA("productos")}>
            Ir al inventario
          </button>
        </div>
      )}
    </section>
  )
}
