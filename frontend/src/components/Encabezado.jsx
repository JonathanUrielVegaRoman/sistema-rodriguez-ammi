import Icono from "./Icono"
import SelectorTema from "./SelectorTema"
import { ir } from "../hooks/useRuta"

/*
  ENCABEZADO, en dos renglones:

  1) Barra fija:  [←][→]  Rodríguez · AMMI   [ Papelería | Óptica ]   [☀/🖥/🌙] [🔒]
     - ← → : regresar y avanzar (igual que el navegador, pero a la mano en el celular).
     - Los dos botones de negocio cambian de "local" sin pasar por el inicio.
     - 🔒 Administración: reportes de ventas (pide la contraseña del dueño).

  2) Menú del negocio actual: solo las secciones de ESE negocio.
*/

const SECCIONES = {
  papeleria: [
    ["/papeleria/vender", "Vender"],
    ["/papeleria/inventario", "Inventario"],
    ["/papeleria/impresiones", "Impresiones"],
  ],
  optica: [
    ["/optica/clientes", "Clientes"],
    ["/optica/armazones", "Armazones"],
    ["/optica/ordenes", "Órdenes"],
    ["/optica/cumpleanos", "Cumpleaños"],
  ],
  admin: [
    ["/admin", "Reportes"],
    ["/admin/ajustes", "Ajustes"],
  ],
}

const NOMBRE_NEGOCIO = { papeleria: "Papelería AMMI", optica: "Óptica RODRÍGUEZ", admin: "Administración" }

export default function Encabezado({ ruta }) {
  const negocio = ruta.split("/")[1] || null // "papeleria", "optica", "admin" o null (inicio)
  const secciones = SECCIONES[negocio] ?? []

  return (
    <header className={"encabezado" + (negocio ? " encabezado--" + negocio : "")}>
      <div className="barra">
        <div className="barra-historial">
          <button className="boton-icono" onClick={() => window.history.back()} title="Regresar" aria-label="Regresar">
            <Icono nombre="izquierda" />
          </button>
          <button className="boton-icono" onClick={() => window.history.forward()} title="Avanzar" aria-label="Avanzar">
            <Icono nombre="derecha" />
          </button>
        </div>

        <button className="barra-marca" onClick={() => ir("/")} title="Inicio">
          <Icono nombre="casa" tamano={18} />
          <span>
            Rodríguez <span className="barra-punto">·</span> AMMI
          </span>
        </button>

        <nav className="barra-negocios" aria-label="Negocio">
          <button className={"negocio-boton" + (negocio === "papeleria" ? " negocio-boton--activo" : "")} onClick={() => ir("/papeleria/vender")}>
            Papelería
          </button>
          <button className={"negocio-boton negocio-boton--optica" + (negocio === "optica" ? " negocio-boton--activo" : "")} onClick={() => ir("/optica/clientes")}>
            Óptica
          </button>
        </nav>

        <div className="barra-derecha">
          <SelectorTema />
          <button
            className={"boton-icono" + (negocio === "admin" ? " boton-icono--activo" : "")}
            onClick={() => ir("/admin")}
            title="Administración (reportes)"
            aria-label="Administración"
          >
            <Icono nombre="candado" />
          </button>
        </div>
      </div>

      {secciones.length > 0 && (
        <nav className="submenu" aria-label={NOMBRE_NEGOCIO[negocio]}>
          <span className="submenu-negocio">{NOMBRE_NEGOCIO[negocio]}</span>
          {secciones.map(([destino, texto]) => {
            // La sección está activa si la ruta empieza igual (ej. /optica/ordenes/15 activa "Órdenes").
            const activa = destino === "/admin" ? ruta === "/admin" : ruta === destino || ruta.startsWith(destino + "/")
            return (
              <button key={destino} className={"submenu-opcion" + (activa ? " submenu-opcion--activa" : "")} onClick={() => ir(destino)}>
                {texto}
              </button>
            )
          })}
        </nav>
      )}
    </header>
  )
}
