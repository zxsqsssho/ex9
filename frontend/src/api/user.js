// src/api/user.js
import request from '@/utils/request'

// 获取用户列表
export function getUserList(params) {
    return request({
        url: '/users',
        method: 'get',
        params
    })
}

// 更新用户状态
export function updateUserStatus(id, status) {
    return request({
        url: `/users/${id}/status/${status}`,
        method: 'patch'
    })
}

// 删除用户
export function deleteUser(id) {
    return request({
        url: `/users/${id}`,
        method: 'delete'
    })
}

// 保存用户（创建或更新）
export function saveUser(data) {
    if (data.id) {
        // 更新
        return request({
            url: `/users/${data.id}`,
            method: 'put',
            data
        })
    } else {
        // 创建
        return request({
            url: '/users',
            method: 'post',
            data
        })
    }
}

// 根据ID获取用户
export function getUserById(id) {
    return request({
        url: `/users/${id}`,
        method: 'get'
    })
}

// 搜索用户
export function searchUsers(keyword, page = 0, size = 10) {
    return request({
        url: '/users/search',
        method: 'get',
        params: {
            keyword,
            page,
            size
        }
    })
}

// 重置密码
export function resetPassword(id) {
    return request({
        url: `/users/${id}/reset-password`,
        method: 'post'
    })
}