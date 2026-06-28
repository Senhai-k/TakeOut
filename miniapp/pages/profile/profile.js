const app = getApp()
const authService = require('../../services/auth')

Page({
  data: {
    baseUrl: '',
    user: null,
    loggingIn: false,
    menus: [
      { icon: '⏱', title: '订单历史', action: 'goOrders' },
      { icon: '🏪', title: '商家订单', action: 'goMerchantOrders' },
      { icon: '♡', title: '收藏商家', action: '' },
      { icon: '▣', title: '支付方式', action: '' },
      { icon: '⌖', title: '收货地址', action: 'goAddresses' },
      { icon: '⚙', title: '设置', action: '' }
    ]
  },

  onLoad() {
    this.setData({
      baseUrl: app.globalData.baseUrl,
      user: authService.getStoredUser()
    })
  },

  onShow() {
    this.setData({
      user: authService.getStoredUser()
    })
  },

  goOrders() {
    wx.switchTab({
      url: '/pages/order-list/order-list'
    })
  },

  goAddresses() {
    wx.navigateTo({
      url: '/pages/address-list/address-list'
    })
  },

  goMerchantOrders() {
    wx.navigateTo({
      url: '/pages/merchant-orders/merchant-orders'
    })
  },

  async login() {
    if (this.data.loggingIn) {
      return
    }
    this.setData({ loggingIn: true })
    try {
      const user = await authService.loginDefaultUser()
      this.setData({ user })
      wx.showToast({
        title: '登录成功',
        icon: 'success'
      })
    } finally {
      this.setData({ loggingIn: false })
    }
  },

  logout() {
    authService.clearUser()
    this.setData({ user: null })
    wx.showToast({
      title: '已退出登录',
      icon: 'none'
    })
  },

  handleMenu(event) {
    const action = event.currentTarget.dataset.action
    if (action === 'goOrders') {
      this.goOrders()
      return
    }
    if (action === 'goAddresses') {
      this.goAddresses()
      return
    }
    if (action === 'goMerchantOrders') {
      this.goMerchantOrders()
      return
    }
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    })
  }
})
