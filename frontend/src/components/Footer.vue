<script setup>
import { tienda, linkWhatsapp } from '../tienda'

const anio = new Date().getFullYear()
</script>

<template>
  <footer class="pie">
    <div class="container py-5">
      <div class="row g-4">
        <div class="col-md-5">
          <p class="pie-marca">{{ tienda.nombre }}</p>
          <p class="mb-0">{{ tienda.direccion }}</p>
          <p class="mb-0">{{ tienda.horario }}</p>
        </div>

        <div class="col-md-4">
          <p class="pie-titulo">Contacto</p>
          <ul class="list-unstyled mb-0">
            <li><a :href="linkWhatsapp()" target="_blank" rel="noopener">WhatsApp</a></li>
            <li><a :href="'tel:' + tienda.telefono.replace(/\s|-/g, '')">{{ tienda.telefono }}</a></li>
            <li><a :href="'mailto:' + tienda.email">{{ tienda.email }}</a></li>
          </ul>
        </div>

        <div class="col-md-3">
          <p class="pie-titulo">Páginas</p>
          <ul class="list-unstyled mb-0">
            <li><RouterLink to="/catalogo">Catálogo</RouterLink></li>
            <li><RouterLink to="/sobre-nosotros">Sobre nosotros</RouterLink></li>
          </ul>
        </div>
      </div>

      <div class="pie-final">
        <span>© {{ anio }} {{ tienda.nombre }}</span>
        <!-- Acceso discreto para el dueño -->
        <RouterLink to="/login">Ingreso</RouterLink>
      </div>
    </div>
  </footer>
</template>
