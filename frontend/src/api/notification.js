// src/api/notification.js
import request from '@/utils/request'

// 获取用户通知列表
export function getMyNotifications(params) {
    return request.get('/notifications/user', { params })
}

// 获取未读通知数量
export function getUnreadCount() {
    return request.get('/notifications/unread-count')
}

// 标记单条通知为已读
export function markAsRead(notificationId) {
    return request.put(`/notifications/mark-read/${notificationId}`)
}

// 标记所有通知为已读
export function markAllAsRead() {
    return request.put('/notifications/mark-all-read')
}

// 管理员：获取所有通知
export function getAllNotifications(params) {
    return request.get('/notifications/admin/all', { params })
}

// 管理员：发送系统通知
export function sendSystemNotification(data) {
    return request.post('/notifications/admin/system', data)
}

// 管理员：发送重要通知
export function sendImportantNotification(data) {
    return request.post('/notifications/admin/important', data)
}