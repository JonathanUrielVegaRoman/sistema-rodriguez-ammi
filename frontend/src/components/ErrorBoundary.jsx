import { Component } from "react"

/*
  RED DE SEGURIDAD de la pantalla.

  Si un componente falla al dibujarse (un dato inesperado, un error de programación), React
  por defecto deja TODA la pantalla en blanco, y en un mostrador eso significa "se cayó el
  sistema". Con esto, en su lugar aparece un mensaje claro y un botón para continuar.

  Es el equivalente en el frontend del ManejadorGlobalErrores del backend.
  (Se escribe como "clase" porque React solo ofrece esta función en componentes de clase.)
*/
export default class ErrorBoundary extends Component {
  state = { error: null }

  static getDerivedStateFromError(error) {
    return { error }
  }

  componentDidCatch(error, info) {
    // Queda en la consola del navegador (F12) para poder revisarlo.
    console.error("Error al mostrar la pantalla:", error, info?.componentStack)
  }

  render() {
    if (!this.state.error) return this.props.children
    return (
      <div className="pantalla-acceso">
        <div className="panel acceso">
          <h1 className="seccion-titulo">Algo salió mal en esta pantalla</h1>
          <p className="seccion-descripcion">
            Tus datos están a salvo: lo que ya se cobró o guardó sigue guardado. Vuelve al inicio para continuar.
          </p>
          <button
            className="btn btn--primario btn--bloque"
            onClick={() => {
              window.location.hash = "/"
              window.location.reload()
            }}
          >
            Volver al inicio
          </button>
          <p className="texto-tenue">{String(this.state.error?.message ?? this.state.error)}</p>
        </div>
      </div>
    )
  }
}
