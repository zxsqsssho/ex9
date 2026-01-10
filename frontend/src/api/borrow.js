// src/api/borrow.js
// 借阅相关API请求
import request from '@/utils/request'

// 获取当前借阅列表
export function getMyBorrowList() {
    return request({
        url: '/borrow/my-borrow',
        method: 'get'
    })
}

// 获取借阅历史
export function getMyBorrowHistory() {
    return request({
        url: '/borrow/my-history',
        method: 'get'
    })
}

// 归还图书
export function returnBook(borrowId) {
    return request({
        url: `/borrow/return/${borrowId}`,
        method: 'post'
    })
}

// 续借图书
export function renewBook(bookId) {
    return request({
        url: `/borrow/renew/${bookId}`,
        method: 'post'
    })
}

// 获取所有借阅记录（管理员用）
export function getAllBorrowRecords(params) {
    return request({
        url: '/borrow/all',
        method: 'get',
        params: params
    })
}

// 更新借阅记录状态（管理员用）
export function updateBorrowStatus(borrowId, status) {
    return request({
        url: `/borrow/${borrowId}/status`,
        method: 'put',
        data: { status }
    })
}

// 批量归还图书
export function batchReturnBooks(borrowIds) {
    return request({
        url: '/borrow/batch-return',
        method: 'post',
        data: { borrowIds }
    })
}