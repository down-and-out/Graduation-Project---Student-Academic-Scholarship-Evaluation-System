import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

/**
 * Vite 配置文件
 * @see https://vitejs.dev/config/
 */
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')

  // 开发服务器配置
  const serverConfig = {
    port: 3000,
    host: '0.0.0.0',
    open: false,
    headers: {
      'X-Content-Type-Options': 'nosniff',
      'X-Frame-Options': 'SAMEORIGIN',
      'X-XSS-Protection': '1; mode=block',
      'Referrer-Policy': 'strict-origin-when-cross-origin'
    },
    proxy: {
      '/api': {
        target: env.VITE_API_TARGET || 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  }

  // 构建配置
  const buildConfig = {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: mode === 'development',
    chunkSizeWarningLimit: 500,
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus'],
          'element-icons': ['@element-plus/icons-vue'],
          'utils': ['axios', 'dayjs']
        }
      }
    },
    minify: 'esbuild',
    esbuildOptions: {
      drop: mode === 'production' ? ['console', 'debugger'] : ['debugger'],
      legalComments: 'none'
    },
    reportCompressedSize: true
  }

  // CSS 配置
  const cssConfig = {
    preprocessorOptions: {
      scss: {}
    }
  }

  // 优化依赖预构建
  const optimizeDepsConfig = {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios',
      'element-plus',
      '@element-plus/icons-vue'
    ],
    exclude: ['jsencrypt']
  }

  // 插件配置
  const pluginsConfig = [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts',
      eslintrc: { enabled: false }
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts',
      dirs: ['src/components']
    })
  ]

  return {
    server: serverConfig,
    build: buildConfig,
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    css: cssConfig,
    optimizeDeps: optimizeDepsConfig,
    plugins: pluginsConfig
  }
})
