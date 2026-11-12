import { dinero, fechaCorta, NOMBRES_ESTADO_ORDEN } from "../utils/formato"
import { ir } from "../hooks/useRuta"
import Icono from "./Icono"

/*
  Tarjeta de una orden de lentes, con la FOTO del armazón como protagonista
  (igual que en el catálogo): así se reconoce de un vistazo qué lentes son.
*/
export default function TarjetaOrden({ orden }) {
  return (
    <button className="tarjeta-orden" onClick={() => ir(`/optica/ordenes/${orden.folio}`)}>
      <div className="tarjeta-orden-foto">
        {orden.fotoUrl ? <img src={orden.fotoUrl} alt="" loading="lazy" /> : <Icono nombre="lentes" tamano={30} />}
      </div>
      <div className="tarjeta-orden-datos">
        <span className="tarjeta-orden-arriba">
          <strong>Folio {orden.folio}</strong>
          <span className={"insignia estado--" + orden.estado}>{NOMBRES_ESTADO_ORDEN[orden.estado]}</span>
        </span>
        <span>{orden.clienteNombre}</span>
        <span className="texto-tenue">{orden.descripcion}</span>
        <span className="tarjeta-orden-abajo cifra">
          <span>{dinero(orden.total)}</span>
          {Number(orden.saldo) > 0 ? <span className="texto-ambar">debe {dinero(orden.saldo)}</span> : <span className="texto-verde">pagado</span>}
          {orden.fechaEntregaEstimada && <span className="texto-tenue">entrega {fechaCorta(orden.fechaEntregaEstimada)}</span>}
        </span>
      </div>
    </button>
  )
}
