<template>
  <v-app class="bg-grey-lighten-4">

    <!-- ================= SIDEBAR ================= -->
    <v-navigation-drawer app permanent width="260" class="sidebar">

      <div class="pa-5 brand">
        <div class="text-h6 font-weight-bold text-primary">
          AdminCorp
        </div>
        <div class="text-caption text-grey-darken-1">
          Company Admin Panel
        </div>
      </div>

      <v-divider />

      <v-list nav density="comfortable" class="px-2">

        <v-list-item to="/company/dashboard" prepend-icon="mdi-view-dashboard" title="Dashboard" />
        <v-list-item to="/users" prepend-icon="mdi-account-group" title="Users" />
        <v-list-item to="/projects" prepend-icon="mdi-folder-multiple" title="Projects" />
        <v-list-item to="/tasks" prepend-icon="mdi-clipboard-check" title="Tasks" />

        <v-divider class="my-4" />

        <v-list-item to="/settings" prepend-icon="mdi-cog-outline" title="Settings" />
        <v-list-item prepend-icon="mdi-logout" title="Logout" class="text-red" />

      </v-list>

    </v-navigation-drawer>

    <!-- ================= TOP BAR ================= -->
    <v-app-bar flat class="appbar" height="70">

      <v-text-field
        density="compact"
        variant="outlined"
        placeholder="Search..."
        prepend-inner-icon="mdi-magnify"
        hide-details
        class="search"
      />

      <v-spacer />

      <v-btn icon variant="text">
        <v-icon>mdi-bell-outline</v-icon>
      </v-btn>

    </v-app-bar>

    <!-- ================= MAIN ================= -->
    <v-main>
      <v-container fluid class="pa-6">

        <!-- HEADER -->
        <div class="mb-6">
          <div class="text-h5 font-weight-bold">Company Dashboard</div>
          <div class="text-body-2 text-grey-darken-1">
            Overview of system data
          </div>
        </div>

        <!-- ================= STATS ================= -->
        <v-row dense>
          <v-col
            v-for="card in stats"
            :key="card.title"
            cols="12" sm="6" md="2"
          >
            <v-card class="pa-4 rounded-xl elevation-2">
              <v-icon color="primary">{{ card.icon }}</v-icon>

              <div class="text-h6 font-weight-bold mt-2">
                {{ card.value }}
              </div>

              <div class="text-caption text-grey">
                {{ card.title }}
              </div>
            </v-card>
          </v-col>
        </v-row>

        <!-- ================= TABLE ================= -->
        <v-card class="rounded-xl elevation-2 mt-6">
          <v-card-title>Tasks</v-card-title>

          <v-table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Status</th>
                <th>User</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="task in tasks" :key="task.id">
                <td>{{ task.id }}</td>

                <td>
                  <v-chip :color="getStatusColor(task.status)" size="small">
                    {{ task.status }}
                  </v-chip>
                </td>

                <td>{{ task.assignedTo ?? 'Unassigned' }}</td>
              </tr>
            </tbody>
          </v-table>

        </v-card>

      </v-container>
    </v-main>

  </v-app>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import taskService from "@/services/task.service"

const tasks = ref<any[]>([])
const stats = ref<any[]>([])

/* STATUS COLOR */
function getStatusColor(status: string) {
  switch (status) {
    case "DONE":
      return "green"
    case "IN_PROGRESS":
      return "blue"
    case "TODO":
      return "orange"
    default:
      return "grey"
  }
}

/* LOAD DATA */
onMounted(async () => {
  const res = await taskService.getAllTasks()
  tasks.value = res

  stats.value = [
    { title: "TOTAL", value: res.length, icon: "mdi-clipboard" },
    { title: "TODO", value: res.filter((t:any)=>t.status==="TODO").length, icon: "mdi-alert" },
    { title: "IN PROGRESS", value: res.filter((t:any)=>t.status==="IN_PROGRESS").length, icon: "mdi-progress-clock" },
    { title: "DONE", value: res.filter((t:any)=>t.status==="DONE").length, icon: "mdi-check" }
  ]
})
</script>

<style scoped>
.sidebar {
  border-right: 1px solid #eee;
}

.appbar {
  background: white !important;
  border-bottom: 1px solid #eee;
}

.search {
  max-width: 400px;
}
</style>