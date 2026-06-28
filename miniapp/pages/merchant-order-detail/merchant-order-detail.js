const merchantOrderDetailService = require('../../services/merchant-order-detail')
const merchantOrderService = require('../../services/merchant-order')
const { money, orderStatusText } = require('../../utils/format')

Page({
  data: {
    order: null,
    steps: []
  },

  async onLoad(options) {
    await this.loadOrder(options.id)
  },

  async onShow() {
    if (this.data.order) {
      await this.loadOrder(this.data.order.id)
    }
  },

  async loadOrder(id) {
    try {
      const order = await merchantOrderDetailService.getOrderDetail(id)
      if (!order) {
        this.setData({ order: null, steps: [] })
        return
      }
      this.setData({
        order: {
          ...order,
          statusText: orderStatusText(order.orderStatus),
          goodsAmountText: money(order.goodsAmount),
          deliveryFeeText: money(order.deliveryFee),
          payAmountText: money(order.payAmount),
          canAccept: Number(order.orderStatus) === 20,
          canReject: Number(order.orderStatus) === 20,
          canToCooking: Number(order.orderStatus) === 30,
          canToDelivering: Number(order.orderStatus) === 40,
          canFinish: Number(order.orderStatus) === 50
        },
        steps: this.getSteps(order.orderStatus)
      })
    } catch (error) {
      this.setData({ order: null, steps: [] })
      wx.showToast({
        title: error.message || '加载订单失败',
        icon: 'none'
      })
    }
  },

  async acceptOrder() {
    if (!this.data.order) return
    try {
      await merchantOrderService.acceptOrder(this.data.order.id)
      wx.showToast({ title: '已接单', icon: 'success' })
      await this.loadOrder(this.data.order.id)
    } catch (error) {
      wx.showToast({ title: error.message || '接单失败', icon: 'none' })
    }
  },

  async rejectOrder() {
    if (!this.data.order) return
    wx.showModal({
      title: '拒单原因',
      content: '确认拒单并使用默认原因吗？',
      confirmText: '拒单',
      success: async result => {
        if (!result.confirm) return
        try {
          await merchantOrderService.rejectOrder(this.data.order.id, '商品已售罄')
          wx.showToast({ title: '已拒单', icon: 'success' })
          await this.loadOrder(this.data.order.id)
        } catch (error) {
          wx.showToast({ title: error.message || '拒单失败', icon: 'none' })
        }
      }
    })
  },

  async updateStatus(event) {
    if (!this.data.order) return
    const status = Number(event.currentTarget.dataset.status)
    try {
      await merchantOrderService.updateOrderStatus(this.data.order.id, status)
      wx.showToast({ title: '状态已更新', icon: 'success' })
      await this.loadOrder(this.data.order.id)
    } catch (error) {
      wx.showToast({ title: error.message || '更新失败', icon: 'none' })
    }
  },

  goBack() {
    wx.navigateBack()
  },

  getSteps(status) {
    const currentIndex = status === 70 ? 0 : status >= 60 ? 4 : status >= 50 ? 3 : status >= 40 ? 2 : status >= 30 ? 1 : 0
    return [
      { title: '已下单', desc: '订单已提交成功' },
      { title: '商家接单', desc: '商家已确认订单' },
      { title: '制作中', desc: '餐品正在制作' },
      { title: '配送中', desc: '订单正在配送' },
      { title: '已完成', desc: '订单完成' }
    ].map((item, index) => ({
      ...item,
      state: index < currentIndex ? 'done' : index === currentIndex ? 'current' : 'pending'
    }))
  }
})
