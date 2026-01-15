// src/api/fines.js 完整修复代码（可直接替换）
import request from '@/utils/request'

// 核心修复：规范分页参数传递，兼容后端 Pageable
export function getMyFines(params = { pageNum: 1, pageSize: 10, payStatus: '' }) {
    return request({
        url: '/fines/my-fines',
        method: 'get',
        params: {
            page: params.pageNum - 1, // 后端 Pageable 从 0 开始，前端 pageNum 从 1 开始
            size: params.pageSize || 10, // 默认页大小 10
            payStatus: params.payStatus || undefined // 空状态不传递参数
        }
    })
}

// 其他方法保持不变
export function payFine(fineId) {
    return request({
        url: `/fines/pay/${fineId}`,
        method: 'post'
    })
}

export function batchPayFines(fineIds) {
    return request({
        url: '/fines/batch-pay',
        method: 'post',
        data: { fineIds }
    })
}

export function getFineDetail(fineId) {
    return request({
        url: `/fines/${fineId}`,
        method: 'get'
    })
}

export function applyFineReduction(fineId, reason) {
    return request({
        url: `/fines/${fineId}/apply-reduction`,
        method: 'post',
        data: { reason }
    })
}

export function getAllFines(params) {
    return request({
        url: '/fines/all',
        method: 'get',
        params: params
    })
}

export function updateFineStatus(fineId, status) {
    return request({
        url: `/fines/${fineId}/status`,
        method: 'put',
        data: { status }
    })
}