import axios from 'axios'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const service = axios.create({
    baseURL: '/api',
    timeout: 5000
})

// 请求拦截：带 token
service.interceptors.request.use((config) => {
    const userStore = useUserStore()
    if (userStore.token) {
        config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
})

// 响应拦截：统一处理 ApiResponse
service.interceptors.response.use(
    (response) => {
        const res = response.data
        if (res.code !== 200) {
            return Promise.reject(res)
        }
        return res
    },
    (error) => {
        if (error.response?.status === 401) {
            const userStore = useUserStore()
            userStore.logout()
            router.replace('/login')
        }
        return Promise.reject(error)
    }
)

export default service
