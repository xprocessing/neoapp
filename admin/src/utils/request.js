import axios from 'axios'
import { MessagePlugin } from 'tdesign-vue-next'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 注入 token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    // 业务错误
    if (res.code === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
      MessagePlugin.error('登录已过期，请重新登录')
    } else {
      MessagePlugin.error(res.msg || '请求失败')
    }
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    console.error('响应错误:', error)
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
        MessagePlugin.error('登录已过期，请重新登录')
      } else {
        MessagePlugin.error(data?.msg || `服务器错误(${status})`)
      }
    } else {
      MessagePlugin.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
