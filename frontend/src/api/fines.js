import request from '@/utils/request'

// 核心修复：参数名从 page/size 改为 pageNum/pageSize，适配后端 Pageable 接收格式
export function getMyFines(params = { pageNum: 1, pageSize: 10, payStatus: '' }) {
    return request({
        url: '/fines/my-fines',
        method: 'get',
        params: {
            pageNum: params.pageNum || 1, // 前端页码从1开始，后端自动转换为Pageable
            pageSize: params.pageSize || 10,
            payStatus: params.payStatus || undefined // 空状态不传递，避免后端处理无效参数
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