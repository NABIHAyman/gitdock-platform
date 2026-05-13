/// <reference types="vite/client" />

declare module '*.vue' {
    import type { DefineComponent } from 'vue'
    const component: DefineComponent<{}, {}, any>
    export default component
}

declare module '@/stores/authStore';
declare module '@/services/UserProgressService';
declare module '@/services/UserBadgeService';