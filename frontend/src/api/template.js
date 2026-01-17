import request from '@/utils/request'

/**
 * 获取启用的风格模板列表
 */
export function getEnabledTemplates() {
  return request({
    url: '/style-template/enabled',
    method: 'get'
  })
}

/**
 * 获取所有风格模板（管理员）
 */
export function getAllTemplates() {
  return request({
    url: '/style-template',
    method: 'get'
  })
}

/**
 * 创建风格模板（管理员）
 */
export function createTemplate(data) {
  return request({
    url: '/style-template',
    method: 'post',
    data
  })
}

/**
 * 更新风格模板（管理员）
 */
export function updateTemplate(id, data) {
  return request({
    url: `/style-template/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除风格模板（管理员）
 */
export function deleteTemplate(id) {
  return request({
    url: `/style-template/${id}`,
    method: 'delete'
  })
}
