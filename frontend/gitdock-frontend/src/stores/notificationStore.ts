import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { notificationService, type InAppNotification } from '@/services/notificationService'
import { useAuthStore } from '@/stores/authStore'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

interface Toast {
    id: string
    type: 'success' | 'error' | 'info' | 'warning'
    message: string
    duration: number
}

function resolveNotificationSockJsUrl(): string {
    const explicit = import.meta.env.VITE_NOTIFICATION_WS_URL
    if (explicit?.trim()) return explicit.trim()

    const base = import.meta.env.VITE_API_BASE_URL
    if (!base) return ''

    try {
        const u = new URL(base)
        return `${u.protocol}//${u.host}/ws-notifications`
    } catch {
        return ''
    }
}

export const useNotificationStore = defineStore('notification', () => {
    // --- TOASTS ---
    const notifications = ref<Toast[]>([])

    const removeNotification = (id: string) => {
        const index = notifications.value.findIndex((n) => n.id === id)
        if (index > -1) notifications.value.splice(index, 1)
    }

    const addNotification = (
        type: Toast['type'],
        message: string,
        duration = 5000,
    ) => {
        const id = Date.now().toString()
        notifications.value.push({ id, type, message, duration })
        if (duration > 0) setTimeout(() => removeNotification(id), duration)
    }

    const success = (message: string, duration?: number) =>
        addNotification('success', message, duration)
    const error = (message: string, duration?: number) =>
        addNotification('error', message, duration)
    const info = (message: string, duration?: number) =>
        addNotification('info', message, duration)
    const warning = (message: string, duration?: number) =>
        addNotification('warning', message, duration)

    // --- IN-APP NOTIFICATIONS ---
    const inAppNotifications = ref<InAppNotification[]>([])
    let stompClient: Client | null = null

    const unreadCount = computed(
        () => inAppNotifications.value.filter((n) => n.read === false).length,
    )

    const fetchInAppNotifications = async () => {
        try {
            inAppNotifications.value = await notificationService.getMyNotifications()
        } catch (e) {
            console.error("Échec de la récupération de l'historique des notifications", e)
        }
    }

    const markAsRead = async (id: number) => {
        const notif = inAppNotifications.value.find((n) => n.id === id)
        if (notif && !notif.read) {
            notif.read = true
            await notificationService.markAsRead(id)
        }
    }

    const markAllAsRead = async () => {
        inAppNotifications.value.forEach((n) => (n.read = true))
        await notificationService.markAllAsRead()
    }

    // --- WEBSOCKET ---
    const connectWebSocket = () => {
        const authStore = useAuthStore()
        if (!authStore.userId) return
        if (stompClient?.connected) return

        const sockUrl = resolveNotificationSockJsUrl()
        if (!sockUrl) {
            console.warn(
                '[notifications] WebSocket URL indisponible : définissez VITE_NOTIFICATION_WS_URL ou VITE_API_BASE_URL',
            )
            return
        }

        stompClient = new Client({
            webSocketFactory: () => new SockJS(sockUrl) as WebSocket,
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        })

        stompClient.onConnect = () => {
            console.log('🟢 Connecté au WebSocket GitDock !')
            
            // ✅ Chargement de l'historique dès que la connexion est établie
            fetchInAppNotifications()

            const userTopic = `/topic/notifications.${authStore.userId}`
            stompClient!.subscribe(userTopic, (message) => {
                const newNotif = JSON.parse(message.body) as InAppNotification
                inAppNotifications.value.unshift(newNotif)
                info(`🔔 ${newNotif.title} : ${newNotif.message}`, 6000)
            })
        }

        stompClient.activate()
    }

    const disconnectWebSocket = () => {
        if (stompClient) {
            stompClient.deactivate()
            console.log('🔴 Déconnecté du WebSocket GitDock')
        }
    }

    const startRealTime = () => {
        connectWebSocket()
    }

    const stopRealTime = () => disconnectWebSocket()

    return {
        notifications,
        addNotification,
        removeNotification,
        success,
        error,
        info,
        warning,
        inAppNotifications,
        unreadCount,
        fetchInAppNotifications,
        markAsRead,
        markAllAsRead,
        startRealTime,
        stopRealTime,
    }
})