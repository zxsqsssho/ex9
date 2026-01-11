import request from '@/utils/request'

// 获取通知列表
export function getNotifications(params) {
    return request({
        url: '/api/notifications',
        method: 'get',
        params
    })
}

// 获取通知详情
export function getNotificationDetail(id) {
    return request({
        url: `/api/notifications/${id}`,
        method: 'get'
    })
}

// 重试发送通知
export function retryNotification(id) {
    return request({
        url: `/api/notifications/${id}/retry`,
        method: 'post'
    })
}

// 手动触发预定检查
export function checkReservations() {
    return request({
        url: '/api/demo/check/reservations',
        method: 'get'
    })
}

// 手动触发逾期检查
export function checkOverdue() {
    return request({
        url: '/api/demo/check/overdue',
        method: 'get'
    })
}

// 手动发送待处理邮件
export function sendPendingEmails() {
    return request({
        url: '/api/demo/send/emails',
        method: 'get'
    })
}