import { useEffect, useRef, useState } from "react"
import { contornoLente, engordar, medidasDe } from "../utils/geometriaArmazon"

/*
  MODELO 3D DE UN ARMAZÓN, construido a partir de sus medidas.

  QUÉ ES (y qué no): no es un escaneo del armazón real. Es un modelo PARAMÉTRICO: se dibuja
  con las medidas de la varilla (lente, puente, varilla), la forma y el color. Por eso sirve
  para lo que más cuesta ver en una foto: el TAMAÑO y las PROPORCIONES. Dos armazones del
  mismo modelo en talla 50 y 52 se ven iguales en foto; aquí se pueden encimar y comparar.

  CÓMO SE CONSTRUYE (con la librería Three.js):
    1. geometriaArmazon.js da el contorno plano del lente (puntos x, y en milímetros).
    2. "Extruir" = darle grosor a ese contorno → aro. (Como una galleta cortada con molde.)
    3. Puente y varillas = "tubos" que siguen una curva.
    4. Luces + cámara + controles para girarlo con el dedo o el mouse.

  Three.js pesa ~170 KB comprimido, así que se carga SOLO cuando alguien abre esta vista
  (import() dinámico): el resto del sistema no se vuelve más lento por tenerla.

  Props:
    armazon   → { anchoLente, puente, varilla, altoLente?, forma?, colorHex? }
    comparar  → otro armazón para encimarlo como "fantasma" (opcional)
*/

const VISTAS = {
  tresCuartos: { nombre: "3/4", direccion: [0.75, 0.45, 1] },
  frente: { nombre: "Frente", direccion: [0, 0.02, 1] },
  lado: { nombre: "Lado", direccion: [1, 0.05, 0.02] },
  arriba: { nombre: "Arriba", direccion: [0, 1, 0.02] },
}

export default function Armazon3D({ armazon, comparar = null }) {
  const contenedor = useRef(null)
  const escena = useRef(null) // guarda lo necesario para cambiar de vista sin reconstruir todo
  const [estado, setEstado] = useState("cargando") // cargando | listo | error
  const [vista, setVista] = useState("tresCuartos")

  // Se reconstruye la escena solo si cambian las medidas, la forma o el color (no en cada pintada).
  const huella = JSON.stringify([armazon, comparar].map((a) => a && [a.anchoLente, a.puente, a.varilla, a.altoLente, a.forma, a.colorHex]))

  useEffect(() => {
    let cancelado = false
    let limpiar = () => {}
    setEstado("cargando")

    async function montar() {
      // Carga diferida: estos archivos solo se descargan al abrir el visor.
      const [THREE, { OrbitControls }, { RoomEnvironment }] = await Promise.all([
        import("three"),
        import("three/addons/controls/OrbitControls.js"),
        import("three/addons/environments/RoomEnvironment.js"),
      ])
      if (cancelado || !contenedor.current) return

      const lienzo = contenedor.current
      const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
      renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
      lienzo.appendChild(renderer.domElement)

      const scene = new THREE.Scene()
      // Un "cuarto" virtual iluminado: da reflejos suaves al acetato y al metal.
      const pmrem = new THREE.PMREMGenerator(renderer)
      scene.environment = pmrem.fromScene(new RoomEnvironment(), 0.04).texture
      const luz = new THREE.DirectionalLight(0xffffff, 1.2)
      luz.position.set(80, 140, 160)
      scene.add(luz)

      const m = medidasDe(armazon)
      const modelo = construirArmazon(THREE, armazon, { fantasma: false })
      scene.add(modelo)
      if (comparar) scene.add(construirArmazon(THREE, comparar, { fantasma: true }))
      scene.add(construirCotas(THREE, m))

      const camera = new THREE.PerspectiveCamera(32, 1, 1, 3000)
      const controls = new OrbitControls(camera, renderer.domElement)
      const centro = new THREE.Vector3(0, -2, -m.L * 0.38)
      controls.target.copy(centro)
      controls.enableDamping = true
      controls.enablePan = false
      controls.minDistance = 120
      controls.maxDistance = 900
      // Gira solo hasta que la persona lo toca (y nunca si pidió menos animaciones en su equipo).
      controls.autoRotate = !window.matchMedia("(prefers-reduced-motion: reduce)").matches
      controls.autoRotateSpeed = 1.2
      controls.addEventListener("start", () => (controls.autoRotate = false))

      const distancia = Math.max(m.anchoTotal, m.L) * 1.95
      const mirarDesde = ([x, y, z]) => {
        camera.position.copy(centro).add(new THREE.Vector3(x, y, z).normalize().multiplyScalar(distancia))
        controls.update()
      }
      mirarDesde(VISTAS.tresCuartos.direccion)

      const ajustarTamano = () => {
        const { clientWidth: w, clientHeight: h } = lienzo
        if (!w || !h) return
        renderer.setSize(w, h)
        camera.aspect = w / h
        camera.updateProjectionMatrix()
      }
      const observador = new ResizeObserver(ajustarTamano)
      observador.observe(lienzo)
      ajustarTamano()

      let cuadro
      const animar = () => {
        controls.update()
        renderer.render(scene, camera)
        cuadro = requestAnimationFrame(animar)
      }
      animar()

      escena.current = { mirarDesde, controls }
      setEstado("listo")

      // Al cerrar el visor se libera la memoria de la tarjeta gráfica (si no, se acumula).
      limpiar = () => {
        cancelAnimationFrame(cuadro)
        observador.disconnect()
        controls.dispose()
        scene.traverse((objeto) => {
          objeto.geometry?.dispose()
          const materiales = Array.isArray(objeto.material) ? objeto.material : [objeto.material]
          materiales.forEach((mat) => {
            mat?.map?.dispose()
            mat?.dispose()
          })
        })
        scene.environment?.dispose()
        pmrem.dispose()
        renderer.dispose()
        renderer.domElement.remove()
        escena.current = null
      }
    }

    montar().catch((e) => {
      console.error("No se pudo crear el visor 3D", e)
      if (!cancelado) setEstado("error")
    })

    return () => {
      cancelado = true
      limpiar()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [huella])

  function cambiarVista(clave) {
    setVista(clave)
    if (escena.current) {
      escena.current.controls.autoRotate = false
      escena.current.mirarDesde(VISTAS[clave].direccion)
    }
  }

  return (
    <div className="visor3d">
      <div className="visor3d-lienzo" ref={contenedor} role="img" aria-label="Modelo 3D del armazón; arrastra para girarlo">
        {estado === "cargando" && <span className="visor3d-aviso">Preparando el modelo 3D…</span>}
        {estado === "error" && <span className="visor3d-aviso">Este dispositivo no pudo mostrar el modelo 3D.</span>}
      </div>
      <div className="visor3d-pie">
        <div className="segmentado" role="group" aria-label="Vista">
          {Object.entries(VISTAS).map(([clave, v]) => (
            <button key={clave} type="button" className={vista === clave ? "activo" : ""} onClick={() => cambiarVista(clave)}>
              {v.nombre}
            </button>
          ))}
        </div>
        <span className="texto-tenue">Arrastra para girar · pellizca o usa la rueda para acercar</span>
      </div>
    </div>
  )
}

/* ------------------------------------------------------------------------------------------
   Construcción del armazón. Unidades: milímetros. El frente queda en el plano XY mirando
   hacia +Z; las varillas se van hacia atrás (-Z).
   ------------------------------------------------------------------------------------------ */
function construirArmazon(THREE, armazon, { fantasma }) {
  const m = medidasDe(armazon)
  const grupo = new THREE.Group()

  // Material del armazón. El "fantasma" (el que se compara) es naranja translúcido.
  const material = fantasma
    ? new THREE.MeshBasicMaterial({ color: 0xff6a00, transparent: true, opacity: 0.38, depthWrite: false })
    : new THREE.MeshStandardMaterial({
        color: new THREE.Color(armazon.colorHex || (m.metalico ? "#b9bcc2" : "#1b1b1b")),
        roughness: m.metalico ? 0.25 : 0.42,
        metalness: m.metalico ? 0.9 : 0.05,
      })
  const cristal = new THREE.MeshPhysicalMaterial({
    color: 0x9cc4e4, transparent: true, opacity: 0.3, roughness: 0.05, metalness: 0, side: THREE.DoubleSide, depthWrite: false,
  })
  const v2 = ([x, y]) => new THREE.Vector2(x, y)
  const alturaVarilla = m.B * 0.22 // las varillas salen un poco arriba del centro del lente

  for (const lado of [1, -1]) {
    const centroX = lado * (m.A / 2 + m.DBL / 2)

    // ARO: contorno de afuera con un hueco (el lente), extruido.
    const interior = contornoLente(m.forma, m.A, m.B, lado)
    const exterior = engordar(interior, m.aro)
    const figura = new THREE.Shape(exterior.map(v2))
    figura.holes.push(new THREE.Path(interior.map(v2).reverse()))
    const aro = new THREE.Mesh(
      new THREE.ExtrudeGeometry(figura, { depth: m.profundidad, bevelEnabled: true, bevelThickness: 0.35, bevelSize: 0.35, bevelSegments: 2, curveSegments: 4 }),
      material,
    )
    aro.position.set(centroX, 0, -m.profundidad / 2)
    grupo.add(aro)

    // LENTE (solo en el modelo principal).
    if (!fantasma) {
      const lente = new THREE.Mesh(new THREE.ShapeGeometry(new THREE.Shape(interior.map(v2))), cristal)
      lente.position.set(centroX, 0, 0)
      grupo.add(lente)
    }

    // BISAGRA: bloque que une el aro con la varilla.
    const orilla = lado * (m.A + m.DBL / 2 + m.aro)
    const bisagra = new THREE.Mesh(new THREE.BoxGeometry(4, m.metalico ? 2 : 6.5, m.profundidad + 2), material)
    bisagra.position.set(orilla + lado * 1.2, alturaVarilla, -1.5)
    grupo.add(bisagra)

    // VARILLA: tubo recto hacia atrás que al final baja para apoyarse en la oreja.
    const x = orilla + lado * 2
    const curva = new THREE.CatmullRomCurve3([
      new THREE.Vector3(x, alturaVarilla, -2),
      new THREE.Vector3(x + lado * 1.5, alturaVarilla, -m.L * 0.3),
      new THREE.Vector3(x + lado * 1.5, alturaVarilla, -(m.L - 42)),
      new THREE.Vector3(x + lado * 0.5, alturaVarilla - 5, -(m.L - 20)),
      new THREE.Vector3(x - lado * 1.5, alturaVarilla - 19, -m.L),
    ])
    const varilla = new THREE.Mesh(new THREE.TubeGeometry(curva, 48, m.metalico ? 0.85 : 1.7, 10), material)
    varilla.scale.y = m.metalico ? 1 : 1.9 // varilla de acetato: más alta que ancha
    varilla.position.y = m.metalico ? 0 : -alturaVarilla * 0.9 // compensa el escalado
    grupo.add(varilla)
  }

  // PUENTE: tubo curvo entre los dos aros (el aviador lleva doble puente).
  const alturaPuente = m.B * (m.metalico ? 0.08 : 0.2)
  const puente = (y, arco, radio) =>
    new THREE.Mesh(
      new THREE.TubeGeometry(
        new THREE.QuadraticBezierCurve3(
          new THREE.Vector3(-m.DBL / 2 - m.aro * 0.6, y, 0),
          new THREE.Vector3(0, y + arco, 1),
          new THREE.Vector3(m.DBL / 2 + m.aro * 0.6, y, 0),
        ),
        16, radio, 10,
      ),
      material,
    )
  grupo.add(puente(alturaPuente, 2.5, m.metalico ? 0.8 : 2.4))
  if (m.metalico) grupo.add(puente(m.B * 0.4, 0, 0.8))

  return grupo
}

/* ------------------------------------------------------------------------------------------
   COTAS: las líneas con medida, como en un plano. Cada una con su etiqueta.
   ------------------------------------------------------------------------------------------ */
function construirCotas(THREE, m) {
  const grupo = new THREE.Group()
  const linea = new THREE.LineBasicMaterial({ color: 0x8a8f99 })

  const cota = (desde, hasta, texto) => {
    grupo.add(new THREE.Line(new THREE.BufferGeometry().setFromPoints([desde, hasta]), linea))
    // Topes en los extremos.
    for (const p of [desde, hasta]) {
      const tope = new THREE.Mesh(new THREE.SphereGeometry(0.9, 8, 8), new THREE.MeshBasicMaterial({ color: 0x8a8f99 }))
      tope.position.copy(p)
      grupo.add(tope)
    }
    const etiqueta = crearEtiqueta(THREE, texto)
    etiqueta.position.copy(desde).add(hasta).multiplyScalar(0.5)
    grupo.add(etiqueta)
  }

  const arriba = m.B / 2 + 9
  const abajo = -m.B / 2 - 11
  const V = (x, y, z) => new THREE.Vector3(x, y, z)
  cota(V(m.DBL / 2, arriba, 0), V(m.DBL / 2 + m.A, arriba, 0), `Lente ${m.A}`)
  cota(V(-m.DBL / 2, arriba + 9, 0), V(m.DBL / 2, arriba + 9, 0), `Puente ${m.DBL}`)
  cota(V(-m.anchoTotal / 2, abajo, 0), V(m.anchoTotal / 2, abajo, 0), `Ancho total ≈ ${m.anchoTotal} mm`)
  const xVarilla = m.anchoTotal / 2 + 10 // del lado que queda hacia la cámara en la vista 3/4
  cota(V(xVarilla, m.B * 0.22, 0), V(xVarilla, m.B * 0.22, -m.L), `Varilla ${m.L}`)
  return grupo
}

/** Etiqueta de texto que siempre mira a la cámara (un "sprite"), dibujada en un lienzo 2D. */
function crearEtiqueta(THREE, texto) {
  const lienzo = document.createElement("canvas")
  const ctx = lienzo.getContext("2d")
  const tamano = 44
  ctx.font = `600 ${tamano}px Poppins, "Segoe UI", sans-serif`
  const ancho = Math.ceil(ctx.measureText(texto).width) + 36
  lienzo.width = ancho
  lienzo.height = tamano + 28
  // Pastilla blanca con texto oscuro: se lee igual en tema claro y oscuro.
  ctx.fillStyle = "rgba(255,255,255,0.94)"
  ctx.beginPath()
  // roundRect no existe en navegadores viejos: ahí la pastilla sale con esquinas rectas.
  if (ctx.roundRect) ctx.roundRect(0, 0, lienzo.width, lienzo.height, lienzo.height / 2)
  else ctx.rect(0, 0, lienzo.width, lienzo.height)
  ctx.fill()
  ctx.font = `600 ${tamano}px Poppins, "Segoe UI", sans-serif`
  ctx.fillStyle = "#1a1b15"
  ctx.textAlign = "center"
  ctx.textBaseline = "middle"
  ctx.fillText(texto, lienzo.width / 2, lienzo.height / 2 + 2)

  const textura = new THREE.CanvasTexture(lienzo)
  textura.colorSpace = THREE.SRGBColorSpace
  const sprite = new THREE.Sprite(new THREE.SpriteMaterial({ map: textura, depthTest: false, transparent: true }))
  const alto = 8 // milímetros de alto en la escena
  sprite.scale.set((alto * lienzo.width) / lienzo.height, alto, 1)
  sprite.renderOrder = 10
  return sprite
}
