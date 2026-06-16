const api = require('../../utils/request')

Page({
  data: { tasks: [], claimedIds: {} },
  onShow() {
    this.loadTasks()
    this.loadClaimed()
  },
  async loadTasks() {
    try { const r = await api.get('/user/tasks'); this.setData({ tasks: r.records || [] }) } catch {}
  },
  async loadClaimed() {
    try {
      const r = await api.get('/user/tasks/orders', { page: 1, size: 100 })
      const ids = {}
      ;(r.records || []).forEach(o => ids[o.taskId] = true)
      this.setData({ claimedIds: ids })
    } catch {}
  },
  async doClaim(e) {
    const id = e.currentTarget.dataset.id
    if (this.data.claimedIds[id]) return
    try {
      await api.post(`/user/tasks/${id}/claim`)
      wx.showToast({ title: '领取成功', icon: 'success' })
      this.loadClaimed()
    } catch {}
  },
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/task-detail/task-detail?id=${id}` })
  }
})
