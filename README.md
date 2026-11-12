# Sistema Rodríguez · AMMI

Sistema para dos negocios vecinos de la misma familia:

| Negocio | Qué resuelve | Estado |
|---|---|---|
| **Papelería AMMI** | Vender (con íconos), ticket, inventario con aviso de "por resurtir", **buzón de impresión** con QR | ✅ Funcionando |
| **Óptica RODRÍGUEZ** | Clientes, graduaciones, **armazones en cuadrícula con foto y medidas**, órdenes de lentes con anticipos, promoción 2x1, cupones de cumpleaños por WhatsApp | ✅ Funcionando |
| **Administración** (dueño) | Reportes de ventas por día / semana / mes, precios de tratamientos, descuentos especiales | ✅ Con contraseña aparte |

### ¿Quién entra a qué?

| Quién | Cómo entra | Qué puede hacer |
|---|---|---|
| Mamá, papá, hija | **PIN** una sola vez por celular (queda recordado 1 año) | Todo el día a día de ambos negocios |
| El dueño | **Contraseña de administrador** (dura unas horas) | Además: reportes de ventas y ajustes |
| Clientes de la papelería | Escanean el **QR del mostrador** | Solo mandar archivos a imprimir |

El PIN y la contraseña se definen con las variables de entorno `APP_PIN` y `APP_ADMIN_PASSWORD`.
En modo prueba son `1234` y `admin123` (**cámbialos antes de usarlo en el negocio**).

Funciona en la **red local** del negocio: una computadora hace de servidor y todos
(mamá, papá e hija) entran desde su celular con el navegador. No hace falta internet ni pagar hosting.

```
   📱 Celular mamá ─┐
   📱 Celular papá ─┼── Wi-Fi del local ──► 💻 PC principal  (Spring Boot + PostgreSQL)
   📱 Celular hija ─┘                         http://192.168.1.50:8080
```

## Tecnologías

| Capa | Tecnología | En una frase |
|---|---|---|
| Frontend (lo que se ve) | React + Vite | Pantallas en JavaScript que corren en el navegador |
| Backend (la lógica) | Java 21 + Spring Boot 3 | Recibe peticiones, aplica las reglas del negocio |
| Base de datos | PostgreSQL | Donde se guarda todo de forma permanente |
| Puente Java ↔ BD | Spring Data JPA (Hibernate) | Convierte clases de Java en tablas, sin escribir SQL |

## Arrancar rápido (sin instalar PostgreSQL)

Necesitas Java 21+ y Node 20+ (ver [docs/02-instalacion.md](docs/02-instalacion.md)).

Terminal 1 — backend con base de datos de prueba en memoria:
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

Terminal 2 — frontend:
```bash
cd frontend
npm install
npm run dev
```

Abre http://localhost:5173. Desde el celular (misma Wi-Fi), usa la dirección "Network" que muestra Vite.

## Mapa de carpetas

```
sistema-rodriguez-ammi/
├── backend/                         ← Spring Boot (Java)
│   ├── pom.xml                      ← lista de librerías
│   └── src/main/java/mx/rodriguezammi/
│       ├── SistemaApplication.java  ← el "interruptor general"
│       ├── comun/                   ← acceso (PIN/admin), errores, archivos, configuración
│       ├── papeleria/
│       │   ├── producto/            ← ⭐ EMPIEZA A LEER AQUÍ: el ejemplo completo
│       │   ├── venta/               ← ventas y ticket
│       │   └── impresion/           ← buzón de impresión (archivos de clientes)
│       ├── optica/                  ← cliente, receta, armazón, orden, tratamiento, promoción
│       │   └── EJERCICIOS.md        ← ejercicios para aprender
│       └── admin/                   ← reportes privados del dueño
├── frontend/                        ← React (JavaScript)
│   └── src/
│       ├── services/api.js          ← todas las llamadas al backend
│       ├── vistas/                  ← una carpeta por negocio, un archivo por pantalla
│       ├── App.jsx                  ← tabla de rutas (#/optica/clientes...) y las 3 "puertas"
│       ├── hooks/                   ← useRuta (navegación ← →) y useTema (claro/oscuro)
│       ├── components/              ← piezas reutilizables (Encabezado, Ticket, CuadriculaArmazones…)
│       └── styles/                  ← colores y diseño
└── docs/                            ← 📚 explicaciones para el equipo
```

## Documentación

0. ⭐ [Empieza aquí: las herramientas explicadas desde cero](docs/00-empieza-aqui-herramientas.md)
1. [El proyecto explicado (para quien empieza)](docs/01-el-proyecto-explicado.md)
2. [Instalación paso a paso](docs/02-instalacion.md)
3. [Git y GitHub: trabajar en equipo](docs/03-git-y-github.md)
4. [Cómo trabajar con IA sin perder el control](docs/04-trabajar-con-ia.md)
5. [Glosario y preguntas de entrevista](docs/05-glosario-y-entrevista.md)
6. [Ideas, pendientes y decisiones](docs/06-ideas-y-pendientes.md)
7. [Cómo se usa (guía para la familia) y decisiones de diseño](docs/07-como-se-usa.md)
