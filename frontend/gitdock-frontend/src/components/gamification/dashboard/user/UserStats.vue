<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  stats: {
    commits: number,
    reviews: number,
    bugs: number,
    streak: number
  },
  role: string
}>()

// Logique pour adapter les cartes au rôle
const displayStats = computed(() => {
  const baseStats = [
    {
      label: 'Active Streak',
      value: `${props.stats.streak} Days`,
      icon: 'mdi-fire',
      color: 'text-amber-600',
      bg: 'bg-amber-50'
    }
  ]

  if (props.role.toLowerCase().includes('engineer') || props.role.toLowerCase().includes('dev')) {
    return [
      { label: 'Contributions', value: props.stats.commits, icon: 'mdi-git', color: 'text-blue-600', bg: 'bg-blue-50' },
      { label: 'Code Reviews', value: props.stats.reviews, icon: 'mdi-eye-check-outline', color: 'text-purple-600', bg: 'bg-purple-50' },
      ...baseStats
    ]
  } else if (props.role.toLowerCase().includes('tester') || props.role.toLowerCase().includes('qa')) {
    return [
      { label: 'Bugs Reported', value: props.stats.bugs, icon: 'mdi-bug-outline', color: 'text-rose-600', bg: 'bg-rose-50' },
      { label: 'Tests Validated', value: 42, icon: 'mdi-clipboard-check-outline', color: 'text-emerald-600', bg: 'bg-emerald-50' },
      ...baseStats
    ]
  }

  // Par défaut (Consultant ou autre)
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