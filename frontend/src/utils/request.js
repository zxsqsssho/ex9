// src/utils/request.js
import axios from 'axios'
import router from '@/router'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
    baseURL: '/api',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 从localStorage获取Basic Auth凭据
function getBasicAuthHeader() {
    const authString = localStorage.getItem('basic_auth')
    if (authString) {
        return `Basic ${authString}`
    }
    return null
}

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 添加Basic Auth认证头
        const authHeader = getBasicAuthHeader()
        if (authHeader) {
            config.headers.Authorization = authHeader
        }

        // 如果是登录/注册请求，使用特殊处理
        // if (config.url.includes('/auth/login') || config.url.includes('/auth/register')) {
        //     // 这些请求不需要Basic Auth头，而是需要在请求体中包含认证信息
        //     delete config.headers.Authorization
        // }

        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data

        // 统一处理响应格式
        if (res.code === 200) {
            return res
        } else {
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || 'Error'))
        }
    },
    error => {
        console.error('响应错误:', error)

        if (error.response) {
            switch (error.response.status) {
                case 401:
                    ElMessage.error('认证失败，请重新登录')
                    localStorage.removeItem('basic_auth')
                    localStorage.removeItem('userInfo')
                    localStorage.removeItem('userRole')
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('权限不足，无法访问')
                    break
                case 404:
                    ElMessage.error('请求的资源不存在')
                    break
                case 500:
                    ElMessage.error('服务器内部错误')
                    break
                default:
                    ElMessage.error(error.response.data?.message || '请求失败')
            }
        } else if (error.request) {
            ElMessage.error('网络错误，请检查网络连接')
        } else {
            ElMessage.error(error.message || '请求失败')
        }

        return Promise.reject(error)
    }
)

export default service