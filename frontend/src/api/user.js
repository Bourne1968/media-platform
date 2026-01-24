import request from '@/utils/request'

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 */
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 获取用户列表（管理员）
 */
export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

/**
 * 管理员删除单个用户
 */
export function deleteUserByAdmin(id) {
  return request({
    url: `/user/admin/${id}`,
    method: 'delete'
  })
}

/**
 * 管理员批量删除用户
 */
export function batchDeleteUsersByAdmin(ids) {
  return request({
    url: '/user/admin/batch-delete',
    method: 'post',
    data: ids
  })
}

/**
 * 管理员更新用户角色
 */
export function updateUserRoleByAdmin(id, role) {
  return request({
    url: `/user/admin/${id}/role`,
    method: 'put',
    data: { role }
  })
}

/**
 * 更新用户信息
 */
export function updateProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

/**
 * 更新用户头像
 */
export function updateAvatar(data) {
  return request({
    url: '/user/avatar',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 */
export function changePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

/**
 * 更新用户偏好设置
 */
export function updatePreferences(data) {
  return request({
    url: '/user/preferences',
    method: 'put',
    data
  })
}

/**
 * 获取用户偏好设置
 */
export function getPreferences() {
  return request({
    url: '/user/preferences',
    method: 'get'
  })
}

/**
 * 获取当前用户信息
 */
export function getProfile() {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

/**
 * 删除用户账户
 */
export function deleteAccount() {
  return request({
    url: '/user/delete',
    method: 'delete'
  })
}