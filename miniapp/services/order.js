const { request } = require('./request')
const { nowText } = require('../utils/format')
const addressService = require('./address')
const orderStore = require('./order-store')

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
    const order = {
      ...orderStore.normalizeBackendOrder(backendOrder),
      paymentMethod: orderStore.normalizePaymentMethod(payload.paymentMethod)
    }
    orderStore.saveLocalOrder(order)
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
    paymentMethod: orderStore.normalizePaymentMethod(payload.paymentMethod),
    payStatus: 0,
    orderStatus: 10,
    items: payload.items,
    createdAt: nowText()
  }
  orderStore.saveLocalOrder(order)
  return order
}

async function listOrders() {
  try {
    const page = await request({ url: '/app/orders' })
    const backendOrders = (page.records || []).map(order => orderStore.mergeLocalMetadata(orderStore.normalizeBackendOrder(order)))
    backendOrders.forEach(orderStore.saveLocalOrder)
    return orderStore.mergeOrders(backendOrders, orderStore.listLocalOrders())
  } catch (error) {
    return orderStore.listLocalOrders()
  }
}

async function listOrdersByStatus(status) {
  const orders = await listOrders()
  if (!status) return orders
  return orders.filter(item => Number(item.orderStatus) === Number(status))
}

async function getOrderDetail(id) {
  try {
    const order = orderStore.mergeLocalMetadata(orderStore.normalizeBackendOrder(await request({ url: `/app/orders/${id}` })))
    orderStore.saveLocalOrder(order)
    return order
  } catch (error) {
    return orderStore.findLocalOrder(id)
  }
}

async function mockPayOrder(id) {
  try {
    const order = orderStore.mergeLocalMetadata(orderStore.normalizeBackendOrder(await request({
      url: `/app/orders/${id}/mock-pay`,
      method: 'POST'
    })))
    orderStore.saveLocalOrder(order)
    return order
  } catch (error) {
    const localOrder = orderStore.patchLocalOrder(id, {
      payStatus: 1,
      orderStatus: 20
    })
    if (localOrder) return localOrder
    throw error
  }
}

async function cancelOrder(id) {
  try {
    const order = orderStore.mergeLocalMetadata(orderStore.normalizeBackendOrder(await request({
      url: `/app/orders/${id}/cancel`,
      method: 'POST'
    })))
    orderStore.saveLocalOrder(order)
    return order
  } catch (error) {
    const localOrder = orderStore.patchLocalOrder(id, {
      orderStatus: 70
    })
    if (localOrder) return localOrder
    throw error
  }
}

async function completeOrder(id) {
  try {
    const order = orderStore.mergeLocalMetadata(orderStore.normalizeBackendOrder(await request({
      url: `/app/orders/${id}/complete`,
      method: 'POST'
    })))
    orderStore.saveLocalOrder(order)
    return order
  } catch (error) {
    const localOrder = orderStore.patchLocalOrder(id, {
      orderStatus: 60
    })
    if (localOrder) return localOrder
    throw error
  }
}

module.exports = {
  createOrder,
  listLocalOrders: orderStore.listLocalOrders,
  listOrders,
  listOrdersByStatus,
  getOrderDetail,
  mockPayOrder,
  cancelOrder,
  completeOrder
}
