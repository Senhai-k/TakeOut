const app = getApp()
const authService = require('../../services/auth')
const favoriteService = require('../../services/favorite')
const profileService = require('../../services/profile')

Page({
  data: {
    baseUrl: '',
    user: null,
    favoriteCount: 0,
    loggingIn: false,
    profileStats: profileService.getProfileStats(null),
    menus: [
      { icon: '⏱', title: '订单历史', action: 'goOrders' },
      { icon: '🏪', title: '商家订单', action: 'goMerchantOrders' },
      { icon: '♡', title: '收藏商家', action: 'goFavorites' },
      { icon: '▣', title: '支付方式', action: '' },
      { icon: '⌖', title: '收货地址', action: 'goAddresses' },
      { icon: '⚙', title: '设置', action: '' }
    ]
  },

  onLoad() {
    const user = authService.getStoredUser()
    this.setData({
      baseUrl: app.globalData.baseUrl,
      user,
      favoriteCount: favoriteService.listFavorites().length,
      profileStats: this.buildStats()
    })
    this.refreshProfileStats(user)
  },

  onShow() {
    const user = authService.getStoredUser()
    this.setData({
      user,
      favoriteCount: favoriteService.listFavorites().length,
      profileStats: this.buildStats()
    })
    this.refreshProfileStats(user)
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

  goFavorites() {
    const favorites = favoriteService.listFavorites()
    if (favorites.length === 0) {
      wx.showToast({
        title: '暂无收藏商家',
        icon: 'none'
      })
      return
    }
    wx.navigateTo({
      url: `/pages/merchant-menu/merchant-menu?id=${favorites[0].id}`
    })
  },

  async login() {
    if (this.data.loggingIn) {
      return
    }
    this.setData({ loggingIn: true })
    try {
      const user = await authService.loginDefaultUser()
      this.setData({
        user,
        profileStats: this.buildStats()
      })
      await this.refreshProfileStats(user)
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
    this.setData({
      user: null,
      profileStats: this.buildStats()
    })
    wx.showToast({
      title: '已退出登录',
      icon: 'none'
    })
  },

  handleMenu(event) {
    const action = event.currentTarget.dataset.action
    if (!this.data.user && ['goOrders', 'goAddresses'].includes(action)) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
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
    if (action === 'goFavorites') {
      this.goFavorites()
      return
    }
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    })
  },

  buildStats() {
    return profileService.getProfileStats()
  },

  async refreshProfileStats(user) {
    const profileStats = await profileService.loadProfileStats(user)
    if (this.data.user === user) {
      this.setData({ profileStats })
    }
  }
})
