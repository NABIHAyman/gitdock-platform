<template>
  <TresPerspectiveCamera :position="[0, 8, 25]" :look-at="[0, 0, 0]" />
  
  <TresAmbientLight :intensity="1" />
  <TresDirectionalLight :position="[10, 20, 10]" :intensity="2" />
  <TresPointLight :position="[-10, -10, -10]" :intensity="0.5" color="#4f46e5" />

  <TresInstancedMesh ref="meshRef" :args="[null, null, MAX_PARTICLES]">
    <TresIcosahedronGeometry :args="[0.15, 1]" />
    <TresMeshPhysicalMaterial 
      :roughness="0.2" 
      :metalness="0.8" 
      :clearcoat="0.5"
      transparent
      :opacity="0.9"
    />
  </TresInstancedMesh>

  <TresGridHelper :args="[40, 40, '#1e293b', '#0f172a']" :position="[0, -5, 0]" />
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import * as THREE from 'three'
import { useLoop } from '@tresjs/core'

const props = defineProps<{
  latestAudit: any 
}>()

const MAX_PARTICLES = 1000 
const meshRef = ref<THREE.InstancedMesh | null>(null)

const COLOR_PENDING = new THREE.Color('#475569') 
const COLOR_CLEAN = new THREE.Color('#10b981')   
const COLOR_DANGER = new THREE.Color('#f43f5e')  

const dummy = new THREE.Object3D()
const colorDummy = new THREE.Color()

interface Particle {
  active: boolean
  id: string
  status: 'PENDING' | 'CLEAN' | 'DANGER'
  x: number
  y: number
  z: number
  speedY: number
}

const particles = Array.from({ length: MAX_PARTICLES }, () => ({
  active: false,
  id: '',
  status: 'PENDING',
  x: 0, y: 0, z: 0,
  speedY: 0
} as Particle))

let currentIndex = 0

// --- INITIALISATION DU GPU ---
onMounted(() => {
  // On attend un petit délai pour s'assurer que meshRef est lié au composant
  setTimeout(() => {
    if (meshRef.value) {
      for (let i = 0; i < MAX_PARTICLES; i++) {
        // Initialiser toutes les particules hors champ
        dummy.position.set(0, -100, 0)
        dummy.updateMatrix()
        meshRef.value.setMatrixAt(i, dummy.matrix)
        
        // Couleur par défaut
        colorDummy.copy(COLOR_PENDING)
        meshRef.value.setColorAt(i, colorDummy)
      }
      // Forcer le GPU à prendre en compte ces changements initiaux
      meshRef.value.instanceMatrix.needsUpdate = true
      if (meshRef.value.instanceColor) {
        meshRef.value.instanceColor.needsUpdate = true
      }
      console.log("✅ RadarScene: GPU InstancedMesh initialisé.")
    }
  }, 100) 
})

// L'injection des particules via le WebSocket
watch(() => props.latestAudit, (newAudit) => {
  if (!newAudit) return

  const p = particles[currentIndex]
  p.active = true
  p.id = newAudit.commit_hash
  p.status = newAudit.is_clean ? 'CLEAN' : 'DANGER'
  
  p.x = (Math.random() - 0.5) * 20 
  p.y = 10 + Math.random() * 2     
  p.z = (Math.random() - 0.5) * 5  
  p.speedY = 0.02 + Math.random() * 0.03 

  currentIndex = (currentIndex + 1) % MAX_PARTICLES
}, { deep: true })

// 💥 LA CORRECTION EST ICI : onBeforeRender avec l'objet déstructuré { delta }
const { onBeforeRender } = useLoop()

onBeforeRender(({ delta }) => {
  if (!meshRef.value) return

  for (let i = 0; i < MAX_PARTICLES; i++) {
    const p = particles[i]
    
    if (p.active) {
      // delta garantit une vitesse fluide peu importe le taux de rafraîchissement de l'écran
      p.y -= p.speedY * (delta * 60)
      
      if (p.y < -5) {
        p.active = false
        p.y = -100 
      }

      dummy.position.set(p.x, p.y, p.z)
      dummy.updateMatrix()
      meshRef.value.setMatrixAt(i, dummy.matrix)

      if (p.status === 'CLEAN') colorDummy.copy(COLOR_CLEAN)
      else if (p.status === 'DANGER') colorDummy.copy(COLOR_DANGER)
      else colorDummy.copy(COLOR_PENDING)
      
      meshRef.value.setColorAt(i, colorDummy)
    } else {
      dummy.position.set(0, -100, 0)
      dummy.updateMatrix()
      meshRef.value.setMatrixAt(i, dummy.matrix)
    }
  }
  
  meshRef.value.instanceMatrix.needsUpdate = true
  if (meshRef.value.instanceColor) {
    meshRef.value.instanceColor.needsUpdate = true
  }
})
</script>