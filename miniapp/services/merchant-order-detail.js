const { request } = require('./request')
const storage = require('../utils/storage')
const { nowText } = require('../utils/format')

const ORDER_KEY = 'TAKEOUT_ORDERS'

function listLocalOrders() {
  return storage.get(ORDER_KEY, [])
}

function normalizeOrder(order) {
  return {
    ...order,
    displayNo: order.orderNo || `#${order.id}`,
    createdAt: typeof order.createdAt === 'string' ? order.createdAt.replace('T', ' ').slice(0, 16) : nowText(),
    items: (order.items || []).map(item => ({
      ...item,
      cartKey: `${item.dishId}`,
      optionText: [item.size, item.spice, item.notes].filter(Boolean).join(' / ')
    }))
  }
}

async function getOrderDetail(id) {
  try {
    return normalizeOrder(await request({ url: `/merchant/orders/${id}` }))
  } catch (error) {
    return normalizeOrder(listLocalOrders().find(item => Number(item.id) === Number(id)) || null)
  }
}

module.exports = {
  getOrderDetail
}
