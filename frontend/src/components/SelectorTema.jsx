import Icono from "./Icono"
import useTema from "../hooks/useTema"

const OPCIONES = [
  { valor: "light", texto: "Tema claro", icono: "sol" },
  { valor: "sistema", texto: "Usar el tema del sistema", icono: "pantalla" },
  { valor: "dark", texto: "Tema oscuro", icono: "luna" },
]

export default function SelectorTema({ flotante = false }) {
  const [tema, setTema] = useTema()

  return (
    <div className={"tema" + (flotante ? " tema--flotante" : "")} role="group" aria-label="Tema de color">
      {OPCIONES.map((o) => (
        <button
          key={o.valor}
          type="button"
          title={o.texto}
          aria-label={o.texto}
          aria-pressed={tema === o.valor}
          onClick={() => setTema(o.valor)}
        >
          <Icono nombre={o.icono} />
        </button>
      ))}
    </div>
  )
}
