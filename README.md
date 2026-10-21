# Mueblería: página web

Vue 3 + Bootstrap 5 en el front, Spring Boot en el back y MySQL.

```
muebleria/
├── muebleria.sql              tablas de la base
├── frontend/
│   ├── index.html
│   ├── vite.config.js
│   ├── public/img/            portada.jpg, portada-chica.jpg, local.jpg (provisorias)
│   └── src/
│       ├── views/             Inicio, Catalogo, SobreNosotros, Login, Panel
│       ├── components/        Navbar, Footer, ProductoCard
│       ├── router.js
│       ├── api.js             llamadas al backend
│       ├── tienda.js          nombre, dirección, teléfono, WhatsApp, categorías
│       ├── estilos.css
│       ├── App.vue
│       └── main.js
└── backend/
    ├── pom.xml
    ├── uploads/               acá se guardan las fotos que sube el dueño
    └── src/main/
        ├── resources/application.properties
        └── java/com/muebleria/
            ├── MuebleriaApplication.java
            ├── Producto.java / Usuario.java              entidades
            ├── ProductoRepository.java / UsuarioRepository.java
            ├── ProductoController.java                    /api/productos
            ├── AuthController.java                        /api/login, logout, cambiar-clave
            ├── Sesiones.java                              tokens del login
            ├── Imagenes.java                              achica y guarda las fotos
            └── WebConfig.java                             protege el panel y sirve las fotos
```

## Qué hace falta instalado

- Java 17 o más nuevo
- Maven 3.9
- Node.js 18 o más nuevo
- MySQL 8
- Tomcat **10.1 o 11** (Spring Boot 3 no funciona en Tomcat 9)

## 1. Crear la base

```bash
mysql -u root -p < muebleria.sql
```

Crea la base `muebleria`, las tablas `productos` y `usuarios`, y un usuario de MySQL
`muebleria` que solo puede leer y escribir datos. Antes de correrlo, cambiá la contraseña
`cambiar-esta-clave` del final del archivo y poné la misma en
`backend/src/main/resources/application.properties` (`spring.datasource.password`).

Al final del SQL hay unos productos de prueba comentados por si querés ver la página con datos.

## 2. Correrlo en tu máquina (desarrollo)

Dos terminales:

```bash
# Terminal 1: backend en http://localhost:8080
cd backend
mvn spring-boot:run
```

```bash
# Terminal 2: frontend en http://localhost:5173
cd frontend
npm install
npm run dev
```

Entrá a **http://localhost:5173**. Vite le pasa los pedidos de `/api` y `/uploads` al backend,
así que no hace falta configurar CORS.

La primera vez que arranca, el backend crea el usuario del dueño con los datos de
`application.properties`:

```
app.dueno.usuario=dueno
app.dueno.clave-inicial=cambiar-esta-clave
```

Entrá en `/login` (también hay un link "Ingreso" en el pie de página) y cambiá la
contraseña desde el panel, en "Cambiar contraseña".

## 3. Subirlo a Tomcat (producción)

```bash
# 1. Armar el front. Queda dentro de backend/src/main/resources/static
cd frontend
npm run build

# 2. Armar el WAR (incluye el front)
cd ../backend
mvn clean package
```

Eso genera `backend/target/ROOT.war`. Copialo a la carpeta `webapps/` de Tomcat
(si ya hay un `ROOT` ahí, borralo antes). La página queda en `http://tu-servidor:8080/`.

**Configuración del servidor.** En vez de dejar contraseñas dentro del WAR, pasalas como
variables de entorno en `tomcat/bin/setenv.sh` (en Windows, `setenv.bat` con `set`):

```bash
export SPRING_DATASOURCE_PASSWORD='la-clave-de-mysql'
export APP_CARPETAUPLOADS='/var/muebleria/uploads'
```

La carpeta de fotos tiene que ser una ruta absoluta fuera de Tomcat, así no se pierden
las fotos cuando subís una versión nueva del WAR. Hacé backup de esa carpeta junto con la base.

**Compresión (recomendado).** Para que el HTML, CSS y JS viajen comprimidos, agregá esto al
`<Connector>` de `tomcat/conf/server.xml`:

```xml
compression="on"
compressibleMimeType="text/html,text/css,application/javascript,application/json,image/svg+xml"
```

Para usarla en internet, poné el sitio detrás de HTTPS (por ejemplo con Nginx o Apache
y un certificado de Let's Encrypt). Sin HTTPS, la contraseña del login viaja sin cifrar.

## 4. Personalizar

- **Nombre, dirección, teléfono, WhatsApp, horario:** `frontend/src/tienda.js`
- **Texto de "Sobre nosotros":** `frontend/src/views/SobreNosotros.vue`
- **Título de la pestaña y descripción para Google:** `frontend/index.html`
- **Fotos de portada y del local:** reemplazá los archivos de `frontend/public/img/`
  (son ilustraciones provisorias). Recomendado:
  - `portada.jpg`: 1600 × 900 px, menos de 250 KB
  - `portada-chica.jpg`: la misma a 800 × 450 px (la usan los celulares)
  - `local.jpg`: 1200 × 900 px, menos de 200 KB

  Para achicarlas sirve cualquier editor o una página como squoosh.app (calidad 75-80).
- **Colores:** variables al principio de `frontend/src/estilos.css`
- **Agregar una categoría:** sumala en `categorias` de `tienda.js` y en `CATEGORIAS`
  de `ProductoController.java`.

## Cómo funciona por dentro

**Fotos de productos.** Cuando el dueño elige una foto en el panel, el navegador la achica
a 1200 px antes de subirla (una foto de celular pasa de 4-8 MB a unos 200 KB y además queda
bien orientada). El backend guarda dos versiones en JPG: una de 1200 px y otra de 600 px
para las tarjetas. Las tarjetas cargan la que corresponde según la pantalla y recién cuando
están por aparecer (`loading="lazy"`). El navegador las guarda en caché por un año, porque
cada foto tiene un nombre único.

**Login.** No hay registro. Al iniciar sesión, el backend devuelve un token al azar que
dura 8 horas y el front lo manda en cada pedido del panel. Las sesiones se guardan en
memoria: si se reinicia el servidor, hay que volver a entrar. La contraseña se guarda
encriptada con BCrypt. Cada intento fallido demora un segundo para frenar a quien pruebe
contraseñas al azar.

**Liviano.** Las únicas dependencias del front son Vue, Vue Router y el CSS de Bootstrap
(el JS de Bootstrap no se usa). Las páginas que no son la de inicio se descargan recién
cuando alguien entra, y Login y Panel nunca se descargan para un visitante común.
Para hablar con el backend se usa `fetch`, que ya viene en el navegador.

## API

| Método | Ruta | Quién |
|---|---|---|
| GET | `/api/productos` (opcional `?categoria=sofas`) | público |
| GET | `/api/productos/ultimos` | público |
| POST | `/api/productos` (formulario con foto) | dueño |
| POST | `/api/productos/{id}` (editar) | dueño |
| DELETE | `/api/productos/{id}` | dueño |
| POST | `/api/login` | público |
| POST | `/api/logout` | dueño |
| POST | `/api/cambiar-clave` | dueño |
