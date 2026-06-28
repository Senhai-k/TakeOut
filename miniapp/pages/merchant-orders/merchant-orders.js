const merchantOrderService = require('../../services/merchant-order')
const merchantStatisticsService = require('../../services/merchant-statistics')
const { money, orderStatusText } = require('../../utils/format')

Page({
  data: {
    tabs: [
      { label: '全部', status: 0 },
      { label: '待接单', status: 20 },
      { label: '已接单', status: 30 },
      { label: '制作中', status: 40 },
      { label: '配送中', status: 50 },
      { label: '已完成', status: 60 }
    ],
    activeStatus: 0,
    orders: [],
    loading: false,
    overview: null
  },

  onShow() {
    this.loadOverview()
    this.loadOrders()
  },

  async loadOverview() {
    try {
      const overview = await merchantStatisticsService.getOverview()
      this.setData({
        overview: {
          ...overview,
          todaySalesAmountText: money(overview.todaySalesAmount)
        }
      })
    } catch (error) {
      this.setData({ overview: null })
    }
  },

  async loadOrders() {
    this.setData({ loading: true })
    try {
      const orders = await merchantOrderService.listOrders({ status: this.data.activeStatus || undefined })
      const normalized = orders.map(order => ({
        ...order,
        statusText: orderStatusText(order.orderStatus),
        payAmountText: money(order.payAmount),
        itemCount: (order.items || []).reduce((sum, item) => sum + Number(item.quantity || 0), 0),
        canAccept: Number(order.orderStatus) === 20,
        canReject: Number(order.orderStatus) === 20,
        canToCooking: Number(order.orderStatus) === 30,
        canToDelivering: Number(order.orderStatus) === 40,
        canFinish: Number(order.orderStatus) === 50
      }))
      this.setData({ orders: normalized })
    } finally {
      this.setData({ loading: false })
    }
  },

  async selectTab(event) {
    this.setData({
      activeStatus: Number(event.currentTarget.dataset.status || 0)
    })
    await this.loadOrders()
  },

  async acceptOrder(event) {
    const id = event.currentTarget.dataset.id
    await merchantOrderService.acceptOrder(id)
    wx.showToast({ title: '已接单', icon: 'success' })
    await this.loadOrders()
  },

  async rejectOrder(event) {
    const id = event.currentTarget.dataset.id
    wx.showModal({
      title: '拒单原因',
      content: '确认拒单并使用默认原因吗？',
      confirmText: '拒单',
      success: async result => {
        if (!result.confirm) return
        try {
          await merchantOrderService.rejectOrder(id, '商品已售罄')
          wx.showToast({ title: '已拒单', icon: 'success' })
          await this.loadOrders()
        } catch (error) {
          wx.showToast({ title: error.message || '拒单失败', icon: 'none' })
        }
      }
    })
  },

  async updateStatus(event) {
    const id = event.currentTarget.dataset.id
    const status = Number(event.currentTarget.dataset.status)
    try {
      await merchantOrderService.updateOrderStatus(id, status)
      wx.showToast({ title: '状态已更新', icon: 'success' })
      await this.loadOrders()
    } catch (error) {
      wx.showToast({ title: error.message || '更新失败', icon: 'none' })
    }
  },

  goDetail(event) {
    wx.navigateTo({
      url: `/pages/merchant-order-detail/merchant-order-detail?id=${event.currentTarget.dataset.id}`
    })
  }
})
