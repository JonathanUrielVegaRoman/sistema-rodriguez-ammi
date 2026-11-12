import { useEffect, useState } from "react"
import QRCode from "qrcode"
import { accesoApi } from "../../services/api"

/*
  CARTEL PARA EL MOSTRADOR con el código QR del buzón de impresión.
  Se imprime en una hoja carta y se pega a la vista de los clientes.

  El QR debe llevar la IP de la COMPUTADORA DEL NEGOCIO (no "localhost"), por eso se la
  preguntamos al backend (/api/publico/info). Si la IP cambia, hay que reimprimir el cartel:
  por eso conviene fijar la IP en el módem (ver docs/06-ideas-y-pendientes.md).
*/
export default function CartelImpresion() {
  const [direcciones, setDirecciones] = useState([])
  const [elegida, setElegida] = useState("")
  const [qr, setQr] = useState("")
  const [error, setError] = useState("")

  useEffect(() => {
    accesoApi
      .info()
      .then((i) => {
        // Si ya estamos entrando por la IP (no localhost), esa es la buena.
        const actual = window.location.hostname !== "localhost" ? window.location.origin : null
        const lista = [...new Set([actual, ...i.direcciones].filter(Boolean))]
        setDirecciones(lista)
        setElegida(lista[0] ?? "")
      })
      .catch((e) => setError(e.message))
  }, [])

  useEffect(() => {
    if (!elegida) return
    QRCode.toDataURL(elegida + "/#/imprimir", { width: 600, margin: 1 }).then(setQr)
  }, [elegida])

  return (
    <section className="angosto">
      <div className="panel no-imprimir">
        <p>Imprime esta hoja y pégala en el mostrador. El QR abre el buzón de impresión.</p>
        {direcciones.length > 1 && (
          <label className="campo">
            <span className="etiqueta">Dirección de la computadora en la red</span>
            <select className="entrada" value={elegida} onChange={(e) => setElegida(e.target.value)}>
              {direcciones.map((d) => (
                <option key={d}>{d}</option>
              ))}
            </select>
          </label>
        )}
        {error && <p className="mensaje-error">{error}</p>}
        <button className="btn btn--primario" onClick={() => window.print()} disabled={!qr}>Imprimir cartel</button>
      </div>

      {qr && (
        <article className="cartel">
          <h1>¿Quieres imprimir?</h1>
          <p className="cartel-pasos">1. Conéctate a la Wi-Fi de la papelería · 2. Escanea · 3. Manda tus archivos</p>
          <img src={qr} alt="Código QR del buzón de impresión" />
          <p className="cartel-direccion">{elegida}/#/imprimir</p>
          <p className="cartel-privacidad">Sin WhatsApp, sin compartir tu número. Tus archivos se borran al entregarte.</p>
          <strong>Papelería AMMI</strong>
        </article>
      )}
    </section>
  )
}
