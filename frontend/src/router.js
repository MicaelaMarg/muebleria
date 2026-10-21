import { createRouter, createWebHistory } from 'vue-router'
import Inicio from './views/Inicio.vue'
import { estaLogueado } from './api'
import { tienda } from './tienda'

// Inicio se carga de entrada. El resto de las páginas se baja recién cuando
// alguien entra (import dinámico), así la primera carga es más liviana.
// Login y Panel nunca se descargan para un visitante común.
const routes = [
  { path: '/', component: Inicio },
  { path: '/catalogo', component: () => import('./views/Catalogo.vue'), meta: { titulo: 'Catálogo' } },
  { path: '/sobre-nosotros', component: () => import('./views/SobreNosotros.vue'), meta: { titulo: 'Sobre nosotros' } },
  { path: '/login', component: () => import('./views/Login.vue'), meta: { titulo: 'Ingresar' } },
  { path: '/panel', component: () => import('./views/Panel.vue'), meta: { titulo: 'Panel', privada: true } },
  // Cualquier otra dirección vuelve al inicio
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  linkExactActiveClass: 'activo',
  // Al cambiar de página, arriba de todo. Si solo cambia el filtro del catálogo, se queda donde está.
  scrollBehavior(to, from, posicionGuardada) {
    if (posicionGuardada) return posicionGuardada
    if (to.path === from.path) return false
    return { top: 0 }
  }
})

router.beforeEach(to => {
  // El panel solo con sesión iniciada
  if (to.meta.privada && !estaLogueado()) return '/login'
  // Si ya entró, no tiene sentido mostrarle el login
  if (to.path === '/login' && estaLogueado()) return '/panel'
})

// Título de la pestaña del navegador
router.afterEach(to => {
  document.title = to.meta.titulo ? `${to.meta.titulo} | ${tienda.nombre}` : tienda.nombre
})

export default router
