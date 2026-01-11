// src/stores/user.js
import { defineStore } from 'pinia'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
    state: () => ({
        userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
        role: localStorage.getItem('userRole') || '',
        username: localStorage.getItem('username') || ''
    }),

    getters: {
        isLoggedIn: (state) => !!state.userInfo,
        isSystemAdmin: (state) => state.role === 'ROLE_SYSTEM_ADMIN',
        isBranchAdmin: (state) => state.role === 'ROLE_BRANCH_ADMIN',
        isTeacher: (state) => state.role === 'ROLE_TEACHER',
        isStudent: (state) => state.role === 'ROLE_STUDENT',
    },

    actions: {
        // 将用户名密码转换为Basic Auth字符串
        encodeBasicAuth(username, password) {
            return btoa(`${username}:${password}`)
        },

        // 登录
        async login(loginForm) {
            try {
                // 1. 先验证用户名密码
                const authString = this.encodeBasicAuth(loginForm.username, loginForm.password)

                // 2. 调用登录接口（不需要Basic Auth头，因为接口是公开的）
                const res = await request.post('/auth/login', loginForm)

                if (res.code === 200) {
                    // 3. 存储认证信息
                    localStorage.setItem('basic_auth', authString)
                    localStorage.setItem('username', loginForm.username)

                    // 4. 获取用户详细信息
                    await this.fetchCurrentUser()

                    return res
                } else {
                    throw new Error(res.message || '登录失败')
                }
            } catch (error) {
                // 清理可能的残留数据
                this.logout()
                throw error
            }
        },

        // 获取当前用户信息（需要Basic Auth）
        async fetchCurrentUser() {
            try {
                const res = await request.get('/auth/current-user')

                if (res.code === 200) {
                    this.userInfo = res.data
                    this.role = res.data.role?.name || ''

                    // 存储到localStorage
                    localStorage.setItem('userInfo', JSON.stringify(res.data))
                    localStorage.setItem('userRole', this.role)

                    return res
                }
            } catch (error) {
                console.error('获取用户信息失败:', error)
                // 如果401，可能是Basic Auth过期，清除认证信息
                if (error.response?.status === 401) {
                    this.logout()
                }
                throw error
            }
        },

        // 注册
        async register(registerForm) {
            try {
                const res = await request.post('/auth/register', registerForm)
                return res
            } catch (error) {
                throw error
            }
        },

        // 登出
        logout() {
            this.userInfo = null
            this.role = ''
            this.username = ''

            // 清除所有存储
            localStorage.removeItem('basic_auth')
            localStorage.removeItem('userInfo')
            localStorage.removeItem('userRole')
            localStorage.removeItem('username')

            // 不需要调用后端登出接口，因为Basic Auth是无状态的
        },

        // 修改密码
        async changePassword(oldPassword, newPassword) {
            try {
                const res = await request.post('/auth/change-password', null, {
                    params: {
                        oldPassword,
                        newPassword
                    }
                })
                return res
            } catch (error) {
                throw error
            }
        },

        // 检查权限
        async checkPermission(permission) {
            try {
                const res = await request.get(`/auth/check-permission/${permission}`)
                return res.data || false
            } catch (error) {
                console.error('检查权限失败:', error)
                return false
            }
        }
    }
})