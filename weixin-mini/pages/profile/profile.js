const api = require('../../utils/request')

Page({
  data: { user: null, points: {}, invite: {}, logining: false },
  onShow() {
    if (wx.getStorageSync('token')) this.loadData()
  },
  async doLogin() {
    this.setData({ logining: true })
    try {
      const { code } = await wx.login()
      const r = await api.post('/auth/wechat/login', { code })
      if (r.bound) {
        wx.setStorageSync('token', r.token)
        this.loadData()
      } else {
        wx.showToast({ title: '请先绑定手机号', icon: 'none' })
      }
    } catch {}
    this.setData({ logining: false })
  },
  async loadData() {
    try { this.setData({ user: await api.get('/user/info') }) } catch {}
    try { this.setData({ points: await api.get('/user/points') }) } catch {}
    try { this.setData({ invite: await api.get('/user/invites') }) } catch {}
  },
  doLogout() {
    wx.removeStorageSync('token')
    this.setData({ user: null, points: {}, invite: {} })
  }
})
