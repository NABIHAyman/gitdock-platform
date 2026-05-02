<template>
  <div class="space-y-6">
    <!-- En-tête interne -->
    <div class="flex items-center justify-between">
      <h2 class="text-xl font-bold text-gray-800">Configuration de la Gamification</h2>
      <div class="flex bg-gray-100 p-1 rounded-lg border">
        <button @click="activeTab = 'badges'" :class="tabClass('badges')">Badges</button>
        <button @click="activeTab = 'tags'" :class="tabClass('tags')">Tags</button>
        <button @click="activeTab = 'levels'" :class="tabClass('levels')">Niveaux</button>
      </div>
    </div>

    <!-- Zone de contenu des sections -->
    <div class="bg-white rounded-xl border p-6 min-h-[400px]">
      <BadgeSection v-if="activeTab === 'badges'" />
      <TagSection v-else-if="activeTab === 'tags'" />
      <LevelSection v-else-if="activeTab === 'levels'" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import BadgeSection from '@/components/gamification/dashboard/manager/BadgeSection.vue'
import TagSection from '@/components/gamification/dashboard/manager/TagSection.vue'
import LevelSection from '@/components/gamification/dashboard/manager/LevelSection.vue'

const activeTab = ref('badges')

const tabClass = (tab: string) => [
  'px-4 py-1.5 rounded-md text-sm font-bold transition-all',
  activeTab.value === tab ? 'bg-white shadow-sm text-indigo-600' : 'text-gray-500 hover:text-gray-700'
]
</script>