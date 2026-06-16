App({
  onLaunch() {
    // 检查登录态
    const token = wx.getStorageSync('token')
    if (!token) {
      // 自动微信登录
      this.autoLogin()
    }
  },

  async autoLogin() {
    try {
      const { code } = await wx.login()
      const res = await this.request('/auth/wechat/login', { method: 'POST', data: { code } })
      if (res.bound) {
        wx.setStorageSync('token', res.token)
      }
    } catch {
      // 静默失败，用户可手动登录
    }
  },

  request(url, options = {}) {
    const token = wx.getStorageSync('token')
    const header = { 'Content-Type': 'application/json' }
    if (token) header.Authorization = `Bearer ${token}`

    return new Promise((resolve, reject) => {
      wx.request({
        url: this.globalData.baseUrl + url,
        method: options.method || 'GET',
        data: options.data,
        header,
        success(res) {
          if (res.data.code === 200) resolve(res.data.data)
          else reject(res.data)
        },
        fail: reject
      })
    })
  },

  globalData: {
    baseUrl: 'http://localhost:8080/api',
    token: '',
    userInfo: null
  }
})
