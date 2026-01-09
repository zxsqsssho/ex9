// src/stores/user.js
import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
    state: () => ({
        userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
        role: localStorage.getItem('userRole') || ''
    }),

    actions: {
        // 登录
        async login(loginForm) {
            const res = await request.post('/auth/login', loginForm)
            // ⭐ res.data 就是 UserResponseDTO
            this.userInfo = res.data
            this.role = res.data.role?.name || ''

            localStorage.setItem('userInfo', JSON.stringify(res.data))
            localStorage.setItem('userRole', this.role)

            return res
        },

        // 获取当前登录用户
        async fetchCurrentUser() {
            const res = await request.get('/auth/current-user')
            this.userInfo = res.data
            this.role = res.data.role?.name || ''
            localStorage.setItem('userInfo', JSON.stringify(res.data))
        },

        logout() {
            this.userInfo = null
            this.role = ''
            localStorage.clear()
        }
    }
})
