App({
  onLaunch() {
    const authService = require('./services/auth')
    authService.loginCurrentUser()
  },
  globalData: {
    baseUrl: 'http://127.0.0.1:8080/api',
    wechatLoginEnabled: false
  }
})
