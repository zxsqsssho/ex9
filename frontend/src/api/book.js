// src/api/book.js
// 图书相关API请求
import request from '@/utils/request'

// 获取图书详情
export function getBookDetail(id) {
    return request({
        url: `/books/${id}`,
        method: 'get'
    })
}

// 搜索图书
export function searchBooks(params) {
    return request({
        url: '/books/search',
        method: 'get',
        params: params
    })
}

// 获取图书分类
export function getBookCategories() {
    return request({
        url: '/books/categories',
        method: 'get'
    })
}

// 获取热门图书
export function getPopularBooks(limit = 10) {
    return request({
        url: '/books/popular',
        method: 'get',
        params: { limit }
    })
}

// 获取新书上架
export function getNewBooks(limit = 10) {
    return request({
        url: '/books/new',
        method: 'get',
        params: { limit }
    })
}

// 获取图书借阅统计
export function getBookBorrowStats(bookId) {
    return request({
        url: `/books/${bookId}/stats`,
        method: 'get'
    })
}

// 添加图书（管理员用）
export function addBook(bookData) {
    return request({
        url: '/books',
        method: 'post',
        data: bookData
    })
}

// 更新图书信息（管理员用）
export function updateBook(bookId, bookData) {
    return request({
        url: `/books/${bookId}`,
        method: 'put',
        data: bookData
    })
}

// 删除图书（管理员用）
export function deleteBook(bookId) {
    return request({
        url: `/books/${bookId}`,
        method: 'delete'
    })
}

// 获取图书库存信息
export function getBookInventory(bookId) {
    return request({
        url: `/books/${bookId}/inventory`,
        method: 'get'
    })
}

// 更新图书库存（管理员用）
export function updateBookInventory(bookId, inventoryData) {
    return request({
        url: `/books/${bookId}/inventory`,
        method: 'put',
        data: inventoryData
    })
}

// 获取相关推荐图书
export function getRelatedBooks(bookId, limit = 5) {
    return request({
        url: `/books/${bookId}/related`,
        method: 'get',
        params: { limit }
    })
}