const api = require('../../utils/request')

Page({
  data: {
    task: null, order: null, claiming: false,
    submitNote: '', uploadList: [], submitting: false, withdrawing: false
  },
  onLoad(options) {
    this.loadTask(options.id)
  },
  async loadTask(id) {
    try {
      this.setData({ task: await api.get(`/user/tasks/${id}`) })
      const r = await api.get('/user/tasks/orders', { page: 1, size: 100 })
      const orders = r.records || []
      const order = orders.find(o => o.taskId === Number(id))
      if (order) this.setData({ order })
    } catch {}
  },
  async doClaim() {
    this.setData({ claiming: true })
    try { await api.post(`/user/tasks/${this.data.task.id}/claim`); wx.showToast({ title: '领取成功' }); this.loadTask(this.data.task.id) } catch {}
    this.setData({ claiming: false })
  },
  onNoteInput(e) { this.setData({ submitNote: e.detail.value }) },
  uploadImg() {
    wx.chooseMedia({ count: 1, mediaType: ['image'], success: res => {
      const path = res.tempFiles[0].tempFilePath
      wx.uploadFile({
        url: 'http://localhost:8080/api/user/upload/avatar',
        filePath: path, name: 'file',
        header: { Authorization: `Bearer ${wx.getStorageSync('token')}` },
        success: res => {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            const list = this.data.uploadList
            list.push(data.data)
            this.setData({ uploadList: list })
          }
        }
      })
    }})
  },
  async doSubmit() {
    if (!this.data.submitNote) { wx.showToast({ title: '请填写订单号', icon: 'none' }); return }
    this.setData({ submitting: true })
    try {
      await api.put(`/user/tasks/orders/${this.data.order.id}/submit`, {
        submitNote: this.data.submitNote, submitImages: this.data.uploadList.join(',')
      })
      wx.showToast({ title: '已提交', icon: 'success' })
      this.loadTask(this.data.task.id)
    } catch {}
    this.setData({ submitting: false })
  },
  async doWithdraw() {
    this.setData({ withdrawing: true })
    try { await api.post(`/user/tasks/orders/${this.data.order.id}/withdraw`); wx.showToast({ title: '已提交提现' }); this.loadTask(this.data.task.id) } catch {}
    this.setData({ withdrawing: false })
  }
})
