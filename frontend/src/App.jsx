/*
  App = el "esqueleto" de la pantalla: barra superior + menú + la vista elegida.

  REACT EN 3 IDEAS:
  1. Todo es un COMPONENTE: una función que devuelve "HTML" (se llama JSX).
  2. ESTADO (useState): datos que, al cambiar, hacen que React vuelva a pintar la pantalla.
     const [vista, setVista] = useState("inicio")
       vista    → el valor actual
       setVista → la función para cambiarlo
  3. PROPS: datos que un componente padre le pasa a un hijo, como parámetros.
     <Inicio irA={setVista} />  → Inicio recibe la función para cambiar de vista.

  Para no meter más librerías al principio, la navegación es un simple useState
  (no usamos React Router). Cuando el sistema crezca, se puede agregar.
*/
import { useState } from "react"
import Inicio from "./vistas/Inicio"
import Vender from "./vistas/papeleria/Vender"
import Productos from "./vistas/papeleria/Productos"
import Corte from "./vistas/papeleria/Corte"
import Proximamente from "./vistas/optica/Proximamente"

// Menú: cada opción dice a qué negocio pertenece y qué componente muestra.
const MENU = [
  { id: "vender", negocio: "AMMI", texto: "Vender", componente: Vender },
  { id: "productos", negocio: "AMMI", texto: "Inventario", componente: Productos },
  { id: "corte", negocio: "AMMI", texto: "Corte del día", componente: Corte },
  { id: "optica", negocio: "RODRÍGUEZ", texto: "Óptica", componente: Proximamente },
]

export default function App() {
  const [vista, setVista] = useState("inicio")

  const opcion = MENU.find((m) => m.id === vista)
  const VistaActual = opcion ? opcion.componente : null

  return (
    <div className="marco">
      <header className="barra">
        <button className="barra-marca" onClick={() => setVista("inicio")}>
          Rodríguez <span>·</span> AMMI
        </button>
        <nav className="barra-menu" aria-label="Menú principal">
          {MENU.map((m) => (
            <button
              key={m.id}
              className={"barra-opcion" + (vista === m.id ? " barra-opcion--activa" : "")}
              onClick={() => setVista(m.id)}
            >
              {m.texto}
            </button>
          ))}
        </nav>
      </header>

      <main className="contenido">
        {VistaActual ? <VistaActual /> : <Inicio irA={setVista} />}
      </main>
    </div>
  )
}
