const cartService = require('../../services/cart')
const menuData = require('../../services/menu-data')
const shopService = require('../../services/shop')
const { money } = require('../../utils/format')

Page({
  categoryScrollLocked: false,
  categoryScrollTimer: null,

  data: {
    shop: {},
    categories: [],
    activeCategory: '热销',
    dishes: [],
    dishGroups: [],
    groupOffsets: [],
    scrollIntoView: '',
    cartItems: [],
    cartCount: 0,
    cartTotalText: '0.00',
    deliveryFeeText: '0.00',
    cartSheetVisible: false
  },

  async onLoad(options) {
    const shopId = Number(options.id || 1)
    const fallbackShop = menuData.getMerchantById(shopId)
    const backendShop = await shopService.getShopById(shopId)
    const shop = this.normalizeShop(backendShop, fallbackShop)
    const backendGroups = await shopService.listDishes(shop.id)
    const backendDishes = this.normalizeBackendDishes(backendGroups, fallbackShop)
    const fallbackDishes = menuData.getDishesByMerchantId(shop.id)
    const sourceDishes = backendDishes.length > 0 ? backendDishes : fallbackDishes
    const shopDishes = sourceDishes.map(item => ({
      ...item,
      priceText: money(item.price),
      cartQuantity: 0
    }))
    const categoryNames = shopDishes
      .map(item => item.category)
      .filter((name, index, array) => array.indexOf(name) === index)
    const sortedCategoryNames = categoryNames.indexOf('热销') >= 0
      ? ['热销'].concat(categoryNames.filter(name => name !== '热销'))
      : categoryNames

    this.setData({
      shop,
      categories: sortedCategoryNames.map(name => ({
        name,
        anchor: this.getCategoryAnchor(name)
      })),
      activeCategory: sortedCategoryNames[0],
      dishes: shopDishes
    })
    this.refreshDishGroups()
    this.refreshCart()
  },

  normalizeShop(backendShop, fallbackShop) {
    if (!backendShop) return fallbackShop
    return {
      ...fallbackShop,
      id: backendShop.id,
      name: backendShop.name,
      notice: backendShop.notice,
      minOrder: `¥${money(backendShop.minOrderAmount)}起`,
      minOrderAmount: Number(backendShop.minOrderAmount || fallbackShop.minOrderAmount || 0),
      deliveryFee: Number(backendShop.deliveryFee || fallbackShop.deliveryFee)
    }
  },

  normalizeBackendDishes(groups, fallbackShop) {
    if (!Array.isArray(groups)) return []
    const themeMap = {
      披萨: 'pizza',
      拉面: 'ramen',
      烧烤: 'bbq',
      饮品: 'drink',
      小吃: 'snack',
      主食: 'snack'
    }
    return groups.flatMap(group => (group.dishes || []).map(dish => ({
      id: dish.id,
      merchantId: fallbackShop.id,
      merchantName: fallbackShop.name,
      merchantDeliveryFee: fallbackShop.deliveryFee,
      category: group.name,
      name: dish.name,
      desc: dish.description,
      description: dish.description,
      price: Number(dish.price || 0),
      sales: dish.salesCount || 0,
      rating: 98,
      icon: fallbackShop.icon,
      theme: themeMap[group.name] || fallbackShop.theme
    })))
  },

  onShow() {
    this.refreshCart()
  },

  onUnload() {
    if (this.categoryScrollTimer) {
      clearTimeout(this.categoryScrollTimer)
      this.categoryScrollTimer = null
    }
    this.categoryScrollLocked = false
  },

  selectCategory(event) {
    const name = event.currentTarget.dataset.name
    this.categoryScrollLocked = true
    if (this.categoryScrollTimer) {
      clearTimeout(this.categoryScrollTimer)
    }
    this.setData({
      activeCategory: name,
      scrollIntoView: ''
    })
    wx.nextTick(() => {
      this.setData({
        scrollIntoView: this.getCategoryAnchor(name)
      })
    })
    this.categoryScrollTimer = setTimeout(() => {
      this.categoryScrollLocked = false
      this.categoryScrollTimer = null
    }, 450)
  },

  getCategoryAnchor(name) {
    return `category-${encodeURIComponent(name).replace(/%/g, '')}`
  },

  refreshDishGroups() {
    const groups = this.data.categories.map(category => ({
      ...category,
      dishes: this.data.dishes.filter(dish => dish.category === category.name)
    })).filter(group => group.dishes.length > 0)
    let offset = 0
    const groupOffsets = groups.map(group => {
      const current = {
        name: group.name,
        top: offset
      }
      offset += 58 + group.dishes.length * 178 + 10
      return current
    })
    this.setData({ dishGroups: groups, groupOffsets })
  },

  onDishScroll(event) {
    if (this.categoryScrollLocked) return
    const scrollTop = event.detail.scrollTop || 0
    const current = this.data.groupOffsets
      .slice()
      .reverse()
      .find(item => scrollTop + 36 >= item.top)
    if (current && current.name !== this.data.activeCategory) {
      this.setData({ activeCategory: current.name })
    }
  },

  openDish(event) {
    wx.navigateTo({
      url: `/pages/dish-detail/dish-detail?id=${event.currentTarget.dataset.id}`
    })
  },

  addDish(event) {
    const id = Number(event.currentTarget.dataset.id)
    const dish = this.data.dishes.find(item => item.id === id)
    if (!dish) return

    if (cartService.hasDifferentShop(this.data.shop.id)) {
      wx.showModal({
        title: '更换商家',
        content: '购物车已有其他商家的商品，是否清空后加入当前商品？',
        confirmText: '清空并加入',
        success: result => {
          if (result.confirm) {
            this.saveDishToCart(dish, true)
          }
        }
      })
      return
    }

    this.saveDishToCart(dish)
  },

  decreaseDish(event) {
    const id = Number(event.currentTarget.dataset.id)
    const target = this.data.cartItems.find(item => Number(item.dishId) === id && !item.optionText) ||
      this.data.cartItems.find(item => Number(item.dishId) === id)
    if (!target) return
    this.updateCartItem(target.cartKey, -1)
  },

  saveDishToCart(dish, replace = false) {
    cartService.addCartItem(
      {
        shopId: this.data.shop.id,
        shopName: this.data.shop.name,
        deliveryFee: this.data.shop.deliveryFee,
        minOrder: this.data.shop.minOrder,
        minOrderAmount: cartService.getMinOrderAmount(this.data.shop),
        dishId: dish.id,
        dishName: dish.name,
        dishImageUrl: '',
        dishPrice: dish.price
      },
      1,
      { replace }
    )
    this.refreshCart()
    wx.showToast({ title: '已加入购物车', icon: 'success' })
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

  updateCartItem(cartKey, delta) {
    const nextItems = this.data.cartItems
      .map(item => item.cartKey === cartKey ? { ...item, quantity: Number(item.quantity || 0) + delta } : item)
      .filter(item => item.quantity > 0)
    const otherShopItems = cartService
      .getCartItems()
      .filter(item => Number(item.shopId) !== Number(this.data.shop.id))
    cartService.saveCartItems(otherShopItems.concat(this.normalizeCartItems(nextItems)))
    this.refreshCart()
  },

  increaseCartItem(event) {
    this.updateCartItem(event.currentTarget.dataset.key, 1)
  },

  decreaseCartItem(event) {
    this.updateCartItem(event.currentTarget.dataset.key, -1)
  },

  clearCurrentCart() {
    wx.showModal({
      title: '清空购物车',
      content: '确定清空当前商家的已选商品吗？',
      confirmText: '清空',
      success: result => {
        if (!result.confirm) return
        const otherShopItems = cartService
          .getCartItems()
          .filter(item => Number(item.shopId) !== Number(this.data.shop.id))
        cartService.saveCartItems(otherShopItems)
        this.setData({ cartSheetVisible: false })
        this.refreshCart()
      }
    })
  },

  refreshCart() {
    const shopItems = cartService
      .getCartItems()
      .filter(item => Number(item.shopId) === Number(this.data.shop.id))
    const summary = cartService.getCartSummary(shopItems)
    const cartItems = shopItems.map(item => {
      const subtotal = Number(item.dishPrice || 0) * Number(item.quantity || 0)
      return {
        ...item,
        cartKey: item.cartKey || cartService.createCartKey(item),
        initial: item.dishName ? item.dishName.substr(0, 1) : '餐',
        optionText: [item.size, item.spice, item.notes].filter(Boolean).join(' / '),
        dishPriceText: money(item.dishPrice),
        subtotalText: money(subtotal)
      }
    })
    const dishes = this.data.dishes.map(dish => ({
      ...dish,
      cartQuantity: shopItems
        .filter(item => Number(item.dishId) === Number(dish.id))
        .reduce((sum, item) => sum + Number(item.quantity || 0), 0)
    }))
    const deliveryFee = summary.count > 0 ? Number(this.data.shop.deliveryFee || 0) : 0
    this.setData({
      dishes,
      cartItems,
      cartCount: summary.count,
      cartTotalText: money(summary.amount),
      deliveryFeeText: money(deliveryFee),
      cartSheetVisible: summary.count > 0 ? this.data.cartSheetVisible : false
    })
    this.refreshDishGroups()
  },

  toggleCartSheet() {
    if (this.data.cartCount === 0) return
    this.setData({
      cartSheetVisible: !this.data.cartSheetVisible
    })
  },

  closeCartSheet() {
    this.setData({ cartSheetVisible: false })
  },

  goCheckout() {
    if (this.data.cartCount === 0) {
      wx.showToast({
        title: '请先选择商品',
        icon: 'none'
      })
      return
    }
    wx.navigateTo({
      url: '/pages/order-confirm/order-confirm'
    })
  }
})
