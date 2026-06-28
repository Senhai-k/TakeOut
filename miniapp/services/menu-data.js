const merchants = [
  {
    id: 1,
    name: '玛利亚披萨',
    rating: '4.8',
    time: '30分钟',
    minOrder: '¥20起',
    minOrderAmount: 20,
    notice: '招牌披萨现烤出餐，高峰期请预留配送时间。',
    deliveryFee: 4,
    icon: '🍕',
    theme: 'pizza'
  },
  {
    id: 2,
    name: '南山拉面馆',
    rating: '4.6',
    time: '28分钟',
    minOrder: '¥20起',
    minOrderAmount: 20,
    notice: '汤面默认分装，辣度可在菜品详情中备注。',
    deliveryFee: 4,
    icon: '🍜',
    theme: 'ramen'
  },
  {
    id: 3,
    name: '炭火烧烤铺',
    rating: '4.7',
    time: '35分钟',
    minOrder: '¥20起',
    minOrderAmount: 20,
    notice: '烧烤现点现烤，满 ¥68 赠送冰柠檬茶。',
    deliveryFee: 5,
    icon: '🍢',
    theme: 'bbq'
  }
]

const dishes = [
  { id: 101, merchantId: 1, category: '热销', name: '玛格丽特披萨', desc: '番茄酱、罗勒、双重芝士', description: '番茄酱、罗勒、双重芝士，现烤出炉香气浓郁', price: 39, sales: 368, rating: 98, icon: '🍕', theme: 'pizza' },
  { id: 102, merchantId: 1, category: '披萨', name: '榴莲芝士披萨', desc: '香甜榴莲果肉，芝士拉满', description: '香甜榴莲果肉搭配浓郁芝士，口感绵密', price: 48, sales: 246, rating: 97, icon: '🍕', theme: 'pizza' },
  { id: 103, merchantId: 1, category: '小吃', name: '黄金炸鸡翅', desc: '外酥里嫩，搭配蜂蜜芥末酱', description: '外酥里嫩，搭配蜂蜜芥末酱，适合多人分享', price: 22, sales: 198, rating: 96, icon: '🍗', theme: 'snack' },
  { id: 104, merchantId: 1, category: '饮品', name: '冰柠檬茶', desc: '清爽解腻，适合搭配披萨', description: '清爽解腻，适合搭配披萨和炸物', price: 8, sales: 421, rating: 95, icon: '🥤', theme: 'drink' },
  { id: 201, merchantId: 2, category: '热销', name: '麻辣拉面', desc: '浓郁汤底，微辣过瘾', description: '浓郁汤底，微辣过瘾，搭配溏心蛋和海苔', price: 25, sales: 512, rating: 98, icon: '🍜', theme: 'ramen' },
  { id: 202, merchantId: 2, category: '拉面', name: '豚骨叉烧拉面', desc: '慢熬豚骨汤，厚切叉烧', description: '慢熬豚骨汤，厚切叉烧，汤底醇厚', price: 32, sales: 386, rating: 97, icon: '🍜', theme: 'ramen' },
  { id: 203, merchantId: 2, category: '小吃', name: '日式煎饺', desc: '底部焦香，肉汁饱满', description: '底部焦香，肉汁饱满，搭配拉面更满足', price: 16, sales: 224, rating: 96, icon: '🥟', theme: 'snack' },
  { id: 204, merchantId: 2, category: '饮品', name: '乌龙冷泡茶', desc: '低糖清爽，解辣不腻', description: '低糖清爽，解辣不腻', price: 9, sales: 168, rating: 95, icon: '🍵', theme: 'drink' },
  { id: 301, merchantId: 3, category: '热销', name: '招牌羊肉串', desc: '炭火慢烤，孜然香气足', description: '炭火慢烤，孜然香气足，默认微辣', price: 28, sales: 456, rating: 98, icon: '🍢', theme: 'bbq' },
  { id: 302, merchantId: 3, category: '烧烤', name: '蜜汁烤鸡翅', desc: '甜咸适中，外皮焦香', description: '甜咸适中，外皮焦香，肉质鲜嫩', price: 24, sales: 312, rating: 97, icon: '🍗', theme: 'bbq' },
  { id: 303, merchantId: 3, category: '主食', name: '牛肉炒饭', desc: '粒粒分明，牛肉香足', description: '粒粒分明，牛肉香足，适合夜宵主食', price: 22, sales: 188, rating: 96, icon: '🍛', theme: 'snack' },
  { id: 304, merchantId: 3, category: '饮品', name: '酸梅汤', desc: '冰镇酸甜，夜宵搭档', description: '冰镇酸甜，烧烤搭档，清爽解腻', price: 8, sales: 236, rating: 95, icon: '🥤', theme: 'drink' }
]

function getMerchants() {
  return merchants
}

function getMerchantById(id) {
  return merchants.find(item => item.id === Number(id)) || merchants[0]
}

function getDishesByMerchantId(merchantId) {
  return dishes.filter(item => item.merchantId === Number(merchantId))
}

function getAllDishes() {
  return dishes
}

function getDishById(id) {
  const dish = dishes.find(item => item.id === Number(id)) || dishes[0]
  const merchant = getMerchantById(dish.merchantId)
  return {
    ...dish,
    merchantName: merchant.name,
    merchantRating: merchant.rating,
    merchantDeliveryFee: merchant.deliveryFee,
    merchantMinOrder: merchant.minOrder,
    merchantMinOrderAmount: merchant.minOrderAmount
  }
}

module.exports = {
  getMerchants,
  getMerchantById,
  getDishesByMerchantId,
  getAllDishes,
  getDishById
}
