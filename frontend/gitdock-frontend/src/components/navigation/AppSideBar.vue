<template>
  <aside class="w-64 bg-white shadow-sm border-r border-gray-200 min-h-[calc(100vh-73px)]">
    <nav class="p-4">
      <ul class="space-y-2">
        <li>
          <router-link
              to="/projects"
              class="flex items-center px-4 py-2 text-sm font-medium text-gray-700 rounded-md hover:bg-gray-100"
              active-class="bg-blue-50 text-blue-700"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
            Projets
          </router-link>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<script setup>
// Sidebar navigation component
</script>

