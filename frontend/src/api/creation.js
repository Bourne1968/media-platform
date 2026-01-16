import request from '@/utils/request'

/**
 * 创建创作记录
 */
export function createRecord(data, autoGenerate = false) {
  return request({
    url: '/creation',
    method: 'post',
    params: { autoGenerate },
    data
  })
}

/**
 * 获取创作记录列表
 */
export function getRecordList(params) {
  return request({
    url: '/creation',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取创作记录
 */
export function getRecordById(id) {
  return request({
    url: `/creation/${id}`,
    method: 'get'
  })
}

/**
 * 更新创作记录
 */
export function updateRecord(id, data) {
  return request({
    url: `/creation/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除创作记录
 */
export function deleteRecord(id) {
  return request({
    url: `/creation/${id}`,
    method: 'delete'
  })
}
