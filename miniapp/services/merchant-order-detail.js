const { request } = require('./request')
const orderStore = require('./order-store')

function normalizeOrder(order) {
  return {
    ...order,
    displayNo: order.orderNo || `#${order.id}`,
    createdAt: orderStore.formatTime(order.createdAt),
    items: (order.items || []).map(item => ({
      ...item,
      cartKey: item.cartKey || `${item.dishId}`,
      optionText: item.optionText || [item.size, item.spice, item.notes].filter(Boolean).join(' / ')
    }))
  }
}

async function getOrderDetail(id) {
  try {
    const order = orderStore.mergeLocalMetadata(orderStore.normalizeBackendOrder(await request({ url: `/merchant/orders/${id}` })))
    orderStore.saveLocalOrder(order)
    return normalizeOrder(order)
  } catch (error) {
    const order = orderStore.findLocalOrder(id)
    return order ? normalizeOrder(order) : null
  }
}

module.exports = {
  getOrderDetail
}
