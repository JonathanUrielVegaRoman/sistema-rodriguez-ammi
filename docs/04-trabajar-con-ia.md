# Cómo trabajar con IA sin perder el control

La idea del equipo: **ser arquitectos**. La IA pone los ladrillos; nosotros hacemos el plano
y revisamos que la casa no se caiga. Para eso hay que poder **leer** lo que genera.

## 1. Antes de pedir: el plano
Escribe en 5 líneas qué quieres, como si fuera una orden de trabajo:
- **Qué**: "Alta de clientes de la óptica".
- **Datos**: nombre (obligatorio), celular (10 dígitos), fecha de nacimiento, acepta promociones.
- **Reglas**: no repetir celular; nombre sin espacios al inicio/fin.
- **Dónde**: `backend/.../optica/cliente/`.
- **Listo cuando**: `POST /api/optica/clientes` crea uno y `GET` lo lista.

## 2. Plantillas de instrucciones (prompts)

**Backend siguiendo el patrón:**
> En este proyecto Spring Boot, la carpeta `papeleria/producto/` es el patrón a seguir
> (Entity, Repository, Dto con records anidados, Service con @Transactional, Controller REST,
> errores con RecursoNoEncontradoException y ReglaDeNegocioException). Crea ClienteDto,
> ClienteService y ClienteController para la entidad `optica/cliente/Cliente.java` con estas
> reglas: [...]. Comenta el código en español para un principiante. No agregues librerías nuevas.

**Frontend siguiendo el patrón:**
> Siguiendo `frontend/src/vistas/papeleria/Productos.jsx` y las clases CSS de `styles/base.css`
> y `styles/app.css`, crea `vistas/optica/Clientes.jsx` que use `clientesApi` en `services/api.js`.
> Sin librerías nuevas. Debe verse bien en celular.

**Para aprender (no solo para obtener código):**
> Explícame línea por línea qué hace este método como si nunca hubiera programado, y dime
> qué pasaría si quito `@Transactional`.

## 3. Después de recibir: la revisión (checklist)
- [ ] ¿Respetó la estructura (carpeta correcta, mismos nombres que el patrón)?
- [ ] ¿Las reglas del negocio están en el **Service** y no en el Controller ni en React?
- [ ] ¿Devuelve **DTO** y no la Entity?
- [ ] ¿Agregó librerías que no pedí? (revisa `pom.xml` y `package.json`)
- [ ] ¿Hay contraseñas, IPs o datos reales escritos en el código?
- [ ] ¿Compila y funciona? (`./mvnw test`, `npm run build`, probarlo en el celular)
- [ ] ¿Puedo explicar con mis palabras qué hace? Si no → pedirle que lo explique.

## 4. Señales de alerta
- La IA "inventa" métodos o clases que no existen en el proyecto → pídele que lea el archivo real.
- Cambió muchos archivos que no tenían que ver → descarta (`git restore`) y pide algo más acotado.
- Para resolver un error, desactivó una validación o una prueba → **no** lo aceptes.

## 5. Por qué Git y la IA se llevan bien
Antes de pedir un cambio grande, haz commit. Si la IA rompe algo, regresas con
`git restore .` (cambios sin commit) y no pierdes nada.
