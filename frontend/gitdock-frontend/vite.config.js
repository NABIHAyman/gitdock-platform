import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            // Cette ligne est celle qui manque : elle lie "@" au dossier "src"
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    server: {
        hmr: {
            clientPort: 5173,
            host: 'localhost'
        },
        proxy: {
            '/api': {
                target: 'http://localhost:8083',
                changeOrigin: true,
                secure: false,
            }
        }
    }
})