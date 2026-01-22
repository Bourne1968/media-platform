import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 60000 // 60秒超时
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    // 如果返回的状态码为200，说明接口请求成功
    if (res.code === 200) {
      return res
    } else {
      // 非200状态码，检查是否是需要静默处理的错误
      const errorMsg = res.message || '请求失败'
      const errorStr = String(errorMsg).toLowerCase()
      
      // 检查各种可能的错误消息格式（不区分大小写）
      if (errorStr.includes('request method') && errorStr.includes('not supported')) {
        // 静默处理，不显示错误提示
        return Promise.reject(new Error(errorMsg))
      }
      if (errorStr.includes('系统异常') && errorStr.includes('request method')) {
        // 静默处理，不显示错误提示
        return Promise.reject(new Error(errorMsg))
      }
      if (errorStr.includes('系统异常：request method') || errorStr.includes('系统异常:request method')) {
        // 静默处理，不显示错误提示
        return Promise.reject(new Error(errorMsg))
      }
      
      // 其他错误正常显示
      ElMessage.error(errorMsg)
      return Promise.reject(new Error(errorMsg))
    }
  },
  error => {
    // 处理HTTP错误
    let message = '请求失败'
    let shouldShowMessage = true
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请先登录'
          // 清除token，跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
          break
        case 403:
          message = '禁止访问'
          break
        case 404:
          // 404错误不显示提示，静默处理
          shouldShowMessage = false
          break
        case 405:
          // 405错误（方法不支持）不显示提示，静默处理
          shouldShowMessage = false
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          // 检查是否是"Request method 'GET' is not supported"这类错误
          // 尝试多种可能的错误消息路径
          const errorData = error.response.data || {}
          const errorMsg = errorData.message || errorData.data?.message || errorData.error || ''
          const errorStr = String(errorMsg).toLowerCase()
          
          // 检查各种可能的错误消息格式（不区分大小写）
          if (errorStr.includes('request method') && errorStr.includes('not supported')) {
            shouldShowMessage = false
          } else if (errorStr.includes('系统异常') && errorStr.includes('request method')) {
            shouldShowMessage = false
          } else if (errorStr.includes('系统异常：request method')) {
            shouldShowMessage = false
          } else if (errorStr.includes('系统异常:request method')) {
            shouldShowMessage = false
          } else {
            message = errorMsg || `请求失败: ${error.response.status}`
          }
      }
    } else if (error.request) {
      message = '网络错误，请检查网络连接'
    } else {
      message = error.message
    }
    
    // 只在需要时显示错误提示
    if (shouldShowMessage) {
      ElMessage.error(message)
    }
    
    return Promise.reject(error)
  }
)

export default request
