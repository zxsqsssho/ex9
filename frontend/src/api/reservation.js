// src/api/reservation.js 完整修复
import request from '@/utils/request'

// 核心修改：接收 status 参数并传递
export function getMyReservations(params = { pageNum: 1, pageSize: 10, status: '' }) {
    return request({
        url: '/reservation/my-reservation',
        method: 'get',
        params: {
            page: params.pageNum - 1, // 后端page从0开始
            size: params.pageSize,
            status: params.status || undefined // 空值不传递，避免筛选干扰
        }
    })
}

// 其他方法保持不变...
export function cancelReservation(reservationId) {
    return request({
        url: `/reservation/cancel/${reservationId}`,
        method: 'delete'
    })
}

export function reserveBook(bookId, branchId) {
    return request({
        url: '/reservation/reserve',
        method: 'post',
        data: { bookId, branchId }
    })
}

export function getBookReservationQueue(bookId) {
    return request({
        url: `/reservation/book/${bookId}/queue`,
        method: 'get'
    })
}

export function completeReservation(reservationId) {
    return request({
        url: `/reservation/${reservationId}/complete`,
        method: 'post'
    })
}

export function updateReservationStatus(reservationId) {
    return request({
        url: `/reservation/${reservationId}/update-status`,
        method: 'post'
    })
}

export function getAllReservations(params) {
    return request({
        url: '/reservation/all',
        method: 'get',
        params: params
    })
}