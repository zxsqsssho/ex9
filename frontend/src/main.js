import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
// 导入路由实例
import router from './router'

// Element Plus 核心
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 在文件最开头添加
const originalError = console.error
console.error = function(...args) {
    if (args[0] && typeof args[0] === 'string') {
        const msg = args[0]
        if (msg.includes('ResizeObserver') ||
            msg.includes('lang attribute') ||
            msg.includes('Form elements must have labels') ||
            msg.includes('type.text is about to be deprecated')) {
            return
        }
    }
    originalError.apply(console, args)
}
const pinia = createPinia()

const app = createApp(App)

app.use(pinia)

// 挂载路由
app.use(router)
// 挂载Element Plus（可选）
app.use(ElementPlus)
app.mount('#app')