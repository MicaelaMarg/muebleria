<script setup>
import { ref, onMounted } from 'vue'
import ProductoCard from '../components/ProductoCard.vue'
import { api } from '../api'
import { tienda, categorias } from '../tienda'

const ultimos = ref([])
const cargando = ref(true)
const error = ref(false)

onMounted(async () => {
  try {
    ultimos.value = await api.ultimos()
  } catch {
    error.value = true
  } finally {
    cargando.value = false
  }
})
</script>

<template>
  <!-- Portada: foto grande con el nombre de la tienda -->
  <section class="portada-envoltorio">
    <div class="portada">
      <!-- Es lo primero que se ve, así que NO lleva lazy loading (al contrario: prioridad alta) -->
      <img
        class="portada-foto"
        src="/img/banner-portada.jpg"
        srcset="/img/banner-portada-chica.jpg 1000w, /img/banner-portada.jpg 1983w"
        sizes="100vw"
        width="1983"
        height="793"
        alt=""
        fetchpriority="high"
      />
      <div class="portada-texto">
        <h1 class="portada-logo" :aria-label="tienda.nombre">
          <span class="portada-marca">{{ tienda.marca }}</span>
          <span class="portada-rubro">Mueblería</span>
        </h1>
        <p class="portada-lema">{{ tienda.lema }}</p>
        <div class="d-flex flex-wrap gap-2">
          <RouterLink to="/catalogo" class="btn btn-pildora btn-terracota btn-lg">Ver catálogo</RouterLink>
          <RouterLink to="/sobre-nosotros" class="btn btn-pildora btn-crema btn-lg">Dónde estamos</RouterLink>
        </div>
      </div>
    </div>
  </section>

  <!-- Categorías -->
  <section class="container py-5">
    <h2 class="mb-4">Categorías</h2>
    <div class="row g-3">
      <div v-for="(categoria, i) in categorias" :key="categoria.id" class="col-md-4">
        <RouterLink
          :to="{ path: '/catalogo', query: { categoria: categoria.id } }"
          class="categoria-bloque"
          :class="'tono-' + (i % 3)"
        >
          <span class="categoria-nombre">{{ categoria.nombre }}</span>
          <span class="categoria-texto">{{ categoria.texto }}</span>
          <span class="categoria-ver">Ver {{ categoria.nombre.toLowerCase() }}</span>
        </RouterLink>
      </div>
    </div>
  </section>

  <!-- Últimos productos -->
  <section class="container pb-5">
    <div class="d-flex justify-content-between align-items-end gap-3 mb-4">
      <h2 class="mb-0">Lo último que entró</h2>
      <RouterLink to="/catalogo" class="enlace-flecha">Ver todo</RouterLink>
    </div>

    <p v-if="cargando" class="texto-suave">Cargando...</p>
    <p v-else-if="error" class="texto-suave">No pudimos cargar los productos. Probá recargar la página.</p>
    <p v-else-if="!ultimos.length" class="texto-suave">Todavía no hay productos cargados.</p>

    <div v-else class="row row-cols-1 row-cols-sm-2 row-cols-lg-3 g-4">
      <div v-for="producto in ultimos" :key="producto.id" class="col">
        <ProductoCard :producto="producto" />
      </div>
    </div>
  </section>
</template>
