const { request } = require('./request')
const storage = require('../utils/storage')
const { money } = require('../utils/format')

const ORDER_KEY = 'TAKEOUT_ORDERS'

function listLocalOrders() {
  return storage.get(ORDER_KEY, [])
}

async function getOverview() {
  try {
    return await request({ url: '/merchant/statistics/overview' })
  } catch (error) {
    const orders = listLocalOrders().filter(item => Number(item.orderStatus) >= 20)
    const pending = orders.filter(item => Number(item.orderStatus) === 20)
    const todaySalesAmount = orders.reduce((sum, item) => sum + Number(item.payAmount || 0), 0)
    return {
      todayOrderCount: orders.length,
      todaySalesAmount: money(todaySalesAmount),
      pendingOrderCount: pending.length,
      dishCount: 0
    }
  }
}

module.exports = {
  getOverview
}
