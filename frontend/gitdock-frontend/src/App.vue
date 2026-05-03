<template>
  <v-app>
    <router-view />
    <NotificationToast />
  </v-app>
</template>

<script setup>
import NotificationToast from '@/components/common/NotificationToast.vue'
</script>

