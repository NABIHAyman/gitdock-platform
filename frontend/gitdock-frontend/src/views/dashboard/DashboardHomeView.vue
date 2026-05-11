<template>
  <div>

    <!-- ═══════════════════════════════════════════════════════════
         MANAGER / OWNER / ADMIN VIEW (unchanged dashboard)
    ════════════════════════════════════════════════════════════ -->
    <div v-if="isManager" class="space-y-8">

      <!-- HEADER -->
      <div class="relative overflow-hidden bg-gradient-to-r from-[#5b13ec] via-indigo-600 to-violet-700 rounded-3xl p-8 shadow-2xl">
        <div class="absolute inset-0 opacity-10">
          <div class="absolute top-0 right-0 w-96 h-96 bg-white rounded-full -translate-y-1/2 translate-x-1/2"></div>
          <div class="absolute bottom-0 left-0 w-64 h-64 bg-white rounded-full translate-y-1/2 -translate-x-1/2"></div>
        </div>
        <div class="relative">
          <div class="flex items-center gap-3 mb-2">
            <div class="w-10 h-10 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center">
              <v-icon icon="mdi-view-dashboard" color="white" size="22"></v-icon>
            </div>
            <span class="text-white/70 text-sm font-bold uppercase tracking-widest">GitDock</span>
          </div>
          <h1 class="text-3xl font-black text-white mb-1">Hello, {{ authStore.firstName }} 👋</h1>
          <p class="text-white/60 text-sm">Overview of your projects and security analytics.</p>
        </div>
      </div>

      <!-- STATS -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-5">
          <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 bg-indigo-50 rounded-xl flex items-center justify-center">
              <v-icon icon="mdi-folder-multiple" color="#5b13ec" size="18"></v-icon>
            </div>
            <span class="text-xs font-black text-slate-400 uppercase tracking-wider">Projects</span>
          </div>
          <p class="text-3xl font-black text-slate-800">{{ stats.totalProjects }}</p>
          <p class="text-xs text-slate-400 mt-1">Total registered</p>
        </div>
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-5">
          <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 bg-emerald-50 rounded-xl flex items-center justify-center">
              <v-icon icon="mdi-shield-check" color="#10b981" size="18"></v-icon>
            </div>
            <span class="text-xs font-black text-slate-400 uppercase tracking-wider">Healthy</span>
          </div>
          <p class="text-3xl font-black text-emerald-600">{{ stats.healthyProjects }}</p>
          <p class="text-xs text-slate-400 mt-1">No anomalies</p>
        </div>
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-5">
          <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 bg-red-50 rounded-xl flex items-center justify-center">
              <v-icon icon="mdi-alert-circle" color="#ef4444" size="18"></v-icon>
            </div>
            <span class="text-xs font-black text-slate-400 uppercase tracking-wider">Anomalies</span>
          </div>
          <p class="text-3xl font-black text-red-600">{{ stats.totalAnomalies }}</p>
          <p class="text-xs text-slate-400 mt-1">Detected total</p>
        </div>
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-5">
          <div class="flex items-center gap-3 mb-3">
            <div class="w-9 h-9 bg-amber-50 rounded-xl flex items-center justify-center">
              <v-icon icon="mdi-source-commit" color="#f59e0b" size="18"></v-icon>
            </div>
            <span class="text-xs font-black text-slate-400 uppercase tracking-wider">Commits</span>
          </div>
          <p class="text-3xl font-black text-slate-800">{{ stats.totalCommits }}</p>
          <p class="text-xs text-slate-400 mt-1">Total analyzed</p>
        </div>
      </div>

      <!-- PROJECTS + AI -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

        <!-- MY PROJECTS -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <div class="w-7 h-7 bg-[#5b13ec]/10 rounded-lg flex items-center justify-center">
                <v-icon icon="mdi-folder-multiple" color="#5b13ec" size="16"></v-icon>
              </div>
              <h3 class="font-black text-slate-800 text-sm">My Projects</h3>
            </div>
            <router-link to="/projects" class="text-xs font-bold text-[#5b13ec] hover:underline">View all →</router-link>
          </div>
          <div class="divide-y divide-slate-50">
            <div v-if="projects.length === 0" class="p-8 text-center">
              <v-icon icon="mdi-folder-open-outline" color="#cbd5e1" size="36" class="mb-2"></v-icon>
              <p class="text-sm text-slate-400 font-bold">No projects yet</p>
            </div>
            <div v-for="project in projects.slice(0, 5)" :key="project.id"
                 @click="goToProject(project.id)"
                 class="flex items-center gap-3 px-6 py-3 hover:bg-slate-50 cursor-pointer transition-colors">
              <div class="w-8 h-8 bg-[#5b13ec]/10 rounded-lg flex items-center justify-center flex-shrink-0">
                <v-icon icon="mdi-folder" color="#5b13ec" size="16"></v-icon>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-black text-slate-700 truncate">{{ project.name }}</p>
                <p class="text-xs text-slate-400">{{ getHostname(project.url) }}</p>
              </div>
              <span class="text-[10px] font-black px-2 py-0.5 rounded-full bg-slate-100 text-slate-600">
                {{ (project.platform || '').toUpperCase() }}
              </span>
            </div>
          </div>
        </div>

        <!-- GITDOCK AI -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b border-slate-100 flex items-center gap-2">
            <div class="w-7 h-7 bg-[#5b13ec]/10 rounded-lg flex items-center justify-center">
              <v-icon icon="mdi-brain" color="#5b13ec" size="16"></v-icon>
            </div>
            <h3 class="font-black text-slate-800 text-sm">GitDock AI — Analysis</h3>
            <span class="ml-auto bg-[#5b13ec]/10 text-[#5b13ec] text-[10px] font-black px-2 py-0.5 rounded-full">SECURITY</span>
          </div>
          <div class="p-6 space-y-4">
            <div class="flex gap-2">
              <div class="flex-1 relative">
                <v-icon icon="mdi-magnify" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size="16"></v-icon>
                <input v-model="aiProjectName" @keyup.enter="analyzeProject" placeholder="Project name..."
                       class="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:border-[#5b13ec]/50 focus:bg-white transition-all"/>
              </div>
              <button @click="analyzeProject" :disabled="aiLoading || !aiProjectName.trim()"
                      class="px-4 py-2.5 bg-[#5b13ec] text-white rounded-xl font-black text-xs hover:bg-[#4a0fd4] disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1.5 shadow-md transition-all">
                <v-progress-circular v-if="aiLoading" indeterminate size="12" width="2" color="white"></v-progress-circular>
                <v-icon v-else icon="mdi-lightning-bolt" size="14"></v-icon>
                {{ aiLoading ? '...' : 'Analyze' }}
              </button>
            </div>

            <!-- SHORTCUTS -->
            <div v-if="projects.length > 0" class="flex flex-wrap gap-1.5">
              <button v-for="p in projects.slice(0, 4)" :key="p.id"
                      @click="aiProjectName = p.name; analyzeProject()"
                      class="text-[10px] font-black px-2.5 py-1 bg-slate-100 hover:bg-[#5b13ec]/10 hover:text-[#5b13ec] text-slate-500 rounded-full transition-all">
                {{ p.name }}
              </button>
            </div>

            <!-- ERROR -->
            <div v-if="aiError" class="bg-red-50 border border-red-100 rounded-xl p-3 flex items-center gap-2">
              <v-icon icon="mdi-alert-circle" color="red" size="16"></v-icon>
              <p class="text-red-600 text-xs font-bold">{{ aiError }}</p>
            </div>

            <!-- RESULTS -->
            <div v-if="aiResult" class="space-y-3">
              <div class="grid grid-cols-3 gap-2">
                <div class="bg-slate-50 rounded-xl p-3 border border-slate-100 text-center">
                  <p class="text-xl font-black text-slate-800">{{ aiResult.total }}</p>
                  <p class="text-[10px] text-slate-400 font-bold uppercase">Commits</p>
                </div>
                <div :class="['rounded-xl p-3 border text-center', aiResult.anomalies > 0 ? 'bg-red-50 border-red-100' : 'bg-emerald-50 border-emerald-100']">
                  <p :class="['text-xl font-black', aiResult.anomalies > 0 ? 'text-red-600' : 'text-emerald-600']">{{ aiResult.anomalies }}</p>
                  <p class="text-[10px] text-slate-400 font-bold uppercase">Anomalies</p>
                </div>
                <div :class="['rounded-xl p-3 border text-center', aiResult.anomalies === 0 ? 'bg-emerald-50 border-emerald-100' : 'bg-amber-50 border-amber-100']">
                  <p :class="['text-lg font-black', aiResult.anomalies === 0 ? 'text-emerald-600' : 'text-amber-600']">{{ aiResult.anomalies === 0 ? '✓' : '⚠' }}</p>
                  <p class="text-[10px] text-slate-400 font-bold uppercase">Status</p>
                </div>
              </div>
              <div v-if="aiResult.details?.length" class="space-y-1.5 max-h-40 overflow-y-auto">
                <div v-for="a in aiResult.details" :key="a.sha" class="bg-red-50 border border-red-100 rounded-xl p-2.5 flex items-start gap-2">
                  <v-icon icon="mdi-bug" color="red" size="14" class="mt-0.5 flex-shrink-0"></v-icon>
                  <div class="min-w-0 flex-1">
                    <p class="text-xs font-black text-red-800 truncate">{{ a.message }}</p>
                    <p class="text-[10px] text-red-400 font-mono">{{ a.sha?.slice(0, 8) }} · {{ a.reason }}</p>
                  </div>
                </div>
              </div>
              <div v-else class="bg-emerald-50 border border-emerald-100 rounded-xl p-3 text-center">
                <p class="text-emerald-700 font-black text-sm">✓ Healthy project — no anomalies</p>
              </div>
            </div>

            <!-- EMPTY -->
            <div v-if="!aiResult && !aiLoading && !aiError" class="text-center py-6">
              <div class="w-12 h-12 bg-[#5b13ec]/10 rounded-2xl flex items-center justify-center mx-auto mb-2">
                <v-icon icon="mdi-shield-search" color="#5b13ec" size="22"></v-icon>
              </div>
              <p class="text-xs font-black text-slate-400">Select a project to start AI analysis</p>
            </div>
          </div>
        </div>
      </div>

      <!-- GLOBAL REPORT -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 bg-slate-900 rounded-lg flex items-center justify-center">
              <v-icon icon="mdi-brain" color="white" size="16"></v-icon>
            </div>
            <h3 class="font-black text-slate-800 text-sm">Global Report — All Projects</h3>
          </div>
          <button @click="loadGlobalReport" :disabled="globalLoading"
                  class="flex items-center gap-1.5 px-4 py-2 bg-slate-900 text-white rounded-xl text-xs font-black hover:bg-slate-700 transition-all disabled:opacity-50">
            <v-progress-circular v-if="globalLoading" indeterminate size="12" width="2" color="white"></v-progress-circular>
            <v-icon v-else icon="mdi-refresh" size="14"></v-icon>
            {{ globalLoading ? 'Loading...' : 'Analyze All' }}
          </button>
        </div>
        <div class="p-6">
          <div v-if="globalReport.length === 0 && !globalLoading" class="text-center py-8">
            <v-icon icon="mdi-chart-bar" color="#cbd5e1" size="40" class="mb-2"></v-icon>
            <p class="text-sm text-slate-400 font-bold">Click "Analyze All" for the full report</p>
          </div>
          <div v-if="globalReport.length > 0" class="space-y-3">
            <div v-for="item in globalReport" :key="item.project_name"
                 class="flex items-center gap-4 p-4 rounded-xl border"
                 :class="item.anomalies_found > 0 ? 'bg-red-50 border-red-100' : 'bg-emerald-50 border-emerald-100'">
              <div class="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0"
                   :class="item.anomalies_found > 0 ? 'bg-red-100' : 'bg-emerald-100'">
                <v-icon :icon="item.anomalies_found > 0 ? 'mdi-alert' : 'mdi-shield-check'"
                        :color="item.anomalies_found > 0 ? 'red' : '#10b981'" size="16"></v-icon>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-black text-slate-700 truncate">{{ item.project_name }}</p>
                <p class="text-xs text-slate-400">{{ item.total_commits }} commits analyzed</p>
              </div>
              <div class="text-right flex-shrink-0">
                <p :class="['text-lg font-black', item.anomalies_found > 0 ? 'text-red-600' : 'text-emerald-600']">{{ item.anomalies_found }}</p>
                <p class="text-[10px] text-slate-400 font-bold uppercase">anomalies</p>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- ═══════════════════════════════════════════════════════════
         DEVELOPER / TESTER / CONSULTANT VIEW (new dashboard)
    ════════════════════════════════════════════════════════════ -->
    <div v-else-if="isDevUser" class="p-6 space-y-6">

      <!-- Dev custom header -->
      <div class="bg-gradient-to-r from-purple-600 to-indigo-600 rounded-2xl p-8 text-white">
        <p class="text-xs font-semibold tracking-widest uppercase opacity-80 mb-1">GITDOCK</p>
        <h1 class="text-3xl font-bold mb-1">Hello, {{ firstName }} 👋</h1>
        <p class="opacity-70 text-sm">Your personal workspace.</p>
        <span class="inline-block mt-3 px-3 py-1 bg-white/20 rounded-full text-xs font-semibold tracking-wide uppercase">
          {{ roleLabel }}
        </span>
      </div>

      <!-- AI Alert if tasks overdue -->
      <div v-if="overdueAlerts.length > 0 && !alertDismissed" class="bg-red-50 border border-red-200 rounded-xl p-4">
        <div class="flex items-start justify-between">
          <div class="flex items-start gap-3">
            <div class="w-8 h-8 bg-red-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
              <svg class="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-red-800 text-sm mb-1">
                🔔 AI Alert — {{ overdueAlerts.length }} task(s) require your attention
              </h3>
              <ul class="space-y-1">
                <li v-for="task in overdueAlerts" :key="task.id" class="text-xs text-red-700">
                  <span v-if="task.isOverdue">🔴</span>
                  <span v-else>⏳</span>
                  <strong class="ml-1">{{ task.title }}</strong>
                  <span class="ml-1 opacity-75">
                    — {{ task.isOverdue ? `overdue by ${task.daysOverdue} day(s)` : `${task.daysLeft} day(s) remaining` }}
                  </span>
                </li>
              </ul>
            </div>
          </div>
          <button @click="alertDismissed = true" class="text-red-400 hover:text-red-600 ml-4">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Dev stats cards -->
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm">
          <div class="flex items-center gap-3 mb-2">
            <div class="w-8 h-8 bg-purple-100 rounded-lg flex items-center justify-center text-purple-600">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/></svg>
            </div>
            <span class="text-xs font-semibold text-gray-500 uppercase tracking-wide">My tasks</span>
          </div>
          <p class="text-2xl font-bold text-gray-900">{{ taskStats.total }}</p>
        </div>
        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm">
          <div class="flex items-center gap-3 mb-2">
            <div class="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center text-blue-600">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            </div>
            <span class="text-xs font-semibold text-gray-500 uppercase tracking-wide">In Progress</span>
          </div>
          <p class="text-2xl font-bold text-gray-900">{{ taskStats.inProgress }}</p>
        </div>
        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm">
          <div class="flex items-center gap-3 mb-2">
            <div class="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center text-green-600">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
            </div>
            <span class="text-xs font-semibold text-gray-500 uppercase tracking-wide">Completed</span>
          </div>
          <p class="text-2xl font-bold text-gray-900">{{ taskStats.done }}</p>
        </div>
        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm">
          <div class="flex items-center gap-3 mb-2">
            <div class="w-8 h-8 bg-amber-100 rounded-lg flex items-center justify-center text-amber-600">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"/></svg>
            </div>
            <span class="text-xs font-semibold text-gray-500 uppercase tracking-wide">Total XP</span>
          </div>
          <p class="text-2xl font-bold text-gray-900">{{ userXp }}</p>
        </div>
      </div>

      <!-- Recent tasks + level + AI Detector -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">

        <!-- My assigned tasks -->
        <div class="lg:col-span-2 bg-white rounded-xl border border-gray-100 shadow-sm p-5">
          <div class="flex items-center justify-between mb-4">
            <h2 class="font-semibold text-gray-900">My Assigned Tasks</h2>
            <router-link to="/dashboard/tasks" class="text-xs text-purple-600 hover:underline font-medium">View all →</router-link>
          </div>
          <div v-if="recentTasks.length === 0" class="text-center py-8 text-gray-400 text-sm">
            No tasks assigned at the moment.
          </div>
          <ul v-else class="space-y-3">
            <li v-for="task in recentTasks" :key="task.id" class="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition-colors">
              <div class="flex items-center gap-3 min-w-0">
                <span class="w-2 h-2 rounded-full flex-shrink-0" :class="{'bg-green-500': task.status === 'done', 'bg-blue-500': task.status === 'in_progress', 'bg-gray-400': task.status === 'todo'}"/>
                <span class="text-sm text-gray-800 truncate">{{ task.title }}</span>
              </div>
              <div class="flex items-center gap-2 flex-shrink-0 ml-3">
                <span v-if="isTaskOverdue(task)" class="px-2 py-0.5 bg-red-100 text-red-700 text-xs rounded-full font-medium">Overdue</span>
                <span v-else-if="task.due_date" class="text-xs text-gray-400">{{ formatDate(task.due_date) }}</span>
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="{'bg-green-100 text-green-700': task.status === 'done', 'bg-blue-100 text-blue-700': task.status === 'in_progress', 'bg-gray-100 text-gray-600': task.status === 'todo'}">
                  {{ task.status === 'done' ? 'Completed' : task.status === 'in_progress' ? 'In Progress' : 'To Do' }}
                </span>
              </div>
            </li>
          </ul>
        </div>

        <!-- Level and XP -->
        <div class="bg-white rounded-xl border border-gray-100 shadow-sm p-5">
          <h2 class="font-semibold text-gray-900 mb-4">My Level</h2>
          <div class="text-center mb-4">
            <div class="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-2">
              <span class="text-2xl font-bold text-purple-700">{{ userLevel }}</span>
            </div>
            <p class="text-sm font-semibold text-gray-700">{{ userLevelName }}</p>
            <p class="text-xs text-gray-400 mt-0.5">{{ userXp }} XP</p>
          </div>
          <div class="mt-3">
            <div class="flex justify-between text-xs text-gray-400 mb-1">
              <span>Progress</span>
              <span>{{ xpProgress }}%</span>
            </div>
            <div class="w-full bg-gray-100 rounded-full h-2">
              <div class="bg-gradient-to-r from-purple-500 to-indigo-500 h-2 rounded-full transition-all duration-500" :style="{ width: xpProgress + '%' }"/>
            </div>
          </div>
          <router-link to="/dashboard/gamification" class="mt-4 block text-center text-xs text-purple-600 hover:underline font-medium">View my badges →</router-link>
        </div>
      </div>

      <!-- AI Anomaly Detector (dev) -->
      <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
        <div class="px-5 py-4 border-b border-gray-100 flex items-center gap-2">
          <div class="w-7 h-7 bg-purple-100 rounded-lg flex items-center justify-center">
            <svg class="w-4 h-4 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17H3a2 2 0 01-2-2V5a2 2 0 012-2h14a2 2 0 012 2v10a2 2 0 01-2 2h-2"/>
            </svg>
          </div>
          <h2 class="font-semibold text-gray-900 text-sm">GitDock AI — Anomaly Detector</h2>
          <span class="ml-auto bg-purple-100 text-purple-700 text-[10px] font-semibold px-2 py-0.5 rounded-full uppercase tracking-wide">Security</span>
        </div>
        <div class="p-5 space-y-4">
          <!-- Input + button -->
          <div class="flex gap-2">
            <div class="flex-1 relative">
              <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-4.35-4.35M17 11A6 6 0 115 11a6 6 0 0112 0z"/>
              </svg>
              <input v-model="devAiProjectName" @keyup.enter="devAnalyzeProject" placeholder="Project name to analyze..."
                     class="w-full pl-9 pr-3 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-purple-400 focus:bg-white transition-all"/>
            </div>
            <button @click="devAnalyzeProject" :disabled="devAiLoading || !devAiProjectName.trim()"
                    class="px-4 py-2.5 bg-purple-600 text-white rounded-xl font-semibold text-xs hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1.5 shadow-sm transition-all">
              <svg v-if="!devAiLoading" class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
              </svg>
              <svg v-else class="w-3.5 h-3.5 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
              </svg>
              {{ devAiLoading ? '...' : 'Analyze' }}
            </button>
          </div>

          <!-- ERROR -->
          <div v-if="devAiError" class="bg-red-50 border border-red-200 rounded-xl p-3 flex items-center gap-2">
            <svg class="w-4 h-4 text-red-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
            </svg>
            <p class="text-red-700 text-xs font-medium">{{ devAiError }}</p>
          </div>

          <!-- RESULTS -->
          <div v-if="devAiResult" class="space-y-3">
            <div class="grid grid-cols-3 gap-2">
              <div class="bg-gray-50 rounded-xl p-3 border border-gray-100 text-center">
                <p class="text-xl font-bold text-gray-900">{{ devAiResult.total }}</p>
                <p class="text-[10px] text-gray-400 font-semibold uppercase tracking-wide">Commits</p>
              </div>
              <div :class="['rounded-xl p-3 border text-center', devAiResult.anomalies > 0 ? 'bg-red-50 border-red-100' : 'bg-green-50 border-green-100']">
                <p :class="['text-xl font-bold', devAiResult.anomalies > 0 ? 'text-red-600' : 'text-green-600']">{{ devAiResult.anomalies }}</p>
                <p class="text-[10px] text-gray-400 font-semibold uppercase tracking-wide">Anomalies</p>
              </div>
              <div :class="['rounded-xl p-3 border text-center', devAiResult.anomalies === 0 ? 'bg-green-50 border-green-100' : 'bg-amber-50 border-amber-100']">
                <p :class="['text-lg font-bold', devAiResult.anomalies === 0 ? 'text-green-600' : 'text-amber-600']">{{ devAiResult.anomalies === 0 ? '✓' : '⚠' }}</p>
                <p class="text-[10px] text-gray-400 font-semibold uppercase tracking-wide">Status</p>
              </div>
            </div>
            <div v-if="devAiResult.details?.length" class="space-y-1.5 max-h-40 overflow-y-auto">
              <div v-for="a in devAiResult.details" :key="a.sha" class="bg-red-50 border border-red-100 rounded-xl p-2.5 flex items-start gap-2">
                <svg class="w-3.5 h-3.5 text-red-500 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
                </svg>
                <div class="min-w-0 flex-1">
                  <p class="text-xs font-semibold text-red-800 truncate">{{ a.message }}</p>
                  <p class="text-[10px] text-red-400 font-mono">{{ a.sha?.slice(0, 8) }} · {{ a.reason }}</p>
                </div>
              </div>
            </div>
            <div v-else class="bg-green-50 border border-green-100 rounded-xl p-3 text-center">
              <p class="text-green-700 font-semibold text-sm">✓ Healthy project — no anomalies detected</p>
            </div>
          </div>

          <!-- EMPTY -->
          <div v-if="!devAiResult && !devAiLoading && !devAiError" class="text-center py-6">
            <div class="w-10 h-10 bg-purple-100 rounded-xl flex items-center justify-center mx-auto mb-2">
              <svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/>
              </svg>
            </div>
            <p class="text-xs text-gray-400 font-medium">Enter a project name to start AI analysis</p>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import LevelWidget from '@/components/dashboard/LevelWidget.vue'
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useProjectStore } from '@/stores/projectStore'
import { useRole } from '@/composables/useRole'
import { TaskService } from '@/services/TaskService'
import { userProgressService } from '@/services/UserProgressService'
import { useAuthStore } from '@/stores/authStore'



const authStore = useAuthStore()
const projectStore = useProjectStore()
const router = useRouter()
const { isDevUser, isManager } = useRole()

const AI_API = 'http://localhost:8000/api/v1'

// ── MANAGER ────────────────────────────────────────────────────────────────
const stats = ref({ totalProjects: 0, healthyProjects: 0, totalAnomalies: 0, totalCommits: 0 })
const projects = computed(() => (projectStore.projects || []) as any[])

const aiProjectName = ref('')
const aiLoading = ref(false)
const aiError = ref('')
const aiResult = ref<any>(null)
const globalReport = ref<any[]>([])
const globalLoading = ref(false)

const analyzeProject = async () => {
  if (!aiProjectName.value.trim()) return
  aiLoading.value = true
  aiError.value = ''
  aiResult.value = null
  try {
    const res = await axios.get(`${AI_API}/analyze-project`, {
      params: { project_name: aiProjectName.value.trim() }
    })
    aiResult.value = res.data
    if (res.data.anomalies === 0) stats.value.healthyProjects++
    else stats.value.totalAnomalies += res.data.anomalies
    stats.value.totalCommits += res.data.total
  } catch (err: any) {
    aiError.value = err.response?.data?.detail || "Projet introuvable ou erreur d'analyse."
  } finally {
    aiLoading.value = false
  }
}

const loadGlobalReport = async () => {
  globalLoading.value = true
  try {
    const projectUrls = projects.value.map((p: any) => {
      try {
        const url = new URL(p.url)
        return url.pathname.replace('/', '').replace('.git', '')
      } catch {
        return p.name
      }
    }).join(',')
    const res = await axios.get(`${AI_API}/analyze-all`, {
      params: { projects: projectUrls }
    })
    globalReport.value = res.data.report || []
    stats.value.totalAnomalies = globalReport.value.reduce((s: number, r: any) => s + r.anomalies_found, 0)
    stats.value.totalCommits = globalReport.value.reduce((s: number, r: any) => s + r.total_commits, 0)
    stats.value.healthyProjects = globalReport.value.filter((r: any) => r.anomalies_found === 0).length
  } catch (err) {
    console.error(err)
  } finally {
    globalLoading.value = false
  }
}

const goToProject = (id: any) => router.push(`/projects/${id}`)

const getHostname = (url: any) => {
  if (!url) return ''
  try { return new URL(url).hostname.replace('www.', '') } catch { return url }
}

// ── DEV ────────────────────────────────────────────────────────────────────
const firstName = computed(() => authStore.firstName)
const currentUserId = computed(() => authStore.userId)

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    ROLE_DEVELOPER: 'Développeur',
    ROLE_TESTER: 'Testeur',
    ROLE_CONSULTANT: 'Consultant',
  }
  return map[authStore.role ?? ''] ?? authStore.role ?? ''
})

const myTasks = ref<any[]>([])
const userXp = ref(0)
const userLevel = ref(1)
const userLevelName = ref('Junior')
const xpProgress = ref(0)
const alertDismissed = ref(false)

const recentTasks = computed(() => myTasks.value.slice(0, 5))

const taskStats = computed(() => ({
  total: myTasks.value.length,
  inProgress: myTasks.value.filter(t => t.status === 'in_progress').length,
  done: myTasks.value.filter(t => t.status === 'done').length,
  todo: myTasks.value.filter(t => t.status === 'todo').length,
}))

const overdueAlerts = computed(() => {
  const now = new Date()
  return myTasks.value
      .filter(t => t.status !== 'done' && t.due_date)
      .map(t => {
        const due = new Date(t.due_date)
        const diffMs = due.getTime() - now.getTime()
        const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24))
        return {
          ...t,
          isOverdue: diffDays < 0,
          daysOverdue: diffDays < 0 ? Math.abs(diffDays) : 0,
          daysLeft: diffDays >= 0 ? diffDays : 0,
          shouldAlert: diffDays <= 3,
        }
      })
      .filter(t => t.shouldAlert)
      .sort((a, b) => a.daysLeft - b.daysLeft)
})

const isTaskOverdue = (task: any) => {
  if (!task.due_date || task.status === 'done') return false
  return new Date(task.due_date) < new Date()
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' })
}

// Dev AI detector
const devAiProjectName = ref('')
const devAiLoading = ref(false)
const devAiError = ref('')
const devAiResult = ref<any>(null)

const devAnalyzeProject = async () => {
  if (!devAiProjectName.value.trim()) return
  devAiLoading.value = true
  devAiError.value = ''
  devAiResult.value = null
  try {
    const res = await axios.get(`${AI_API}/analyze-project`, {
      params: { project_name: devAiProjectName.value.trim() }
    })
    devAiResult.value = res.data
  } catch (err: any) {
    devAiError.value = err.response?.data?.detail || "Projet introuvable ou erreur d'analyse."
  } finally {
    devAiLoading.value = false
  }
}

// ── MOUNTED ────────────────────────────────────────────────────────────────
onMounted(async () => {
  if (isManager.value) {
    await projectStore.fetchProjects()
    stats.value.totalProjects = projects.value.length
  }

  if (isDevUser.value && currentUserId.value) {
    try {
      const tasks = await TaskService.getByAssignee(currentUserId.value)
      myTasks.value = tasks

      const progress = await userProgressService.getProgress(authStore.userId!)
      if (progress) {
        userXp.value = progress.xp ?? 0
        userLevel.value = progress.level ?? 1
        userLevelName.value = progress.levelName ?? 'Junior'
        xpProgress.value = progress.progressPercent ?? 0
      }
    } catch (e) {
      console.error('Dashboard dev — erreur chargement:', e)
    }
  }
})
</script>