<template>
  <div class="space-y-6">

    <!-- HEADER -->
    <div class="relative overflow-hidden bg-gradient-to-r from-[#5b13ec] via-indigo-600 to-violet-700 rounded-2xl p-6 shadow-xl">
      <div class="absolute inset-0 opacity-10">
        <div class="absolute top-0 right-0 w-64 h-64 bg-white rounded-full -translate-y-1/2 translate-x-1/2"></div>
      </div>
      <div class="relative flex items-center justify-between">
        <div>
          <div class="flex items-center gap-2 mb-1">
            <div class="w-8 h-8 bg-white/20 rounded-xl flex items-center justify-center">
              <v-icon icon="mdi-trophy-variant" color="white" size="18"></v-icon>
            </div>
            <span class="text-white/70 text-xs font-bold uppercase tracking-widest">Gamification</span>
          </div>
          <h1 class="text-2xl font-black text-white">Configuration & Merits</h1>
          <p class="text-white/60 text-xs mt-0.5">Gerez les regles et recompensez vos developpeurs.</p>
        </div>

        <!-- TABS -->
        <div class="flex gap-1.5 bg-white/10 backdrop-blur-sm p-1.5 rounded-2xl border border-white/20">
          <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key"
                  :class="[
              'flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-black transition-all duration-200',
              activeTab === tab.key ? 'bg-white text-[#5b13ec] shadow-lg' : 'text-white/70 hover:text-white hover:bg-white/10'
            ]">
            <v-icon :icon="tab.icon" size="14"></v-icon>
            {{ tab.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- CONTENU -->
    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden min-h-[450px]">
      <transition name="fade" mode="out-in">
        <div :key="activeTab">
          <BadgeSection v-if="activeTab === 'badges'" />
          <TagSection v-else-if="activeTab === 'tags'" />
          <LevelSection v-else-if="activeTab === 'levels'" />
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
import ContributorSection from '@/components/gamification/dashboard/manager/ContributorSection.vue'

const tabs = [
  { key: 'badges',       label: 'Badges',       icon: 'mdi-medal' },
  { key: 'tags',         label: 'Tags',          icon: 'mdi-tag-multiple' },
  { key: 'levels',       label: 'Niveaux',       icon: 'mdi-chart-line' },
  { key: 'contributors', label: 'Contributeurs', icon: 'mdi-account-group' },
]
const activeTab = ref('badges')
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>