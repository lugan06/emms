import { fileURLToPath, URL } from 'node:url'

import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

const proxyTimeout = 300_000

interface ProxyResponse {
  writableEnded?: boolean
  headersSent: boolean
  writeHead: (statusCode: number, headers: Record<string, string>) => void
  end: (body: string) => void
}

interface ProxyError {
  message?: string
}

type ProxyErrorListener = (error: ProxyError, request: unknown, response?: ProxyResponse) => void

function configureProxy(proxy: { on: (event: 'error', listener: ProxyErrorListener) => void }): void {
  proxy.on('error', (error, _request, response) => {
    console.error('[vite proxy] upstream request failed:', error?.message || error)
    if (!response || response.writableEnded) return
    if (!response.headersSent) {
      response.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' })
    }
    response.end(JSON.stringify({ message: '后端服务连接失败，请稍后重试' }))
  })
}

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        proxyTimeout,
        timeout: proxyTimeout,
        configure: configureProxy,
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        proxyTimeout,
        timeout: proxyTimeout,
        configure: configureProxy,
      },
    },
  },
})
