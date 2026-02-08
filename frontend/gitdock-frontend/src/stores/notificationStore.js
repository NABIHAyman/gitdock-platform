import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
    const notifications = ref([])

    /**
     * Ajoute une notification à la file
     */
    const addNotification = (type, message, duration = 5000) => {
        const id = Date.now().toString()
        const notification = {
            id,
            type,
            message,
            duration,
        }
        notifications.value.push(notification)

        // Auto-suppression après la durée impartie
        if (duration > 0) {
            setTimeout(() => {
                removeNotification(id)
            }, duration)
        }
    }

    /**
     * Supprime une notification par son ID
     */
    const removeNotification = (id) => {
        const index = notifications.value.findIndex((n) => n.id === id)
        if (index > -1) {
            notifications.value.splice(index, 1)
        }
    }

    // Raccourcis pour les différents types
    const success = (message, duration) => addNotification('success', message, duration)
    const error = (message, duration) => addNotification('error', message, duration)
    const info = (message, duration) => addNotification('info', message, duration)
    const warning = (message, duration) => addNotification('warning', message, duration)

    return {
        notifications,
        addNotification,
        removeNotification,
        success,
        error,
        info,
        warning,
    }
})