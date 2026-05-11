<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-indigo-50/30">

    <!-- ═══════════════════════ MANAGER VIEW ═══════════════════════ -->
    <div v-if="isManager" class="space-y-6">

      <!-- MANAGER HEADER -->
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
            <h1 class="text-2xl font-black text-white">Settings & Merits</h1>
            <p class="text-white/60 text-xs mt-0.5">Manage rules and reward your developers.</p>
          </div>
          <!-- MANAGER TABS -->
          <div class="flex gap-1.5 bg-white/10 backdrop-blur-sm p-1.5 rounded-2xl border border-white/20">
            <button v-for="tab in managerTabs" :key="tab.key" @click="activeTab = tab.key"
                    :class="['flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-black transition-all duration-200',
                        activeTab === tab.key ? 'bg-white text-[#5b13ec] shadow-lg' : 'text-white/70 hover:text-white hover:bg-white/10']">
              <v-icon :icon="tab.icon" size="14"></v-icon>
              {{ tab.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- MANAGER CONTENT -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden min-h-[450px]">
        <transition name="fade" mode="out-in">
          <div :key="activeTab">
            <BadgeSection       v-if="activeTab === 'badges'" />
            <TagSection         v-else-if="activeTab === 'tags'" />
            <LevelSection       v-else-if="activeTab === 'levels'" />
            <ContributorSection v-else-if="activeTab === 'contributors'" />
          </div>
        </transition>
      </div>
    </div>

    <!-- ═══════════════════════ DEVELOPER VIEW ═══════════════════════ -->
    <div v-else class="space-y-6">

      <!-- DEV HEADER -->
      <div class="relative overflow-hidden bg-gradient-to-r from-[#5b13ec] via-indigo-600 to-violet-700 rounded-2xl p-6 shadow-xl">
        <div class="absolute inset-0 opacity-10">
          <div class="absolute top-0 right-0 w-64 h-64 bg-white rounded-full -translate-y-1/2 translate-x-1/2"></div>
        </div>
        <div class="relative">
          <!-- Row 1: Title + Quick Stats -->
          <div class="flex items-start justify-between mb-5">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <div class="w-8 h-8 bg-white/20 rounded-xl flex items-center justify-center">
                  <v-icon icon="mdi-shield-star" color="white" size="18"></v-icon>
                </div>
                <span class="text-white/70 text-xs font-bold uppercase tracking-widest">Gamification</span>
              </div>
              <h1 class="text-2xl font-black text-white">My Merits</h1>
              <p class="text-white/60 text-xs mt-0.5">Your achievements and progression.</p>
            </div>

            <!-- XP / Level / Badges Stats -->
            <div v-if="userProgress" class="flex gap-3">
              <div class="bg-white/10 backdrop-blur-sm rounded-2xl px-5 py-3 text-center border border-white/20">
                <p class="text-xl font-black text-white">{{ userProgress.totalExperience }}</p>
                <p class="text-white/60 text-[10px] font-bold uppercase">Total XP</p>
              </div>
              <div class="bg-white/10 backdrop-blur-sm rounded-2xl px-5 py-3 text-center border border-white/20">
                <p class="text-xl font-black text-white">Lv{{ userProgress.currentLevel }}</p>
                <p class="text-white/60 text-[10px] font-bold uppercase">{{ userProgress.levelName }}</p>
              </div>
              <div class="bg-white/10 backdrop-blur-sm rounded-2xl px-5 py-3 text-center border border-white/20">
                <p class="text-xl font-black text-white">{{ myBadgesCount }} / {{ allBadges.length }}</p>
                <p class="text-white/60 text-[10px] font-bold uppercase">Badges</p>
              </div>
            </div>
          </div>

          <!-- Row 2: XP Progress Bar -->
          <div v-if="userProgress" class="mb-5">
            <div class="flex justify-between text-white/60 text-[10px] font-bold mb-1.5">
              <span>Progress to Lv{{ userProgress.currentLevel + 1 }}</span>
              <span>{{ userProgress.totalExperience }} / {{ userProgress.nextLevelXp }} XP</span>
            </div>
            <div class="w-full bg-white/20 rounded-full h-2.5">
              <div class="bg-white h-full rounded-full transition-all duration-1000"
                   :style="{ width: xpProgressPercent + '%' }"></div>
            </div>
            <p class="text-white/40 text-[10px] mt-1">
              {{ xpToNextLevel }} XP remaining to reach {{ userProgress.nextLevelName || 'the next level' }}
            </p>
          </div>

          <!-- DEV TABS -->
          <div class="flex gap-1.5 bg-white/10 backdrop-blur-sm p-1.5 rounded-2xl w-fit border border-white/20">
            <button v-for="tab in devTabs" :key="tab.key" @click="activeDevTab = tab.key"
                    :class="['flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-black transition-all duration-200',
                        activeDevTab === tab.key ? 'bg-white text-[#5b13ec] shadow-lg' : 'text-white/70 hover:text-white hover:bg-white/10']">
              <v-icon :icon="tab.icon" size="14"></v-icon>
              {{ tab.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- DEV CONTENT -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden min-h-[450px]">
        <transition name="fade" mode="out-in">
          <div :key="activeDevTab">
            <DevBadgeView
                v-if="activeDevTab === 'badges'"
                :userProgress="userProgress"
                :myBadges="myBadges"
                :allBadges="allBadges"
                :loading="loading"
            />
            <DevLeaderboardView
                v-else-if="activeDevTab === 'leaderboard'"
                :current-user-id="currentUserId"
            />
          </div>
        </transition>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import { useRole } from '@/composables/useRole'

// Manager sections
import BadgeSection       from '@/components/gamification/dashboard/manager/BadgeSection.vue'
import TagSection         from '@/components/gamification/dashboard/manager/TagSection.vue'
import LevelSection       from '@/components/gamification/dashboard/manager/LevelSection.vue'
import ContributorSection from '@/components/gamification/dashboard/manager/ContributorSection.vue'

// Dev sections
import DevBadgeView       from '@/components/gamification/dashboard/dev/DevBadgeView.vue'
import DevLeaderboardView from '@/components/gamification/dashboard/dev/DevLeaderboardView.vue'

// Services
import { userProgressService } from '@/services/UserProgressService'
import { userBadgeService }    from '@/services/UserBadgeService'

const { isManager, currentUserId } = useRole()

// --- TABS ---
const managerTabs = [
  { key: 'badges',       label: 'Badges',       icon: 'mdi-medal' },
  { key: 'tags',         label: 'Tags',          icon: 'mdi-tag-multiple' },
  { key: 'levels',       label: 'Levels',       icon: 'mdi-chart-line' },
  { key: 'contributors', label: 'Contributors', icon: 'mdi-account-group' },
]
const devTabs = [
  { key: 'badges',      label: 'My Badges',    icon: 'mdi-medal' },
  { key: 'leaderboard', label: 'Leaderboard',  icon: 'mdi-podium' },
]

const activeTab    = ref('badges')
const activeDevTab = ref('badges')

// --- DEV DATA ---
const loading      = ref(false)
const userProgress = ref<any>(null)
const myBadges     = ref<any[]>([])
const allBadges    = ref<any[]>([])

// --- COMPUTED XP ---
const myBadgesCount = computed(() => myBadges.value.length)

const xpProgressPercent = computed(() => {
  if (!userProgress.value) return 0
  const { totalExperience, nextLevelXp, prevLevelXp = 0 } = userProgress.value
  const range = nextLevelXp - prevLevelXp
  const current = totalExperience - prevLevelXp
  if (range <= 0) return 100
  return Math.min(Math.round((current / range) * 100), 100)
})

const xpToNextLevel = computed(() => {
  if (!userProgress.value) return 0
  return Math.max(0, userProgress.value.nextLevelXp - userProgress.value.totalExperience)
})

// --- MOUNT ---
onMounted(async () => {
  if (!isManager.value) {
    loading.value = true
    try {
      const [progress, myB, allB] = await Promise.all([
        userProgressService.getProgress(currentUserId.value!),
        userBadgeService.getMyBadges(),
        userBadgeService.getAllAvailableBadges(),
      ])
      userProgress.value = progress
      myBadges.value     = myB
      allBadges.value    = allB
    } catch (err) {
      console.error('[GamificationDashboard] Error loading dev data:', err)
    } finally {
      loading.value = false
    }
  }
})
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s ease; }
.fade-enter-from, .fade-leave-to       { opacity: 0; }
</style>