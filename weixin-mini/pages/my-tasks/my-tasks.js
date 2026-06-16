const api = require('../../utils/request')

Page({
  data: {
    orders: [],
    statusText: { 1: '待提交', 2: '待审核', 3: '已通过', 4: '已驳回', 5: '已过期' }
  },
  onShow() { this.loadOrders() },
  async loadOrders() {
    try { const r = await api.get('/user/tasks/orders'); this.setData({ orders: r.records || [] }) } catch {}
  },
  async withdraw(e) {
    const id = e.currentTarget.dataset.id
    try { await api.post(`/user/tasks/orders/${id}/withdraw`); wx.showToast({ title: '已提交提现' }); this.loadOrders() } catch {}
  },
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/task-detail/task-detail?id=${id}` })
  }
})
