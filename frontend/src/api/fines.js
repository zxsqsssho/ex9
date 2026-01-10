// src/api/fines.js
// 罚款相关API请求
import request from '@/utils/request'

// 获取我的罚款记录
export function getMyFines() {
    return request({
        url: '/fines/my-fines',
        method: 'get'
    })
}

// 支付罚款
export function payFine(fineId) {
    return request({
        url: `/fines/pay/${fineId}`,
        method: 'post'
    })
}

// 批量支付罚款
export function batchPayFines(fineIds) {
    return request({
        url: '/fines/batch-pay',
        method: 'post',
        data: { fineIds }
    })
}

// 获取罚款详情
export function getFineDetail(fineId) {
    return request({
        url: `/fines/${fineId}`,
        method: 'get'
    })
}

// 申请减免罚款
export function applyFineReduction(fineId, reason) {
    return request({
        url: `/fines/${fineId}/apply-reduction`,
        method: 'post',
        data: { reason }
    })
}

// 获取所有罚款记录（管理员用）
export function getAllFines(params) {
    return request({
        url: '/fines/all',
        method: 'get',
        params: params
    })
}

// 更新罚款状态（管理员用）
export function updateFineStatus(fineId, status) {
    return request({
        url: `/fines/${fineId}/status`,
        method: 'put',
        data: { status }
    })
}