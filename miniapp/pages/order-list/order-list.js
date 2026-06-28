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
    orders: []
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
      orders
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
  }
})
