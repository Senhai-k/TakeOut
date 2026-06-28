const cartService = require('../../services/cart')
const menuData = require('../../services/menu-data')
const { money } = require('../../utils/format')

Page({
  data: {
    dish: {},
    size: '普通',
    spice: '不辣',
    spiceOptions: [
      { value: '不辣', label: '不辣' },
      { value: '微辣', label: '微辣' },
      { value: '中辣', label: '中辣' }
    ],
    notes: '',
    quantity: 1,
    totalText: '0.00'
  },

  onLoad(options) {
    const selectedDish = menuData.getDishById(options.id || 101)
    this.setData({
      dish: {
        ...selectedDish,
        rating: selectedDish.merchantRating,
        merchantName: selectedDish.merchantName,
        price: money(selectedDish.price)
      }
    })
    this.refreshTotal()
  },

  selectSize(event) {
    this.setData({ size: event.currentTarget.dataset.size })
  },

  selectSpice(event) {
    this.setData({ spice: event.detail.value })
  },

  onNotesInput(event) {
    this.setData({ notes: event.detail.value })
  },

  increase() {
    this.setData({ quantity: this.data.quantity + 1 })
    this.refreshTotal()
  },

  decrease() {
    if (this.data.quantity <= 1) return
    this.setData({ quantity: this.data.quantity - 1 })
    this.refreshTotal()
  },

  addToCart() {
    if (cartService.hasDifferentShop(this.data.dish.merchantId)) {
      wx.showModal({
        title: '更换商家',
        content: '购物车已有其他商家的商品，是否清空后加入当前商品？',
        confirmText: '清空并加入',
        success: result => {
          if (result.confirm) {
            this.saveCurrentDish(true)
          }
        }
      })
      return
    }
    this.saveCurrentDish()
  },

  saveCurrentDish(replace = false) {
    cartService.addCartItem(
      {
        shopId: this.data.dish.merchantId,
        shopName: this.data.dish.merchantName,
        deliveryFee: this.data.dish.merchantDeliveryFee,
        minOrder: this.data.dish.merchantMinOrder,
        minOrderAmount: this.data.dish.merchantMinOrderAmount,
        dishId: this.data.dish.id,
        dishName: this.data.dish.name,
        dishImageUrl: this.data.dish.imageUrl || '',
        dishPrice: Number(this.data.dish.price),
        size: this.data.size,
        spice: this.data.spice,
        notes: this.data.notes
      },
      this.data.quantity,
      { replace }
    )
    wx.showToast({ title: '已加入购物车', icon: 'success' })
    setTimeout(() => wx.navigateBack(), 500)
  },

  refreshTotal() {
    this.setData({
      totalText: money(Number(this.data.dish.price || 0) * this.data.quantity)
    })
  }
})
