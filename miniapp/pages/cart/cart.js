const cartService = require('../../services/cart')
const { money } = require('../../utils/format')

Page({
  data: {
    items: [],
    promoCode: '',
    shopName: '',
    subtotal: 0,
    minOrderAmount: 0,
    minOrderGap: 0,
    canCheckout: false,
    deliveryFee: 4,
    packingFee: 2,
    total: 0,
    subtotalText: '0.00',
    minOrderGapText: '0.00',
    deliveryFeeText: '4.00',
    taxText: '2.00',
    totalText: '0.00'
  },

  onShow() {
    this.refresh()
  },

  refresh() {
    const storedItems = cartService.getCartItems()
    const items = storedItems.map(item => {
      const subtotal = Number(item.dishPrice) * item.quantity
      const optionText = [item.size, item.spice, item.notes].filter(Boolean).join(' / ')
      return {
        ...item,
        cartKey: item.cartKey || cartService.createCartKey(item),
        optionText,
        initial: item.dishName ? item.dishName.substr(0, 1) : '餐',
        dishPriceText: money(item.dishPrice),
        subtotal,
        subtotalText: money(subtotal)
      }
    })
    const subtotal = items.reduce((sum, item) => sum + item.subtotal, 0)
    const packingFee = subtotal > 0 ? 2 : 0
    const deliveryFee = subtotal > 0 ? Number(items[0].deliveryFee || 4) : 0
    const minOrderAmount = items[0] ? cartService.getMinOrderAmount(items[0]) : 0
    const minOrderGap = Math.max(minOrderAmount - subtotal, 0)
    const total = subtotal + packingFee + deliveryFee
    this.setData({
      items,
      shopName: items[0] ? items[0].shopName : '',
      subtotal,
      minOrderAmount,
      minOrderGap,
      canCheckout: items.length > 0 && minOrderGap <= 0,
      packingFee,
      deliveryFee,
      total,
      subtotalText: money(subtotal),
      minOrderGapText: money(minOrderGap),
      deliveryFeeText: money(deliveryFee),
      taxText: money(packingFee),
      totalText: money(total)
    })
  },

  normalizeCartItems(items) {
    return items.map(item => ({
      cartKey: item.cartKey,
      shopId: item.shopId,
      shopName: item.shopName,
      deliveryFee: item.deliveryFee,
      dishId: item.dishId,
      dishName: item.dishName,
      dishImageUrl: item.dishImageUrl,
      dishPrice: item.dishPrice,
      size: item.size,
      spice: item.spice,
      notes: item.notes,
      quantity: item.quantity
    }))
  },

  updateItemQuantity(cartKey, delta) {
    const items = this.data.items
      .map(item => item.cartKey === cartKey ? { ...item, quantity: Number(item.quantity || 0) + delta } : item)
      .filter(item => item.quantity > 0)
    cartService.saveCartItems(this.normalizeCartItems(items))
    this.refresh()
  },

  decreaseItem(event) {
    this.updateItemQuantity(event.currentTarget.dataset.key, -1)
  },

  increaseItem(event) {
    this.updateItemQuantity(event.currentTarget.dataset.key, 1)
  },

  clearCart() {
    wx.showModal({
      title: '清空购物车',
      content: '确定移除当前购物车里的所有商品吗？',
      confirmText: '清空',
      success: result => {
        if (result.confirm) {
          cartService.clearCart()
          this.refresh()
        }
      }
    })
  },

  removeItem(event) {
    const cartKey = event.currentTarget.dataset.key
    const items = this.data.items.filter(item => item.cartKey !== cartKey)
    cartService.saveCartItems(items)
    this.refresh()
  },

  goBack() {
    wx.navigateBack({
      fail() {
        wx.switchTab({ url: '/pages/index/index' })
      }
    })
  },

  goHome() {
    wx.switchTab({
      url: '/pages/index/index'
    })
  },

  onPromoInput(event) {
    this.setData({ promoCode: event.detail.value })
  },

  applyPromo() {
    wx.showToast({
      title: this.data.promoCode ? '优惠码已记录' : '请输入优惠码',
      icon: 'none'
    })
  },

  goCheckout() {
    if (this.data.items.length === 0) return
    if (!this.data.canCheckout) {
      wx.showToast({
        title: `还差 ¥${this.data.minOrderGapText} 起送`,
        icon: 'none'
      })
      return
    }
    wx.navigateTo({
      url: '/pages/order-confirm/order-confirm'
    })
  }
})
