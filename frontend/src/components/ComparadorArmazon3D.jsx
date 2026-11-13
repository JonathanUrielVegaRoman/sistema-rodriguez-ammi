import { useEffect, useState } from "react"
import { armazonesApi } from "../services/api"
import { compararMedidas, medidasDe } from "../utils/geometriaArmazon"
import Armazon3D from "./Armazon3D"

/*
  El visor 3D de la ficha del armazón, con la herramienta que resuelve el problema original
  del cliente: "hay modelos parecidos pero en diferentes tamaños".

  Se elige otro armazón y aparece ENCIMADO en naranja translúcido sobre el actual, más una
  tabla que dice en milímetros cuál es más ancho, más alto o de varilla más larga.
*/
export default function ComparadorArmazon3D({ armazon }) {
  const [otros, setOtros] = useState([])
  const [compararId, setCompararId] = useState("")

  useEffect(() => {
    setCompararId("")
    armazonesApi
      .buscar({})
      .then((lista) => {
        const conMedidas = lista.filter((a) => a.id !== armazon.id && a.anchoLente && a.puente)
        // Primero los del mismo modelo (son los que más se confunden entre sí).
        const mismoModelo = (a) => (a.modelo && a.modelo === armazon.modelo ? 0 : 1)
        setOtros(conMedidas.sort((a, b) => mismoModelo(a) - mismoModelo(b) || a.codigo.localeCompare(b.codigo)))
      })
      .catch(() => setOtros([]))
  }, [armazon.id, armazon.modelo])

  if (!armazon.anchoLente || !armazon.puente) {
    return (
      <p className="aviso-barra">
        Para ver el modelo 3D captura al menos las medidas de lente y puente (toca "Editar datos").
      </p>
    )
  }

  const otro = otros.find((a) => String(a.id) === compararId) ?? null
  const m = medidasDe(armazon)

  return (
    <div className="comparador3d">
      <Armazon3D armazon={armazon} comparar={otro} />

      <p className="texto-tenue">
        Modelo aproximado, dibujado con las medidas{armazon.altoLente ? "" : " (el alto del lente se estimó por la forma)"}.
        Ancho total ≈ <strong className="cifra">{m.anchoTotal} mm</strong>. No sustituye a la foto: sirve para ver tamaño y proporciones.
      </p>

      {otros.length > 0 && (
        <label className="campo">
          <span className="etiqueta">Comparar tamaño con otro armazón</span>
          <select className="entrada" value={compararId} onChange={(e) => setCompararId(e.target.value)}>
            <option value="">Sin comparar</option>
            {otros.map((a) => (
              <option key={a.id} value={a.id}>
                {a.codigo} · {[a.marca, a.modelo].filter(Boolean).join(" ")} · {a.medidas}
              </option>
            ))}
          </select>
        </label>
      )}

      {otro && (
        <div className="tabla-desplazable">
          <table className="tabla cifra">
            <thead>
              <tr>
                <th>Medida (mm)</th>
                <th>{armazon.codigo}</th>
                <th><i className="muestra muestra--fantasma" /> {otro.codigo}</th>
                <th>Diferencia</th>
              </tr>
            </thead>
            <tbody>
              {compararMedidas(armazon, otro).map((f) => (
                <tr key={f.nombre}>
                  <th>{f.nombre}</th>
                  <td>{f.este}</td>
                  <td>{f.otro}</td>
                  <td>{f.diferencia === 0 ? "igual" : `${Math.abs(f.diferencia)} ${f.diferencia > 0 ? "más" : "menos"}`}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
