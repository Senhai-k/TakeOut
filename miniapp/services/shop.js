const { request } = require('./request')

const mockShop = {
  id: 1,
  name: '示例外卖店',
  notice: '欢迎下单，第一版系统默认自配送。',
  phone: '13800000000',
  address: '示例地址',
  minOrderAmount: 20,
  deliveryFee: 3,
  businessStatus: 1
}

const mockCategories = [
  {
    id: 1,
    name: '热销',
    sort: 1,
    dishes: [
      { id: 1, shopId: 1, categoryId: 1, name: '招牌鸡腿饭', description: '鸡腿饭套餐', price: 22, stock: 100, salesCount: 30, status: 1 },
      { id: 2, shopId: 1, categoryId: 1, name: '黑椒牛肉饭', description: '黑椒牛肉配米饭', price: 26, stock: 80, salesCount: 18, status: 1 }
    ]
  },
  {
    id: 2,
    name: '饮品',
    sort: 2,
    dishes: [
      { id: 3, shopId: 1, categoryId: 2, name: '冰柠檬茶', description: '清爽解腻', price: 8, stock: 200, salesCount: 42, status: 1 },
      { id: 4, shopId: 1, categoryId: 2, name: '矿泉水', description: '瓶装饮用水', price: 3, stock: 300, salesCount: 60, status: 1 }
    ]
  }
]

async function getShop() {
  try {
    return await request({ url: '/app/shop' })
  } catch (error) {
    return mockShop
  }
}

async function getShopById(id) {
  try {
    return await request({ url: `/app/shop?shopId=${id}` })
  } catch (error) {
    return null
  }
}

async function listDishes(shopId) {
  try {
    return await request({
      url: shopId ? `/app/dishes?shopId=${shopId}` : '/app/dishes'
    })
  } catch (error) {
    return mockCategories
  }
}

module.exports = {
  getShop,
  getShopById,
  listDishes
}
