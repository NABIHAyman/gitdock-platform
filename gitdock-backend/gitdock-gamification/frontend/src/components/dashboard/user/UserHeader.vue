<script setup lang="ts">
defineProps<{
  name: string,
  role: string,
  level: number,
  xp: number,
  nextXp: number
}>()
</script>

<template>
  <div class="bg-white border border-slate-200 rounded-[2rem] p-5 shadow-sm flex flex-col md:flex-row items-center gap-6 relative overflow-hidden">

    <div class="absolute -left-10 -top-10 w-32 h-32 bg-amber-100/30 rounded-full blur-3xl"></div>

    <div class="relative shrink-0 z-10">
      <v-avatar size="70" class="border-4 border-slate-50 shadow-sm">
        <v-img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Rihab"></v-img>
      </v-avatar>
      <div class="absolute bottom-0 right-0 w-3.5 h-3.5 bg-amber-400 border-2 border-white rounded-full shadow-sm"></div>
    </div>

    <div class="flex-1 w-full space-y-4 z-10">
      <div class="flex flex-col md:flex-row md:items-end justify-between gap-2">
        <div>
          <h2 class="text-xl font-black text-slate-900 tracking-tight leading-tight">{{ name }}</h2>

          <div class="flex items-center gap-1.5 mt-0.5">
            <v-icon icon="mdi-star-four-points" size="18" color="amber-darken-1"></v-icon>
            <p class="text-[11px] font-black text-[#5b13ec] uppercase tracking-widest">
              {{ role }}
            </p>
          </div>
        </div>

        <div class="text-right">
          <span class="text-sm font-black text-slate-900">{{ xp }}</span>
          <span class="text-[10px] font-bold text-slate-400 uppercase ml-1">/ {{ nextXp }} XP</span>
        </div>
      </div>

      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-xl bg-slate-900 text-white flex flex-col items-center justify-center shrink-0 shadow-lg border-t border-amber-400/50">
          <p class="text-[7px] font-black uppercase text-slate-500 leading-none">Lvl</p>
          <p class="text-lg font-black leading-none text-amber-400">{{ level }}</p>
        </div>

        <div class="flex-1">
          <v-progress-linear
              :model-value="(xp / nextXp) * 100"
              color="#5b13ec"
              height="8"
              rounded="xl"
              bg-color="slate-100"
              bg-opacity="1"
          ></v-progress-linear>

          <div class="flex justify-between mt-1">
            <p class="text-[8px] font-black text-slate-300 uppercase tracking-tighter">Progression</p>
            <p class="text-[8px] font-black text-amber-600 uppercase tracking-tighter italic">
              {{ Math.round((xp / nextXp) * 100) }}% to next level
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Pour une transition fluide de la barre */
:deep(.v-progress-linear__determinate) {
  transition: width 1s ease-in-out;
}
</style>