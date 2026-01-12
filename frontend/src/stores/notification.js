// src/stores/notification.js
import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useNotificationStore = defineStore('notification', {
    state: () => ({
        unreadCount: 0
    }),

    actions: {
        async fetchUnreadCount() {
            try {
                const res = await request.get('/notifications/unread-count')
                if (res.code === 200) {
                    this.unreadCount = res.data
                }
            } catch (error) {
                console.error('获取未读通知失败:', error)
            }
        },

        async markAllAsRead() {
            try {
                const res = await request.put('/notifications/mark-all-read')
                if (res.code === 200) {
                    this.unreadCount = 0
                    return true
                }
            } catch (error) {
                console.error('标记全部已读失败:', error)
                return false
            }
        }
    }
})