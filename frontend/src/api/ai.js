import request from '@/utils/request'

/**
 * 生成文本
 */
export function generateText(data) {
  return request({
    url: '/ai/text',
    method: 'post',
    data
  })
}

/**
 * 生成图片
 */
export function generateImage(data) {
  return request({
    url: '/ai/image',
    method: 'post',
    data
  })
}
