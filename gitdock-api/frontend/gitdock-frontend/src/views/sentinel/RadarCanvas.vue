<template>
  <div class="w-full h-full relative bg-[#050510] overflow-hidden rounded-xl border border-slate-800">
    <div class="absolute inset-0 pointer-events-none z-10 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-transparent via-[#050510]/80 to-[#050510]"></div>
    
    <TresCanvas clear-color="#050510" window-size>
      <RadarScene :latest-audit="latestAudit" />
    </TresCanvas>
  </div>
</template>

<script setup lang="ts">
import { TresCanvas } from '@tresjs/core'
import RadarScene from './RadarScene.vue'

defineProps<{
  latestAudit: any 
}>()
</script>