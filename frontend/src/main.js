import { createApp } from 'vue'

// Solo el CSS de Bootstrap. El JS no hace falta: el menú del celular lo maneja Vue.
import 'bootstrap/dist/css/bootstrap.min.css'
import './estilos.css'

import App from './App.vue'
import router from './router'

createApp(App).use(router).mount('#app')
