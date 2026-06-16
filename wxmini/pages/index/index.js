Page({
  data: {
    year: new Date().getFullYear(),
    features: [
      { emoji: '⚡', title: '高效管理', desc: '直观操作界面，轻松管理业务', bgClass: 'bg-blue' },
      { emoji: '🛡️', title: '安全可靠', desc: '多层安全防护，数据无忧', bgClass: 'bg-green' },
      { emoji: '📊', title: '数据分析', desc: '数据可视化，辅助精准决策', bgClass: 'bg-orange' },
      { emoji: '👥', title: '团队协作', desc: '灵活权限管理，高效协作', bgClass: 'bg-purple' },
    ]
  },
  onLoad() {
    console.log('首页加载')
  }
})
