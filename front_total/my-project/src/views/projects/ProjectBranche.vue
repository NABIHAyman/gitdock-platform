<template>
  <div class="p-6 space-y-6">

    <!-- HEADER -->
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold text-gray-800">
        Branches
      </h1>

      <span class="text-sm text-gray-500">
        Liste des branches du projet
      </span>
    </div>

    <!-- LIST -->
    <div v-if="branches.length === 0" class="text-gray-500 text-sm">
      Aucune branche disponible
    </div>

    <div v-else class="space-y-3">

      <div
        v-for="b in branches"
        :key="b.id"
        class="bg-white border border-gray-100 p-4 rounded-lg shadow-sm flex items-center justify-between hover:shadow transition"
      >
        <!-- NAME -->
        <div class="flex items-center space-x-3">
          <span class="text-lg">🌿</span>
          <span class="font-medium text-gray-800">
            {{ b.name }}
          </span>
        </div>

        <!-- STATUS -->
        <span class="text-xs text-gray-400">
          updated recently
        </span>
      </div>

    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'

const branches = ref([
  { id: 1, name: 'main' },
  { id: 2, name: 'dev' }
])
</script>