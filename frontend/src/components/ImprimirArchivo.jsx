import { useEffect, useState } from "react"
import { impresionesApi } from "../services/api"
import Modal from "./Modal"
import Icono from "./Icono"

/*
  VENTANA DE IMPRESIÓN de un archivo del buzón (PDF o imagen).

  A la izquierda, una VISTA PREVIA que simula la hoja: tamaño de papel, orientación,
  color o blanco y negro, y página por página en los PDF.
  A la derecha, las OPCIONES (como en cualquier programa): impresora, copias, color,
  papel, orientación, ajuste, páginas y doble cara.

  "Imprimir" manda el trabajo DIRECTO a la impresora de la computadora del negocio
  (lo hace el backend; ver ImpresoraService.java). No se abren pestañas ni hay que
  dar Ctrl+P. Funciona igual desde el celular.

  Props: trabajo, archivo, alCerrar(), alImprimir(hojas)
*/

// Proporción ancho/alto de cada papel en vertical.
const PAPELES = {
  CARTA: { nombre: "Carta", proporcion: 8.5 / 11 },
  OFICIO: { nombre: "Oficio", proporcion: 8.5 / 14 },
  A4: { nombre: "A4", proporcion: 210 / 297 },
}

export default function ImprimirArchivo({ trabajo, archivo, alCerrar, alImprimir }) {
  const [info, setInfo] = useState(null)
  const [impresoras, setImpresoras] = useState({ nombres: [], predeterminada: null })
  const [pagina, setPagina] = useState(1)
  const [vista, setVista] = useState(null) // { url, horizontal }
  const [o, setO] = useState({
    impresora: "",
    copias: trabajo.copias,
    color: trabajo.color,
    papel: "CARTA",
    orientacion: "AUTO",
    ajuste: "AJUSTAR",
    paginas: "",
    dobleCara: trabajo.dobleCara,
  })
  const [enviando, setEnviando] = useState(false)
  const [resultado, setResultado] = useState(null)
  const [error, setError] = useState("")

  useEffect(() => {
    impresionesApi.info(trabajo.id, archivo.id).then(setInfo).catch((e) => setError(e.message))
    impresionesApi
      .impresoras()
      .then((r) => {
        setImpresoras(r)
        setO((x) => ({ ...x, impresora: r.predeterminada ?? r.nombres[0] ?? "" }))
      })
      .catch((e) => setError(e.message))
  }, [trabajo.id, archivo.id])

  // Vista previa: se vuelve a pedir al cambiar de página o de color.
  useEffect(() => {
    let cancelado = false
    impresionesApi
      .vistaPrevia(trabajo.id, archivo.id, pagina, o.color)
      .then((u) => {
        const img = new Image()
        img.onload = () => {
          if (cancelado) return URL.revokeObjectURL(u)
          // Se libera la imagen anterior solo cuando la nueva ya está lista (así no parpadea).
          setVista((anterior) => {
            if (anterior) URL.revokeObjectURL(anterior.url)
            return { url: u, horizontal: img.width > img.height }
          })
        }
        img.src = u
      })
      .catch((e) => setError(e.message))
    return () => {
      cancelado = true
    }
  }, [trabajo.id, archivo.id, pagina, o.color])

  const cambiar = (campo, valor) => setO({ ...o, [campo]: valor })

  // Orientación de la hoja simulada.
  const horizontal = o.orientacion === "HORIZONTAL" || (o.orientacion === "AUTO" && vista?.horizontal)
  const proporcion = horizontal ? 1 / PAPELES[o.papel].proporcion : PAPELES[o.papel].proporcion

  async function imprimir() {
    setError("")
    setEnviando(true)
    try {
      const r = await impresionesApi.imprimir(trabajo.id, archivo.id, { ...o, copias: Number(o.copias) })
      setResultado(r)
      alImprimir(r.hojas)
    } catch (e) {
      setError(e.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal titulo={`Imprimir · ${archivo.nombre}`} alCerrar={alCerrar} ancho>
      <div className="impresion">
        <div className="impresion-vista">
          <div className="hoja-simulada" style={{ "--proporcion": proporcion }}>
            {vista ? (
              <img src={vista.url} alt={`Vista previa, página ${pagina}`} className={o.ajuste === "TAMANO_REAL" ? "tamano-real" : ""} />
            ) : (
              <span className="texto-tenue">Cargando vista previa…</span>
            )}
          </div>
          {info?.paginas > 1 && (
            <div className="fila fila--centro">
              <button className="boton-icono" onClick={() => setPagina(pagina - 1)} disabled={pagina <= 1} aria-label="Página anterior"><Icono nombre="izquierda" /></button>
              <span className="cifra">Página {pagina} de {info.paginas}</span>
              <button className="boton-icono" onClick={() => setPagina(pagina + 1)} disabled={pagina >= info.paginas} aria-label="Página siguiente"><Icono nombre="derecha" /></button>
            </div>
          )}
          <p className="texto-tenue texto-centro">Simulación de la hoja: {PAPELES[o.papel].nombre}, {horizontal ? "horizontal" : "vertical"}, {o.color ? "color" : "blanco y negro"}</p>
        </div>

        <div className="formulario">
          <label className="campo">
            <span className="etiqueta">Impresora</span>
            <select className="entrada" value={o.impresora} onChange={(e) => cambiar("impresora", e.target.value)}>
              {impresoras.nombres.map((n) => (
                <option key={n} value={n}>{n}{n === impresoras.predeterminada ? " (predeterminada)" : ""}</option>
              ))}
            </select>
          </label>

          <div className="rejilla">
            <label className="campo">
              <span className="etiqueta">Copias</span>
              <input className="entrada cifra" type="number" min="1" max="100" value={o.copias} onChange={(e) => cambiar("copias", e.target.value)} />
            </label>
            <label className="campo">
              <span className="etiqueta">Páginas {info ? `(de ${info.paginas})` : ""}</span>
              <input className="entrada cifra" placeholder="Todas · ej. 1-3,5" value={o.paginas} onChange={(e) => cambiar("paginas", e.target.value)} />
            </label>
          </div>

          <div className="campo">
            <span className="etiqueta">Color</span>
            <div className="segmentado">
              <button type="button" className={!o.color ? "activo" : ""} onClick={() => cambiar("color", false)}>Blanco y negro</button>
              <button type="button" className={o.color ? "activo" : ""} onClick={() => cambiar("color", true)}>Color</button>
            </div>
          </div>

          <div className="campo">
            <span className="etiqueta">Papel</span>
            <div className="segmentado">
              {Object.entries(PAPELES).map(([v, p]) => (
                <button key={v} type="button" className={o.papel === v ? "activo" : ""} onClick={() => cambiar("papel", v)}>{p.nombre}</button>
              ))}
            </div>
          </div>

          <div className="campo">
            <span className="etiqueta">Orientación</span>
            <div className="segmentado">
              {[["AUTO", "Automática"], ["VERTICAL", "Vertical"], ["HORIZONTAL", "Horizontal"]].map(([v, t]) => (
                <button key={v} type="button" className={o.orientacion === v ? "activo" : ""} onClick={() => cambiar("orientacion", v)}>{t}</button>
              ))}
            </div>
          </div>

          <div className="campo">
            <span className="etiqueta">Tamaño</span>
            <div className="segmentado">
              <button type="button" className={o.ajuste === "AJUSTAR" ? "activo" : ""} onClick={() => cambiar("ajuste", "AJUSTAR")}>Ajustar a la hoja</button>
              <button type="button" className={o.ajuste === "TAMANO_REAL" ? "activo" : ""} onClick={() => cambiar("ajuste", "TAMANO_REAL")}>Tamaño real</button>
            </div>
          </div>

          <label className="casilla">
            <input type="checkbox" checked={o.dobleCara} onChange={(e) => cambiar("dobleCara", e.target.checked)} />
            Doble cara (si la impresora lo permite)
          </label>

          {trabajo.notas && <p className="aviso-barra">El cliente pidió: {trabajo.notas}</p>}
          {error && <p className="mensaje-error">{error}</p>}
          {resultado && (
            <p className="alerta alerta--exito">
              Enviado a {resultado.impresora}: {resultado.paginasImpresas} pág. × {o.copias} = {resultado.hojas} hojas
            </p>
          )}

          <button className="btn btn--primario btn--bloque" onClick={imprimir} disabled={enviando || !o.impresora}>
            <Icono nombre="impresora" /> {enviando ? "Enviando a la impresora…" : "Imprimir"}
          </button>
          <button className="btn btn--texto" onClick={() => impresionesApi.abrirArchivo(trabajo.id, archivo.id).catch((e) => setError(e.message))}>
            Abrir el archivo en otra pestaña
          </button>
        </div>
      </div>
    </Modal>
  )
}
