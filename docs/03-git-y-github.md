# Git y GitHub: trabajar en equipo

## La idea en 30 segundos

- **Git** es un "guardar partida" para código: cada **commit** es una foto del proyecto
  a la que puedes regresar.
- **GitHub** es la nube donde vive la copia compartida. Tú **subes** (push) tus fotos y
  **bajas** (pull) las de tu compañero.
- Una **rama** (branch) es una línea de trabajo paralela. Trabajas en tu rama sin romper
  la principal (`main`), y cuando está lista la **unes** con un **Pull Request**.

```
main     ●────────●───────────────●──────►   (siempre funciona)
          \                      /
mi-rama    ●──●──●  (Pull Request) 
```

## Configuración (una sola vez por computadora)
```bash
git config --global user.name "Tu Nombre"
git config --global user.email "tu-correo@ejemplo.com"
git config --global init.defaultBranch main
```

## Subir el proyecto a GitHub por primera vez (solo lo hace UNA persona)
1. En github.com → **New repository** → nombre `sistema-rodriguez-ammi` → **Private** → sin README.
2. En la carpeta del proyecto:
   ```bash
   git add .
   git commit -m "Estructura inicial del sistema"
   git remote add origin https://github.com/USUARIO/sistema-rodriguez-ammi.git
   git push -u origin main
   ```
3. En GitHub → **Settings → Collaborators** → invitar al compañero.

## El compañero descarga el proyecto (una sola vez)
```bash
git clone https://github.com/USUARIO/sistema-rodriguez-ammi.git
```

## 🔁 El flujo de TODOS LOS DÍAS

```bash
# 1. Ponerte al día antes de empezar
git switch main
git pull

# 2. Crear una rama para lo que vas a hacer (nombre corto y claro)
git switch -c clientes-optica

# 3. ...programar...

# 4. Ver qué cambió
git status
git diff

# 5. Guardar la "foto"
git add .
git commit -m "Agrega alta y búsqueda de clientes de la óptica"

# 6. Subir tu rama
git push -u origin clientes-optica      # la primera vez
git push                                # las siguientes
```
7. En GitHub aparece el botón **Compare & pull request** → descríbelo → el compañero lo
   revisa → **Merge**.
8. Regresas a `main` y bajas lo nuevo:
   ```bash
   git switch main
   git pull
   ```

## Comandos más usados

| Comando | Qué hace |
|---|---|
| `git status` | ¿Qué archivos cambié? (úsalo TODO el tiempo) |
| `git diff` | ¿Qué líneas cambié exactamente? |
| `git add .` | Prepara todos los cambios para el commit |
| `git commit -m "mensaje"` | Guarda la foto con una descripción |
| `git pull` | Baja los cambios de GitHub |
| `git push` | Sube tus commits a GitHub |
| `git switch -c nombre` | Crea una rama nueva y se cambia a ella |
| `git switch main` | Regresa a la rama principal |
| `git branch` | Lista las ramas (la actual tiene `*`) |
| `git log --oneline -10` | Últimos 10 commits |
| `git restore archivo` | Descarta los cambios NO guardados de un archivo ⚠️ |
| `git stash` / `git stash pop` | Guarda cambios "en un cajón" un momento y luego los recupera |
| `git merge main` | Trae a tu rama lo nuevo de main |

## Reglas del equipo
1. **Nunca** trabajar directo en `main`; siempre en una rama.
2. `git pull` en `main` **antes** de crear una rama nueva.
3. Commits pequeños y con mensaje que diga **qué** y **para qué** ("Agrega stock mínimo a productos"),
   no "cambios" o "ya quedó".
4. **Nunca** subir contraseñas. Por eso usamos `DB_PASSWORD` y el `.gitignore`.
5. Repartirse el trabajo por carpetas (uno backend de clientes, otro su pantalla) reduce choques.

## ¿Y si sale un CONFLICTO?
Pasa cuando los dos cambiaron las **mismas líneas**. Git marca el archivo así:
```
<<<<<<< HEAD
lo que tú escribiste
=======
lo que escribió tu compañero
>>>>>>> main
```
1. Abre el archivo, deja la versión correcta (o una mezcla) y **borra las marcas** `<<<<<<<`, `=======`, `>>>>>>>`.
2. `git add archivo` y `git commit`.
3. Si te pierdes: `git merge --abort` regresa todo a como estaba antes del merge.

VS Code e IntelliJ tienen botones para resolver conflictos ("Accept current / incoming / both").
