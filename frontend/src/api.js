// Todas las llamadas al backend pasan por acá.
// Usamos fetch (viene con el navegador), así no sumamos otra librería.

const CLAVE_TOKEN = 'token'

export function getToken() {
  return localStorage.getItem(CLAVE_TOKEN)
}

export function estaLogueado() {
  return Boolean(getToken())
}

// Hace el pedido, agrega el token si hay sesión y devuelve el JSON de la respuesta.
// Si algo sale mal, tira un Error con el mensaje que mandó el backend.
async function pedir(url, opciones = {}) {
  const headers = { ...opciones.headers }
  const token = getToken()
  if (token) headers.Authorization = 'Bearer ' + token

  let respuesta
  try {
    respuesta = await fetch(url, { ...opciones, headers })
  } catch {
    throw new Error('No hay conexión con el servidor. Probá de nuevo en un rato.')
  }

  // La sesión venció o no es válida: volvemos al login
  if (respuesta.status === 401 && token) {
    localStorage.removeItem(CLAVE_TOKEN)
    window.location.href = '/login'
    throw new Error('Tu sesión venció. Volvé a entrar.')
  }

  if (!respuesta.ok) {
    let mensaje = 'Algo salió mal. Probá de nuevo.'
    try {
      const datos = await respuesta.json()
      if (datos.message) mensaje = datos.message
    } catch {
      // la respuesta no era JSON, dejamos el mensaje genérico
    }
    throw new Error(mensaje)
  }

  // 204 = salió bien pero no hay nada que devolver (ej: al borrar)
  return respuesta.status === 204 ? null : respuesta.json()
}

// Para mandar datos en JSON (login y cambio de clave)
function json(datos) {
  return {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(datos)
  }
}

export const api = {
  // ----- Productos (públicos) -----
  productos(categoria) {
    const filtro = categoria ? '?categoria=' + encodeURIComponent(categoria) : ''
    return pedir('/api/productos' + filtro)
  },

  ultimos() {
    return pedir('/api/productos/ultimos')
  },

  // ----- Productos (solo el dueño). "datos" es un FormData con la foto y los campos -----
  crear(datos) {
    return pedir('/api/productos', { method: 'POST', body: datos })
  },

  editar(id, datos) {
    return pedir(`/api/productos/${id}`, { method: 'POST', body: datos })
  },

  borrar(id) {
    return pedir(`/api/productos/${id}`, { method: 'DELETE' })
  },

  // ----- Sesión -----
  async login(usuario, clave) {
    const datos = await pedir('/api/login', json({ usuario, clave }))
    localStorage.setItem(CLAVE_TOKEN, datos.token)
  },

  async logout() {
    try {
      await pedir('/api/logout', { method: 'POST' })
    } finally {
      localStorage.removeItem(CLAVE_TOKEN)
    }
  },

  cambiarClave(actual, nueva) {
    return pedir('/api/cambiar-clave', json({ actual, nueva }))
  }
}

// Arma la URL de la foto de un producto.
// chica = true devuelve la versión de 600 px (la que se usa en las tarjetas).
export function urlImagen(nombre, chica = false) {
  if (!nombre) return null
  return '/uploads/' + (chica ? nombre.replace('.jpg', '_chica.jpg') : nombre)
}
