import { dioptria, fechaCorta } from "../utils/formato"

/*
  GRADUACIÓN en el formato que usan las ópticas:

           ESF     CIL    EJE    ADD
    OD   −2.25   −0.75   180°    —        DIP 62 mm
    OI   −2.00   −0.50   170°    —
*/
export default function TablaReceta({ receta }) {
  const eje = (v) => (v === null || v === undefined ? "—" : `${v}°`)
  return (
    <div className="receta">
      <div className="receta-encabezado">
        <span>Examen del {fechaCorta(receta.fecha)}</span>
        {receta.dip && <span className="cifra">DIP {receta.dip} mm</span>}
      </div>
      <table className="tabla-receta cifra">
        <thead>
          <tr>
            <th></th>
            <th>ESF</th>
            <th>CIL</th>
            <th>EJE</th>
            <th>ADD</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <th title="Ojo derecho">OD</th>
            <td>{dioptria(receta.odEsfera)}</td>
            <td>{dioptria(receta.odCilindro)}</td>
            <td>{eje(receta.odEje)}</td>
            <td>{dioptria(receta.odAdicion)}</td>
          </tr>
          <tr>
            <th title="Ojo izquierdo">OI</th>
            <td>{dioptria(receta.oiEsfera)}</td>
            <td>{dioptria(receta.oiCilindro)}</td>
            <td>{eje(receta.oiEje)}</td>
            <td>{dioptria(receta.oiAdicion)}</td>
          </tr>
        </tbody>
      </table>
      {receta.observaciones && <p className="texto-tenue">{receta.observaciones}</p>}
    </div>
  )
}
