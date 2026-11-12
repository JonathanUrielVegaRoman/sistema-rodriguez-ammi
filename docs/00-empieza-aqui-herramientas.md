# Empieza aquí: las herramientas, explicadas desde cero

> Para quien nunca ha programado. Aquí no hay que memorizar nada: léelo como un mapa.
> Al final sabrás **qué es cada cosa, por qué la usamos, cómo instalarla y qué más se
> puede hacer con ella**. Todo lo de esta lista es **gratis**.

---

## 1. Primero, el panorama: ¿qué estamos construyendo?

Un sistema que se usa desde el **navegador** (Chrome) del celular o de la computadora,
igual que una página web, pero que vive **dentro del negocio**, no en internet.

Tiene 3 partes. Piensa en una **tienda con mostrador**:

```
  ┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
  │    FRONTEND      │      │     BACKEND      │      │  BASE DE DATOS   │
  │  (el mostrador)  │ ───► │  (la trastienda) │ ───► │    (la bodega)   │
  │                  │ ◄─── │                  │ ◄─── │                  │
  │ Lo que se ve y   │      │ Reglas: ¿hay     │      │ Donde se guarda  │
  │ se toca.         │      │ existencia? ¿cuál│      │ todo para        │
  │                  │      │ es el total?     │      │ siempre.         │
  │ React + Vite     │      │ Java + Spring    │      │ PostgreSQL       │
  │ (JavaScript)     │      │ Boot             │      │                  │
  └──────────────────┘      └──────────────────┘      └──────────────────┘
        en el celular          en la PC del negocio      en la PC del negocio
```

Y alrededor, herramientas para **escribir** el código (editor), **guardarlo y compartirlo**
(Git y GitHub) y **probarlo** (Thunder Client, el navegador).

---

## 2. Lista de instalación (en este orden)

| # | Programa | ¿Obligatorio? | Tiempo aprox. |
|---|---|---|---|
| 1 | Git (incluye Git Bash) | ✅ | 5 min |
| 2 | Cuenta de GitHub | ✅ | 5 min |
| 3 | JDK 21 (Java) | ✅ | 5 min |
| 4 | Node.js LTS | ✅ | 5 min |
| 5 | Editor: IntelliJ IDEA Community **o** VS Code | ✅ (uno) | 10 min |
| 6 | PostgreSQL + pgAdmin | Más adelante (al principio usamos el modo de prueba) | 15 min |
| 7 | Thunder Client (extensión de VS Code) o Postman | Recomendado | 2 min |
| 8 | Google Chrome | Probablemente ya lo tienes | — |

**Al terminar**, abre la terminal (ver sección 3) y escribe uno por uno:

```bash
git --version
java -version
node -v
npm -v
```

Si cada uno responde con un número de versión, todo quedó bien instalado. Si alguno dice
*"no se reconoce como un comando"*, ve a la sección 6 (Problemas comunes).

---

## 3. Antes de todo: la TERMINAL

**Qué es:** una ventana donde le das órdenes a la computadora **escribiendo** en lugar de
dar clics. Se ve "de hacker", pero es solo otra forma de usar la computadora.

**Por qué importa:** casi todas las herramientas de programación se manejan desde ahí:
arrancar el sistema, subir cambios a GitHub, instalar librerías.

**Cuál usar en Windows:**
- **Git Bash**: se instala junto con Git. Es la que usan los ejemplos de esta documentación.
- **PowerShell**: viene con Windows. Funciona casi igual, con pequeñas diferencias
  (por ejemplo, `.\mvnw.cmd` en lugar de `./mvnw`).
- Los editores (IntelliJ, VS Code) traen una terminal integrada abajo.

**Los 6 comandos que vas a usar siempre:**

| Comando | Qué hace | Ejemplo |
|---|---|---|
| `pwd` | ¿En qué carpeta estoy? | `pwd` |
| `ls` | ¿Qué hay en esta carpeta? | `ls` |
| `cd carpeta` | Entrar a una carpeta | `cd backend` |
| `cd ..` | Regresar a la carpeta de arriba | `cd ..` |
| `clear` | Limpiar la pantalla | `clear` |
| `Ctrl + C` | **Detener** lo que está corriendo (por ejemplo, el servidor) | — |

Truco: escribe el principio del nombre de una carpeta y presiona **Tab**: se completa solo.
Flecha ↑ repite el comando anterior.

---

## 4. Cada herramienta explicada

Formato de cada una: **Qué es · Analogía · Para qué la usamos aquí · Por qué es importante ·
Cómo instalar · Qué más se puede construir · Quién la usa**.

---

### 4.1 Java (el JDK)

- **Qué es:** un lenguaje de programación (una forma de escribirle instrucciones a la
  computadora). El **JDK** (*Java Development Kit*) es el paquete que trae todo lo necesario
  para **escribir y ejecutar** programas en Java.
- **Analogía:** Java es el **idioma**; el JDK es el **diccionario + el traductor** que convierte
  lo que escribimos en algo que la computadora entiende.
- **Para qué aquí:** todo el **backend** está escrito en Java.
- **Por qué es importante:**
  - Lleva 30 años en uso y es de los lenguajes más pedidos en empleos, sobre todo en bancos,
    aseguradoras, gobierno y empresas grandes.
  - Es **estricto** (te obliga a decir de qué tipo es cada dato: número, texto, fecha...).
    Al principio parece más trabajo, pero detecta muchos errores **antes** de ejecutar.
  - "Escríbelo una vez, ejecútalo donde sea": el mismo programa corre en Windows, Mac o Linux.
- **Instalar:** https://adoptium.net → **Temurin 21 (LTS)** → instalador `.msi` para Windows.
  Durante la instalación marca las opciones **"Set JAVA_HOME"** y **"Add to PATH"**.
  *LTS = Long Term Support: versión estable con soporte por años.*
- **Qué más se puede construir:** aplicaciones Android (Java y su "primo" Kotlin), sistemas
  bancarios, tiendas en línea, juegos (Minecraft Java Edition está hecho en Java), programas
  de escritorio y procesamiento de grandes volúmenes de datos.
- **Quién lo usa:** bancos en todo el mundo, Amazon, Netflix, LinkedIn, Google (en muchos
  servicios internos y en Android), entre muchas otras.

---

### 4.2 Maven (y el `mvnw` del proyecto)

- **Qué es:** una herramienta que **descarga las librerías** que necesita el proyecto,
  **compila** el código y lo **empaqueta** en un archivo que se puede ejecutar (`.jar`).
- **Analogía:** el **jefe de obra**: lee la lista de materiales (`pom.xml`), los consigue y
  arma la construcción.
- **Librería:** código que otros ya escribieron y que reutilizamos (por ejemplo, el que se
  conecta a PostgreSQL). Sería absurdo escribirlo desde cero.
- **Para qué aquí:** arrancar el backend (`./mvnw spring-boot:run`), correr las pruebas
  (`./mvnw test`) y empaquetarlo para el negocio.
- **Instalar:** **no hace falta**. El proyecto trae `mvnw` ("Maven Wrapper"), que descarga
  Maven solo la primera vez.
- **Quién lo usa:** la gran mayoría de los proyectos Java. Su alternativa es **Gradle**
  (la usa Android).

---

### 4.3 Spring Boot

- **Qué es:** un **framework** para Java. Un framework es un "esqueleto" ya hecho que resuelve
  lo repetitivo (recibir peticiones de internet, conectarse a la base de datos, manejar
  errores) para que tú solo escribas **lo particular de tu negocio**.
- **Analogía:** construir una casa con **obra negra ya hecha** (cimientos, tuberías,
  electricidad) en lugar de empezar haciendo tus propios ladrillos.
- **Para qué aquí:** el backend completo: Controllers, Services, Repositories (ver `01-el-proyecto-explicado.md`).
- **Por qué es importante:** es **el framework más usado del mundo Java** para hacer
  servidores y APIs. Si buscas trabajo como desarrollador Java, casi siempre piden Spring.
- **Instalar:** nada. Viene como librería dentro del `pom.xml` y Maven la descarga.
- **Qué más se puede construir:** APIs para apps móviles, tiendas en línea, sistemas de
  nómina, plataformas de pagos y **microservicios** (sistemas grandes divididos en muchos
  programas pequeños que se hablan entre sí).
- **Quién lo usa:** Netflix es el caso más conocido (publicó varias herramientas para Spring),
  además de bancos, fintechs y empresas de comercio electrónico en todo el mundo.
- **Pieza relacionada — JPA / Hibernate:** la parte de Spring que convierte **clases de Java
  en tablas** de la base de datos. Gracias a ella casi no escribimos SQL a mano.

---

### 4.4 PostgreSQL (y pgAdmin)

- **Qué es:** un **gestor de base de datos relacional**. Guarda la información en **tablas**
  (como hojas de Excel) que se **relacionan** entre sí: un cliente → sus recetas;
  una venta → sus productos.
- **Analogía:** un **archivero** muy ordenado con cajones (tablas) y fichas (renglones),
  que nunca pierde un papel y responde preguntas como *"¿cuánto se vendió hoy?"* en milisegundos.
- **¿Por qué no un Excel?** Porque varias personas pueden escribir **al mismo tiempo** sin
  pisarse, valida los datos, no se corrompe si se va la luz a la mitad de una venta
  (transacciones) y aguanta millones de registros.
- **SQL:** el idioma para hacerle preguntas a la base de datos, por ejemplo
  `SELECT * FROM productos WHERE existencia < 5;` ("dame los productos con menos de 5 piezas").
  Vale mucho la pena aprenderlo; se usa en casi cualquier empresa, incluso fuera de programación.
- **Para qué aquí:** guardar productos, ventas, clientes, recetas y armazones.
- **pgAdmin:** programa con ventanas para **ver** las tablas y los datos sin usar la terminal.
- **Instalar:** https://www.postgresql.org/download/windows/ → instalador de EDB → deja
  marcados **pgAdmin** y **Command Line Tools**. **Anota la contraseña** que pongas al
  usuario `postgres`; la vas a necesitar.
  *Al principio no es obligatorio:* el proyecto tiene un **modo de prueba (perfil h2)** que usa
  una base de datos temporal en memoria.
- **Qué más se puede construir:** cualquier sistema que guarde información: inventarios,
  reservaciones, expedientes, redes sociales, sistemas de análisis de datos y mapas (con PostGIS).
- **Quién lo usa:** Instagram, Reddit y Spotify, entre otras. Muchas plataformas modernas
  (por ejemplo Supabase) están construidas sobre PostgreSQL.
- **Primos:** MySQL (muy usado, sobre todo en hosting web), SQL Server (Microsoft), Oracle
  (empresas grandes). Si sabes uno, aprender los otros es fácil.

---

### 4.5 JavaScript, HTML y CSS

Los tres "idiomas" que entiende **cualquier navegador**:

| Lenguaje | Qué hace | Analogía (una casa) |
|---|---|---|
| **HTML** | La **estructura**: títulos, botones, campos | Paredes y cuartos |
| **CSS** | El **diseño**: colores, tamaños, acomodo | Pintura y decoración |
| **JavaScript** | El **comportamiento**: qué pasa al dar clic, pedir datos al servidor | Electricidad: los apagadores hacen algo |

- **Ojo:** *Java* y *JavaScript* **no tienen nada que ver** (solo se parecen en el nombre,
  por una decisión de mercadotecnia de los años 90).
- **Por qué es importante:** JavaScript es el **único** lenguaje que corre nativamente en los
  navegadores, así que **toda** página web lo usa. Es de los lenguajes más usados del mundo.
- **Para qué aquí:** todo el **frontend** (`frontend/src`).

---

### 4.6 Node.js y npm

- **Qué es Node.js:** permite ejecutar JavaScript **fuera** del navegador, en la computadora.
  Lo necesitamos porque las herramientas del frontend (como Vite) están hechas en JavaScript.
- **Qué es npm:** la "tienda" de librerías de JavaScript (*Node Package Manager*). Es el
  equivalente a Maven, pero para el frontend. Lee la lista `package.json` y descarga todo en
  la carpeta `node_modules`.
- **Comandos que usaremos:**
  - `npm install` → descarga las librerías (la primera vez, o cuando cambie `package.json`).
  - `npm run dev` → arranca el frontend en modo desarrollo.
  - `npm run build` → prepara la versión final y la mete dentro del backend.
- **Instalar:** https://nodejs.org → botón **LTS** → siguiente, siguiente.
- **Dato:** `node_modules` pesa mucho y **nunca** se sube a GitHub (está en `.gitignore`);
  cada quien lo descarga con `npm install`.
- **Qué más se puede construir con Node:** servidores web completos (como alternativa a
  Spring), bots de Discord o WhatsApp, herramientas de línea de comandos y apps de escritorio
  (VS Code está hecho con Electron, que usa Node).
- **Quién lo usa:** Netflix, PayPal y LinkedIn, entre muchas otras.

---

### 4.7 React

- **Qué es:** una **librería de JavaScript** para construir interfaces dividiéndolas en
  **componentes**: piezas reutilizables como `Ticket`, `Carrito` o `TarjetaProducto`.
- **Analogía:** **LEGO**. En lugar de esculpir toda la pantalla de un solo bloque, armas
  piezas pequeñas y las combinas. Si cambias una pieza, cambia en todos lados donde se usa.
- **La idea clave:** tú describes **cómo debe verse la pantalla según los datos**, y cuando
  los datos cambian (agregas un producto al carrito), React **redibuja solo** lo necesario.
- **Para qué aquí:** todas las pantallas: Vender, Inventario, Impresiones, Clientes, Armazones, Órdenes y Reportes.
- **Por qué es importante:** es la librería de frontend **más usada y pedida** en empleos.
- **Instalar:** nada; viene en `package.json` y se descarga con `npm install`.
- **Qué más se puede construir:** cualquier aplicación web (tiendas, paneles de control,
  redes sociales) y, con **React Native**, apps para Android e iPhone con la misma forma de
  trabajar.
- **Quién lo usa:** Meta la creó y la usa en Facebook, Instagram y WhatsApp Web; también
  Netflix, Airbnb y miles de empresas más.

---

### 4.8 Vite

- **Qué es:** la herramienta que **arranca el frontend mientras programas** y lo
  **empaqueta** al final.
- **Analogía:** la **cocina de prueba**: guardas un archivo y en menos de un segundo ves el
  cambio en el navegador, sin recargar nada.
- **Para qué aquí:** `npm run dev` (desarrollo) y `npm run build` (versión final). También
  reenvía las peticiones `/api` al backend (ver `vite.config.js`).
- **Por qué es importante:** es la forma moderna y rápida de crear proyectos de React (y de
  Vue, Svelte, etc.); reemplazó a herramientas más lentas como *Create React App*.

---

### 4.9 Git

- **Qué es:** un **sistema de control de versiones**: guarda "fotos" (*commits*) de tu
  proyecto a lo largo del tiempo.
- **Analogía:** el **"guardar partida" de un videojuego**, más una **máquina del tiempo**.
  Si algo se rompe, regresas a la foto anterior. Y cada quien puede jugar su propia
  "partida paralela" (*rama*) sin afectar a los demás.
- **Por qué es importante:**
  - **Nunca pierdes trabajo.** Se acabó el "proyecto_final_ahora_si_v3.zip".
  - Permite que **varias personas** trabajen en el mismo proyecto sin pisarse.
  - Con IA es indispensable: si la IA rompe algo, regresas a la foto anterior en un segundo.
  - Lo usa **prácticamente toda** empresa de software; no saber Git es como no saber usar el correo.
- **Instalar:** https://git-scm.com → Download for Windows → siguiente en todo
  (deja las opciones por defecto). Después, una sola vez:
  ```bash
  git config --global user.name "Tu Nombre"
  git config --global user.email "tu-correo@ejemplo.com"
  ```
- **Dato curioso:** lo creó Linus Torvalds (el creador de Linux) en 2005 para manejar el
  código de Linux.
- **Guía de uso:** `03-git-y-github.md`.

---

### 4.10 GitHub

- **Qué es:** una página web donde se **guardan en la nube** los proyectos de Git y donde los
  equipos colaboran.
- **Git vs GitHub:** Git es la **cámara** que toma las fotos (en tu computadora); GitHub es el
  **álbum compartido en internet** donde todos suben y bajan esas fotos.
- **Para qué aquí:** compartir el código entre los dos, revisar los cambios del otro
  (*Pull Requests*) y tener un respaldo del código.
- **Por qué es importante:**
  - Es tu **portafolio**: los reclutadores revisan GitHub para ver qué has construido.
  - Los Pull Requests son exactamente como se trabaja en las empresas.
- **Crear cuenta:** https://github.com → Sign up. Usa un nombre de usuario profesional
  (lo verán reclutadores).
- **Quién lo usa:** es propiedad de Microsoft y lo usan más de 100 millones de desarrolladores.
  Alternativas: GitLab y Bitbucket.
- **Para iniciar sesión desde la terminal:** la primera vez que hagas `git push` o `git clone`
  de un repositorio privado, se abre una ventana del navegador para autorizar. Acepta y listo.

---

### 4.11 El editor: IntelliJ IDEA o VS Code

Un **editor de código** (o **IDE**, *Entorno de Desarrollo Integrado*) es como Word, pero para
programar: colorea el código, avisa errores mientras escribes, autocompleta y permite
ejecutar el proyecto con un botón.

| | IntelliJ IDEA Community | VS Code |
|---|---|---|
| Ideal para | **Java / Spring** | **JavaScript / React** (y de todo un poco) |
| Ventaja | Entiende Java "de fábrica": autocompletado y detección de errores excelentes | Ligero, rápido, miles de extensiones |
| Desventaja | Más pesado | Para Java necesita extensiones |
| Descargar | https://www.jetbrains.com/idea/download (baja hasta **Community Edition**, la gratuita) | https://code.visualstudio.com |

**Recomendación:** si la computadora tiene 8 GB de RAM o menos, usen **solo VS Code** con
estas extensiones (se buscan en el ícono de cuadritos de la barra izquierda):
- **Extension Pack for Java** (Microsoft)
- **Spring Boot Extension Pack** (VMware / Broadcom)
- **ES7+ React snippets**
- **Thunder Client** (para probar la API; ver 4.12)
- **Spanish Language Pack** (si prefieren los menús en español)

Si la computadora es más potente: IntelliJ para el backend y VS Code para el frontend.

---

### 4.12 Thunder Client o Postman (probar la API)

- **Qué es:** un programa para **hablar directamente con el backend** sin necesidad de
  tener pantallas hechas.
- **Analogía:** **probar la cocina directamente** sin pasar por el mesero: pides un platillo
  y ves exactamente qué sale.
- **Ejemplo:** eliges `GET`, escribes `http://localhost:8080/api/papeleria/productos`, das
  **Send**, y ves la lista de productos en formato JSON.
- **Por qué es importante:** cuando algo falla, te dice si el problema está en el backend o
  en el frontend. Es de lo primero que se usa en cualquier trabajo de backend.
- **Instalar:** Thunder Client es una extensión de VS Code. Postman es un programa aparte
  (https://www.postman.com).

---

### 4.13 Las herramientas del navegador (DevTools)

- En Chrome presiona **F12**. Se abre un panel con "rayos X" de la página.
- Pestañas útiles:
  - **Console:** aquí aparecen los errores de JavaScript en rojo.
  - **Network:** muestra cada petición al backend, qué se mandó y qué respondió.
  - **Botón del celular (Toggle device toolbar):** muestra cómo se ve la página en un teléfono.
- No se instala nada: ya viene en Chrome.

---

### 4.14 La IA (Claude, ChatGPT, etc.)

- **Qué es para nosotros:** un **asistente** que escribe borradores de código, explica errores
  y enseña. **No** sustituye entender lo que hace el sistema.
- **Regla del equipo:** nunca aceptes código que no puedas explicar con tus palabras. Pídele
  que te lo explique (ver `04-trabajar-con-ia.md`).
- **Truco para aprender:** cuando la terminal muestre un error, cópialo **completo** y
  pregunta *"¿qué significa este error y cómo lo soluciono? Explícamelo como principiante"*.

---

## 5. ¿Cómo encaja todo? El viaje completo de un cambio

Ejemplo: *"quiero que el ticket diga 'Vuelva pronto'"*.

1. **GitHub → tu PC:** `git switch main` y `git pull` para tener lo último.
2. **Git:** `git switch -c ticket-vuelva-pronto` (rama nueva).
3. **Editor:** abres `frontend/src/components/Ticket.jsx` y cambias el texto.
4. **Vite:** con `npm run dev` corriendo, el navegador muestra el cambio al instante.
5. **Spring Boot + PostgreSQL:** el backend sigue corriendo y entregando los datos del ticket.
6. **Git:** `git add .` → `git commit -m "Cambia el pie del ticket"` → `git push`.
7. **GitHub:** abres un **Pull Request**; tu compañero lo revisa y lo une a `main`.
8. **Al negocio:** `npm run build` + `./mvnw package` → copias el `.jar` a la PC del negocio.

---

## 6. Problemas comunes al instalar

| Síntoma | Causa probable | Solución |
|---|---|---|
| `"java" no se reconoce como un comando` | Java no quedó en el **PATH** (la lista de lugares donde Windows busca programas) | Reinstala Temurin marcando **Add to PATH** y **Set JAVA_HOME**; **cierra y vuelve a abrir** la terminal |
| Instalé algo pero la terminal no lo encuentra | La terminal estaba abierta antes de instalar | Ciérrala y ábrela de nuevo (a veces hay que reiniciar la PC) |
| `Port 8080 was already in use` | Ya hay un backend corriendo (quizá en otra ventana) | Ciérralo con **Ctrl + C** en esa ventana, o reinicia la PC |
| `password authentication failed for user "postgres"` | La contraseña de PostgreSQL no coincide | Usa la que pusiste al instalar, en `DB_PASSWORD` (ver `02-instalacion.md`) |
| `npm ERR!` al hacer `npm install` | Sin internet o versión de Node muy vieja | Verifica `node -v` (debe ser 20 o más) y la conexión |
| La página no abre desde el celular | El firewall de Windows bloquea la conexión, o no están en la misma Wi-Fi | Permite Java/Node en **Redes privadas** y verifica la red |
| `./mvnw: Permission denied` o no lo reconoce | Estás en PowerShell | Usa `.\mvnw.cmd` |

---

## 7. ¿Y después? Qué más pueden aprender y construir

Con exactamente estas herramientas se puede construir:
- Sistemas para **cualquier negocio local**: consultorios, talleres, estéticas, escuelas
  (citas, inventario, clientes, cobranza). Un mismo sistema, adaptado, puede venderse a varios negocios.
- Una **tienda en línea** (al publicar el backend en internet).
- **Apps móviles** con React Native que usen el mismo backend de Spring.
- **Paneles de control** con gráficas de ventas.
- **Integraciones:** WhatsApp Business, pagos con tarjeta (Mercado Pago, Stripe), facturación electrónica (CFDI).

Siguientes temas, en orden razonable:
1. **SQL básico** (consultas, filtros, sumas). Sirve en cualquier trabajo.
2. **Spring Security** (inicio de sesión y permisos).
3. **Docker** (empaquetar el sistema para que corra igual en cualquier computadora).
4. **Desplegar en la nube** (Railway, Render, AWS, Azure) cuando haga falta acceso desde fuera del local.
5. **TypeScript** (JavaScript con tipos, como Java; muy pedido en empleos de frontend).

### Caminos profesionales

| Rol | Qué hace | Herramientas de este proyecto |
|---|---|---|
| **Backend** | Lógica, APIs, bases de datos | Java, Spring Boot, PostgreSQL, SQL |
| **Frontend** | Pantallas e interacción | JavaScript, React, HTML/CSS |
| **Full stack** | Ambos | Todo lo anterior |
| **QA / Pruebas** | Asegura que todo funcione | Pruebas automáticas, Postman |
| **DevOps** | Publicar y mantener sistemas | Git, Docker, nube |

---

## 8. Dónde aprender gratis (fuentes oficiales y confiables)

| Tema | Recurso |
|---|---|
| Java desde cero | https://dev.java/learn/ (oficial) |
| Spring Boot | https://spring.io/guides (guías oficiales cortas, paso a paso) |
| JavaScript, HTML y CSS | https://developer.mozilla.org/es/ (MDN, en español) |
| React | https://es.react.dev/learn (oficial, en español) |
| SQL | https://www.postgresql.org/docs/current/tutorial.html y https://sqlbolt.com |
| Git | https://git-scm.com/book/es/v2 (libro oficial, en español) |
| Todo lo anterior con ejercicios | https://www.freecodecamp.org/espanol/ |

**Consejo final:** no intenten aprender todo antes de empezar. Aprendan **lo justo para el
siguiente paso** del proyecto. Construir algo real (como este sistema) enseña más que diez
cursos.
