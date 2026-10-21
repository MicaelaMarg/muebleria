<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { estaLogueado } from '../api'
import { tienda } from '../tienda'

const route = useRoute()

// Menú desplegable en el celular (lo manejamos con Vue, sin el JS de Bootstrap)
const abierto = ref(false)

// Se cierra solo al cambiar de página
watch(() => route.fullPath, () => { abierto.value = false })

// Se recalcula en cada cambio de página (por si entró o salió del panel)
const logueado = computed(() => {
  route.fullPath
  return estaLogueado()
})
</script>

<template>
  <nav class="navbar navbar-expand-md barra sticky-top">
    <div class="container">
      <RouterLink class="navbar-brand marca" to="/">{{ tienda.nombre }}</RouterLink>

      <button
        class="navbar-toggler"
        type="button"
        :aria-expanded="abierto"
        aria-controls="menu-principal"
        aria-label="Abrir menú"
        @click="abierto = !abierto"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div id="menu-principal" class="collapse navbar-collapse" :class="{ show: abierto }">
        <ul class="navbar-nav ms-auto gap-md-1">
          <li class="nav-item"><RouterLink class="nav-link" to="/">Inicio</RouterLink></li>
          <li class="nav-item"><RouterLink class="nav-link" to="/catalogo">Catálogo</RouterLink></li>
          <li class="nav-item"><RouterLink class="nav-link" to="/sobre-nosotros">Sobre nosotros</RouterLink></li>
          <li v-if="logueado" class="nav-item"><RouterLink class="nav-link" to="/panel">Panel</RouterLink></li>
        </ul>
      </div>
    </div>
  </nav>
</template>
