import { dinero, fechaHora, NOMBRES_PAGO } from "../utils/formato"

/*
  TICKET de venta. Es un componente reutilizable: se usa al cobrar y se podrá usar
  para reimprimir. Recibe el ticket por "props" ({ ticket }).

  Al imprimir (window.print()), app.css esconde todo lo demás y deja solo el elemento
  con la clase "ticket", con el ancho de una impresora térmica de 58 mm.
*/
export default function Ticket({ ticket }) {
  return (
    <article className="ticket">
      <header className="ticket-encabezado">
        <strong>PAPELERÍA AMMI</strong>
        <span>Folio {ticket.folio}</span>
        <span>{fechaHora(ticket.fecha)}</span>
      </header>

      <table className="ticket-tabla">
        <tbody>
          {ticket.renglones.map((r, i) => (
            <tr key={i}>
              <td>
                {r.cantidad} × {r.descripcion}
                <br />
                <small>{dinero(r.precioUnitario)} c/u</small>
              </td>
              <td className="cifra">{dinero(r.importe)}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <dl className="ticket-totales">
        <dt>TOTAL</dt>
        <dd className="cifra">{dinero(ticket.total)}</dd>
        <dt>Pago</dt>
        <dd>{NOMBRES_PAGO[ticket.metodoPago]}</dd>
        {ticket.pagaCon != null && (
          <>
            <dt>Recibido</dt>
            <dd className="cifra">{dinero(ticket.pagaCon)}</dd>
            <dt>Cambio</dt>
            <dd className="cifra">{dinero(ticket.cambio)}</dd>
          </>
        )}
      </dl>

      <footer className="ticket-pie">¡Gracias por su compra!</footer>
    </article>
  )
}
