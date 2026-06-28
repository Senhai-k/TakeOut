const menuData = require('../../services/menu-data')
const { money } = require('../../utils/format')

Page({
  data: {
    categories: [
      { name: '披萨', icon: '🍕' },
      { name: '拉面', icon: '🍜' },
      { name: '烧烤', icon: '🍢' },
      { name: '小吃', icon: '🥟' },
      { name: '饮品', icon: '🥤' },
      { name: '主食', icon: '🍛' }
    ],
    merchants: []
  },

  onLoad() {
    this.setData({
      merchants: menuData.getMerchants().map(item => ({
        ...item,
        desc: item.notice,
        deliveryFeeText: `配送 ¥${money(item.deliveryFee)}`,
        tags: this.getMerchantTags(item.theme)
      }))
    })
  },

  getMerchantTags(theme) {
    const tagMap = {
      pizza: ['现烤', '多人套餐'],
      ramen: ['汤面分装', '工作餐'],
      bbq: ['夜宵', '满减']
    }
    return tagMap[theme] || ['好评优选']
  },

  goSearch() {
    wx.navigateTo({
      url: '/pages/search/search'
    })
  },

  goSearchByCategory(event) {
    wx.navigateTo({
      url: `/pages/search/search?keyword=${encodeURIComponent(event.currentTarget.dataset.name)}`
    })
  },

  goMerchant(event) {
    wx.navigateTo({
      url: `/pages/merchant-menu/merchant-menu?id=${event.currentTarget.dataset.id}`
    })
  }
})
