/*
  REDUCIR FOTOS antes de subirlas.

  Una foto del celular pesa 3–6 MB. Si la subimos así, la cuadrícula de armazones tardaría
  mucho en cargar. Aquí la achicamos a máximo 1200 px de lado y la guardamos como JPG
  (≈ 150–300 KB): se ve bien y carga rápido.

  Se usa un <canvas> (un "lienzo" del navegador donde se puede dibujar una imagen).
*/
export async function reducirFoto(archivo, ladoMaximo = 1200, calidad = 0.82) {
  const imagen = await createImageBitmap(archivo)
  const escala = Math.min(1, ladoMaximo / Math.max(imagen.width, imagen.height))
  const ancho = Math.round(imagen.width * escala)
  const alto = Math.round(imagen.height * escala)

  const lienzo = document.createElement("canvas")
  lienzo.width = ancho
  lienzo.height = alto
  lienzo.getContext("2d").drawImage(imagen, 0, 0, ancho, alto)

  return new Promise((resolver, rechazar) =>
    lienzo.toBlob((blob) => (blob ? resolver(blob) : rechazar(new Error("No se pudo procesar la foto"))), "image/jpeg", calidad),
  )
}
