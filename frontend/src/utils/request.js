import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const service = axios.create({
    baseURL: 'http://localhost:8080', // 后端地址+端口
    timeout: 5000
})

// 请求拦截器：添加token
service.interceptors.request.use(
    (config) => {
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers['Authorization'] = 'Bearer ' + userStore.token
        }
        return config
    },
    (error) => {
        console.error('请求错误：', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    (response) => {
        const res = response.data
        // 业务错误码处理（根据后端约定）
        if (res.code !== 200) {
            ElMessage.error(res.msg || '请求失败')
            // token过期
            if (res.code === 401) {
                ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
                    confirmButtonText: '重新登录',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    const userStore = useUserStore()
                    userStore.logout()
                    router.push('/login')
                })
            }
            return Promise.reject(res)
        }
        return res
    },
    (error) => {
        console.error('响应错误：', error)
        ElMessage.error(error.message || '服务器错误')
        return Promise.reject(error)
    }
)

export default service