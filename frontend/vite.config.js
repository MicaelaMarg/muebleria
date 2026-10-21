import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],

  // En desarrollo (npm run dev) los pedidos a /api y /uploads se mandan al backend.
  // Así no hace falta configurar CORS.
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/uploads': 'http://localhost:8080'
    }
  },

  // "npm run build" deja la página armada dentro del backend,
  // así el WAR incluye todo y Tomcat sirve front y back juntos.
  build: {
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true
  }
})
