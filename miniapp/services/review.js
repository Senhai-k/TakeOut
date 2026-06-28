const storage = require('../utils/storage')
const { nowText } = require('../utils/format')

const REVIEW_KEY = 'TAKEOUT_ORDER_REVIEWS'

function listReviews() {
  return storage.get(REVIEW_KEY, [])
}

function getReview(orderId) {
  return listReviews().find(item => Number(item.orderId) === Number(orderId)) || null
}

function saveReview(payload) {
  const review = {
    orderId: Number(payload.orderId),
    rating: Number(payload.rating),
    content: (payload.content || '').trim(),
    createdAt: nowText()
  }
  const reviews = listReviews().filter(item => Number(item.orderId) !== review.orderId)
  reviews.unshift(review)
  storage.set(REVIEW_KEY, reviews)
  return review
}

module.exports = {
  getReview,
  saveReview
}
