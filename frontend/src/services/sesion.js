/*
  PASES (tokens) guardados en ESTE dispositivo.

  - Pase de PERSONAL: se obtiene una vez con el PIN y dura 1 año. Es la "pulsera" del celular.
  - Pase de ADMIN: se obtiene con la contraseña del dueño y dura pocas horas.

  Se guardan en localStorage (memoria del navegador que sobrevive a cerrar la pestaña).
  Si el navegador no deja guardar (modo incógnito), funciona igual pero pedirá el PIN otra vez.
*/

const CLAVE_PERSONAL = "pase-personal"
const CLAVE_ADMIN = "pase-admin"

function leer(clave) {
  try {
    return localStorage.getItem(clave)
  } catch {
    return null
  }
}

function escribir(clave, valor) {
  try {
    if (valor) localStorage.setItem(clave, valor)
    else localStorage.removeItem(clave)
  } catch {
    // sin almacenamiento: no se recuerda, pero no falla
  }
}

export const sesion = {
  personal: () => leer(CLAVE_PERSONAL),
  admin: () => leer(CLAVE_ADMIN),
  guardarPersonal: (token) => escribir(CLAVE_PERSONAL, token),
  guardarAdmin: (token) => escribir(CLAVE_ADMIN, token),
  olvidarPersonal: () => escribir(CLAVE_PERSONAL, null),
  olvidarAdmin: () => escribir(CLAVE_ADMIN, null),
}

/** Aviso para toda la app: "se perdió el acceso" (el App muestra la pantalla de PIN). */
export const EVENTO_SIN_ACCESO = "sin-acceso"
