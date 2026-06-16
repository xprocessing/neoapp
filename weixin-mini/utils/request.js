const BASE = 'http://localhost:8080/api'

function request(url, options = {}) {
  const token = wx.getStorageSync('token')
  const header = { 'Content-Type': 'application/json' }
  if (token) header.Authorization = `Bearer ${token}`

  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE + url,
      method: options.method || 'GET',
      data: options.data,
      header,
      success(res) {
        if (res.data.code === 200) {
          resolve(res.data.data)
        } else if (res.data.code === 401) {
          wx.removeStorageSync('token')
          wx.showToast({ title: '请重新登录', icon: 'none' })
          reject(res.data)
        } else {
          wx.showToast({ title: res.data.msg || '请求失败', icon: 'none' })
          reject(res.data)
        }
      },
      fail(err) {
        wx.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = {
  get: (url, data) => request(url, { method: 'GET', data }),
  post: (url, data) => request(url, { method: 'POST', data }),
  put: (url, data) => request(url, { method: 'PUT', data }),
  del: (url, data) => request(url, { method: 'DELETE', data }),
}
