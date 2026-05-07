<template>
  <div class="fixed bottom-6 right-6 z-[100] font-sans">
    
    <transition name="bounce">
      <button 
        v-if="!isOpen" 
        @click="toggleChat"
        class="w-16 h-16 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-full shadow-2xl flex items-center justify-center hover:scale-105 transition-transform duration-200 focus:outline-none focus:ring-4 focus:ring-indigo-300"
      >
        <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
        </svg>
      </button>
    </transition>

    <transition name="slide-up">
      <div v-if="isOpen" class="w-[400px] h-[600px] bg-white rounded-2xl shadow-2xl flex flex-col border border-gray-100 overflow-hidden">
        
        <div class="bg-gradient-to-r from-blue-600 to-indigo-600 p-4 flex justify-between items-center text-white">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <div>
              <h3 class="font-bold text-sm">GitDock YAM</h3>
              <p class="text-[10px] text-blue-100">Assistant RH & Architecture</p>
            </div>
          </div>

          <button @click="resetChat" class="text-white/80 hover:text-white transition-colors p-1" title="Nouvelle conversation">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
          </button>

          <button @click="toggleChat" class="text-white/80 hover:text-white transition-colors">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div class="flex-1 p-4 overflow-y-auto bg-gray-50 flex flex-col gap-4" ref="messagesContainer">
          
          <div class="flex gap-3">
            <div class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center shrink-0">
              🤖
            </div>
            <div class="bg-white p-3 rounded-2xl rounded-tl-none shadow-sm text-sm text-gray-700 border border-gray-100 max-w-[85%]">
              Bonjour ! Je suis GitDock-YAM. Je peux vous aider à constituer l'équipe parfaite pour votre prochain projet. Que cherchez-vous ?
            </div>
          </div>

          <div v-for="(msg, index) in messages" :key="index" class="flex gap-3" :class="msg.role === 'user' ? 'flex-row-reverse' : ''">
            
            <div v-if="msg.role === 'ai'" class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center shrink-0">
              🤖
            </div>

            <div class="flex flex-col gap-2 max-w-[85%]">
              <div 
                :class="[
                  'p-3 rounded-2xl shadow-sm text-sm border',
                  msg.role === 'user' 
                    ? 'bg-blue-600 text-white rounded-tr-none border-blue-700' 
                    : 'bg-white text-gray-700 rounded-tl-none border-gray-100'
                ]"
              >
                {{ msg.text }}
              </div>

              <div v-if="msg.role === 'ai' && msg.teamIds && msg.teamIds.length > 0" class="bg-indigo-50 border border-indigo-100 rounded-xl p-3 mt-1 shadow-sm">
                <p class="text-xs font-semibold text-indigo-800 mb-2">Équipe suggérée (IDs: {{ msg.teamIds.join(', ') }})</p>
                <button 
                  @click="triggerProjectCreation(msg.teamIds)"
                  class="w-full py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-lg transition-colors flex items-center justify-center gap-2"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Créer le projet avec cette équipe
                </button>
              </div>
            </div>
          </div>

          <div v-if="isTyping" class="flex gap-3">
            <div class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center shrink-0">🤖</div>
            <div class="bg-white p-3 rounded-2xl rounded-tl-none shadow-sm border border-gray-100 flex items-center gap-1">
              <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></span>
              <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></span>
              <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.4s"></span>
            </div>
          </div>

        </div>

        <div class="p-3 border-t border-gray-100 bg-white">
          <form @submit.prevent="sendMessage" class="flex gap-2">
            <input 
              v-model="currentInput" 
              type="text" 
              placeholder="Ex: J'ai besoin d'un dev Vue.js..." 
              class="flex-1 bg-gray-50 border border-gray-200 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all"
              :disabled="isTyping"
            />
            <button 
              type="submit" 
              :disabled="!currentInput.trim() || isTyping"
              class="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center text-white hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              <svg class="w-4 h-4 transform rotate-90" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
            </button>
          </form>
        </div>

      </div>
    </transition>

    <div v-if="showSagaModal" class="fixed inset-0 bg-gray-900/50 backdrop-blur-sm z-[200] flex items-center justify-center">
      <div class="bg-white rounded-2xl p-6 w-[400px] shadow-2xl">
        <h3 class="text-lg font-bold text-gray-900 mb-2">🚀 Lancement de la Saga</h3>
        <p class="text-sm text-gray-500 mb-4">GitDock-YAM a sélectionné l'équipe. Donnez un nom à ce nouveau projet.</p>
        
        <form @submit.prevent="executeSaga">
          <div class="space-y-4">
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Nom du projet</label>
              <input v-model="sagaForm.name" type="text" required class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">URL du Dépôt (Git)</label>
              <input v-model="sagaForm.repoUrl" type="url" required class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
            </div>
          </div>
          
          <div class="mt-6 flex justify-end gap-3">
            <button type="button" @click="showSagaModal = false" class="px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-100 rounded-lg">Annuler</button>
            <button type="submit" :disabled="isSagaRunning" class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg flex items-center">
              <span v-if="isSagaRunning" class="animate-spin mr-2">⟳</span>
              {{ isSagaRunning ? 'Orchestration...' : 'Créer' }}
            </button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notificationStore'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api' // Ton instance axios configurée

import { ref, nextTick, onMounted } from 'vue'

const router = useRouter()
const notifStore = useNotificationStore()
const authStore = useAuthStore()
const currentSessionId = ref('')

// Génère une session unique au montage
onMounted(() => {
  if (authStore.userId) {
    currentSessionId.value = `user_${authStore.userId}_${crypto.randomUUID()}`
  }
})

// Fonction pour vider le chat localement ET changer de session !
const resetChat = () => {
  messages.value = []
  currentSessionId.value = `user_${authStore.userId}_${crypto.randomUUID()}`
  // Tu peux ajouter un message d'intro :
  messages.value.push({
    role: 'ai',
    text: "Mémoire effacée. Je suis prêt pour un nouveau projet. Que cherchez-vous ?"
  })
}

// --- ETATS DU CHAT ---
const isOpen = ref(false)
const currentInput = ref('')
const messages = ref([])
const isTyping = ref(false)
const messagesContainer = ref(null)

// --- ETATS DE LA SAGA ---
const showSagaModal = ref(false)
const isSagaRunning = ref(false)
const sagaForm = ref({
  name: '',
  repoUrl: 'https://github.com/mon-org/mon-repo',
  teamIds: []
})

// Ouvre/Ferme le chat
const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) scrollToBottom()
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Envoi d'un message à GitDock-YAM
const sendMessage = async () => {
  const text = currentInput.value.trim()
  if (!text) return

  // 1. Ajoute le message de l'utilisateur
  messages.value.push({ role: 'user', text })
  currentInput.value = ''
  isTyping.value = true
  scrollToBottom()

  try {
    // Appel direct au backend Python
    const response = await api.post('/yam/team-builder', {
      question: text,
      provider: 'gemini',
      // 👇 On envoie une session unique par utilisateur !
      session_id: currentSessionId.value 
    })

    // 3. Ajoute la réponse de l'IA
    messages.value.push({ 
      role: 'ai', 
      text: response.data.answer,
      teamIds: response.data.suggested_dev_ids || []
    })

  } catch (error) {
    messages.value.push({ 
      role: 'ai', 
      text: "Oups, j'ai eu un problème de connexion avec mes neurones (Serveur injoignable)." 
    })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

// Ouvre la modale SAGA avec l'équipe choisie
const triggerProjectCreation = (teamIds) => {
  sagaForm.value.teamIds = teamIds
  sagaForm.value.name = '' // Reset
  showSagaModal.value = true
}

// 🚀 LE CLIMAX : Appel à l'Orchestrateur Java
const executeSaga = async () => {
  isSagaRunning.value = true
  try {
    // Appel via ton instance API (qui gère le JWT) vers ton backend Java
    const response = await api.post('/projects/saga/create-with-team', {
      name: sagaForm.value.name,
      description: "Projet généré par l'IA GitDock-YAM",
      repoUrl: sagaForm.value.repoUrl,
      teamIds: sagaForm.value.teamIds
    }, {
      headers: {
        'X-User-Id': authStore.userId
      }
    })

    notifStore.success(`🎉 Saga réussie ! Projet ${response.data.name} créé avec l'équipe.`)
    showSagaModal.value = false
    
    // Ajoute un message de confirmation dans le chat
    messages.value.push({
      role: 'ai',
      text: `✅ Excellent ! Le projet "${response.data.name}" a été créé et l'équipe a été assignée avec succès.`
    })
    scrollToBottom()

    // Optionnel : rediriger vers la page du projet
    // router.push(`/projects/${response.data.id}`)

  } catch (error) {
    console.error("Erreur SAGA:", error)
    notifStore.error("Échec de la Saga : " + (error.response?.data?.message || error.message))
  } finally {
    isSagaRunning.value = false
  }
}
</script>

<style scoped>
.bounce-enter-active {
  animation: bounce-in 0.5s;
}
.bounce-leave-active {
  animation: bounce-in 0.5s reverse;
}
@keyframes bounce-in {
  0% { transform: scale(0); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease-out;
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
</style>