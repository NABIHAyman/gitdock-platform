<template>
  <div class="min-h-screen bg-gray-50 p-6">

    <!-- HEADER -->
    <div class="flex items-center justify-between mb-6">

      <div>
        <h1 class="text-2xl font-bold text-gray-900">
          Parties du projet
        </h1>
        <p class="text-sm text-gray-500">
          Organisation du projet en modules
        </p>
      </div>

      <div class="flex gap-3">

        <router-link
          :to="`/projects/${route.params.id}/stats`"
          class="px-4 py-2 bg-gray-200 rounded-lg hover:bg-gray-300"
        >
          ← Retour
        </router-link>

        <button
          @click="openModal"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          + Ajouter
        </button>

      </div>
    </div>

    <!-- EMPTY -->
    <div v-if="parts.length === 0" class="text-center text-gray-500 mt-10">
      Aucune partie créée
    </div>

    <!-- LIST -->
    <div v-for="part in parts" :key="part.id"
         class="bg-white shadow rounded-lg p-4 mb-4">

      <div class="flex justify-between items-start">

        <div>
          <h2 class="text-lg font-semibold text-gray-900">
            {{ part.name }}
          </h2>
          <p class="text-sm text-gray-500">
            {{ part.description }}
          </p>
        </div>

        <div class="flex gap-3 text-sm">

          <button
            @click="editPart(part)"
            class="text-blue-600 hover:underline"
          >
            Modifier
          </button>

          <button
            @click="deletePart(part.id)"
            class="text-red-600 hover:underline"
          >
            Supprimer
          </button>

        </div>

      </div>

      <!-- LEVELS -->
      <div class="mt-3 text-sm text-gray-600">

        <div v-if="part.levels.length > 0">
          <div v-for="lvl in part.levels" :key="lvl.id">
            • {{ lvl.name }}
          </div>
        </div>

        <div v-else class="text-gray-400 italic">
          Aucun niveau
        </div>

      </div>

    </div>

    <!-- MODAL -->
    <div v-if="showModal"
         class="fixed inset-0 bg-black/40 flex items-center justify-center">

      <div class="bg-white p-6 rounded-lg w-96">

        <h2 class="text-lg font-bold mb-4">
          {{ editing ? 'Modifier' : 'Créer' }} une partie
        </h2>

        <input
          v-model="form.name"
          placeholder="Nom"
          class="w-full border rounded p-2 mb-3"
        />

        <textarea
          v-model="form.description"
          placeholder="Description"
          class="w-full border rounded p-2 mb-3"
        />

        <div class="flex justify-end gap-2">

          <button
            @click="closeModal"
            class="px-3 py-1 bg-gray-200 rounded"
          >
            Annuler
          </button>

          <button
            @click="savePart"
            class="px-3 py-1 bg-blue-600 text-white rounded"
          >
            Save
          </button>

        </div>

      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

/* ================= FRONT DATA ================= */

const parts = ref([
  {
    id: 1,
    name: 'Accounting Module',
    description: 'Financial module',
    levels: [
      { id: 1, name: 'Beginner' },
      { id: 2, name: 'Advanced' }
    ]
  },
  {
    id: 2,
    name: 'Reporting Module',
    description: 'Reports system',
    levels: []
  }
])

/* ================= MODAL ================= */

const showModal = ref(false)
const editing = ref(null)

const form = ref({
  name: '',
  description: ''
})

/* ================= METHODS ================= */

const openModal = () => {
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editing.value = null
  form.value = { name: '', description: '' }
}

const savePart = () => {
  if (!form.value.name) return

  if (editing.value) {
    const p = parts.value.find(x => x.id === editing.value.id)
    p.name = form.value.name
    p.description = form.value.description
  } else {
    parts.value.push({
      id: Date.now(),
      name: form.value.name,
      description: form.value.description,
      levels: []
    })
  }

  closeModal()
}

const editPart = (part) => {
  editing.value = part
  form.value = { ...part }
  showModal.value = true
}

const deletePart = (id) => {
  parts.value = parts.value.filter(p => p.id !== id)
}
</script>