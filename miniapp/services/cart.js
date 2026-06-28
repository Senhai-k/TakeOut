const storage = require('../utils/storage')

const CART_KEY = 'TAKEOUT_CART_ITEMS'

function getCartItems() {
  return storage.get(CART_KEY, [])
}

function saveCartItems(items) {
  storage.set(CART_KEY, items)
}

function getCartSummary(items = getCartItems()) {
  return items.reduce(
    (summary, item) => {
      const quantity = Number(item.quantity || 0)
      summary.count += quantity
      summary.amount += Number(item.dishPrice || 0) * quantity
      return summary
    },
    { count: 0, amount: 0 }
  )
}

function getMinOrderAmount(itemOrShop = {}) {
  if (itemOrShop.minOrderAmount !== undefined && itemOrShop.minOrderAmount !== null) {
    return Number(itemOrShop.minOrderAmount || 0)
  }
  const text = itemOrShop.minOrder || ''
  const matched = String(text).match(/\d+(\.\d+)?/)
  return matched ? Number(matched[0]) : 0
}

function hasDifferentShop(shopId, items = getCartItems()) {
  return items.some(item => item.shopId && Number(item.shopId) !== Number(shopId))
}

function createCartKey(item) {
  return [
    item.dishId,
    item.size || '',
    item.spice || '',
    item.notes || ''
  ].join('|')
}

function addCartItem(cartItem, quantity = 1, options = {}) {
  const cartItems = options.replace ? [] : getCartItems()
  const nextItem = {
    ...cartItem,
    cartKey: createCartKey(cartItem)
  }
  const current = cartItems.find(item => (item.cartKey || createCartKey(item)) === nextItem.cartKey)
  if (current) {
    current.quantity += quantity
  } else {
    cartItems.push({
      ...nextItem,
      quantity
    })
  }
  saveCartItems(cartItems)
  return cartItems
}

function clearCart() {
  storage.remove(CART_KEY)
}

module.exports = {
  getCartItems,
  saveCartItems,
  getCartSummary,
  getMinOrderAmount,
  hasDifferentShop,
  createCartKey,
  addCartItem,
  clearCart
}
