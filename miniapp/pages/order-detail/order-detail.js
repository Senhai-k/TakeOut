const orderService = require('../../services/order')
const cartService = require('../../services/cart')
const reviewService = require('../../services/review')
const { money, orderStatusText } = require('../../utils/format')

Page({
  data: {
    order: null,
    orderId: null,
    ratingOptions: [1, 2, 3, 4, 5],
    reviewDraft: {
      rating: 5,
      content: ''
    }
  },

  async onLoad(options) {
    this.setData({ orderId: options.id })
    try {
      const order = await orderService.getOrderDetail(options.id)
      this.setOrder(order)
    } catch (error) {
      this.setOrder(null)
      wx.showToast({
        title: error.message || '加载订单失败',
        icon: 'none'
      })
    }
  },

  setOrder(order) {
    if (!order) {
      this.setData({ order: null })
      return
    }
    const review = order.review || reviewService.getReview(order.id)
    this.setData({
      order: {
        ...order,
        review,
        displayNo: order.orderNo || `#${order.id}`,
        statusText: orderStatusText(order.orderStatus),
        etaTitle: this.getEtaTitle(order.orderStatus),
        etaDesc: this.getEtaDesc(order.orderStatus),
        driverStatus: this.getDriverStatus(order.orderStatus),
        goodsAmountText: money(order.goodsAmount),
        deliveryFeeText: money(order.deliveryFee),
        payAmountText: money(order.payAmount),
        paymentMethodName: order.paymentMethod ? order.paymentMethod.name : '模拟支付',
        canPay: order.orderStatus === 10,
        canCancel: order.orderStatus < 50 && order.orderStatus !== 70,
        canComplete: order.orderStatus === 50,
        canReorder: order.orderStatus >= 60 || order.orderStatus === 70,
        canReview: order.orderStatus === 60 && !review,
        items: order.items.map(item => ({
          ...item,
          initial: item.dishName ? item.dishName.substr(0, 1) : '餐',
          subtotalAmountText: money(item.subtotalAmount)
        }))
      },
      steps: this.getSteps(order.orderStatus)
    })
  },

  async mockPay() {
    if (!this.data.order) return
    try {
      const order = await orderService.mockPayOrder(this.data.order.id)
      this.setOrder(order)
      wx.showToast({ title: `${this.data.order.paymentMethodName}成功`, icon: 'success' })
    } catch (error) {
      wx.showToast({ title: error.message || '支付失败', icon: 'none' })
    }
  },

  async cancelOrder() {
    if (!this.data.order) return
    wx.showModal({
      title: '取消订单',
      content: '确定取消当前订单吗？',
      confirmText: '取消订单',
      success: async result => {
        if (!result.confirm) return
        try {
          const order = await orderService.cancelOrder(this.data.order.id)
          this.setOrder(order)
          wx.showToast({ title: '已取消', icon: 'success' })
        } catch (error) {
          wx.showToast({ title: error.message || '取消失败', icon: 'none' })
        }
      }
    })
  },

  async completeOrder() {
    if (!this.data.order) return
    try {
      const order = await orderService.completeOrder(this.data.order.id)
      this.setOrder(order)
      wx.showToast({ title: '已完成', icon: 'success' })
    } catch (error) {
      wx.showToast({ title: error.message || '操作失败', icon: 'none' })
    }
  },

  reorder() {
    const order = this.data.order
    if (!order) return
    const items = order.items.map(item => ({
      cartKey: cartService.createCartKey(item),
      shopId: order.shopId,
      shopName: order.shopName,
      deliveryFee: order.deliveryFee,
      dishId: item.dishId,
      dishName: item.dishName,
      dishImageUrl: item.dishImageUrl,
      dishPrice: item.dishPrice,
      size: item.size,
      spice: item.spice,
      notes: item.notes,
      quantity: item.quantity
    }))
    cartService.saveCartItems(items)
    wx.navigateTo({
      url: `/pages/order-confirm/order-confirm`
    })
  },

  setReviewRating(event) {
    this.setData({
      'reviewDraft.rating': Number(event.currentTarget.dataset.rating)
    })
  },

  onReviewInput(event) {
    this.setData({
      'reviewDraft.content': event.detail.value
    })
  },

  submitReview() {
    const order = this.data.order
    if (!order || !order.canReview) return
    const review = reviewService.saveReview({
      orderId: order.id,
      rating: this.data.reviewDraft.rating,
      content: this.data.reviewDraft.content
    })
    this.setData({
      'reviewDraft.content': ''
    })
    this.setOrder({
      ...order,
      review
    })
    wx.showToast({
      title: '评价成功',
      icon: 'success'
    })
  },

  getEtaTitle(status) {
    if (status === 70) return '订单已取消'
    if (status >= 60) return '订单已送达'
    if (status >= 50) return '预计 12 分钟送达'
    if (status >= 40) return '预计 25 分钟送达'
    if (status >= 30) return '商家正在备餐'
    return '等待商家接单'
  },

  getEtaDesc(status) {
    if (status === 70) return '订单已取消，如需用餐请重新下单'
    if (status >= 60) return '感谢使用，欢迎再次下单'
    if (status >= 50) return '骑手正在配送中，请留意电话'
    if (status >= 40) return '餐品正在制作，完成后骑手将取餐'
    if (status >= 30) return '商家已确认订单，正在安排制作'
    return '订单已提交，正在等待商家确认'
  },

  getDriverStatus(status) {
    if (status === 70) return '已取消'
    if (status >= 60) return '已送达'
    if (status >= 50) return '配送中'
    if (status >= 40) return '待取餐'
    return '待分配'
  },

  getSteps(status) {
    const currentIndex = status === 70 ? 0 : status >= 60 ? 4 : status >= 50 ? 3 : status >= 40 ? 2 : status >= 30 ? 1 : 0
    return [
      { title: '已下单', desc: '订单已提交成功' },
      { title: '商家接单', desc: '商家已确认订单' },
      { title: '制作中', desc: '餐品正在制作' },
      { title: '骑手已取餐', desc: '骑手正在配送中' },
      { title: '已送达', desc: '等待送达确认' }
    ].map((item, index) => ({
      ...item,
      state: index < currentIndex ? 'done' : index === currentIndex ? 'current' : 'pending'
    }))
  }
})
