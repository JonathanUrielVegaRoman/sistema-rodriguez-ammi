# Instalación paso a paso (Windows)

## 1. Programas necesarios

| Programa | Para qué | Dónde |
|---|---|---|
| **JDK 21** (o más nuevo) | Ejecutar y compilar Java | https://adoptium.net (Temurin 21 LTS) |
| **Node.js LTS** | Ejecutar el frontend | https://nodejs.org |
| **Git** | Control de versiones | https://git-scm.com (incluye "Git Bash") |
| **PostgreSQL 16+** | Base de datos real | https://www.postgresql.org/download/windows/ — **anota la contraseña** que pongas al usuario `postgres` |
| **IntelliJ IDEA Community** o **VS Code** | Editor | IntelliJ es más cómodo para Java; VS Code con "Extension Pack for Java" también sirve |

> No hace falta instalar Maven: el proyecto trae `mvnw` (Maven Wrapper), que se descarga solo.

Comprueba en una terminal:
```bash
java -version
node -v
git --version
```

## 2. Obtener el proyecto
```bash
git clone https://github.com/USUARIO/sistema-rodriguez-ammi.git
cd sistema-rodriguez-ammi
```

## 3. Modo prueba (sin PostgreSQL)

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```
En **PowerShell** se escribe `.\mvnw.cmd` en lugar de `./mvnw`.

Viene con productos de ejemplo. Al apagarlo se borra todo; es solo para practicar.

## 4. Modo real (con PostgreSQL)

1. Crear la base de datos (una sola vez). Abre **pgAdmin** o la terminal **SQL Shell (psql)** y ejecuta:
   ```sql
   CREATE DATABASE rodriguez_ammi;
   ```
2. Decirle al backend tu contraseña **sin escribirla en el código**. En Git Bash:
   ```bash
   export DB_PASSWORD="tu_contraseña"
   ./mvnw spring-boot:run
   ```
   En PowerShell:
   ```powershell
   $env:DB_PASSWORD="tu_contraseña"; .\mvnw.cmd spring-boot:run
   ```
   Las tablas se crean solas la primera vez.
3. Definir el **PIN del personal** y la **contraseña del dueño** de la misma forma
   (si no, se usan los de ejemplo `1234` y `admin123`):
   ```bash
   export APP_PIN="un PIN de 4 a 6 números"
   export APP_ADMIN_PASSWORD="una contraseña larga"
   ```
   En la PC del negocio conviene guardarlas como variables de entorno de Windows
   (Configuración → Sistema → Acerca de → Configuración avanzada → Variables de entorno),
   para no escribirlas cada vez.

## 5. Frontend
```bash
cd frontend
npm install      # solo la primera vez (o cuando cambie package.json)
npm run dev
```
Abre http://localhost:5173.

## 6. Ponerlo en el negocio (una sola computadora)

1. Compilar el frontend **dentro** del backend:
   ```bash
   cd frontend && npm run build
   ```
2. Empaquetar el backend en un solo archivo:
   ```bash
   cd ../backend && ./mvnw clean package -DskipTests
   ```
   Resultado: `backend/target/sistema-0.1.0.jar`.
3. En la PC del negocio:
   ```bash
   java -jar sistema-0.1.0.jar
   ```
4. Desde los celulares: `http://IP-DE-LA-PC:8080`. Para saber la IP, en la PC ejecuta `ipconfig`
   y busca "Dirección IPv4" (algo como `192.168.1.50`).

Detalles importantes para la PC del negocio (ver también `06-ideas-y-pendientes.md`):
- **IP fija**: en el módem/router, "reservar" la IP de la PC; si cambia, los celulares no la encuentran.
- **Firewall de Windows**: la primera vez que se ejecuta pregunta si se permite la conexión; elegir **Redes privadas**.
- **Arranque automático** y **respaldos diarios**: pendiente de configurar.
