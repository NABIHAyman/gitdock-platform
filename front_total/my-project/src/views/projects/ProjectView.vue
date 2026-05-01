<template>
  <div class="min-h-screen bg-gray-50 text-gray-900 p-6 space-y-6">

    <!-- HEADER -->
    <div class="bg-white border rounded-xl p-5 flex flex-col md:flex-row md:items-center md:justify-between gap-4 shadow-sm">

      <div>
        <h1 class="text-2xl font-semibold">
          {{ currentView === 'projects' ? 'Gestion des projets' : 'Gestion des collaborateurs' }}
        </h1>
        <p class="text-sm text-gray-500">
          {{ currentView === 'projects'
            ? 'Vue centralisée des projets Git'
            : 'Vue des équipes et collaborateurs'
          }}
        </p>
      </div>

      <div class="flex gap-3">

        <!-- SWITCH -->
        <div class="flex bg-gray-100 p-1 rounded-lg">
          <button
            @click="currentView = 'projects'"
            class="px-3 py-1 text-sm rounded-md"
            :class="currentView === 'projects' ? 'bg-white shadow text-black' : 'text-gray-500'"
          >
            Projets
          </button>

          <button
            @click="currentView = 'collaborators'"
            class="px-3 py-1 text-sm rounded-md"
            :class="currentView === 'collaborators' ? 'bg-white shadow text-black' : 'text-gray-500'"
          >
            Collaborateurs
          </button>
        </div>

      </div>
    </div>

    <!-- ================= PROJECTS ================= -->
    <div v-if="currentView === 'projects'" class="space-y-4">

      <!-- ACTION BAR -->
      <div class="bg-white border rounded-xl p-4 flex justify-between items-center shadow-sm">

        <p class="text-sm text-gray-500">
          {{ projects.length }} projets
        </p>

        <div class="flex gap-2">
          <button class="px-4 py-2 text-sm bg-gray-100 rounded-lg hover:bg-gray-200">
            Trier
          </button>

          <button class="px-4 py-2 text-sm bg-indigo-600 text-white rounded-lg hover:bg-indigo-700">
            + Ajouter
          </button>
        </div>

      </div>

      <!-- TABLE -->
      <div class="bg-white border rounded-xl overflow-hidden shadow-sm">

        <table class="w-full text-sm">

          <thead class="bg-gray-50 text-gray-500">
            <tr>
              <th class="text-left p-3">Nom</th>
              <th class="text-left p-3">URL</th>
              <th class="text-left p-3">Plateforme</th>
              <th class="text-left p-3">Manager</th>
            </tr>
          </thead>

          <tbody>

            <tr
              v-for="p in projects"
              :key="p.id"
              class="border-t hover:bg-gray-50"
            >

              <td class="p-3 font-medium">
                {{ p.name }}
              </td>

              <td class="p-3 text-indigo-600">
                {{ getHostname(p.url) }}
              </td>

              <td class="p-3">
                <span class="px-2 py-1 text-xs bg-gray-100 rounded-full">
                  {{ p.platform }}
                </span>
              </td>

              <td class="p-3 text-gray-500">
                {{ p.manager ? p.manager.firstName : '-' }}
              </td>

            </tr>

          </tbody>

        </table>

      </div>
    </div>

    <!-- ================= COLLABORATORS ================= -->
    <div v-if="currentView === 'collaborators'" class="space-y-6">

      <div
        v-for="group in collaborators"
        :key="group.projectId"
        class="bg-white border rounded-xl shadow-sm overflow-hidden"
      >

        <!-- HEADER GROUP -->
        <div class="p-4 border-b flex justify-between items-center">
          <h3 class="font-semibold">
            {{ group.projectName }}
          </h3>

          <span class="text-xs bg-gray-100 px-3 py-1 rounded-full">
            {{ group.collaborators.length }} membres
          </span>
        </div>

        <!-- TABLE -->
        <table class="w-full text-sm">

          <thead class="bg-gray-50 text-gray-500">
            <tr>
              <th class="p-3 text-left">Nom</th>
              <th class="p-3 text-left">Email</th>
              <th class="p-3 text-left">Rôle</th>
              <th class="p-3 text-left">Status</th>
            </tr>
          </thead>

          <tbody>

            <tr
              v-for="c in group.collaborators"
              :key="c.id"
              class="border-t hover:bg-gray-50"
            >

              <td class="p-3 font-medium">
                {{ c.firstName }} {{ c.lastName }}
              </td>

              <td class="p-3 text-gray-500">
                {{ c.email }}
              </td>

              <td class="p-3">
                <span class="px-2 py-1 text-xs bg-indigo-100 text-indigo-700 rounded-full">
                  {{ c.role }}
                </span>
              </td>

              <td class="p-3">
                <span
                  class="px-2 py-1 text-xs rounded-full"
                  :class="c.status === 'active'
                    ? 'bg-green-100 text-green-700'
                    : 'bg-yellow-100 text-yellow-700'"
                >
                  {{ c.status }}
                </span>
              </td>

            </tr>

          </tbody>

        </table>

      </div>

    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'

const currentView = ref('projects')

const projects = ref([
  {
    id: 1,
    name: 'GitDock',
    url: 'https://github.com/test/gitdock',
    platform: 'github',
    manager: { firstName: 'Maryam' }
  },
  {
    id: 2,
    name: 'HR System',
    url: 'https://gitlab.com/test/hr',
    platform: 'gitlab',
    manager: null
  }
])

const collaborators = ref([
  {
    projectId: 1,
    projectName: 'GitDock',
    collaborators: [
      {
        id: 1,
        firstName: 'Ali',
        lastName: 'Ahmed',
        email: 'ali@test.com',
        role: 'ADMIN',
        status: 'active'
      }
    ]
  }
])

const getHostname = (url) => {
  try {
    return new URL(url).hostname.replace('www.', '')
  } catch {
    return url
  }
}
</script>