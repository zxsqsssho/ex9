import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
// 导入路由实例
import router from './router'

// Element Plus 核心
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'


const pinia = createPinia()

const app = createApp(App)

app.use(pinia)

// 挂载路由
app.use(router)
// 挂载Element Plus（可选）
app.use(ElementPlus)
app.mount('#app')

