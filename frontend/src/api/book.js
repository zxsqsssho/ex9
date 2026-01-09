// src/api/book.js
// 图书相关API请求
import request from '@/utils/request' // 假设你有请求工具类

export function getBookDetail(id) {
    return request({
        url: `/books/${id}`,
        method: 'get'
    })
}

// 根据需要添加其他图书相关接口