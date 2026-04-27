<script setup lang="ts">
import { ref, onMounted } from 'vue'
import UserHeader from '@/components/gamification/dashboard/user/UserHeader.vue'
import UserStats from '@/components/gamification/dashboard/user/UserStats.vue'
import UserBadges from '@/components/gamification/dashboard/user/UserBadges.vue'
import { useAuthStore } from '@/stores/authStore'
// Importe tes services ici
// import { userService } from '../Services/UserService'

const authStore = useAuthStore()

const loading = ref(true)
const userData = ref({
  fullName: `${authStore.firstName || ''} ${authStore.lastName || ''}`.trim() || 'Utilisateur',
  role: authStore.role?.replace('ROLE_', '').replace('_', ' ') || 'Utilisateur',
  level: 4,
  currentXP: 1450,
  nextLevelXP: 2000,
  stats: {
    commits: 248,
    reviews: 32,
    bugs: 12,
    streak: 5
  },
  achievements: [] // Liste des badges récupérés
})

onMounted(async () => {
  try {
    // const res = await userService.getProfile()
    // userData.value = res.data
    loading.value = false
  } catch (error) {
    console.error("Erreur profil", error)
  }
})
</script>

<template>
  <div class="min-h-screen bg-slate-50/30 p-8 space-y-8">
    <div v-if="loading" class="flex h-[60vh] items-center justify-center">
      <v-progress-circular indeterminate color="#5b13ec" size="64"></v-progress-circular>
    </div>

    <template v-else>
      <UserHeader
          :name="userData.fullName"
          :role="userData.role"
          :level="userData.level"
          :xp="userData.currentXP"
          :nextXp="userData.nextLevelXP"
      />

      <UserStats :stats="userData.stats" :role="userData.role" />

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2">
          <UserBadges :badges="userData.achievements" />
        </div>

        <div class="bg-slate-900 rounded-[2.5rem] p-8 text-white shadow-xl">
          <h3 class="text-xl font-bold mb-4">Next Milestone</h3>
          <div class="space-y-4">
            <div class="p-4 rounded-2xl bg-white/5 border border-white/10 flex items-center gap-3">
              <v-icon icon="mdi-target" color="amber"></v-icon>
              <p class="text-sm font-medium text-slate-300">Push 5 more commits to unlock "Git Pro"</p>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>