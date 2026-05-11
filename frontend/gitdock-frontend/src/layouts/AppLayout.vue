<template>
  <div class="min-h-screen bg-gray-50">
    <AppHeader />
    <div class="flex">
      <AppSidebar />
      <main class="flex-1 p-6">
        <slot />
      </main>
    </div>
<<<<<<< HEAD
=======
    <NotificationToast />

    <GitDockAiChat />
>>>>>>> f15b548226eb791a8b13956d26c47455fe8cc944
  </div>
</template>

<script setup>
import AppHeader from '@/components/navigation/AppHeader.vue'
import AppSidebar from '@/components/navigation/AppSidebar.vue'
<<<<<<< HEAD
=======
import NotificationToast from '@/components/common/NotificationToast.vue'
import GitDockAiChat from '@/components/common/GitDockAiChat.vue'
>>>>>>> f15b548226eb791a8b13956d26c47455fe8cc944
</script>

