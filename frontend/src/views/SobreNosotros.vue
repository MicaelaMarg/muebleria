<script setup>
import { tienda, linkWhatsapp } from '../tienda'

// Abre Google Maps buscando la dirección (sin cargar ningún mapa en la página)
const linkMapa = 'https://www.google.com/maps/search/?api=1&query=' + encodeURIComponent(tienda.direccion)
</script>

<template>
  <section class="container py-5">
    <div class="row g-5 align-items-center">
      <div class="col-lg-6">
        <h1 class="mb-4">Sobre nosotros</h1>
        <!-- Cambiá este texto por la historia real de la tienda -->
        <p>
          Somos una mueblería de barrio. Vendemos sofás, sillones y sillas, y muchos
          de los muebles los armamos en nuestro propio taller.
        </p>
        <p>
          Si buscás algo en una medida o una tela especial, consultanos: muchas veces
          lo podemos hacer a pedido.
        </p>
        <p class="mb-0">
          Te esperamos en el local para que pruebes los muebles antes de comprar.
        </p>
      </div>

      <div class="col-lg-6">
        <img
          class="foto-local"
          src="/img/local.jpg"
          alt="Nuestro local"
          width="1200"
          height="900"
          loading="lazy"
          decoding="async"
        />
      </div>
    </div>
  </section>

  <section class="container pb-5">
    <h2 class="mb-4">Contacto</h2>
    <div class="row g-3">
      <div class="col-sm-6 col-lg-3">
        <div class="dato-contacto">
          <h3>Dirección</h3>
          <p>{{ tienda.direccion }}</p>
          <a :href="linkMapa" target="_blank" rel="noopener">Cómo llegar</a>
        </div>
      </div>
      <div class="col-sm-6 col-lg-3">
        <div class="dato-contacto">
          <h3>Horario</h3>
          <p>{{ tienda.horario }}</p>
        </div>
      </div>
      <div class="col-sm-6 col-lg-3">
        <div class="dato-contacto">
          <h3>Teléfono</h3>
          <p>{{ tienda.telefono }}</p>
          <a :href="linkWhatsapp()" target="_blank" rel="noopener">Escribinos por WhatsApp</a>
        </div>
      </div>
      <div class="col-sm-6 col-lg-3">
        <div class="dato-contacto">
          <h3>Email</h3>
          <p><a :href="'mailto:' + tienda.email">{{ tienda.email }}</a></p>
        </div>
      </div>
    </div>
  </section>
</template>
