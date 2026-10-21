<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api, urlImagen } from '../api'
import { categorias, nombreCategoria, formatearPrecio } from '../tienda'

const router = useRouter()

const productos = ref([])
const cargando = ref(true)
const guardando = ref(false)
const aviso = ref('')   // mensajes tipo "Producto agregado."
const error = ref('')

// ---------- Formulario ----------
// Si form.id tiene valor estamos editando; si es null, es un producto nuevo.
const vacio = { id: null, nombre: '', categoria: 'sofas', precio: '', descripcion: '' }
const form = reactive({ ...vacio })
const foto = ref(null)        // foto nueva ya achicada (Blob), lista para subir
const vistaPrevia = ref(null) // URL para mostrar la foto en el formulario
const inputFoto = ref(null)   // el <input type="file">, para poder vaciarlo

async function cargar() {
  cargando.value = true
  try {
    productos.value = await api.productos()
  } catch (e) {
    error.value = e.message
  } finally {
    cargando.value = false
  }
}

onMounted(cargar)

function cambiarVistaPrevia(url) {
  // Las URL "blob:" ocupan memoria hasta que se liberan
  if (vistaPrevia.value && vistaPrevia.value.startsWith('blob:')) {
    URL.revokeObjectURL(vistaPrevia.value)
  }
  vistaPrevia.value = url
}

function limpiarFormulario() {
  Object.assign(form, vacio)
  foto.value = null
  cambiarVistaPrevia(null)
  if (inputFoto.value) inputFoto.value.value = ''
}

function editar(producto) {
  limpiarFormulario()
  Object.assign(form, {
    id: producto.id,
    nombre: producto.nombre,
    categoria: producto.categoria,
    precio: producto.precio,
    descripcion: producto.descripcion || ''
  })
  cambiarVistaPrevia(urlImagen(producto.imagen, true))
  aviso.value = ''
  error.value = ''
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// Achica la foto en el navegador antes de subirla.
// Las fotos del celular pesan varios MB: así se suben en un segundo.
// Además, al dibujarla en un canvas queda bien orientada aunque se haya sacado con el celular girado.
async function achicarFoto(archivo, anchoMax = 1200) {
  const url = URL.createObjectURL(archivo)
  try {
    const imagen = new Image()
    imagen.src = url
    await imagen.decode()

    const escala = Math.min(1, anchoMax / imagen.naturalWidth)
    const canvas = document.createElement('canvas')
    canvas.width = Math.round(imagen.naturalWidth * escala)
    canvas.height = Math.round(imagen.naturalHeight * escala)

    const ctx = canvas.getContext('2d')
    ctx.fillStyle = '#ffffff' // fondo blanco por si es un PNG transparente
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    ctx.imageSmoothingQuality = 'high'
    ctx.drawImage(imagen, 0, 0, canvas.width, canvas.height)

    return await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.9))
  } finally {
    URL.revokeObjectURL(url)
  }
}

async function elegirFoto(evento) {
  const archivo = evento.target.files[0]
  if (!archivo) return
  error.value = ''
  try {
    foto.value = await achicarFoto(archivo)
    cambiarVistaPrevia(URL.createObjectURL(foto.value))
  } catch {
    error.value = 'No se pudo abrir la foto. Probá con un archivo JPG o PNG.'
    evento.target.value = ''
  }
}

async function guardar() {
  aviso.value = ''
  error.value = ''
  guardando.value = true

  const editando = Boolean(form.id)
  const datos = new FormData()
  datos.append('nombre', form.nombre)
  datos.append('categoria', form.categoria)
  datos.append('precio', form.precio)
  datos.append('descripcion', form.descripcion)
  if (foto.value) datos.append('imagen', foto.value, 'foto.jpg')

  try {
    if (editando) {
      await api.editar(form.id, datos)
    } else {
      await api.crear(datos)
    }
    aviso.value = editando ? 'Cambios guardados.' : 'Producto agregado.'
    limpiarFormulario()
    await cargar()
  } catch (e) {
    error.value = e.message
  } finally {
    guardando.value = false
  }
}

async function borrar(producto) {
  if (!confirm(`¿Borrar "${producto.nombre}"? No se puede deshacer.`)) return
  aviso.value = ''
  error.value = ''
  try {
    await api.borrar(producto.id)
    if (form.id === producto.id) limpiarFormulario()
    aviso.value = 'Producto borrado.'
    await cargar()
  } catch (e) {
    error.value = e.message
  }
}

async function salir() {
  await api.logout().catch(() => {})
  router.push('/login')
}

// ---------- Cambio de contraseña ----------
const clave = reactive({ actual: '', nueva: '' })
const avisoClave = ref('')
const errorClave = ref('')

async function cambiarClave() {
  avisoClave.value = ''
  errorClave.value = ''
  try {
    await api.cambiarClave(clave.actual, clave.nueva)
    avisoClave.value = 'Contraseña cambiada.'
    clave.actual = ''
    clave.nueva = ''
  } catch (e) {
    errorClave.value = e.message
  }
}
</script>

<template>
  <section class="container py-5">
    <div class="d-flex justify-content-between align-items-center gap-3 mb-4">
      <h1 class="mb-0">Panel</h1>
      <button class="btn btn-pildora btn-borde btn-sm" @click="salir">Salir</button>
    </div>

    <p v-if="aviso" class="aviso" role="status">{{ aviso }}</p>
    <p v-if="error" class="aviso aviso-error" role="alert">{{ error }}</p>

    <div class="row g-4">
      <!-- Formulario para agregar o editar -->
      <div class="col-lg-5">
        <form class="caja-panel" @submit.prevent="guardar">
          <h2 class="h4 mb-3">{{ form.id ? 'Editar producto' : 'Nuevo producto' }}</h2>

          <div class="mb-3">
            <label for="foto" class="form-label">Foto</label>
            <img v-if="vistaPrevia" :src="vistaPrevia" alt="" class="vista-previa" />
            <input
              id="foto"
              ref="inputFoto"
              type="file"
              accept="image/*"
              class="form-control"
              @change="elegirFoto"
            />
            <div v-if="form.id" class="form-text">Si no elegís otra, queda la foto actual.</div>
          </div>

          <div class="mb-3">
            <label for="nombre" class="form-label">Nombre</label>
            <input id="nombre" v-model="form.nombre" class="form-control" maxlength="120" required />
          </div>

          <div class="row g-3 mb-3">
            <div class="col-sm-6">
              <label for="categoria" class="form-label">Categoría</label>
              <select id="categoria" v-model="form.categoria" class="form-select">
                <option v-for="c in categorias" :key="c.id" :value="c.id">{{ c.nombre }}</option>
              </select>
            </div>
            <div class="col-sm-6">
              <label for="precio" class="form-label">Precio ($)</label>
              <input
                id="precio"
                v-model="form.precio"
                type="number"
                min="0"
                step="any"
                inputmode="decimal"
                class="form-control"
                required
              />
            </div>
          </div>

          <div class="mb-4">
            <label for="descripcion" class="form-label">Descripción</label>
            <textarea
              id="descripcion"
              v-model="form.descripcion"
              rows="4"
              class="form-control"
              placeholder="Medidas, materiales, colores disponibles..."
            ></textarea>
          </div>

          <div class="d-flex gap-2">
            <button type="submit" class="btn btn-pildora btn-terracota" :disabled="guardando">
              {{ guardando ? 'Guardando...' : 'Guardar' }}
            </button>
            <button v-if="form.id" type="button" class="btn btn-pildora btn-borde" @click="limpiarFormulario">
              Cancelar
            </button>
          </div>
        </form>

        <!-- Cambiar contraseña (desplegable, sin JS) -->
        <details class="caja-panel mt-4">
          <summary>Cambiar contraseña</summary>
          <form class="mt-3" @submit.prevent="cambiarClave">
            <div class="mb-3">
              <label for="clave-actual" class="form-label">Contraseña actual</label>
              <input id="clave-actual" v-model="clave.actual" type="password" class="form-control" autocomplete="current-password" required />
            </div>
            <div class="mb-3">
              <label for="clave-nueva" class="form-label">Contraseña nueva</label>
              <input id="clave-nueva" v-model="clave.nueva" type="password" class="form-control" autocomplete="new-password" minlength="8" required />
              <div class="form-text">Mínimo 8 caracteres.</div>
            </div>
            <p v-if="avisoClave" class="text-success">{{ avisoClave }}</p>
            <p v-if="errorClave" class="text-danger">{{ errorClave }}</p>
            <button type="submit" class="btn btn-pildora btn-marron btn-sm">Cambiar</button>
          </form>
        </details>
      </div>

      <!-- Lista de productos cargados -->
      <div class="col-lg-7">
        <div class="caja-panel">
          <h2 class="h4 mb-3">Productos ({{ productos.length }})</h2>

          <p v-if="cargando" class="texto-suave mb-0">Cargando...</p>
          <p v-else-if="!productos.length" class="texto-suave mb-0">Todavía no cargaste ningún producto.</p>

          <ul v-else class="lista-panel">
            <li v-for="producto in productos" :key="producto.id" :class="{ editando: form.id === producto.id }">
              <img
                v-if="producto.imagen"
                :src="urlImagen(producto.imagen, true)"
                alt=""
                width="72"
                height="54"
                loading="lazy"
              />
              <div v-else class="mini-sin-foto"></div>

              <div class="lista-datos">
                <strong>{{ producto.nombre }}</strong>
                <small>{{ nombreCategoria(producto.categoria) }} · {{ formatearPrecio(producto.precio) }}</small>
              </div>

              <div class="d-flex gap-2">
                <button class="btn btn-pildora btn-borde btn-sm" @click="editar(producto)">Editar</button>
                <button class="btn btn-pildora btn-borrar btn-sm" @click="borrar(producto)">Borrar</button>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </section>
</template>
