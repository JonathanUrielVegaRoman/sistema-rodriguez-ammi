import { useEffect, useMemo, useState } from "react"
import { clientesApi, ordenesApi, tratamientosApi } from "../../services/api"
import { ir } from "../../hooks/useRuta"
import { dinero, fechaCorta, NOMBRES_MICA, NOMBRES_PAGO } from "../../utils/formato"
import CuadriculaArmazones from "../../components/CuadriculaArmazones"
import TablaReceta from "../../components/TablaReceta"
import Modal from "../../components/Modal"
import Icono from "../../components/Icono"

/*
  NUEVA ORDEN DE LENTES, en 5 pasos dentro de la misma pantalla:

    1. Cliente        → buscarlo (o llega ya elegido desde su ficha: ?cliente=7)
    2. Graduación     → se propone la más reciente
    3. Pares          → armazón (cuadrícula con foto), mica, material, tratamientos
                        "+ Par de regalo (2x1)": micas y tratamientos gratis, solo tratamientos permitidos
    4. Descuentos     → código de cupón (cumpleaños) y/o descuento especial con motivo
    5. Entrega y pago → fecha estimada, anticipo

  El RESUMEN de la derecha le pregunta al backend el total cada vez que algo cambia
  (POST /cotizar). Así las reglas y los cálculos viven en UN solo lugar: el backend.
*/

let siguienteClave = 1
const parNuevo = (esRegalo = false) => ({
  clave: siguienteClave++,
  armazon: null,
  propio: false,
  precioArmazon: "",
  tipoMica: "MONOFOCAL",
  material: "CR-39",
  precioMicas: esRegalo ? "0" : "",
  tratamientoIds: [],
  esRegalo,
})

export default function NuevaOrden({ consulta }) {
  const [cliente, setCliente] = useState(null)
  const [recetas, setRecetas] = useState([])
  const [recetaId, setRecetaId] = useState(null)
  const [tratamientos, setTratamientos] = useState([])
  const [pares, setPares] = useState([parNuevo()])
  const [eligiendoPara, setEligiendoPara] = useState(null) // clave del par que está eligiendo armazón
  const [codigoCupon, setCodigoCupon] = useState("")
  const [descuentoManual, setDescuentoManual] = useState("")
  const [motivoDescuento, setMotivoDescuento] = useState("")
  const [fechaEntrega, setFechaEntrega] = useState("")
  const [anticipo, setAnticipo] = useState("")
  const [metodo, setMetodo] = useState("EFECTIVO")
  const [notas, setNotas] = useState("")
  const [cotizacion, setCotizacion] = useState(null)
  const [errorCotizacion, setErrorCotizacion] = useState("")
  const [error, setError] = useState("")
  const [guardando, setGuardando] = useState(false)

  useEffect(() => {
    tratamientosApi.activos().then(setTratamientos).catch((e) => setError(e.message))
    if (consulta?.cliente) clientesApi.obtener(consulta.cliente).then(elegirCliente).catch((e) => setError(e.message))
  }, [consulta?.cliente])

  function elegirCliente(c) {
    setCliente(c)
    clientesApi.recetas(c.id).then((r) => {
      setRecetas(r)
      setRecetaId(r[0]?.id ?? null) // la más reciente
    })
  }

  const cambiarPar = (clave, cambios) => setPares((ps) => ps.map((p) => (p.clave === clave ? { ...p, ...cambios } : p)))

  function alternarTratamiento(par, t) {
    const ids = par.tratamientoIds.includes(t.id) ? par.tratamientoIds.filter((x) => x !== t.id) : [...par.tratamientoIds, t.id]
    cambiarPar(par.clave, { tratamientoIds: ids })
  }

  // Lo que se manda al backend (mismo formato para /cotizar y para crear).
  const solicitud = useMemo(() => {
    if (!cliente) return null
    const completos = pares.every((p) => (p.armazon || p.propio) && p.precioArmazon !== "" && p.precioMicas !== "")
    if (!completos) return null
    return {
      clienteId: cliente.id,
      recetaId,
      fechaEntregaEstimada: fechaEntrega || null,
      partidas: pares.map((p) => ({
        armazonId: p.propio ? null : p.armazon.id,
        precioArmazon: Number(p.precioArmazon),
        tipoMica: p.tipoMica,
        material: p.material,
        precioMicas: p.tipoMica === "SOLO_ARMAZON" ? 0 : Number(p.precioMicas),
        tratamientoIds: p.tipoMica === "SOLO_ARMAZON" ? [] : p.tratamientoIds,
        esRegalo: p.esRegalo,
      })),
      codigoCupon: codigoCupon.trim() || null,
      descuentoManual: descuentoManual ? Number(descuentoManual) : 0,
      motivoDescuento: motivoDescuento || null,
      anticipo: anticipo ? Number(anticipo) : 0,
      metodoPagoAnticipo: anticipo ? metodo : null,
      notas: notas || null,
    }
  }, [cliente, recetaId, pares, codigoCupon, descuentoManual, motivoDescuento, fechaEntrega, anticipo, metodo, notas])

  // Cotizar 400 ms después del último cambio.
  useEffect(() => {
    if (!solicitud) {
      setCotizacion(null)
      return
    }
    const espera = setTimeout(() => {
      ordenesApi
        .cotizar({ ...solicitud, anticipo: 0, metodoPagoAnticipo: null })
        .then((c) => {
          setCotizacion(c)
          setErrorCotizacion("")
        })
        .catch((e) => {
          setCotizacion(null)
          setErrorCotizacion(e.message)
        })
    }, 400)
    return () => clearTimeout(espera)
  }, [solicitud])

  async function crear() {
    setError("")
    setGuardando(true)
    try {
      const orden = await ordenesApi.crear(solicitud)
      ir(`/optica/ordenes/${orden.folio}`)
    } catch (e) {
      setError(e.message)
    } finally {
      setGuardando(false)
    }
  }

  const hayPagado = pares.some((p) => !p.esRegalo)
  const regalos = pares.filter((p) => p.esRegalo).length
  const pagados = pares.length - regalos

  return (
    <section>
      <h1 className="titulo-seccion">Nueva orden de lentes</h1>

      <div className="orden-layout">
        <div>
          {/* 1. CLIENTE */}
          <div className="panel">
            <h2 className="panel-titulo"><span className="paso">1</span> Cliente</h2>
            {cliente ? (
              <div className="fila fila--entre">
                <div>
                  <strong>{cliente.nombre}</strong>
                  <div className="texto-tenue cifra">{cliente.celular ?? "sin celular"}</div>
                </div>
                {!consulta?.cliente && <button className="btn btn--texto" onClick={() => setCliente(null)}>Cambiar</button>}
              </div>
            ) : (
              <BuscarCliente alElegir={elegirCliente} />
            )}
          </div>

          {/* 2. GRADUACIÓN */}
          {cliente && (
            <div className="panel">
              <h2 className="panel-titulo"><span className="paso">2</span> Graduación</h2>
              {recetas.length === 0 ? (
                <p className="aviso-barra">
                  Este cliente no tiene graduación registrada.{" "}
                  <a href={`#/optica/clientes/${cliente.id}`}>Capturarla en su ficha</a> o continúa si solo compra armazón.
                </p>
              ) : (
                <>
                  <select className="entrada" value={recetaId ?? ""} onChange={(e) => setRecetaId(e.target.value ? Number(e.target.value) : null)}>
                    {recetas.map((r, i) => (
                      <option key={r.id} value={r.id}>Examen del {fechaCorta(r.fecha)}{i === 0 ? " (más reciente)" : ""}</option>
                    ))}
                    <option value="">Sin graduación (solo armazón)</option>
                  </select>
                  {recetaId && <TablaReceta receta={recetas.find((r) => r.id === recetaId)} />}
                </>
              )}
            </div>
          )}

          {/* 3. PARES DE LENTES */}
          {cliente && (
            <div className="panel">
              <h2 className="panel-titulo"><span className="paso">3</span> Lentes</h2>
              {pares.map((p, i) => (
                <div key={p.clave} className={"par" + (p.esRegalo ? " par--regalo" : "")}>
                  <div className="fila fila--entre">
                    <strong>
                      Par {i + 1} {p.esRegalo && <span className="insignia insignia--verde"><Icono nombre="regalo" tamano={14} /> Regalo 2x1</span>}
                    </strong>
                    {pares.length > 1 && <button className="btn btn--texto btn--peligro-texto" onClick={() => setPares(pares.filter((x) => x.clave !== p.clave))}>Quitar</button>}
                  </div>

                  {/* Armazón */}
                  <div className="par-armazon">
                    <div className="par-armazon-foto">
                      {p.armazon?.fotoUrl ? <img src={p.armazon.fotoUrl} alt="" /> : <Icono nombre="lentes" tamano={28} />}
                    </div>
                    <div className="par-armazon-datos">
                      {p.propio ? <strong>Armazón propio del cliente</strong> : p.armazon ? (
                        <>
                          <strong>{p.armazon.codigo}</strong>
                          <span className="texto-tenue">{[p.armazon.marca, p.armazon.modelo].filter(Boolean).join(" · ")} {p.armazon.medidas && `· ${p.armazon.medidas}`}</span>
                        </>
                      ) : <span className="texto-tenue">Elige el armazón</span>}
                      <div className="acciones acciones--sin-margen">
                        <button className="btn btn--contorno btn--chico" onClick={() => setEligiendoPara(p.clave)}>Elegir del catálogo</button>
                        <button className="btn btn--texto" onClick={() => cambiarPar(p.clave, { propio: true, armazon: null, precioArmazon: "0" })}>Trae el suyo</button>
                      </div>
                    </div>
                  </div>

                  <div className="rejilla">
                    <label className="campo">
                      <span className="etiqueta">Precio del armazón</span>
                      <input className="entrada cifra" type="number" min="0" inputMode="decimal" value={p.precioArmazon} onChange={(e) => cambiarPar(p.clave, { precioArmazon: e.target.value })} />
                    </label>
                    <label className="campo">
                      <span className="etiqueta">Tipo de mica</span>
                      <select className="entrada" value={p.tipoMica} onChange={(e) => cambiarPar(p.clave, { tipoMica: e.target.value })}>
                        {Object.entries(NOMBRES_MICA).map(([v, t]) => <option key={v} value={v}>{t}</option>)}
                      </select>
                    </label>
                    {p.tipoMica !== "SOLO_ARMAZON" && (
                      <>
                        <label className="campo">
                          <span className="etiqueta">Material</span>
                          <input className="entrada" list="materiales" value={p.material} onChange={(e) => cambiarPar(p.clave, { material: e.target.value })} />
                        </label>
                        <label className="campo">
                          <span className="etiqueta">Precio de las micas {p.esRegalo && "(no se cobra)"}</span>
                          <input className="entrada cifra" type="number" min="0" inputMode="decimal" value={p.precioMicas} onChange={(e) => cambiarPar(p.clave, { precioMicas: e.target.value })} />
                        </label>
                      </>
                    )}
                  </div>

                  {p.tipoMica !== "SOLO_ARMAZON" && (
                    <div className="chips" role="group" aria-label="Tratamientos">
                      <span className="etiqueta">Tratamientos</span>
                      {tratamientos.map((t) => {
                        const bloqueado = p.esRegalo && !t.aplicaEnPromocion
                        return (
                          <button
                            key={t.id}
                            type="button"
                            className={"chip" + (p.tratamientoIds.includes(t.id) ? " chip--activo" : "")}
                            onClick={() => alternarTratamiento(p, t)}
                            disabled={bloqueado}
                            title={bloqueado ? "No entra en el lente de regalo" : ""}
                          >
                            {t.nombre} <span className="cifra">{dinero(t.precio)}</span>
                          </button>
                        )
                      })}
                    </div>
                  )}
                </div>
              ))}
              <datalist id="materiales">
                <option value="CR-39" />
                <option value="Policarbonato" />
                <option value="Alto índice 1.67" />
                <option value="Trivex" />
              </datalist>
              <div className="acciones">
                <button className="btn btn--contorno" onClick={() => setPares([...pares, parNuevo()])}>+ Otro par</button>
                <button className="btn btn--contorno" onClick={() => setPares([...pares, parNuevo(true)])} disabled={!hayPagado || regalos >= pagados}>
                  <Icono nombre="regalo" /> + Par de regalo (2x1)
                </button>
              </div>
            </div>
          )}

          {/* 4. DESCUENTOS */}
          {cliente && (
            <div className="panel">
              <h2 className="panel-titulo"><span className="paso">4</span> Descuentos</h2>
              <div className="rejilla">
                <label className="campo">
                  <span className="etiqueta">Código de cupón (cumpleaños, etc.)</span>
                  <input className="entrada cifra" value={codigoCupon} onChange={(e) => setCodigoCupon(e.target.value.toUpperCase())} placeholder="CUMPLE-XXXXX" />
                </label>
                <label className="campo">
                  <span className="etiqueta">Descuento especial ($)</span>
                  <input className="entrada cifra" type="number" min="0" inputMode="decimal" value={descuentoManual} onChange={(e) => setDescuentoManual(e.target.value)} />
                </label>
              </div>
              {Number(descuentoManual) > 0 && (
                <label className="campo">
                  <span className="etiqueta">Motivo del descuento</span>
                  <input className="entrada" value={motivoDescuento} onChange={(e) => setMotivoDescuento(e.target.value)} placeholder="Ej. cliente frecuente" />
                </label>
              )}
            </div>
          )}

          {/* 5. ENTREGA Y PAGO */}
          {cliente && (
            <div className="panel">
              <h2 className="panel-titulo"><span className="paso">5</span> Entrega y anticipo</h2>
              <div className="rejilla">
                <label className="campo">
                  <span className="etiqueta">Fecha estimada de entrega</span>
                  <input className="entrada" type="date" value={fechaEntrega} onChange={(e) => setFechaEntrega(e.target.value)} />
                </label>
                <label className="campo">
                  <span className="etiqueta">Anticipo ($)</span>
                  <input className="entrada cifra" type="number" min="0" inputMode="decimal" value={anticipo} onChange={(e) => setAnticipo(e.target.value)} />
                </label>
              </div>
              {Number(anticipo) > 0 && (
                <div className="segmentado">
                  {Object.entries(NOMBRES_PAGO).map(([v, t]) => (
                    <button key={v} type="button" className={metodo === v ? "activo" : ""} onClick={() => setMetodo(v)}>{t}</button>
                  ))}
                </div>
              )}
              <label className="campo">
                <span className="etiqueta">Notas para el laboratorio o la entrega</span>
                <input className="entrada" value={notas} onChange={(e) => setNotas(e.target.value)} />
              </label>
            </div>
          )}
        </div>

        {/* RESUMEN */}
        <aside className="panel panel--pegado orden-resumen">
          <h2 className="panel-titulo">Resumen</h2>
          {!solicitud && <p className="texto-tenue">Completa cliente, armazón y precios para ver el total.</p>}
          {errorCotizacion && <p className="mensaje-error">{errorCotizacion}</p>}
          {cotizacion && (
            <dl className="totales cifra">
              {cotizacion.partidas.map((p, i) => (
                <div key={i} className="totales-renglon">
                  <dt>Par {i + 1}{p.esRegalo ? " (regalo)" : ""}</dt>
                  <dd>{dinero(p.subtotal)}</dd>
                </div>
              ))}
              {Number(cotizacion.descuentoCupon) > 0 && (
                <div className="totales-renglon texto-verde"><dt>Cupón {cotizacion.porcentajeCupon}%</dt><dd>−{dinero(cotizacion.descuentoCupon)}</dd></div>
              )}
              {Number(cotizacion.descuentoManual) > 0 && (
                <div className="totales-renglon texto-verde"><dt>Descuento especial</dt><dd>−{dinero(cotizacion.descuentoManual)}</dd></div>
              )}
              <div className="totales-renglon totales-total"><dt>Total</dt><dd>{dinero(cotizacion.total)}</dd></div>
              {Number(anticipo) > 0 && (
                <>
                  <div className="totales-renglon"><dt>Anticipo</dt><dd>{dinero(anticipo)}</dd></div>
                  <div className="totales-renglon"><dt>Saldo</dt><dd>{dinero(Number(cotizacion.total) - Number(anticipo))}</dd></div>
                </>
              )}
            </dl>
          )}
          {error && <p className="mensaje-error">{error}</p>}
          <button className="btn btn--verde btn--bloque" disabled={!cotizacion || guardando} onClick={crear}>
            {guardando ? "Guardando…" : "Crear orden"}
          </button>
        </aside>
      </div>

      {eligiendoPara && (
        <Modal titulo="Elegir armazón" alCerrar={() => setEligiendoPara(null)} ancho>
          <CuadriculaArmazones
            inicialDisponibles
            alTocar={(a) => {
              if (a.existencia <= 0) return
              const par = pares.find((x) => x.clave === eligiendoPara)
              cambiarPar(eligiendoPara, { armazon: a, propio: false, precioArmazon: par.esRegalo && par.precioArmazon !== "" ? par.precioArmazon : String(a.precio) })
              setEligiendoPara(null)
            }}
          />
        </Modal>
      )}
    </section>
  )
}

function BuscarCliente({ alElegir }) {
  const [texto, setTexto] = useState("")
  const [resultados, setResultados] = useState([])

  useEffect(() => {
    if (!texto.trim()) {
      setResultados([])
      return
    }
    const espera = setTimeout(() => clientesApi.buscar(texto).then(setResultados).catch(() => {}), 250)
    return () => clearTimeout(espera)
  }, [texto])

  return (
    <div>
      <input className="entrada" placeholder="Nombre o celular…" value={texto} onChange={(e) => setTexto(e.target.value)} autoFocus aria-label="Buscar cliente" />
      <ul className="lista-simple lista-simple--clic">
        {resultados.map((c) => (
          <li key={c.id}>
            <button onClick={() => alElegir(c)}>
              <span>{c.nombre}</span>
              <span className="texto-tenue cifra">{c.celular ?? ""}</span>
              <span></span>
            </button>
          </li>
        ))}
      </ul>
      <p className="texto-tenue">¿Cliente nuevo? <a href="#/optica/clientes">Regístralo primero</a>.</p>
    </div>
  )
}
