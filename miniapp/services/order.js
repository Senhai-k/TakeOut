const { request } = require('./request')
const storage = require('../utils/storage')
const { nowText } = require('../utils/format')
const addressService = require('./address')

const ORDER_KEY = 'TAKEOUT_ORDERS'

function listLocalOrders() {
  return storage.get(ORDER_KEY, [])
}

function saveLocalOrder(order) {
  const orders = listLocalOrders().filter(item => item.id !== order.id)
  orders.unshift(order)
  storage.set(ORDER_KEY, orders)
}

function updateLocalOrder(id, patch) {
  const orderId = Number(id)
  const orders = listLocalOrders()
  const target = orders.find(item => Number(item.id) === orderId)
  if (!target) return null
  const next = {
    ...target,
    ...patch
  }
  storage.set(ORDER_KEY, orders.map(item => Number(item.id) === orderId ? next : item))
  return next
}

async function createOrder(payload) {
  let backendResult = null
  let backendOrder = null
  try {
    const backendAddress = await addressService.ensureBackendAddress(payload.address)
    await request({
      url: '/app/cart',
      method: 'DELETE'
    })
    const backendCartItems = []
    for (const item of payload.items) {
      const backendItem = await request({
        url: '/app/cart/items',
        method: 'POST',
        data: {
          shopId: payload.shopId,
          dishId: item.dishId,
          quantity: item.quantity,
          size: item.size,
          spice: item.spice,
          notes: item.notes
        }
      })
      backendCartItems.push(backendItem)
    }
    backendResult = await request({
      url: '/app/orders',
      method: 'POST',
      data: {
        shopId: payload.shopId,
        addressId: backendAddress.id,
        cartItemIds: backendCartItems.map(item => item.id),
        remark: payload.remark
      }
    })
    backendOrder = await request({ url: `/app/orders/${backendResult.orderId}` })
  } catch (error) {
    if (error && error.code) {
      throw error
    }
    backendResult = null
    backendOrder = null
  }

  if (backendOrder) {
    const order = normalizeBackendOrder(backendOrder)
    saveLocalOrder(order)
    return order
  }

  const id = backendResult ? backendResult.orderId : Date.now()
  const orderNo = backendResult ? backendResult.orderNo : `TO${Date.now()}`
  const order = {
    id,
    orderNo,
    shopId: payload.shopId,
    shopName: payload.shopName,
    receiverName: payload.address.receiverName,
    receiverPhone: payload.address.receiverPhone,
    receiverAddress: `${payload.address.province}${payload.address.city}${payload.address.district}${payload.address.detail} ${payload.address.houseNumber || ''}`,
    remark: payload.remark,
    goodsAmount: payload.goodsAmount,
    deliveryFee: payload.deliveryFee,
    discountAmount: 0,
    payAmount: payload.payAmount,
    payStatus: 0,
    orderStatus: 10,
    items: payload.items,
    createdAt: nowText()
  }
  saveLocalOrder(order)
  return order
}

async function listOrders() {
  try {
    const page = await request({ url: '/app/orders' })
    const orders = (page.records || []).map(normalizeBackendOrder)
    orders.forEach(saveLocalOrder)
    return orders
  } catch (error) {
    return listLocalOrders()
  }
}

async function listOrdersByStatus(status) {
  const orders = await listOrders()
  if (!status) return orders
  return orders.filter(item => Number(item.orderStatus) === Number(status))
}

async function getOrderDetail(id) {
  try {
    const order = normalizeBackendOrder(await request({ url: `/app/orders/${id}` }))
    saveLocalOrder(order)
    return order
  } catch (error) {
    return listLocalOrders().find(item => item.id === Number(id)) || null
  }
}

async function mockPayOrder(id) {
  try {
    const order = normalizeBackendOrder(await request({
      url: `/app/orders/${id}/mock-pay`,
      method: 'POST'
    }))
    saveLocalOrder(order)
    return order
  } catch (error) {
    const localOrder = updateLocalOrder(id, {
      payStatus: 1,
      orderStatus: 20
    })
    if (localOrder) return localOrder
    throw error
  }
}

async function cancelOrder(id) {
  try {
    const order = normalizeBackendOrder(await request({
      url: `/app/orders/${id}/cancel`,
      method: 'POST'
    }))
    saveLocalOrder(order)
    return order
  } catch (error) {
    const localOrder = updateLocalOrder(id, {
      orderStatus: 70
    })
    if (localOrder) return localOrder
    throw error
  }
}

async function completeOrder(id) {
  try {
    const order = normalizeBackendOrder(await request({
      url: `/app/orders/${id}/complete`,
      method: 'POST'
    }))
    saveLocalOrder(order)
    return order
  } catch (error) {
    const localOrder = updateLocalOrder(id, {
      orderStatus: 60
    })
    if (localOrder) return localOrder
    throw error
  }
}

function normalizeBackendOrder(order) {
  return {
    ...order,
    items: (order.items || []).map(item => ({
      ...item,
      cartKey: `${item.dishId}`,
      optionText: [item.size, item.spice, item.notes].filter(Boolean).join(' / ')
    })),
    createdAt: typeof order.createdAt === 'string' ? order.createdAt.replace('T', ' ').slice(0, 16) : nowText()
  }
}

module.exports = {
  createOrder,
  listLocalOrders,
  listOrders,
  listOrdersByStatus,
  getOrderDetail,
  mockPayOrder,
  cancelOrder,
  completeOrder
}
