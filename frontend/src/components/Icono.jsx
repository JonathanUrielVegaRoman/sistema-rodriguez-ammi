/**
 * Iconos de línea sencillos en SVG (mismo estilo que el proyecto de sedimento urinario).
 * SVG = dibujos hechos con código: se ven nítidos en cualquier tamaño y toman el color del texto.
 * Uso: <Icono nombre="casa" tamano={20} />
 */
const TRAZOS = {
  alerta: (
    <>
      <path d="M12 3.5 2.5 20h19L12 3.5Z" />
      <path d="M12 10v4.5M12 17.2v.3" />
    </>
  ),
  check: <path d="m5 12.5 4.5 4.5L19 7.5" />,
  derecha: <path d="M5 12h14m-5-5 5 5-5 5" />,
  izquierda: <path d="M19 12H5m5-5-5 5 5 5" />,
  mas: <path d="M12 5v14M5 12h14" />,
  cerrar: <path d="M6 6l12 12M18 6 6 18" />,
  casa: (
    <>
      <path d="M3.5 11 12 4l8.5 7" />
      <path d="M5.5 9.5V20h13V9.5M10 20v-5h4v5" />
    </>
  ),
  buscar: (
    <>
      <circle cx="11" cy="11" r="6.5" />
      <path d="m16 16 4.5 4.5" />
    </>
  ),
  candado: (
    <>
      <rect x="5" y="10.5" width="14" height="10" rx="1.5" />
      <path d="M8 10.5V8a4 4 0 0 1 8 0v2.5M12 14.5v2.5" />
    </>
  ),
  camara: (
    <>
      <path d="M3.5 8h4l1.5-2.5h6L16.5 8h4v11.5h-17Z" />
      <circle cx="12" cy="13.5" r="3.5" />
    </>
  ),
  imagen: (
    <>
      <rect x="3.5" y="4.5" width="17" height="15" rx="1" />
      <circle cx="9" cy="10" r="1.6" />
      <path d="m4 17 5-4.5 3.5 3 3-2.5 4.5 4" />
    </>
  ),
  lentes: (
    <>
      <circle cx="6.5" cy="14" r="3.5" />
      <circle cx="17.5" cy="14" r="3.5" />
      <path d="M10 13.5c1.3-1 2.7-1 4 0M3 14l1-6.5h2M21 14l-1-6.5h-2" />
    </>
  ),
  impresora: (
    <>
      <path d="M7 8.5V3.5h10v5" />
      <rect x="3.5" y="8.5" width="17" height="8" rx="1.5" />
      <path d="M7 14h10v6.5H7Z" />
    </>
  ),
  usuario: (
    <>
      <circle cx="12" cy="8" r="3.8" />
      <path d="M4.5 20.5a7.5 7.5 0 0 1 15 0" />
    </>
  ),
  regalo: (
    <>
      <rect x="3.5" y="9" width="17" height="4" />
      <path d="M5 13v7.5h14V13M12 9v11.5M12 9C10 5 7 5.5 7 7.3S10 9 12 9Zm0 0c2-4 5-3.5 5-1.7S14 9 12 9Z" />
    </>
  ),
  grafica: <path d="M4 20V4M4 20h16M8 16v-4M12 16V8M16 16v-6" />,
  ajustes: (
    <>
      <path d="M4 7h10M18 7h2M4 17h4M12 17h8" />
      <circle cx="16" cy="7" r="2" />
      <circle cx="10" cy="17" r="2" />
    </>
  ),
  documento: (
    <>
      <path d="M6.5 3.5h8l3.5 3.5v13.5h-11.5Z" />
      <path d="M14.5 3.5V7H18M9 11.5h6M9 15h6" />
    </>
  ),
  bandeja: (
    <>
      <path d="M3.5 13.5 6 5.5h12l2.5 8v5h-17Z" />
      <path d="M3.5 13.5H9l1 2h4l1-2h5.5" />
    </>
  ),
  reloj: (
    <>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3 2" />
    </>
  ),
  mensaje: <path d="M4 5.5h16v11H9l-5 4Z" />,
  salir: (
    <>
      <path d="M9.5 4.5H5.5v15h4" />
      <path d="M14 8l4 4-4 4M18 12H9" />
    </>
  ),
  sol: (
    <>
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2.5v2M12 19.5v2M2.5 12h2M19.5 12h2M5.3 5.3l1.4 1.4M17.3 17.3l1.4 1.4M5.3 18.7l1.4-1.4M17.3 6.7l1.4-1.4" />
    </>
  ),
  luna: <path d="M19.5 14.5A8 8 0 0 1 9.5 4.5a8 8 0 1 0 10 10Z" />,
  pantalla: (
    <>
      <rect x="3" y="4.5" width="18" height="12" rx="1.5" />
      <path d="M8.5 20h7M12 16.5V20" />
    </>
  ),
}

export default function Icono({ nombre, tamano = 18 }) {
  return (
    <svg
      width={tamano}
      height={tamano}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {TRAZOS[nombre]}
    </svg>
  )
}
