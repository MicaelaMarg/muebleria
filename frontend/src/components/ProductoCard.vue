<script setup>
import { computed } from 'vue'
import { urlImagen } from '../api'
import { nombreCategoria, formatearPrecio, linkWhatsapp } from '../tienda'

const props = defineProps({
  producto: { type: Object, required: true }
})

const consulta = computed(() =>
  linkWhatsapp(`Hola, quería consultar por "${props.producto.nombre}".`)
)
</script>

<template>
  <article class="tarjeta">
    <div class="tarjeta-foto">
      <!--
        loading="lazy": la foto se baja recién cuando está por aparecer en pantalla.
        srcset: el navegador elige la versión chica (600 px) o la grande (1200 px)
        según el tamaño de la pantalla.
      -->
      <img
        v-if="producto.imagen"
        :src="urlImagen(producto.imagen, true)"
        :srcset="`${urlImagen(producto.imagen, true)} 600w, ${urlImagen(producto.imagen)} 1200w`"
        sizes="(min-width: 992px) 33vw, (min-width: 576px) 50vw, 100vw"
        :alt="producto.nombre"
        width="600"
        height="450"
        loading="lazy"
        decoding="async"
      />
      <div v-else class="sin-foto">Sin foto</div>
    </div>

    <div class="tarjeta-cuerpo">
      <span class="tarjeta-categoria">{{ nombreCategoria(producto.categoria) }}</span>
      <h3 class="tarjeta-titulo">{{ producto.nombre }}</h3>
      <p v-if="producto.descripcion" class="tarjeta-descripcion">{{ producto.descripcion }}</p>

      <div class="tarjeta-pie">
        <span class="precio">{{ formatearPrecio(producto.precio) }}</span>
        <a :href="consulta" target="_blank" rel="noopener" class="btn btn-pildora btn-terracota btn-sm">
          Consultar
        </a>
      </div>
    </div>
  </article>
</template>
