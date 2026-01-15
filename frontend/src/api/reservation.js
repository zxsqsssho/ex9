import request from '@/utils/request'

// 核心保持不变：传递bookId和branchId为JSON格式
export function reserveBook(bookId, branchId) {
    return request({
        url: '/reservation/reserve',
        method: 'post',
        data: { bookId, branchId } // 确保是JSON格式，与后端DTO对应
    })
}

// 其他方法保持不变
export function getMyReservations(params = { pageNum: 1, pageSize: 10, status: '' }) {
    return request({
        url: '/reservation/my-reservation',
        method: 'get',
        params: {
            page: params.pageNum - 1,
            size: params.pageSize,
            status: params.status || undefined
        }
    })
}

export function cancelReservation(reservationId) {
    return request({
        url: `/reservation/cancel/${reservationId}`,
        method: 'delete'
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