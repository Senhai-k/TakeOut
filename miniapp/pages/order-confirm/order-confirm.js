const cartService = require('../../services/cart')
const addressService = require('../../services/address')
const orderService = require('../../services/order')
const shopService = require('../../services/shop')
const { money } = require('../../utils/format')

Page({
  data: {
    shop: null,
    shopName: '示例外卖店',
    address: null,
    items: [],
    itemCount: 0,
    remark: '',
    goodsAmount: 0,
    deliveryFee: 0,
    payAmount: 0,
    minOrderAmount: 0,
    minOrderGap: 0,
    canSubmit: false,
    submitting: false,
    goodsAmountText: '0.00',
    minOrderGapText: '0.00',
    deliveryFeeText: '0.00',
    payAmountText: '0.00'
  },

  onShow() {
    this.loadPage()
  },

  async loadPage() {
    const cartItems = cartService.getCartItems()
    const cartShop = cartItems[0] || {}
    const shop = cartShop.shopId
      ? (await shopService.getShopById(cartShop.shopId)) || await shopService.getShop()
      : await shopService.getShop()
    const items = cartItems.map(item => {
      const subtotal = Number(item.dishPrice) * item.quantity
      const optionText = [item.size, item.spice, item.notes].filter(Boolean).join(' / ')
      return {
        ...item,
        cartKey: item.cartKey || cartService.createCartKey(item),
        optionText,
        initial: item.dishName ? item.dishName.substr(0, 1) : '餐',
        subtotalAmount: subtotal,
        subtotalAmountText: money(subtotal)
      }
    })
    const goodsAmount = items.reduce((sum, item) => sum + item.subtotalAmount, 0)
    const deliveryFee = goodsAmount > 0 ? Number(cartShop.deliveryFee || shop.deliveryFee || 0) : 0
    const payAmount = goodsAmount + deliveryFee
    const minOrderAmount = cartService.getMinOrderAmount(cartShop.minOrderAmount ? cartShop : shop)
    const minOrderGap = Math.max(minOrderAmount - goodsAmount, 0)
    const address = await addressService.getDefaultAddress()

    this.setData({
      shop: {
        ...shop,
        id: cartShop.shopId || shop.id
      },
      shopName: cartShop.shopName || shop.name,
      address,
      items,
      itemCount: items.reduce((sum, item) => sum + Number(item.quantity || 0), 0),
      goodsAmount,
      deliveryFee,
      payAmount,
      minOrderAmount,
      minOrderGap,
      canSubmit: items.length > 0 && minOrderGap <= 0 && !!address,
      goodsAmountText: money(goodsAmount),
      minOrderGapText: money(minOrderGap),
      deliveryFeeText: money(deliveryFee),
      payAmountText: money(payAmount)
    })
  },

  chooseAddress() {
    wx.navigateTo({
      url: '/pages/address-list/address-list'
    })
  },

  onRemarkInput(event) {
    this.setData({
      remark: event.detail.value
    })
  },

  async submitOrder() {
    if (this.data.submitting) return
    if (this.data.items.length === 0) {
      wx.showToast({
        title: '请先添加商品',
        icon: 'none'
      })
      return
    }
    if (this.data.minOrderGap > 0) {
      wx.showToast({
        title: `还差 ¥${this.data.minOrderGapText} 起送`,
        icon: 'none'
      })
      return
    }
    if (!this.data.address) {
      wx.showToast({
        title: '请选择地址',
        icon: 'none'
      })
      return
    }
    try {
      this.setData({ submitting: true })
      const order = await orderService.createOrder({
        shopId: this.data.shop.id,
        shopName: this.data.shopName,
        address: this.data.address,
        items: this.data.items,
        remark: this.data.remark,
        goodsAmount: this.data.goodsAmount,
        deliveryFee: this.data.deliveryFee,
        payAmount: this.data.payAmount
      })
      cartService.clearCart()
      wx.redirectTo({
        url: `/pages/order-detail/order-detail?id=${order.id}`
      })
    } catch (error) {
      wx.showToast({
        title: error.message || '提交订单失败',
        icon: 'none'
      })
    } finally {
      this.setData({ submitting: false })
    }
  },

  goMenu() {
    wx.redirectTo({
      url: `/pages/merchant-menu/merchant-menu?id=${this.data.shop ? this.data.shop.id : 1}`
    })
  }
})
