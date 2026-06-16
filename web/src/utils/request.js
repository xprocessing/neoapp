import axios from 'axios'
import { MessagePlugin } from 'tdesign-vue-next'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  // Content-Type 由 axios 自动检测：JSON 数据自动设 application/json，FormData 自动设 multipart/form-data
})

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

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    MessagePlugin.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    console.error('响应错误:', error)
    if (error.response) {
      const { data } = error.response
      MessagePlugin.error(data?.msg || '服务器错误')
    } else {
      MessagePlugin.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
