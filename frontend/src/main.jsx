// Punto de entrada del frontend: "pega" el componente App dentro del <div id="root"> de index.html.
import { StrictMode } from "react"
import { createRoot } from "react-dom/client"
import "./styles/base.css" // colores, botones y formularios (los mismos del proyecto de sedimento urinario)
import "./styles/app.css" // estructura propia de este sistema + estilo del ticket impreso
import App from "./App.jsx"

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
