<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import ProductoCard from '../components/ProductoCard.vue'
import { api } from '../api'
import { categorias, nombreCategoria } from '../tienda'

const route = useRoute()

const productos = ref([])
const cargando = ref(true)
const error = ref('')

// La categoría elegida está en la URL (/catalogo?categoria=sofas),
// así se puede compartir el link o volver atrás con el navegador.
const categoriaActual = computed(() => route.query.categoria || '')

async function cargar() {
  cargando.value = true
  error.value = ''
  try {
    productos.value = await api.productos(categoriaActual.value)
  } catch (e) {
    error.value = e.message
  } finally {
    cargando.value = false
  }
}

// Carga al entrar y cada vez que cambia el filtro
watch(categoriaActual, cargar, { immediate: true })
</script>

<template>
  <section class="container py-5">
    <h1>{{ categoriaActual ? nombreCategoria(categoriaActual) : 'Catálogo' }}</h1>
    <p class="texto-suave mb-4">
      Si querés saber medidas, telas o colores, tocá "Consultar" y te respondemos por WhatsApp.
    </p>

    <!-- Filtro por categoría -->
    <nav class="filtros" aria-label="Filtrar por categoría">
      <RouterLink
        to="/catalogo"
        class="btn btn-pildora"
        :class="categoriaActual ? 'btn-borde' : 'btn-marron'"
      >
        Todos
      </RouterLink>
      <RouterLink
        v-for="categoria in categorias"
        :key="categoria.id"
        :to="{ path: '/catalogo', query: { categoria: categoria.id } }"
        class="btn btn-pildora"
        :class="categoriaActual === categoria.id ? 'btn-marron' : 'btn-borde'"
      >
        {{ categoria.nombre }}
      </RouterLink>
    </nav>

    <p v-if="cargando" class="texto-suave">Cargando...</p>
    <p v-else-if="error" class="text-danger">{{ error }}</p>
    <p v-else-if="!productos.length" class="texto-suave">Por ahora no hay productos en esta categoría.</p>

    <div v-else class="row row-cols-1 row-cols-sm-2 row-cols-lg-3 g-4">
      <div v-for="producto in productos" :key="producto.id" class="col">
        <ProductoCard :producto="producto" />
      </div>
    </div>
  </section>
</template>
