const api = require('../../utils/request')

Page({
  data: { member: { isMember: false }, packages: [], buying: false },
  onShow() { this.loadData() },
  async loadData() {
    try { this.setData({ packages: await api.get('/user/member/packages') }) } catch {}
    try { this.setData({ member: await api.get('/user/member/info') }) } catch {}
  },
  async buy(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ buying: true })
    try {
      const r = await api.post(`/user/member/buy/${id}`)
      if (r.h5_url) {
        wx.showToast({ title: '请使用Web端支付', icon: 'none' })
      } else {
        wx.showToast({ title: '购买成功' })
        this.loadData()
      }
    } catch {}
    this.setData({ buying: false })
  }
})
