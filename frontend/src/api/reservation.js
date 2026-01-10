// src/api/reservation.js
// 预定相关API请求
import request from '@/utils/request'

// 获取我的预定列表
export function getMyReservations() {
    return request({
        url: '/reservation/my-reservation',
        method: 'get'
    })
}

// 取消预定
export function cancelReservation(reservationId) {
    return request({
        url: `/reservation/cancel/${reservationId}`,
        method: 'delete'
    })
}

// 预定图书
export function reserveBook(bookId) {
    return request({
        url: '/reservation/reserve',
        method: 'post',
        data: { bookId }
    })
}

// 获取图书的预定队列
export function getBookReservationQueue(bookId) {
    return request({
        url: `/reservation/book/${bookId}/queue`,
        method: 'get'
    })
}

// 完成预定（管理员确认预定完成）
export function completeReservation(reservationId) {
    return request({
        url: `/reservation/${reservationId}/complete`,
        method: 'post'
    })
}

// 获取所有预定记录（管理员用）
export function getAllReservations(params) {
    return request({
        url: '/reservation/all',
        method: 'get',
        params: params
    })
}