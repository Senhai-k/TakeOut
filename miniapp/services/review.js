const storage = require('../utils/storage')
const { nowText } = require('../utils/format')
const { request } = require('./request')

const REVIEW_KEY = 'TAKEOUT_ORDER_REVIEWS'

function listReviews() {
  return storage.get(REVIEW_KEY, [])
}

function getReview(orderId) {
  return listReviews().find(item => Number(item.orderId) === Number(orderId)) || null
}

function saveLocalReview(payload) {
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

async function saveReview(payload) {
  try {
    const review = await request({
      url: `/app/orders/${payload.orderId}/review`,
      method: 'POST',
      data: {
        rating: Number(payload.rating),
        content: (payload.content || '').trim()
      }
    })
    return saveLocalReview(review)
  } catch (error) {
    return saveLocalReview(payload)
  }
}

module.exports = {
  getReview,
  saveReview,
  saveLocalReview
}
