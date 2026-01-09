import { defineStore } from 'pinia'
import axios from '@/utils/request'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        userInfo: JSON.parse(localStorage.getItem('userInfo')) || {},
        userRole: localStorage.getItem('userRole') || ''
    }),
    actions: {
        // 登录
        async login(loginForm) {
            try {
                const res = await axios.post('/api/user/login', loginForm)
                const { token, userInfo, role } = res.data
                this.token = token
                this.userInfo = userInfo
                this.userRole = role
                // 持久化存储
                localStorage.setItem('token', token)
                localStorage.setItem('userInfo', JSON.stringify(userInfo))
                localStorage.setItem('userRole', role)
                return true
            } catch (error) {
                ElMessage.error('登录失败：' + error.response.data.msg)
                return false
            }
        },
        // 退出登录
        logout() {
            this.token = ''
            this.userInfo = {}
            this.userRole = ''
            localStorage.clear()
        }
    }
})