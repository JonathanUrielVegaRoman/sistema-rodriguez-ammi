import { useState } from "react"
import { clientesApi } from "../../services/api"

/*
  Formulario de cliente (se usa para crear y para editar).
  Props: inicial (cliente a editar, opcional), alGuardar(clienteGuardado)
*/
export default function FormCliente({ inicial, alGuardar }) {
  const [f, setF] = useState({
    nombre: inicial?.nombre ?? "",
    celular: inicial?.celular ?? "",
    fechaNacimiento: inicial?.fechaNacimiento ?? "",
    aceptaPromociones: inicial?.aceptaPromociones ?? true,
    notas: inicial?.notas ?? "",
  })
  const [error, setError] = useState("")

  async function guardar(e) {
    e.preventDefault()
    setError("")
    const datos = { ...f, celular: f.celular.replace(/\D/g, ""), fechaNacimiento: f.fechaNacimiento || null }
    try {
      const c = inicial ? await clientesApi.actualizar(inicial.id, datos) : await clientesApi.crear(datos)
      alGuardar(c)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <form className="formulario" onSubmit={guardar}>
      <label className="campo">
        <span className="etiqueta">Nombre completo</span>
        <input className="entrada" value={f.nombre} onChange={(e) => setF({ ...f, nombre: e.target.value })} required autoFocus />
      </label>
      <div className="rejilla">
        <label className="campo">
          <span className="etiqueta">Celular (10 dígitos)</span>
          <input className="entrada cifra" type="tel" inputMode="numeric" value={f.celular} onChange={(e) => setF({ ...f, celular: e.target.value })} placeholder="5512345678" />
        </label>
        <label className="campo">
          <span className="etiqueta">Fecha de nacimiento</span>
          <input className="entrada" type="date" value={f.fechaNacimiento} onChange={(e) => setF({ ...f, fechaNacimiento: e.target.value })} />
        </label>
      </div>
      <label className="casilla">
        <input type="checkbox" checked={f.aceptaPromociones} onChange={(e) => setF({ ...f, aceptaPromociones: e.target.checked })} />
        Acepta recibir felicitación y promociones por WhatsApp (pregúntale al cliente)
      </label>
      <label className="campo">
        <span className="etiqueta">Notas (opcional)</span>
        <input className="entrada" value={f.notas} onChange={(e) => setF({ ...f, notas: e.target.value })} />
      </label>
      {error && <p className="mensaje-error">{error}</p>}
      <button className="btn btn--verde btn--bloque" type="submit">{inicial ? "Guardar cambios" : "Registrar cliente"}</button>
    </form>
  )
}
