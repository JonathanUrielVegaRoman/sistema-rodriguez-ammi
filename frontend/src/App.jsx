/*
  App = el "esqueleto" de la pantalla: encabezado + la vista que corresponde a la dirección (#/...).

  REACT EN 3 IDEAS:
  1. Todo es un COMPONENTE: una función que devuelve "HTML" (se llama JSX).
  2. ESTADO (useState): datos que, al cambiar, hacen que React vuelva a pintar la pantalla.
  3. PROPS: datos que un componente padre le pasa a un hijo, como parámetros.

  TRES "PUERTAS":
  - #/imprimir    → pública: la usan los CLIENTES para mandar archivos. No pide nada.
  - #/admin...    → pide la contraseña del DUEÑO (reportes de ventas, precios, cupones).
  - todo lo demás → pide el PIN del PERSONAL una sola vez por celular.
*/
import { useCallback, useEffect, useState } from "react"
import useRuta, { coincide } from "./hooks/useRuta"
import useInactividad, { marcarActividad } from "./hooks/useInactividad"
import { sesion, EVENTO_SIN_ACCESO } from "./services/sesion"
import { accesoApi, cerrarSesionAdmin } from "./services/api"
import Encabezado from "./components/Encabezado"
import Acceso from "./vistas/Acceso"
import Inicio from "./vistas/Inicio"
import Imprimir from "./vistas/publico/Imprimir"
import Vender from "./vistas/papeleria/Vender"
import Productos from "./vistas/papeleria/Productos"
import Impresiones from "./vistas/papeleria/Impresiones"
import CartelImpresion from "./vistas/papeleria/CartelImpresion"
import Clientes from "./vistas/optica/Clientes"
import ClienteDetalle from "./vistas/optica/ClienteDetalle"
import Armazones from "./vistas/optica/Armazones"
import Ordenes from "./vistas/optica/Ordenes"
import NuevaOrden from "./vistas/optica/NuevaOrden"
import OrdenDetalle from "./vistas/optica/OrdenDetalle"
import Cumpleanos from "./vistas/optica/Cumpleanos"
import Reportes from "./vistas/admin/Reportes"
import Ajustes from "./vistas/admin/Ajustes"
import Tickets from "./vistas/admin/Tickets"

/*
  TABLA DE RUTAS: dirección → pantalla. El orden importa: "/optica/ordenes/nueva" va antes
  que "/optica/ordenes/:folio" para que "nueva" no se confunda con un folio.
*/
const RUTAS = [
  ["/", Inicio],
  ["/papeleria/vender", Vender],
  ["/papeleria/inventario", Productos],
  ["/papeleria/impresiones", Impresiones],
  ["/papeleria/cartel", CartelImpresion],
  ["/optica/clientes", Clientes],
  ["/optica/clientes/:id", ClienteDetalle],
  ["/optica/armazones", Armazones],
  ["/optica/ordenes", Ordenes],
  ["/optica/ordenes/nueva", NuevaOrden],
  ["/optica/ordenes/:folio", OrdenDetalle],
  ["/optica/cumpleanos", Cumpleanos],
  ["/admin", Reportes],
  ["/admin/tickets", Tickets],
  ["/admin/ajustes", Ajustes],
]

export default function App() {
  const { ruta, consulta } = useRuta()
  const [conAcceso, setConAcceso] = useState(Boolean(sesion.personal()))
  const [minutosInactividad, setMinutosInactividad] = useState(10)
  const [bloqueadoPorInactividad, setBloqueadoPorInactividad] = useState(false)

  // Al abrir: confirmar con el servidor que el pase sigue vigente.
  useEffect(() => {
    if (!sesion.personal()) return
    accesoApi
      .estado()
      .then((e) => {
        setMinutosInactividad(e.minutosInactividad)
        if (!e.personal && !e.admin) {
          sesion.olvidarPersonal()
          setConAcceso(false)
        }
      })
      .catch(() => {}) // sin conexión: dejamos entrar; cada pantalla mostrará el error
  }, [])

  // Si cualquier petición responde 401 con el pase del personal, volvemos a pedir el PIN.
  useEffect(() => {
    const alPerderAcceso = (e) => e.detail === "personal" && setConAcceso(false)
    window.addEventListener(EVENTO_SIN_ACCESO, alPerderAcceso)
    return () => window.removeEventListener(EVENTO_SIN_ACCESO, alPerderAcceso)
  }, [])

  // Administración: en cuanto se sale de las pantallas #/admin (con ←, con el menú o por error),
  // se cierra la sesión del dueño. Para volver a ver las ventas hay que escribir la contraseña otra vez.
  useEffect(() => {
    if (!ruta.startsWith("/admin") && sesion.admin()) cerrarSesionAdmin()
  }, [ruta])

  // Bloqueo por inactividad: tras N minutos sin tocar la pantalla, se vuelve a pedir el PIN.
  // (N lo decide el dueño en Administración → Ajustes; por defecto 10.)
  const bloquear = useCallback(async () => {
    await accesoApi.bloquear().catch(() => {})
    await cerrarSesionAdmin()
    sesion.olvidarPersonal()
    setBloqueadoPorInactividad(true)
    setConAcceso(false)
  }, [])
  useInactividad({ activo: conAcceso && ruta !== "/imprimir", minutos: minutosInactividad, alBloquear: bloquear })

  // Puerta pública: el buzón de impresión para clientes, sin menú del sistema.
  if (ruta === "/imprimir") return <Imprimir />

  if (!conAcceso)
    return (
      <Acceso
        porInactividad={bloqueadoPorInactividad}
        alEntrar={(minutos) => {
          marcarActividad()
          if (minutos) setMinutosInactividad(minutos)
          setBloqueadoPorInactividad(false)
          setConAcceso(true)
        }}
      />
    )

  let Vista = null
  let parametros = {}
  for (const [patron, componente] of RUTAS) {
    const p = coincide(patron, ruta)
    if (p) {
      Vista = componente
      parametros = p
      break
    }
  }

  return (
    <div className="marco">
      <Encabezado ruta={ruta} />
      <main className="contenido">
        {Vista ? (
          <Vista {...parametros} consulta={consulta} />
        ) : (
          <p className="mensaje-error">Esta pantalla no existe. Usa el botón de inicio.</p>
        )}
      </main>
    </div>
  )
}
