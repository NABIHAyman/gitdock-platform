<template>
  <div class="min-h-screen bg-gray-50">
    <AppHeader />
    <div class="flex">
      <AppSidebar />
      <main class="flex-1 p-6">
        <slot />
      </main>
    </div>
    <NotificationToast />
  </div>
</template>

<script setup>
import AppHeader from '@/components/navigation/AppHeader.vue'
import AppSidebar from '@/components/navigation/AppSidebar.vue'
import NotificationToast from '@/components/common/NotificationToast.vue'
</script>

