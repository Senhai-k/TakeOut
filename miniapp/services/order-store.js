const storage = require('../utils/storage')
const { nowText } = require('../utils/format')
const paymentService = require('./payment')
const reviewService = require('./review')

const ORDER_KEY = 'TAKEOUT_ORDERS'

function listLocalOrders() {
  return storage.get(ORDER_KEY, [])
}

function saveLocalOrders(orders) {
  storage.set(ORDER_KEY, orders)
}

function findLocalOrder(id) {
  return listLocalOrders().find(item => Number(item.id) === Number(id)) || null
}

function saveLocalOrder(order) {
  const next = mergeLocalMetadata(order)
  const orders = listLocalOrders().filter(item => !isSameOrder(item, next))
  orders.unshift(next)
  saveLocalOrders(sortOrders(orders))
  return next
}

function patchLocalOrder(id, patch) {
  const orderId = Number(id)
  const orders = listLocalOrders()
  const target = orders.find(item => Number(item.id) === orderId)
  if (!target) return null
  const next = mergeLocalMetadata({
    ...target,
    ...patch,
    updatedAt: nowText()
  })
  saveLocalOrders(orders.map(item => Number(item.id) === orderId ? next : item))
  return next
}

function normalizeBackendOrder(order) {
  return {
    ...order,
    items: (order.items || []).map(item => ({
      ...item,
      cartKey: `${item.dishId}`,
      optionText: [item.size, item.spice, item.notes].filter(Boolean).join(' / ')
    })),
    createdAt: formatTime(order.createdAt)
  }
}

function mergeLocalMetadata(order) {
  if (!order) return null
  const local = findLocalOrder(order.id) || findLocalOrderByNo(order.orderNo)
  const paymentMethod = normalizePaymentMethod(order.paymentMethod || (local && local.paymentMethod))
  const review = order.review || reviewService.getReview(order.id) || (local && local.review) || null
  return {
    ...order,
    paymentMethod,
    review
  }
}

function mergeOrders(primaryOrders, secondaryOrders) {
  const merged = []
  const seen = new Set()
  primaryOrders.concat(secondaryOrders).forEach(order => {
    if (!order) return
    const normalized = mergeLocalMetadata(order)
    const key = getOrderKey(normalized)
    if (seen.has(key)) return
    seen.add(key)
    merged.push(normalized)
  })
  return sortOrders(merged)
}

function normalizePaymentMethod(method) {
  const key = method && method.key
  return paymentService.getPaymentMethods().find(item => item.key === key) || paymentService.getPaymentMethod()
}

function sortOrders(orders) {
  return orders.slice().sort((left, right) => timestampOf(right.createdAt) - timestampOf(left.createdAt))
}

function findLocalOrderByNo(orderNo) {
  if (!orderNo) return null
  return listLocalOrders().find(item => item.orderNo === orderNo) || null
}

function isSameOrder(left, right) {
  if (left.orderNo && right.orderNo) return left.orderNo === right.orderNo
  return Number(left.id) === Number(right.id)
}

function getOrderKey(order) {
  return order.orderNo || `id:${order.id}`
}

function formatTime(value) {
  return typeof value === 'string' ? value.replace('T', ' ').slice(0, 16) : nowText()
}

function timestampOf(value) {
  if (!value) return 0
  const normalized = String(value).replace(' ', 'T')
  const timestamp = new Date(normalized).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

module.exports = {
  listLocalOrders,
  saveLocalOrders,
  findLocalOrder,
  saveLocalOrder,
  patchLocalOrder,
  normalizeBackendOrder,
  mergeLocalMetadata,
  mergeOrders,
  normalizePaymentMethod,
  formatTime
}
