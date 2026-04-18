import './assets/main.css'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // <--- 1. AJOUTE CETTE LIGNE

// Vuetify
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { aliases, mdi } from 'vuetify/iconsets/mdi'
import '@mdi/font/css/materialdesignicons.css'

const vuetify = createVuetify({
    components,
    directives,
    icons: {
        defaultSet: 'mdi',
        aliases,
        sets: { mdi },
    },
})

const app = createApp(App)

app.use(router)  // <--- 2. AJOUTE CETTE LIGNE (Avant le mount)
app.use(vuetify)
app.mount('#app')