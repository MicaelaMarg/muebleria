// Datos de la tienda. Cambialos acá y se actualizan en toda la página.

export const tienda = {
  nombre: 'Mueblería Salvini',
  marca: 'Salvini', // lo que va grande en la portada
  lema: 'Sofás, sillones y sillas para tu casa. Vení a probarlos al local.',
  direccion: 'Av. San Martín 1234, Buenos Aires',
  telefono: '11 1234-5678',
  whatsapp: '5491112345678', // con código de país, sin espacios ni el +
  email: 'contacto@muebleriaelroble.com.ar',
  horario: 'Lunes a viernes de 9 a 18 h. Sábados de 9 a 13 h.'
}

// Categorías. El "id" tiene que coincidir con las del backend (ProductoController).
export const categorias = [
  { id: 'sofas', nombre: 'Sofás', texto: 'De dos y tres cuerpos, en tela o cuero.' },
  { id: 'sillones', nombre: 'Sillones', texto: 'Individuales, para el living o el cuarto.' },
  { id: 'sillas', nombre: 'Sillas', texto: 'Para el comedor, la cocina o el escritorio.' }
]

export function nombreCategoria(id) {
  const categoria = categorias.find(c => c.id === id)
  return categoria ? categoria.nombre : id
}

// 850000 -> "$ 850.000"
export function formatearPrecio(precio) {
  return new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0
  }).format(precio)
}

// Link para abrir WhatsApp con un mensaje ya escrito
export function linkWhatsapp(mensaje = 'Hola, quería hacer una consulta.') {
  return `https://wa.me/${tienda.whatsapp}?text=${encodeURIComponent(mensaje)}`
}
