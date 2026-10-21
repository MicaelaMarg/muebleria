<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'

const router = useRouter()

const usuario = ref('')
const clave = ref('')
const error = ref('')
const enviando = ref(false)

async function entrar() {
  error.value = ''
  enviando.value = true
  try {
    await api.login(usuario.value, clave.value)
    router.push('/panel')
  } catch (e) {
    error.value = e.message
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <section class="container py-5">
    <div class="caja-login">
      <h1 class="h2 mb-1">Ingresar</h1>
      <p class="texto-suave mb-4">Acceso para cargar y editar productos.</p>

      <form @submit.prevent="entrar">
        <div class="mb-3">
          <label for="usuario" class="form-label">Usuario</label>
          <input id="usuario" v-model="usuario" class="form-control" autocomplete="username" required />
        </div>
        <div class="mb-4">
          <label for="clave" class="form-label">Contraseña</label>
          <input id="clave" v-model="clave" type="password" class="form-control" autocomplete="current-password" required />
        </div>

        <p v-if="error" class="text-danger" role="alert">{{ error }}</p>

        <button type="submit" class="btn btn-pildora btn-terracota w-100" :disabled="enviando">
          {{ enviando ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>
    </div>
  </section>
</template>
