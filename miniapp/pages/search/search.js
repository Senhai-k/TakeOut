const menuData = require('../../services/menu-data')
const { money } = require('../../utils/format')

Page({
  data: {
    keyword: '',
    searched: false,
    activeFilter: '全部',
    hotKeywords: ['麻辣拉面', '披萨', '奶茶', '汉堡', '烧烤', '轻食'],
    filters: [
      { name: '全部', icon: '🔥' },
      { name: '商家', icon: '🏪' },
      { name: '菜品', icon: '🍱' },
      { name: '披萨', icon: '🍕' },
      { name: '拉面', icon: '🍜' },
      { name: '小吃', icon: '🥟' },
      { name: '饮品', icon: '🥤' }
    ],
    allResults: [],
    results: []
  },

  onLoad(options = {}) {
    const keyword = decodeURIComponent(options.keyword || '')
    if (keyword) {
      this.setData({
        keyword,
        searched: true
      })
    }
    this.buildResults()
    this.refreshResults()
  },

  buildResults() {
    const themeTextMap = {
      pizza: '披萨 西餐',
      ramen: '拉面 面食',
      bbq: '烧烤 夜宵'
    }
    const merchants = menuData.getMerchants().map(item => ({
      id: item.id,
      type: 'merchant',
      name: item.name,
      desc: item.notice,
      rating: item.rating,
      time: item.time,
      price: item.minOrder,
      icon: item.icon,
      theme: item.theme,
      searchText: `${item.name} ${item.notice} ${themeTextMap[item.theme] || ''}`
    }))
    const dishes = menuData.getAllDishes().map(item => {
      const merchant = menuData.getMerchantById(item.merchantId)
      return {
        id: item.id,
        type: 'dish',
        name: item.name,
        desc: item.desc,
        rating: item.rating,
        time: merchant.name,
        price: `¥${money(item.price)}`,
        icon: item.icon,
        theme: item.theme,
        searchText: `${item.name} ${item.desc} ${item.category} ${merchant.name}`
      }
    })
    this.setData({
      allResults: merchants.concat(dishes)
    })
  },

  onInput(event) {
    this.setData({
      keyword: event.detail.value,
      searched: Boolean(event.detail.value)
    })
    this.refreshResults()
  },

  onSearch() {
    this.setData({ searched: true })
    this.refreshResults()
  },

  clearKeyword() {
    this.setData({
      keyword: '',
      searched: false
    })
    this.refreshResults()
  },

  tapKeyword(event) {
    this.setData({
      keyword: event.currentTarget.dataset.keyword,
      searched: true
    })
    this.refreshResults()
  },

  selectFilter(event) {
    this.setData({
      activeFilter: event.currentTarget.dataset.name
    })
    this.refreshResults()
  },

  refreshResults() {
    const keyword = this.data.keyword.trim()
    const activeFilter = this.data.activeFilter
    const results = this.data.allResults.filter(item => {
      const searchText = item.searchText || `${item.name} ${item.desc}`
      const matchesKeyword = !keyword || searchText.indexOf(keyword) >= 0
      const matchesFilter =
        activeFilter === '全部' ||
        (activeFilter === '商家' && item.type === 'merchant') ||
        (activeFilter === '菜品' && item.type === 'dish') ||
        searchText.indexOf(activeFilter) >= 0
      return matchesKeyword && matchesFilter
    })
    this.setData({ results })
  },

  openResult(event) {
    const type = event.currentTarget.dataset.type
    const id = event.currentTarget.dataset.id
    if (type === 'dish') {
      wx.navigateTo({
        url: `/pages/dish-detail/dish-detail?id=${id}`
      })
      return
    }
    wx.navigateTo({
      url: `/pages/merchant-menu/merchant-menu?id=${id}`
    })
  }
})
