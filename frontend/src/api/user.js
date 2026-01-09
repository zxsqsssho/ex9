// 用户相关API请求
import request from '@/utils/request' // 假设你有请求工具类

export function getUserList(params) {
    return request({
        url: '/users',
        method: 'get',
        params
    })
}

// 根据需要添加其他用户相关接口