import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 1. Importation des styles
import './style.css' // Tailwind
import 'vuetify/styles' // Styles de base Vuetify
import '@mdi/font/css/materialdesignicons.css' // Icônes (si installées)

// 2. Importation de Vuetify
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

const vuetify = createVuetify({
    components,
    directives,
})

const app = createApp(App)
const pinia = createPinia()

// 3. Utilisation des plugins
app.use(pinia)
app.use(router)
app.use(vuetify)

app.mount('#app')