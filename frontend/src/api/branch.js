// src/api/branch.js
// 图书馆分馆相关API
import request from '@/utils/request'

// 获取所有分馆列表
export function getAllBranches() {
    return request({
        url: '/branches',
        method: 'get'
    })
}

// 获取分馆详情
export function getBranchDetail(branchId) {
    return request({
        url: `/branches/${branchId}`,
        method: 'get'
    })
}

// 获取分馆的图书库存
export function getBranchInventory(branchId, params) {
    return request({
        url: `/branches/${branchId}/inventory`,
        method: 'get',
        params: params
    })
}

// 获取分馆开放时间
export function getBranchOpeningHours(branchId) {
    return request({
        url: `/branches/${branchId}/hours`,
        method: 'get'
    })
}

// 搜索附近分馆
export function searchNearbyBranches(latitude, longitude, radius = 10) {
    return request({
        url: '/branches/nearby',
        method: 'get',
        params: { latitude, longitude, radius }
    })
}