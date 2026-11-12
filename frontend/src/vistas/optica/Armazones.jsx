import { useState } from "react"
import { armazonesApi } from "../../services/api"
import { dinero } from "../../utils/formato"
import { reducirFoto } from "../../utils/imagen"
import CuadriculaArmazones from "../../components/CuadriculaArmazones"
import Modal from "../../components/Modal"
import Icono from "../../components/Icono"

/*
  CATÁLOGO DE ARMAZONES en cuadrícula con foto.

  Tocar un armazón abre su ficha: foto grande, medidas, ubicación, y botones para cambiar
  la foto o editar. La foto se toma con la cámara del celular directamente desde aquí.

  Consejo para las fotos (para que la cuadrícula se vea pareja):
    fondo blanco, armazón de frente, a la misma distancia, con buena luz.
*/
const VACIO = { codigo: "", marca: "", modelo: "", color: "", anchoLente: "", puente: "", varilla: "", precio: "", existencia: 1, ubicacion: "" }

export default function Armazones() {
  const [recarga, setRecarga] = useState(0)
  const [ficha, setFicha] = useState(null) // armazón abierto
  const [formulario, setFormulario] = useState(null) // null = cerrado
  const [error, setError] = useState("")
  const [subiendo, setSubiendo] = useState(false)

  const recargar = () => setRecarga((n) => n + 1)

  function abrirNuevo() {
    setError("")
    setFormulario({ ...VACIO, id: null })
  }

  function editar(a) {
    setError("")
    setFicha(null)
    setFormulario({
      id: a.id, codigo: a.codigo, marca: a.marca ?? "", modelo: a.modelo ?? "", color: a.color ?? "",
      anchoLente: a.anchoLente ?? "", puente: a.puente ?? "", varilla: a.varilla ?? "",
      precio: a.precio, existencia: a.existencia, ubicacion: a.ubicacion ?? "",
    })
  }

  const numero = (v) => (v === "" || v === null ? null : Number(v))

  async function guardar(e) {
    e.preventDefault()
    setError("")
    const datos = {
      ...formulario,
      anchoLente: numero(formulario.anchoLente), puente: numero(formulario.puente), varilla: numero(formulario.varilla),
      precio: Number(formulario.precio), existencia: Number(formulario.existencia),
    }
    try {
      const guardado = formulario.id ? await armazonesApi.actualizar(formulario.id, datos) : await armazonesApi.crear(datos)
      setFormulario(null)
      recargar()
      setFicha(guardado) // al terminar, se abre su ficha para tomarle la foto
    } catch (err) {
      setError(err.message)
    }
  }

  async function cambiarFoto(e) {
    const archivo = e.target.files[0]
    e.target.value = "" // permite volver a elegir el mismo archivo
    if (!archivo) return
    setError("")
    setSubiendo(true)
    try {
      const reducida = await reducirFoto(archivo)
      const actualizado = await armazonesApi.subirFoto(ficha.id, reducida)
      setFicha(actualizado)
      recargar()
    } catch (err) {
      setError(err.message)
    } finally {
      setSubiendo(false)
    }
  }

  async function desactivar() {
    if (!window.confirm(`¿Quitar el armazón ${ficha.codigo} del catálogo? Las órdenes anteriores no se afectan.`)) return
    try {
      await armazonesApi.desactivar(ficha.id)
      setFicha(null)
      recargar()
    } catch (err) {
      setError(err.message)
    }
  }

  const campo = (nombre) => ({
    name: nombre,
    value: formulario[nombre],
    onChange: (e) => setFormulario({ ...formulario, [nombre]: e.target.value }),
  })

  return (
    <section>
      <div className="titulo-con-accion">
        <h1 className="titulo-seccion">Armazones</h1>
        <button className="btn btn--verde" onClick={abrirNuevo}>+ Armazón</button>
      </div>

      <CuadriculaArmazones recarga={recarga} alTocar={(a) => { setError(""); setFicha(a) }} />

      {ficha && (
        <Modal titulo={ficha.codigo} alCerrar={() => setFicha(null)}>
          <div className="ficha-armazon">
            <div className="ficha-foto">
              {ficha.fotoUrl ? <img src={ficha.fotoUrl} alt={ficha.codigo} /> : <span className="armazon-sin-foto"><Icono nombre="camara" tamano={36} />Sin foto</span>}
            </div>
            <div className="acciones">
              {/* capture="environment" abre directo la cámara trasera del celular */}
              <label className="btn btn--primario">
                <Icono nombre="camara" /> {subiendo ? "Subiendo…" : "Tomar foto"}
                <input type="file" accept="image/*" capture="environment" hidden onChange={cambiarFoto} disabled={subiendo} />
              </label>
              <label className="btn btn--contorno">
                <Icono nombre="imagen" /> Galería
                <input type="file" accept="image/*" hidden onChange={cambiarFoto} disabled={subiendo} />
              </label>
            </div>
            <dl className="datos">
              <dt>Marca / modelo</dt>
              <dd>{[ficha.marca, ficha.modelo].filter(Boolean).join(" · ") || "—"}</dd>
              <dt>Color</dt>
              <dd>{ficha.color || "—"}</dd>
              <dt>Medidas</dt>
              <dd className="cifra">{ficha.medidas ?? "—"} <span className="texto-tenue">(lente □ puente, varilla)</span></dd>
              <dt>Precio</dt>
              <dd className="cifra">{dinero(ficha.precio)}</dd>
              <dt>Existencia</dt>
              <dd className="cifra">{ficha.existencia}</dd>
              <dt>Ubicación</dt>
              <dd>{ficha.ubicacion || "—"}</dd>
            </dl>
            {error && <p className="mensaje-error">{error}</p>}
            <div className="acciones">
              <button className="btn btn--contorno" onClick={() => editar(ficha)}>Editar datos</button>
              <button className="btn btn--texto btn--peligro-texto" onClick={desactivar}>Quitar del catálogo</button>
            </div>
          </div>
        </Modal>
      )}

      {formulario && (
        <Modal titulo={formulario.id ? "Editar armazón" : "Nuevo armazón"} alCerrar={() => setFormulario(null)}>
          <form className="formulario" onSubmit={guardar}>
            <div className="rejilla">
              <label className="campo">
                <span className="etiqueta">Código (el que trae el armazón)</span>
                <input className="entrada" required {...campo("codigo")} autoFocus />
              </label>
              <label className="campo">
                <span className="etiqueta">Precio</span>
                <input className="entrada" type="number" min="0" step="1" inputMode="decimal" required {...campo("precio")} />
              </label>
              <label className="campo">
                <span className="etiqueta">Marca</span>
                <input className="entrada" {...campo("marca")} />
              </label>
              <label className="campo">
                <span className="etiqueta">Modelo</span>
                <input className="entrada" {...campo("modelo")} />
              </label>
              <label className="campo">
                <span className="etiqueta">Color</span>
                <input className="entrada" {...campo("color")} />
              </label>
              <label className="campo">
                <span className="etiqueta">Existencia</span>
                <input className="entrada" type="number" min="0" inputMode="numeric" {...campo("existencia")} />
              </label>
            </div>
            <fieldset className="grupo">
              <legend className="etiqueta">Medidas grabadas en la varilla (mm), ej. 52 □ 18 140</legend>
              <div className="rejilla rejilla--3">
                <label className="campo">
                  <span className="etiqueta">Lente</span>
                  <input className="entrada" type="number" min="30" max="70" inputMode="numeric" placeholder="52" {...campo("anchoLente")} />
                </label>
                <label className="campo">
                  <span className="etiqueta">Puente</span>
                  <input className="entrada" type="number" min="10" max="30" inputMode="numeric" placeholder="18" {...campo("puente")} />
                </label>
                <label className="campo">
                  <span className="etiqueta">Varilla</span>
                  <input className="entrada" type="number" min="110" max="160" inputMode="numeric" placeholder="140" {...campo("varilla")} />
                </label>
              </div>
            </fieldset>
            <label className="campo">
              <span className="etiqueta">Ubicación en el local</span>
              <input className="entrada" placeholder="Ej. Vitrina 2, charola de arriba" {...campo("ubicacion")} />
            </label>
            {error && <p className="mensaje-error">{error}</p>}
            <button className="btn btn--verde btn--bloque" type="submit">{formulario.id ? "Guardar cambios" : "Guardar y tomar foto"}</button>
          </form>
        </Modal>
      )}
    </section>
  )
}
