App({
  onLaunch() {
    console.log('NeoApp 小程序启动')
  },
  globalData: {
    baseUrl: 'http://localhost:8080/api',
    token: '',
    userInfo: null
  }
})
