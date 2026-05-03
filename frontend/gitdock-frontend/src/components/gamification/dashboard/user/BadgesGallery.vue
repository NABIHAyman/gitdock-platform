<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { userBadgeService } from '@/services/UserBadgeService'
import { badgeService } from '@/services/BadgeService'

// Local interface to avoid Namespace conflict TS2709
interface GalleryBadge {
  id: string;
  title: string;
  description: string;
  icon: string;
  color: string;
  type: number;
  xp: number;
}

const authStore = useAuthStore()
const loading = ref<boolean>(true)

const achievements = ref<GalleryBadge[]>([])
const allBadges = ref<GalleryBadge[]>([])

// Fix TS2345: Ensure userId is always a string and never undefined
const userId = computed((): string => {
  const id = (authStore as any).userId
  return id ? String(id) : ""
})

const mapBadge = (data: any[]): GalleryBadge[] => {
  return (data || []).map(b => ({
    id: b.id || '',
    title: b.name || b.title || 'Badge',
    description: b.description || '',
    icon: b.imageUrl || b.icon || 'mdi-medal',
    color: b.color || '#5b13ec',
    type: typeof b.type === 'number' ? b.type : 1,
    xp: Number(b.xp) || 0
  }))
}

const loadGalleryData = async (): Promise<void> => {
  // Safety check to prevent passing undefined to services
  if (!userId.value) {
    loading.value = false
    return
  }

  loading.value = true
  try {
    const [userBadges, globalBadges] = await Promise.all([
      userBadgeService.getByUserId(userId.value), // Now receives string, not undefined
      badgeService.getAll()
    ])

    achievements.value = mapBadge(userBadges)
    allBadges.value = mapBadge(globalBadges)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(loadGalleryData)

// Logic to check if a badge is locked or not
const isUnlocked = (badgeId: string): boolean => {
  return achievements.value.some(a => a.id === badgeId)
}
</script>

<template>
  <div class="p-4">
    <div v-if="loading" class="flex justify-center">
      <v-progress-circular indeterminate color="#5b13ec"></v-progress-circular>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
      <v-card
          v-for="badge in allBadges"
          :key="badge.id"
          :class="{ 'opacity-50 grayscale': !isUnlocked(badge.id) }"
          class="rounded-xl border border-slate-200"
          elevation="0"
      >
        <v-card-text class="text-center p-6">
          <v-avatar size="80" :color="badge.color + '20'" class="mb-4">
            <v-icon :icon="badge.icon" :color="badge.color" size="40"></v-icon>
          </v-avatar>
          <div class="text-lg font-bold">{{ badge.title }}</div>
          <div class="text-xs text-slate-500 mt-2">{{ badge.description }}</div>
          <v-chip size="x-small" class="mt-4" :color="isUnlocked(badge.id) ? 'success' : 'slate'">
            {{ isUnlocked(badge.id) ? 'Unlocked' : 'Locked' }}
          </v-chip>
        </v-card-text>
      </v-card>
    </div>
  </div>
</template>