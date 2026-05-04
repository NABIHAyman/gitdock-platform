<template>
  <div class="space-y-6">
    <!-- En-tête interne -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-xl font-bold text-gray-800">Configuration de la Gamification</h2>
        <p class="text-xs text-gray-500">Gérez les règles et récompensez vos développeurs.</p>
      </div>

      <!-- Navigation par onglets -->
      <div class="flex bg-gray-100 p-1 rounded-lg border shadow-sm">
        <button @click="activeTab = 'badges'" :class="tabClass('badges')">Badges</button>
        <button @click="activeTab = 'tags'" :class="tabClass('tags')">Tags</button>
        <button @click="activeTab = 'levels'" :class="tabClass('levels')">Niveaux</button>
        <!-- NOUVEAU BOUTON -->
        <button @click="activeTab = 'contributors'" :class="tabClass('contributors')">
          Contributeurs
        </button>
      </div>
    </div>

    <!-- Zone de contenu dynamique -->
    <div class="bg-white rounded-xl border border-gray-200 p-6 min-h-[450px] shadow-sm">
      <transition name="fade" mode="out-in">
        <div :key="activeTab">
          <BadgeSection v-if="activeTab === 'badges'" />
          <TagSection v-else-if="activeTab === 'tags'" />
          <LevelSection v-else-if="activeTab === 'levels'" />
          <!-- NOUVELLE SECTION -->
          <ContributorSection v-else-if="activeTab === 'contributors'" />
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import BadgeSection from '@/components/gamification/dashboard/manager/BadgeSection.vue'
import TagSection from '@/components/gamification/dashboard/manager/TagSection.vue'
import LevelSection from '@/components/gamification/dashboard/manager/LevelSection.vue'
// N'oublie pas de créer ce fichier (voir l'étape précédente)
import ContributorSection from '@/components/gamification/dashboard/manager/ContributorSection.vue'

const activeTab = ref('badges')

const tabClass = (tab: string) => [
  'px-4 py-1.5 rounded-md text-sm font-bold transition-all duration-200',
  activeTab.value === tab
      ? 'bg-white shadow-md text-indigo-600'
      : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
]
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>