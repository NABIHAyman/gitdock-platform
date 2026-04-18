<script setup lang="ts">
defineProps<{
  badges: any[]
}>()
</script>

<template>
  <div class="bg-white border border-slate-200 rounded-[2rem] p-6 shadow-sm h-full">

    <div class="flex justify-between items-center mb-6">
      <div>
        <h3 class="text-xl font-black text-slate-900 tracking-tight">Achievements</h3>
        <p class="text-[9px] text-slate-400 font-bold uppercase tracking-widest italic">Milestones reached</p>
      </div>
      <v-btn variant="tonal" color="#5b13ec" density="comfortable" class="text-none font-bold rounded-lg px-4 text-[11px]">
        Gallery
      </v-btn>
    </div>

    <div v-if="badges.length === 0" class="flex flex-col items-center py-6 opacity-30">
      <v-icon icon="mdi-lock-outline" size="48"></v-icon>
      <p class="font-bold mt-2 uppercase text-[10px] tracking-widest">No badges yet</p>
    </div>

    <div v-else class="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-4">
      <div v-for="badge in badges" :key="badge.id"
           class="flex flex-col items-center group cursor-pointer relative">

        <v-tooltip activator="parent" location="top" class="custom-tooltip">
          {{ badge.description || 'Keep contributing to earn this!' }}
        </v-tooltip>

        <div :class="[badge.isUnlocked ? 'bg-white shadow-sm' : 'bg-slate-50 opacity-30 grayscale']"
             class="w-16 h-16 rounded-[1.5rem] border border-slate-100 flex items-center justify-center mb-2 transition-all duration-300 group-hover:-translate-y-1">

          <v-icon :icon="badge.icon || 'mdi-seal'" size="28"
                  :style="{ color: badge.isUnlocked ? badge.color : '#94a3b8' }"></v-icon>

          <div v-if="badge.isUnlocked" class="absolute -top-1 -right-1 bg-emerald-500 text-white rounded-full p-0.5 border-2 border-white">
            <v-icon icon="mdi-check" size="8"></v-icon>
          </div>
        </div>

        <p class="text-[9px] font-black text-slate-500 uppercase tracking-tighter text-center line-clamp-1 w-full px-1">
          {{ badge.name }}
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-tooltip :deep(.v-overlay__content) {
  background: #0f172a !important;
  border-radius: 8px !important;
  font-size: 9px !important;
  font-weight: 700 !important;
  text-transform: uppercase;
}
</style>