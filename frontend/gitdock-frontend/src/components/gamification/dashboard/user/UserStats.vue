<script setup lang="ts">
import { computed } from 'vue'

interface Stats {
  commits: number;
  reviews: number;
  bugs: number;
  streak: number;
}

const props = defineProps<{
  stats: Stats,
  role: string
}>()

const displayStats = computed(() => {
  const userRole = props.role ? props.role.toLowerCase() : 'user';

  const baseStats = [
    { label: 'Active Streak', value: `${props.stats.streak} Days`, icon: 'mdi-fire', color: 'text-amber-600', bg: 'bg-amber-50' }
  ]

  if (userRole.includes('engineer') || userRole.includes('dev') || userRole.includes('user')) {
    return [
      { label: 'Contributions', value: props.stats.commits, icon: 'mdi-git', color: 'text-blue-600', bg: 'bg-blue-50' },
      { label: 'Code Reviews', value: props.stats.reviews, icon: 'mdi-eye-check-outline', color: 'text-purple-600', bg: 'bg-purple-50' },
      ...baseStats
    ]
  }

  return [
    { label: 'Docs Written', value: 12, icon: 'mdi-file-document-outline', color: 'text-indigo-600', bg: 'bg-indigo-50' },
    ...baseStats
  ]
})
</script>

<template>
  <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
    <div v-for="stat in displayStats" :key="stat.label"
         class="bg-white border border-slate-200 rounded-[2rem] p-5 hover:shadow-md transition-all group">
      <div :class="[stat.bg, stat.color]" class="w-10 h-10 rounded-xl flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
        <v-icon :icon="stat.icon" size="20"></v-icon>
      </div>
      <p class="text-2xl font-black text-slate-900 leading-none">{{ stat.value }}</p>
      <p class="text-[9px] font-bold text-slate-400 uppercase tracking-widest mt-1 italic">{{ stat.label }}</p>
    </div>
  </div>
</template>