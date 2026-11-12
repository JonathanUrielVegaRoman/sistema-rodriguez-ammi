import { useEffect, useState } from "react"
import { armazonesApi } from "../services/api"
import { dinero } from "../utils/formato"
import Icono from "./Icono"

/*
  CUADRÍCULA DE ARMAZONES: la FOTO es lo principal, porque así se reconocen más rápido
  que leyendo un código. Debajo van código, medidas (talla) y precio.

  Filtros:
    - Búsqueda por código, marca, modelo o color.
    - TALLA (ancho de lente): botones con las tallas que existen, para distinguir los
      modelos iguales en distinto tamaño.
    - Solo con existencia.

  Se usa en dos lugares:
    - Armazones.jsx  → catálogo (tocar = ver/editar)
    - NuevaOrden.jsx → elegir el armazón de un par de lentes (tocar = elegir)

  Props: alTocar(armazon), recarga (número que, al cambiar, vuelve a pedir la lista),
         inicialDisponibles (true/false)
*/
export default function CuadriculaArmazones({ alTocar, recarga = 0, inicialDisponibles = false }) {
  const [armazones, setArmazones] = useState([])
  const [tallas, setTallas] = useState([])
  const [texto, setTexto] = useState("")
  const [ancho, setAncho] = useState(null)
  const [disponibles, setDisponibles] = useState(inicialDisponibles)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState("")

  useEffect(() => {
    armazonesApi.tallas().then(setTallas).catch(() => {})
  }, [recarga])

  useEffect(() => {
    // Esperamos 300 ms después de la última tecla para no pedir al servidor con cada letra.
    const espera = setTimeout(() => {
      setCargando(true)
      armazonesApi
        .buscar({ q: texto, ancho, disponibles })
        .then((lista) => {
          setArmazones(lista)
          setError("")
        })
        .catch((e) => setError(e.message))
        .finally(() => setCargando(false))
    }, 300)
    return () => clearTimeout(espera)
  }, [texto, ancho, disponibles, recarga])

  return (
    <div>
      <div className="filtros">
        <input className="entrada" placeholder="Código, marca, modelo o color…" value={texto} onChange={(e) => setTexto(e.target.value)} aria-label="Buscar armazón" />
        <label className="casilla">
          <input type="checkbox" checked={disponibles} onChange={(e) => setDisponibles(e.target.checked)} />
          Solo con existencia
        </label>
      </div>

      {tallas.length > 0 && (
        <div className="chips" role="group" aria-label="Talla (ancho de lente)">
          <span className="etiqueta">Talla</span>
          <button className={"chip" + (ancho === null ? " chip--activo" : "")} onClick={() => setAncho(null)}>Todas</button>
          {tallas.map((t) => (
            <button key={t} className={"chip cifra" + (ancho === t ? " chip--activo" : "")} onClick={() => setAncho(ancho === t ? null : t)}>
              {t} mm
            </button>
          ))}
        </div>
      )}

      {error && <p className="mensaje-error">{error}</p>}
      {!cargando && armazones.length === 0 && <div className="panel vacio">No hay armazones con esos filtros.</div>}

      <div className="cuadricula-armazones">
        {armazones.map((a) => (
          <button key={a.id} className={"armazon" + (a.existencia <= 0 ? " armazon--agotado" : "")} onClick={() => alTocar(a)}>
            <div className="armazon-foto">
              {a.fotoUrl ? (
                <img src={a.fotoUrl} alt={`${a.marca ?? ""} ${a.modelo ?? ""}`} loading="lazy" />
              ) : (
                <span className="armazon-sin-foto">
                  <Icono nombre="camara" tamano={28} />
                  Sin foto
                </span>
              )}
              {a.medidas && <span className="armazon-medidas cifra">{a.medidas}</span>}
            </div>
            <div className="armazon-datos">
              <strong className="armazon-codigo">{a.codigo}</strong>
              <span className="armazon-modelo">{[a.marca, a.modelo, a.color].filter(Boolean).join(" · ")}</span>
              <span className="armazon-pie">
                <span className="cifra">{dinero(a.precio)}</span>
                <span className={"insignia cifra " + (a.existencia > 0 ? "insignia--verde" : "insignia--ambar")}>
                  {a.existencia > 0 ? `${a.existencia} pza${a.existencia > 1 ? "s" : ""}` : "agotado"}
                </span>
              </span>
              {a.ubicacion && <span className="texto-tenue">{a.ubicacion}</span>}
            </div>
          </button>
        ))}
      </div>
    </div>
  )
}
