import { useEffect } from "react"

/*
  BLOQUEO POR INACTIVIDAD (lado del navegador).

  Anota la hora del último toque, tecla o desplazamiento. Si pasan más de "minutos" sin
  actividad, llama a alBloquear() → la app vuelve a pedir el PIN.

  También revisa al regresar a la pestaña (por ejemplo, al desbloquear el celular), porque
  los relojes de JavaScript se pausan cuando la pantalla está apagada.

  El servidor aplica la misma regla por su cuenta (AccesoService.tieneRol), así que aunque
  alguien saltara esta pantalla, el pase ya no serviría.
*/
const CLAVE = "ultima-actividad"

export function marcarActividad() {
  try {
    localStorage.setItem(CLAVE, String(Date.now()))
  } catch {
    // sin almacenamiento: el bloqueo depende solo del servidor
  }
}

function ultimaActividad() {
  try {
    return Number(localStorage.getItem(CLAVE)) || Date.now()
  } catch {
    return Date.now()
  }
}

export default function useInactividad({ activo, minutos, alBloquear }) {
  useEffect(() => {
    if (!activo || !minutos) return

    let ultimaEscritura = 0
    const alUsar = () => {
      // Se guarda como máximo cada 5 segundos para no escribir con cada movimiento.
      if (Date.now() - ultimaEscritura > 5000) {
        ultimaEscritura = Date.now()
        marcarActividad()
      }
    }

    const revisar = () => {
      if (document.visibilityState === "hidden") return
      if (Date.now() - ultimaActividad() > minutos * 60_000) alBloquear()
    }

    const eventos = ["pointerdown", "keydown", "touchstart", "wheel"]
    eventos.forEach((e) => window.addEventListener(e, alUsar, { passive: true }))
    document.addEventListener("visibilitychange", revisar)
    const reloj = setInterval(revisar, 20_000)
    revisar() // al abrir la app

    return () => {
      eventos.forEach((e) => window.removeEventListener(e, alUsar))
      document.removeEventListener("visibilitychange", revisar)
      clearInterval(reloj)
    }
  }, [activo, minutos, alBloquear])
}
