const orderService = require('../../services/order')
const { money, orderStatusText } = require('../../utils/format')

Page({
  data: {
    tabs: [
      { label: '全部', status: 0 },
      { label: '待支付', status: 10 },
      { label: '待接单', status: 20 },
      { label: '制作中', status: 40 },
      { label: '配送中', status: 50 },
      { label: '已完成', status: 60 }
    ],
    activeStatus: 0,
    orders: [],
    summary: {
      orderCount: 0,
      itemCount: 0,
      paidAmountText: '0.00'
    }
  },

  async onShow() {
    await this.loadOrders()
  },

  async loadOrders() {
    const source = this.data.activeStatus
      ? await orderService.listOrdersByStatus(this.data.activeStatus)
      : await orderService.listOrders()
    const orders = source.map(item => ({
      ...item,
      displayNo: item.orderNo || `#${item.id}`,
      statusText: orderStatusText(item.orderStatus),
      payAmountText: money(item.payAmount),
      itemCount: item.items.reduce((sum, dish) => sum + Number(dish.quantity || 0), 0)
    }))
    this.setData({
      orders,
      summary: this.buildSummary(orders)
    })
  },

  async selectTab(event) {
    this.setData({
      activeStatus: Number(event.currentTarget.dataset.status || 0)
    })
    await this.loadOrders()
  },

  goDetail(event) {
    wx.navigateTo({
      url: `/pages/order-detail/order-detail?id=${event.currentTarget.dataset.id}`
    })
  },

  buildSummary(orders) {
    const itemCount = orders.reduce((sum, order) => sum + Number(order.itemCount || 0), 0)
    const paidAmount = orders
      .filter(order => Number(order.payStatus) === 1 && Number(order.orderStatus) !== 70)
      .reduce((sum, order) => sum + Number(order.payAmount || 0), 0)
    return {
      orderCount: orders.length,
      itemCount,
      paidAmountText: money(paidAmount)
    }
  }
})
