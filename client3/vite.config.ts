import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // Default: api-gateway local port (see server5/api-gateway application.yaml).
  // Override with VITE_GATEWAY_URL — use http://localhost:30081 when only Kubernetes NodePort is running.
  const gatewayTarget = env.VITE_GATEWAY_URL || 'http://localhost:8081'

  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      proxy: {
        '/api': {
          target: gatewayTarget,
          changeOrigin: true,
        },
        '/uploads': {
          target: gatewayTarget,
          changeOrigin: true,
        },
        '/aimodel': {
          target: gatewayTarget,
          changeOrigin: true,
        },
        '/dataset': {
          target: gatewayTarget,
          changeOrigin: true,
        },
        '/training-orchestrator': {
          target: gatewayTarget,
          changeOrigin: true,
        },
      },
    },
  }
})
