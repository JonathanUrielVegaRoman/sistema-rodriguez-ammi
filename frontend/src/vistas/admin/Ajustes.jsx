import { useEffect, useState } from "react"
import { accesoApi, clientesApi, promocionesApi, tratamientosApi } from "../../services/api"
import { dinero, fechaCorta } from "../../utils/formato"
import PuertaAdmin from "../../components/PuertaAdmin"

/*
  AJUSTES DEL DUEÑO:
    1. Tratamientos: precio y si entran en el lente de regalo (2x1).
    2. Cupones de descuento especial (para un cliente o para cualquiera).
    3. Seguridad: sacar a todos los celulares (si se pierde o roban uno).
*/
export default function Ajustes() {
  return (
    <PuertaAdmin>
      <h1 className="titulo-seccion">Ajustes</h1>
      <Tratamientos />
      <Cupones />
      <Seguridad />
    </PuertaAdmin>
  )
}

function Tratamientos() {
  const [lista, setLista] = useState([])
  const [nuevo, setNuevo] = useState({ nombre: "", precio: "", aplicaEnPromocion: false, activo: true })
  const [error, setError] = useState("")

  const cargar = () => tratamientosApi.todos().then(setLista).catch((e) => setError(e.message))
  useEffect(() => { cargar() }, [])

  async function guardar(t) {
    setError("")
    try {
      await tratamientosApi.actualizar(t.id, { ...t, precio: Number(t.precio) })
      cargar()
    } catch (e) {
      setError(e.message)
    }
  }

  async function crear(e) {
    e.preventDefault()
    setError("")
    try {
      await tratamientosApi.crear({ ...nuevo, precio: Number(nuevo.precio) })
      setNuevo({ nombre: "", precio: "", aplicaEnPromocion: false, activo: true })
      cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  const cambiar = (id, cambios) => setLista(lista.map((t) => (t.id === id ? { ...t, ...cambios } : t)))

  return (
    <div className="panel">
      <h2 className="panel-titulo">Tratamientos de micas</h2>
      <p className="texto-tenue">"Entra en 2x1" = se permite en el lente de regalo. Los caros normalmente no.</p>
      <div className="tabla-desplazable">
        <table className="tabla">
          <thead>
            <tr><th>Tratamiento</th><th>Precio</th><th>Entra en 2x1</th><th>Activo</th><th></th></tr>
          </thead>
          <tbody>
            {lista.map((t) => (
              <tr key={t.id}>
                <th>{t.nombre}</th>
                <td><input className="entrada entrada--chica cifra" type="number" min="0" value={t.precio} onChange={(e) => cambiar(t.id, { precio: e.target.value })} /></td>
                <td><input type="checkbox" checked={t.aplicaEnPromocion} onChange={(e) => cambiar(t.id, { aplicaEnPromocion: e.target.checked })} aria-label="Entra en 2x1" /></td>
                <td><input type="checkbox" checked={t.activo} onChange={(e) => cambiar(t.id, { activo: e.target.checked })} aria-label="Activo" /></td>
                <td><button className="btn btn--texto" onClick={() => guardar(t)}>Guardar</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <form className="fila fila--envolver" onSubmit={crear}>
        <input className="entrada" placeholder="Nuevo tratamiento" value={nuevo.nombre} onChange={(e) => setNuevo({ ...nuevo, nombre: e.target.value })} required />
        <input className="entrada entrada--chica cifra" type="number" min="0" placeholder="Precio" value={nuevo.precio} onChange={(e) => setNuevo({ ...nuevo, precio: e.target.value })} required />
        <label className="casilla">
          <input type="checkbox" checked={nuevo.aplicaEnPromocion} onChange={(e) => setNuevo({ ...nuevo, aplicaEnPromocion: e.target.checked })} /> Entra en 2x1
        </label>
        <button className="btn btn--contorno">Agregar</button>
      </form>
      {error && <p className="mensaje-error">{error}</p>}
    </div>
  )
}

function Cupones() {
  const [cupones, setCupones] = useState([])
  const [busqueda, setBusqueda] = useState("")
  const [clientes, setClientes] = useState([])
  const [cliente, setCliente] = useState(null)
  const [porcentaje, setPorcentaje] = useState(10)
  const [dias, setDias] = useState(30)
  const [nota, setNota] = useState("")
  const [creado, setCreado] = useState(null)
  const [error, setError] = useState("")

  const cargar = () => promocionesApi.cupones().then(setCupones).catch((e) => setError(e.message))
  useEffect(() => { cargar() }, [])

  useEffect(() => {
    if (!busqueda.trim()) return setClientes([])
    const espera = setTimeout(() => clientesApi.buscar(busqueda).then(setClientes).catch(() => {}), 250)
    return () => clearTimeout(espera)
  }, [busqueda])

  async function crear(e) {
    e.preventDefault()
    setError("")
    try {
      const c = await promocionesApi.crearCupon({ clienteId: cliente?.id ?? null, porcentaje: Number(porcentaje), diasVigencia: Number(dias), nota })
      setCreado(c)
      setNota("")
      cargar()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="panel">
      <h2 className="panel-titulo">Descuentos especiales</h2>
      <form className="formulario" onSubmit={crear}>
        <div className="campo">
          <span className="etiqueta">Para un cliente (opcional; si no, lo puede usar cualquiera)</span>
          {cliente ? (
            <div className="fila"><strong>{cliente.nombre}</strong><button type="button" className="btn btn--texto" onClick={() => setCliente(null)}>Quitar</button></div>
          ) : (
            <>
              <input className="entrada" placeholder="Buscar cliente…" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} />
              <div className="chips">
                {clientes.slice(0, 6).map((c) => (
                  <button type="button" key={c.id} className="chip" onClick={() => { setCliente(c); setBusqueda("") }}>{c.nombre}</button>
                ))}
              </div>
            </>
          )}
        </div>
        <div className="rejilla rejilla--3">
          <label className="campo">
            <span className="etiqueta">Descuento %</span>
            <input className="entrada cifra" type="number" min="1" max="100" value={porcentaje} onChange={(e) => setPorcentaje(e.target.value)} />
          </label>
          <label className="campo">
            <span className="etiqueta">Válido por (días)</span>
            <input className="entrada cifra" type="number" min="1" max="365" value={dias} onChange={(e) => setDias(e.target.value)} />
          </label>
          <label className="campo">
            <span className="etiqueta">Nota</span>
            <input className="entrada" value={nota} onChange={(e) => setNota(e.target.value)} placeholder="Ej. Buen Fin" />
          </label>
        </div>
        <button className="btn btn--primario">Crear cupón</button>
      </form>
      {creado && <p className="alerta alerta--exito">Código creado: {creado.codigo}</p>}
      {error && <p className="mensaje-error">{error}</p>}

      <div className="tabla-desplazable">
        <table className="tabla">
          <thead>
            <tr><th>Código</th><th>%</th><th>Cliente</th><th>Vence</th><th>Estado</th></tr>
          </thead>
          <tbody>
            {cupones.map((c) => (
              <tr key={c.id}>
                <th className="cifra">{c.codigo}</th>
                <td className="cifra">{c.porcentaje}%</td>
                <td>{c.cliente ?? "Cualquiera"}</td>
                <td>{fechaCorta(c.vigenteHasta)}</td>
                <td>{c.usadoEnOrden ? `Usado (folio ${c.usadoEnOrden})` : c.nota ?? (c.tipo === "CUMPLEANOS" ? "Cumpleaños" : "Disponible")}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function Seguridad() {
  const [mensaje, setMensaje] = useState("")

  async function cerrarTodos() {
    if (!window.confirm("Todos los celulares del personal tendrán que volver a escribir el PIN. ¿Continuar?")) return
    try {
      const n = await accesoApi.cerrarPersonal()
      setMensaje(`Listo: se cerraron ${n} sesiones. Si se perdió un celular, cambia también el PIN (variable APP_PIN).`)
    } catch (e) {
      setMensaje(e.message)
    }
  }

  return (
    <div className="panel">
      <h2 className="panel-titulo">Seguridad</h2>
      <p className="texto-tenue">Si se pierde o roban un celular del personal, saca a todos y vuelvan a entrar con el PIN.</p>
      <button className="btn btn--peligro" onClick={cerrarTodos}>Cerrar sesión en todos los celulares</button>
      {mensaje && <p className="aviso-barra">{mensaje}</p>}
      <p className="texto-tenue">Precios de ejemplo en tratamientos e impresiones ({dinero(2)} B/N, {dinero(6)} color): ajústalos aquí y en Inventario.</p>
    </div>
  )
}
