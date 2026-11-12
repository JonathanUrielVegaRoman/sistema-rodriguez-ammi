/*
  PASES (tokens) guardados en ESTE dispositivo.

  - Pase de PERSONAL: se obtiene una vez con el PIN y dura 1 año. Es la "pulsera" del celular.
    Se guarda en localStorage (sobrevive a cerrar el navegador).
  - Pase de ADMIN: se obtiene con la contraseña del dueño. Se guarda en sessionStorage, que se
    borra al cerrar la pestaña, y además App.jsx lo cierra en cuanto se sale de Administración.

  Si el navegador no deja guardar (modo incógnito), funciona igual pero pedirá el PIN otra vez.
*/

const CLAVE_PERSONAL = "pase-personal"
const CLAVE_ADMIN = "pase-admin"

function leer(almacen, clave) {
  try {
    return almacen().getItem(clave)
  } catch {
    return null
  }
}

function escribir(almacen, clave, valor) {
  try {
    if (valor) almacen().setItem(clave, valor)
    else almacen().removeItem(clave)
  } catch {
    // sin almacenamiento: no se recuerda, pero no falla
  }
}

const local = () => window.localStorage
const pestana = () => window.sessionStorage

export const sesion = {
  personal: () => leer(local, CLAVE_PERSONAL),
  admin: () => leer(pestana, CLAVE_ADMIN),
  guardarPersonal: (token) => escribir(local, CLAVE_PERSONAL, token),
  guardarAdmin: (token) => escribir(pestana, CLAVE_ADMIN, token),
  olvidarPersonal: () => escribir(local, CLAVE_PERSONAL, null),
  olvidarAdmin: () => {
    escribir(pestana, CLAVE_ADMIN, null)
    escribir(local, CLAVE_ADMIN, null) // por si quedó de la versión anterior
  },
}

/** Aviso para toda la app: "se perdió el acceso" (el App muestra la pantalla de PIN). */
export const EVENTO_SIN_ACCESO = "sin-acceso"
