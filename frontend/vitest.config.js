import { defineConfig } from "vitest/config"

/*
  Configuración de las PRUEBAS del frontend (Vitest).
  Se corren con:  npm test

  Solo se prueban funciones "puras" (las de utils/ y hooks/ que no dibujan nada): reciben
  datos y devuelven datos, así que no hace falta abrir un navegador.
*/
export default defineConfig({
  test: {
    environment: "node",
    include: ["src/**/*.test.js"],
  },
})
