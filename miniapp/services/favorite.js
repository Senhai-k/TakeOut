const storage = require('../utils/storage')

const FAVORITE_KEY = 'TAKEOUT_FAVORITE_MERCHANTS'

function listFavorites() {
  return storage.get(FAVORITE_KEY, [])
}

function isFavorite(merchantId) {
  return listFavorites().some(item => Number(item.id) === Number(merchantId))
}

function addFavorite(merchant) {
  const favorites = listFavorites().filter(item => Number(item.id) !== Number(merchant.id))
  const next = {
    id: merchant.id,
    name: merchant.name,
    rating: merchant.rating,
    time: merchant.time,
    minOrder: merchant.minOrder,
    icon: merchant.icon,
    theme: merchant.theme
  }
  storage.set(FAVORITE_KEY, [next].concat(favorites))
  return true
}

function removeFavorite(merchantId) {
  storage.set(FAVORITE_KEY, listFavorites().filter(item => Number(item.id) !== Number(merchantId)))
  return false
}

function toggleFavorite(merchant) {
  return isFavorite(merchant.id) ? removeFavorite(merchant.id) : addFavorite(merchant)
}

module.exports = {
  listFavorites,
  isFavorite,
  toggleFavorite
}
