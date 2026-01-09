import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
// 导入路由实例
import router from './router'
// 若使用Element Plus，补充导入（可选）
// import ElementPlus from 'element-plus'
// import 'element-plus/dist/index.css'

const app = createApp(App)
// 挂载路由
app.use(router)
// 挂载Element Plus（可选）
// app.use(ElementPlus)
app.mount('#app')

