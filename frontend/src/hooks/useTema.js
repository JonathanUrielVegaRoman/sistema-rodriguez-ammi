import { useEffect, useState } from "react"

// "sistema" sigue prefers-color-scheme del navegador; "light"/"dark" lo fijan.
// index.html aplica el valor guardado antes de pintar.
const leer = () => {
  try {
    const t = localStorage.getItem("tema")
    return t === "light" || t === "dark" ? t : "sistema"
  } catch {
    return "sistema"
  }
}

export default function useTema() {
  const [tema, setTema] = useState(leer)

  useEffect(() => {
    const raiz = document.documentElement
    try {
      if (tema === "sistema") {
        delete raiz.dataset.theme
        localStorage.removeItem("tema")
      } else {
        raiz.dataset.theme = tema
        localStorage.setItem("tema", tema)
      }
    } catch {
      // Sin almacenamiento el tema funciona igual, solo no se recuerda.
    }
  }, [tema])

  return [tema, setTema]
}
