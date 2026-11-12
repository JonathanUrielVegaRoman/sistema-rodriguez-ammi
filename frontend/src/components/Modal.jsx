import { useEffect } from "react"
import Icono from "./Icono"

/*
  VENTANA EMERGENTE reutilizable. Uso:
    {abierto && <Modal titulo="Nuevo armazón" alCerrar={() => setAbierto(false)}> ...contenido... </Modal>}
  Se cierra con la X, con la tecla Escape o tocando fuera.
*/
export default function Modal({ titulo, alCerrar, ancho = false, children }) {
  useEffect(() => {
    const tecla = (e) => e.key === "Escape" && alCerrar()
    window.addEventListener("keydown", tecla)
    document.body.style.overflow = "hidden" // que no se mueva la página de atrás
    return () => {
      window.removeEventListener("keydown", tecla)
      document.body.style.overflow = ""
    }
  }, [alCerrar])

  return (
    <div className="modal-velo" onMouseDown={(e) => e.target === e.currentTarget && alCerrar()}>
      <div className={"ventana" + (ancho ? " ventana--ancha" : "")} role="dialog" aria-modal="true" aria-label={titulo}>
        <div className="ventana-encabezado">
          <h2 className="ventana-titulo">{titulo}</h2>
          <button className="boton-icono" onClick={alCerrar} aria-label="Cerrar">
            <Icono nombre="cerrar" />
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}
