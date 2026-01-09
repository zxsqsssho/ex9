import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
        role: localStorage.getItem('userRole') || ''
    }),

    actions: {
        // 登录
        async login(loginForm) {
            const res = await request.post('/auth/login', loginForm)
            // ⭐ res 就是 ApiResponse
            const { token, userInfo, role } = res.data

            this.token = token
            this.userInfo = userInfo
            this.role = role

            localStorage.setItem('token', token)
            localStorage.setItem('userInfo', JSON.stringify(userInfo))
            localStorage.setItem('userRole', role)

            return res
        },

        // 获取当前用户
        async fetchCurrentUser() {
            const res = await request.get('/auth/current-user')
            this.userInfo = res.data
            localStorage.setItem('userInfo', JSON.stringify(res.data))
        },

        logout() {
            this.token = ''
            this.userInfo = null
            this.role = ''
            localStorage.clear()
        }
    }
})
