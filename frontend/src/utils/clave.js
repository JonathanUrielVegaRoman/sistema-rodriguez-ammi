/*
  CLAVE ÚNICA para cada cobro (idempotencia).

  Problema: con Wi-Fi inestable, el celular manda "cobrar", el servidor cobra, pero la
  respuesta se pierde en el camino. La persona ve un error, toca "Cobrar" otra vez... y
  sin protección se cobraría DOS veces.

  Solución: cada intento de cobro lleva una clave aleatoria. Si el servidor recibe dos veces
  la misma clave, la segunda vez devuelve el ticket que ya existía en lugar de crear otro
  (ver VentaService.registrar en el backend).

  Nota: se usa crypto.getRandomValues y no crypto.randomUUID porque este último solo funciona
  en páginas con HTTPS, y el sistema corre con HTTP dentro de la red del local.
*/
export function nuevaClave() {
  const bytes = new Uint8Array(16)
  crypto.getRandomValues(bytes)
  return [...bytes].map((b) => b.toString(16).padStart(2, "0")).join("")
}
