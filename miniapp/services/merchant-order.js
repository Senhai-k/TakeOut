const { request } = require('./request')
const orderStore = require('./order-store')

function normalizeOrder(order) {
  return {
    ...order,
    displayNo: order.orderNo || `#${order.id}`,
    createdAt: orderStore.formatTime(order.createdAt)
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
    const orders = (page.records || []).map(item => orderStore.mergeLocalMetadata(orderStore.normalizeBackendOrder(item)))
    orders.forEach(orderStore.saveLocalOrder)
    return orders.map(normalizeOrder)
  } catch (error) {
    return filterOrders(orderStore.listLocalOrders().map(normalizeOrder), params.status)
  }
}

async function acceptOrder(id) {
  try {
    const order = orderStore.mergeLocalMetadata(orderStore.normalizeBackendOrder(await request({
      url: `/merchant/orders/${id}/accept`,
      method: 'POST'
    })))
    orderStore.saveLocalOrder(order)
    return normalizeOrder(order)
  } catch (error) {
    const next = orderStore.patchLocalOrder(id, { orderStatus: 30 })
    if (next) return normalizeOrder(next)
    throw error
  }
}

async function rejectOrder(id, reason) {
  try {
    const order = orderStore.mergeLocalMetadata(orderStore.normalizeBackendOrder(await request({
      url: `/merchant/orders/${id}/reject`,
      method: 'POST',
      data: reason ? { reason } : {}
    })))
    orderStore.saveLocalOrder(order)
    return normalizeOrder(order)
  } catch (error) {
    const next = orderStore.patchLocalOrder(id, { orderStatus: 70, remark: reason ? `商家拒单：${reason}` : undefined })
    if (next) return normalizeOrder(next)
    throw error
  }
}

async function updateOrderStatus(id, status) {
  try {
    const order = orderStore.mergeLocalMetadata(orderStore.normalizeBackendOrder(await request({
      url: `/merchant/orders/${id}/status`,
      method: 'POST',
      data: { status }
    })))
    orderStore.saveLocalOrder(order)
    return normalizeOrder(order)
  } catch (error) {
    const next = orderStore.patchLocalOrder(id, { orderStatus: status })
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
