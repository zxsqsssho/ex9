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
        // user.js
        async login(loginForm) {
            const res = await axios.post('/api/user/login', loginForm)
            const { token, userInfo, role } = res.data

            this.token = token
            this.userInfo = userInfo
            this.userRole = role

            localStorage.setItem('token', token)
            localStorage.setItem('userInfo', JSON.stringify(userInfo))
            localStorage.setItem('userRole', role)

            return res.data
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