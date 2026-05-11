import { createApp } from 'vue'
import App from './App.vue'
import './style.css'

import '@fortawesome/fontawesome-free/css/all.min.css'

import 'vuetify/styles'
import { createVuetify } from 'vuetify'

// Importer uniquement les composants utilisés
import {
  VApp,
  VMain,
  VContainer,
  VRow,
  VCol,
  VNavigationDrawer,
  VAvatar,
  VTextField,
  VBtn,
  VIcon,
  VList,
  VListItem,
  VCard,
  VProgressLinear,
  VDataTable
} from 'vuetify/components'

// Importer uniquement les directives utilisées
import { Ripple } from 'vuetify/directives'

import { router } from './router/router'

const vuetify = createVuetify({
  components: {
    VApp,
    VMain,
    VContainer,
    VRow,
    VCol,
    VNavigationDrawer,
    VAvatar,
    VTextField,
    VBtn,
    VIcon,
    VList,
    VListItem,
    VCard,
    VProgressLinear,
    VDataTable
  },
  directives: {
    Ripple
  }
})

createApp(App)
  .use(router)
  .use(vuetify)
  .mount('#app')
