const addressService = require('../../services/address')

Page({
  data: {
    addresses: []
  },

  async onShow() {
    this.setData({
      addresses: await addressService.listAddresses()
    })
  },

  async selectAddress(event) {
    await addressService.setDefaultAddress(event.currentTarget.dataset.id)
    wx.navigateBack()
  },

  addAddress() {
    wx.navigateTo({
      url: '/pages/address-edit/address-edit'
    })
  },

  editAddress(event) {
    wx.navigateTo({
      url: `/pages/address-edit/address-edit?id=${event.currentTarget.dataset.id}`
    })
  },

  removeAddress(event) {
    const id = event.currentTarget.dataset.id
    wx.showModal({
      title: '删除地址',
      content: '确定删除这条收货地址吗？',
      confirmText: '删除',
      success: async result => {
        if (!result.confirm) return
        await addressService.removeAddress(id)
        this.setData({
          addresses: await addressService.listAddresses()
        })
        wx.showToast({ title: '已删除', icon: 'success' })
      }
    })
  }
})
