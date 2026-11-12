/*
  Lugar reservado para la Óptica RODRÍGUEZ.
  Cuando el backend tenga los endpoints de clientes, recetas y armazones
  (ver backend/.../optica/EJERCICIOS.md), aquí irán sus pantallas:
    - Clientes.jsx      (alta y búsqueda)
    - Recetas.jsx       (graduación OD / OI)
    - Armazones.jsx     (catálogo con foto y medidas; la foto se toma con <input type="file" accept="image/*" capture="environment">)
    - Cumpleaneros.jsx  (botón por cliente que abre WhatsApp con el cupón)
*/
export default function Proximamente() {
  return (
    <section className="angosto">
      <h1 className="nombre-pagina">Óptica RODRÍGUEZ</h1>
      <p className="descripcion-pagina">Este módulo está en construcción.</p>
      <div className="panel">
        <ul className="lista-pasos">
          <li>Clientes con celular y fecha de nacimiento</li>
          <li>Historial de graduaciones (OD / OI)</li>
          <li>Armazones con foto, código y medidas</li>
          <li>Órdenes de lentes con estado del trabajo</li>
          <li>Cupones de cumpleaños y promociones</li>
        </ul>
      </div>
    </section>
  )
}
