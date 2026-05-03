<template>
  <div class="min-h-screen bg-surface text-text p-6">

    <!-- ================= HEADER ================= -->
    <div class="flex items-center justify-between mb-6">

      <div>
        <h1 class="text-2xl font-semibold">Utilisateurs du projet</h1>
        <p class="text-sm text-muted">
          Gestion simple des collaborateurs
        </p>
      </div>

      <div class="flex gap-3">
        <button class="gd-btn-secondary">Retour</button>
        <button class="gd-btn-primary">Ajouter utilisateur</button>
      </div>

    </div>

    <!-- ================= CARD ================= -->
    <div class="gd-card overflow-hidden">

      <table class="gd-table w-full">

        <thead>
          <tr>
            <th>Nom</th>
            <th>Email</th>
            <th>Rôle</th>
            <th>Status</th>
            <th class="text-right">Actions</th>
          </tr>
        </thead>

        <tbody>

          <tr
            v-for="u in users"
            :key="u.id"
            class="gd-tr hover:bg-surface/50 transition"
          >

            <!-- NAME -->
            <td class="font-medium px-6 py-4">
              {{ u.firstName }} {{ u.lastName }}
            </td>

            <!-- EMAIL -->
            <td class="text-muted px-6 py-4">
              {{ u.email }}
            </td>

            <!-- ROLE -->
            <td class="px-6 py-4">
              <span class="gd-badge-accent">
                {{ u.role }}
              </span>
            </td>

            <!-- STATUS -->
            <td class="px-6 py-4">
              <span
                :class="[
                  'gd-badge',
                  u.status === 'active'
                    ? 'gd-badge-success'
                    : u.status === 'pending'
                    ? 'gd-badge-warning'
                    : 'gd-badge-danger'
                ]"
              >
                {{ u.status }}
              </span>
            </td>

            <!-- ACTIONS -->
            <td class="px-6 py-4 text-right">

              <div class="flex justify-end gap-2">

                <button class="gd-btn-ghost text-accent">
                  Edit
                </button>

                <button class="gd-btn-ghost text-amber-600">
                  Disable
                </button>

                <button class="gd-btn-ghost text-rose-600">
                  Delete
                </button>

              </div>

            </td>

          </tr>

        </tbody>

      </table>

    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'

const users = ref([
  {
    id: 1,
    firstName: 'Ali',
    lastName: 'Ahmed',
    email: 'ali@test.com',
    role: 'ADMIN',
    status: 'active'
  },
  {
    id: 2,
    firstName: 'Sara',
    lastName: 'Ben',
    email: 'sara@test.com',
    role: 'DEVELOPER',
    status: 'pending'
  },
  {
    id: 3,
    firstName: 'Omar',
    lastName: 'El',
    email: 'omar@test.com',
    role: 'MANAGER',
    status: 'disabled'
  }
])
</script>