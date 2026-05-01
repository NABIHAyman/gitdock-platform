<template>
  <div class="p-6">

    <!-- HEADER -->
    <div class="mb-6">
      <router-link :to="`/projects/${route.params.id}/branches`" class="text-blue-600">
        ← Retour aux branches
      </router-link>

      <h1 class="text-xl font-bold mt-3">
        Historique des Commits
      </h1>

      <p class="text-gray-600">
        Projet: {{ projectName }}
      </p>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="text-center py-10">
      ⟳ Chargement...
    </div>

    <!-- EMPTY -->
    <div v-else-if="commits.length === 0" class="text-center py-10 text-gray-500">
      Aucun commit trouvé
    </div>

    <!-- TABLE -->
    <div v-else>
      <table class="w-full border border-gray-200">

        <thead class="bg-gray-100">
          <tr>
            <th class="p-2">Auteur</th>
            <th class="p-2">Message</th>
            <th class="p-2">Date</th>
            <th class="p-2">SHA</th>
            <th class="p-2">Stats</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="c in commits" :key="c.id" class="border-t">

            <td class="p-2">
              {{ c.authorName }}
            </td>

            <td class="p-2">
              {{ c.message }}
            </td>

            <td class="p-2 text-gray-600">
              {{ formatDate(c.date) }}
            </td>

            <td class="p-2">
              {{ c.hash?.substring(0, 8) }}
            </td>

            <td class="p-2">
              <span v-if="c.additions">+{{ c.additions }}</span>
              <span v-if="c.deletions"> -{{ c.deletions }}</span>
            </td>

          </tr>
        </tbody>

      </table>
    </div>

    <!-- LOAD MORE -->
    <div class="text-center mt-5" v-if="hasMore">
      <button @click="loadMore" :disabled="loadingMore">
        {{ loadingMore ? 'Chargement...' : 'Charger plus' }}
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()

const commits = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(false)
const page = ref(0)
const projectName = ref('')

const fetchCommits = async (reset = false) => {
  const projectId = route.params.id
  const branchId = route.params.branchId

  if (reset) {
    commits.value = []
    page.value = 0
  }

  loading.value = true

  try {
    const res = await axios.get(
      `http://localhost:8080/api/projects/${projectId}/branches/${branchId}/commits?page=${page.value}`
    )

    commits.value.push(...(res.data.content || []))
    hasMore.value = !res.data.last
    projectName.value = res.data.projectName || ''

  } catch (e) {
    console.log(e)
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  loadingMore.value = true
  page.value++
  await fetchCommits()
  loadingMore.value = false
}

const formatDate = (d) => {
  if (!d) return ''
  return new Date(d).toLocaleString()
}

onMounted(() => {
  fetchCommits(true)
})
</script>