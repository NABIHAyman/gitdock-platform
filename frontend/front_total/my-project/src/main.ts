import { createApp } from 'vue'
import App from './App.vue'

import router from './router'   // 👈 IMPORTANT

import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

import './style.css'

const vuetify = createVuetify({
  components,
  directives,
})

createApp(App)
  .use(router)   // 👈 IMPORTANT
  .use(vuetify)
  .mount('#app')