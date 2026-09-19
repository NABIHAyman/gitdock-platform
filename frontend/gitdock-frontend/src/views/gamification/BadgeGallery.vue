<script setup>
import { onMounted } from 'vue';
import { useBadgeStore } from '@/stores/badgeStore';

// 1. On initialise le store
const badgeStore = useBadgeStore();

// 2. On demande au store de charger les badges DEPUIS LE DÉBUT
onMounted(async () => {
  // Cette ligne va appeler ton service -> gateway -> microservice -> DB
  await badgeStore.fetchBadges();
});

// Ta fonction pour enregistrer un nouveau badge
const saveBadge = async (newBadgeData) => {
  try {
    await badgeStore.addBadge(newBadgeData);
    // Ici, le store fait un .push() automatique,
    // donc le nouveau badge apparaît sans recharger toute la liste.
  } catch (error) {
    console.error("Erreur d'enregistrement", error);
  }
};
</script>

<template>
  <div>
    <!-- On utilise badgeStore.badges pour l'affichage -->
    <div v-if="badgeStore.loading">Chargement...</div>

    <div v-else class="grid">
      <div v-for="badge in badgeStore.badges" :key="badge.id">
        {{ badge.title }}
      </div>
    </div>
  </div>
</template>