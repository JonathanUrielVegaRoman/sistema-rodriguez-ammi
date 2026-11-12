import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"

/*
  Configuración de Vite (la herramienta que corre y empaqueta el frontend).

  1) server.proxy: en desarrollo, el frontend corre en el puerto 5173 y el backend en 8080.
     Todo lo que empiece con /api se reenvía al backend. Así el código siempre llama
     a "/api/..." sin preocuparse del puerto.

  2) server.host: true → permite abrir la página desde el CELULAR en la misma red
     (http://IP-DE-LA-PC:5173). Vite muestra la dirección "Network" al arrancar.

  3) build.outDir: al ejecutar "npm run build", el resultado se guarda DENTRO del backend
     (carpeta static). Spring Boot sirve esos archivos, y así todo el sistema es un solo
     programa en el puerto 8080.
*/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
  build: {
    outDir: "../backend/src/main/resources/static",
    emptyOutDir: true,
  },
})
