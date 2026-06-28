const { request } = require('./request')
const storage = require('../utils/storage')
const { nowText } = require('../utils/format')

const ORDER_KEY = 'TAKEOUT_ORDERS'

function listLocalOrders() {
  return storage.get(ORDER_KEY, [])
}

function saveLocalOrders(orders) {
  storage.set(ORDER_KEY, orders)
}

function patchLocalOrder(id, patch) {
  const orderId = Number(id)
  const orders = listLocalOrders()
  const nextOrders = orders.map(item => (
    Number(item.id) === orderId
      ? { ...item, ...patch, updatedAt: nowText() }
      : item
  ))
  saveLocalOrders(nextOrders)
  return nextOrders.find(item => Number(item.id) === orderId) || null
}

function normalizeBackendOrder(order) {
  return {
    ...order,
    items: (order.items || []).map(item => ({
      ...item,
      cartKey: `${item.dishId}`,
      optionText: [item.size, item.spice, item.notes].filter(Boolean).join(' / ')
    }))
  }
}

function normalizeOrder(order) {
  return {
    ...order,
    displayNo: order.orderNo || `#${order.id}`,
    createdAt: typeof order.createdAt === 'string' ? order.createdAt.replace('T', ' ').slice(0, 16) : nowText()
  }
}

function filterOrders(orders, status) {
  return status ? orders.filter(item => Number(item.orderStatus) === Number(status)) : orders
}

function buildQuery(params) {
  const query = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null && params[key] !== '')
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  return query ? `?${query}` : ''
}

async function listOrders(params = {}) {
  try {
    const page = await request({
      url: `/merchant/orders${buildQuery({
        ...(params.status ? { status: String(params.status) } : {}),
        ...(params.orderNo ? { orderNo: params.orderNo } : {}),
        page: String(params.page || 1),
        pageSize: String(params.pageSize || 20)
      })}`
    })
    return (page.records || []).map(item => normalizeOrder(normalizeBackendOrder(item)))
  } catch (error) {
    return filterOrders(listLocalOrders().map(normalizeOrder), params.status)
  }
}

async function acceptOrder(id) {
  try {
    return normalizeOrder(normalizeBackendOrder(await request({
      url: `/merchant/orders/${id}/accept`,
      method: 'POST'
    })))
  } catch (error) {
    const next = patchLocalOrder(id, { orderStatus: 30 })
    if (next) return normalizeOrder(next)
    throw error
  }
}

async function rejectOrder(id, reason) {
  try {
    return normalizeOrder(normalizeBackendOrder(await request({
      url: `/merchant/orders/${id}/reject`,
      method: 'POST',
      data: reason ? { reason } : {}
    })))
  } catch (error) {
    const next = patchLocalOrder(id, { orderStatus: 70, remark: reason ? `商家拒单：${reason}` : undefined })
    if (next) return normalizeOrder(next)
    throw error
  }
}

async function updateOrderStatus(id, status) {
  try {
    return normalizeOrder(normalizeBackendOrder(await request({
      url: `/merchant/orders/${id}/status`,
      method: 'POST',
      data: { status }
    })))
  } catch (error) {
    const next = patchLocalOrder(id, { orderStatus: status })
    if (next) return normalizeOrder(next)
    throw error
  }
}

module.exports = {
  listOrders,
  acceptOrder,
  rejectOrder,
  updateOrderStatus
}
