import { useEffect, useState } from "react"
import { accesoApi } from "../services/api"
import { sesion, EVENTO_SIN_ACCESO } from "../services/sesion"
import Icono from "./Icono"

/*
  PUERTA DEL ADMINISTRADOR: envuelve las pantallas privadas (reportes, ajustes).
  Si este dispositivo no tiene pase de administrador vigente, pide la contraseña del dueño.
  El pase dura unas horas; "Cerrar sesión de administrador" lo quita al momento
  (útil si el dueño revisó los reportes en el celular de alguien más).

  Uso:  <PuertaAdmin> ...pantalla privada... </PuertaAdmin>
*/
export default function PuertaAdmin({ children }) {
  const [dentro, setDentro] = useState(Boolean(sesion.admin()))
  const [clave, setClave] = useState("")
  const [error, setError] = useState("")

  useEffect(() => {
    const alPerder = (e) => e.detail === "admin" && setDentro(false)
    window.addEventListener(EVENTO_SIN_ACCESO, alPerder)
    return () => window.removeEventListener(EVENTO_SIN_ACCESO, alPerder)
  }, [])

  async function entrar(e) {
    e.preventDefault()
    setError("")
    try {
      const pase = await accesoApi.admin(clave)
      sesion.guardarAdmin(pase.token)
      setClave("")
      setDentro(true)
    } catch (err) {
      setError(err.message)
    }
  }

  async function salir() {
    await accesoApi.salirAdmin().catch(() => {})
    sesion.olvidarAdmin()
    setDentro(false)
  }

  if (!dentro) {
    return (
      <form className="panel acceso" onSubmit={entrar}>
        <div className="acceso-logo"><Icono nombre="candado" tamano={30} /></div>
        <h1 className="seccion-titulo">Administración</h1>
        <p className="seccion-descripcion">Reportes de ventas y ajustes. Solo con la contraseña del dueño.</p>
        <input className="entrada" type="password" autoComplete="current-password" value={clave} onChange={(e) => setClave(e.target.value)} placeholder="Contraseña" aria-label="Contraseña" autoFocus />
        {error && <p className="mensaje-error">{error}</p>}
        <button className="btn btn--primario btn--bloque" disabled={!clave}>Entrar</button>
      </form>
    )
  }

  return (
    <>
      {children}
      <div className="texto-centro no-imprimir">
        <button className="btn btn--texto" onClick={salir}><Icono nombre="salir" /> Cerrar sesión de administrador</button>
      </div>
    </>
  )
}
