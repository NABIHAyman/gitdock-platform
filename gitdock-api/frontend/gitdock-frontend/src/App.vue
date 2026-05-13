<template>
  <v-app>
    <router-view />
    <NotificationToast />
  </v-app>
</template>

<script setup>
import { watch, onUnmounted } from 'vue'
import NotificationToast from '@/components/common/NotificationToast.vue'
import { useAuthStore } from '@/stores/authStore'
import { useNotificationStore } from '@/stores/notificationStore'

const authStore = useAuthStore()
const notifStore = useNotificationStore()

// 👉 La connexion WS est gérée ici globalement !
watch(() => authStore.isAuthenticated, (isAuth) => {
  if (isAuth) {
    notifStore.startRealTime()
  } else {
    notifStore.stopRealTime()
  }
}, { immediate: true })

onUnmounted(() => {
  notifStore.stopRealTime()
})
</script>

