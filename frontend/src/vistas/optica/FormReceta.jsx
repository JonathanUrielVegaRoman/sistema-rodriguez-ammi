import { useState } from "react"
import { clientesApi } from "../../services/api"
import { hoyISO } from "../../utils/formato"

/*
  Captura de GRADUACIÓN. Los valores se escriben como vienen en el examen: −2.25, +1.50...
  (en el celular, el teclado numérico a veces no trae el signo menos: por eso es "decimal" + texto).
*/
const OJO = ["Esfera", "Cilindro", "Eje", "Adicion"]

export default function FormReceta({ clienteId, alGuardar }) {
  const [f, setF] = useState({ fecha: hoyISO(), dip: "", observaciones: "" })
  const [error, setError] = useState("")

  const valor = (clave) => ({ value: f[clave] ?? "", onChange: (e) => setF({ ...f, [clave]: e.target.value }) })
  const numero = (v) => (v === undefined || v === "" ? null : Number(String(v).replace("−", "-").replace(",", ".")))

  async function guardar(e) {
    e.preventDefault()
    setError("")
    const datos = { fecha: f.fecha, dip: numero(f.dip), observaciones: f.observaciones || null }
    for (const lado of ["od", "oi"]) {
      for (const campo of OJO) datos[lado + campo] = numero(f[lado + campo])
    }
    try {
      alGuardar(await clientesApi.crearReceta(clienteId, datos))
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <form className="formulario" onSubmit={guardar}>
      <label className="campo">
        <span className="etiqueta">Fecha del examen</span>
        <input className="entrada" type="date" required {...valor("fecha")} />
      </label>

      <div className="captura-receta">
        <span></span>
        <span className="etiqueta">ESF</span>
        <span className="etiqueta">CIL</span>
        <span className="etiqueta">EJE</span>
        <span className="etiqueta">ADD</span>
        {["od", "oi"].map((lado) => (
          <Fila key={lado} lado={lado} valor={valor} />
        ))}
      </div>

      <div className="rejilla">
        <label className="campo">
          <span className="etiqueta">DIP (distancia interpupilar, mm)</span>
          <input className="entrada cifra" inputMode="decimal" placeholder="62" {...valor("dip")} />
        </label>
        <label className="campo">
          <span className="etiqueta">Observaciones</span>
          <input className="entrada" {...valor("observaciones")} />
        </label>
      </div>
      {error && <p className="mensaje-error">{error}</p>}
      <button className="btn btn--verde btn--bloque" type="submit">Guardar graduación</button>
    </form>
  )
}

function Fila({ lado, valor }) {
  return (
    <>
      <strong title={lado === "od" ? "Ojo derecho" : "Ojo izquierdo"}>{lado.toUpperCase()}</strong>
      <input className="entrada cifra" inputMode="text" placeholder="−2.25" aria-label={`${lado} esfera`} {...valor(lado + "Esfera")} />
      <input className="entrada cifra" inputMode="text" placeholder="−0.75" aria-label={`${lado} cilindro`} {...valor(lado + "Cilindro")} />
      <input className="entrada cifra" inputMode="numeric" placeholder="180" aria-label={`${lado} eje`} {...valor(lado + "Eje")} />
      <input className="entrada cifra" inputMode="decimal" placeholder="—" aria-label={`${lado} adición`} {...valor(lado + "Adicion")} />
    </>
  )
}
