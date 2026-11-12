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
import { useEffect, useState } from "react"
import useRuta, { coincide } from "./hooks/useRuta"
import { sesion, EVENTO_SIN_ACCESO } from "./services/sesion"
import { accesoApi } from "./services/api"
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
  ["/admin/ajustes", Ajustes],
]

export default function App() {
  const { ruta, consulta } = useRuta()
  const [conAcceso, setConAcceso] = useState(Boolean(sesion.personal()))

  // Al abrir: confirmar con el servidor que el pase sigue vigente.
  useEffect(() => {
    if (!sesion.personal()) return
    accesoApi
      .estado()
      .then((e) => {
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

  // Puerta pública: el buzón de impresión para clientes, sin menú del sistema.
  if (ruta === "/imprimir") return <Imprimir />

  if (!conAcceso) return <Acceso alEntrar={() => setConAcceso(true)} />

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
