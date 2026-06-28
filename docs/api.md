# 外卖小程序接口文档

## 1. 接口规范

### 1.1 基础路径

```text
/api
```

用户端接口：

```text
/api/app
```

商家端接口：

```text
/api/merchant
```

平台管理端接口：

```text
/api/admin
```

### 1.2 请求格式

- `GET`：查询数据，参数放在 Query String。
- `POST`：新增数据或执行业务操作，参数放在 JSON Body。
- `PUT`：更新数据，参数放在 JSON Body。
- `DELETE`：删除数据。

请求头：

```text
Content-Type: application/json
Authorization: Bearer <token>
```

登录、首页商品列表等公开接口可以不传 `Authorization`。

### 1.3 统一响应格式

成功：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

失败：

```json
{
  "code": 40001,
  "message": "参数错误",
  "data": null
}
```

分页响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "pageSize": 10
  }
}
```

### 1.4 常用错误码

| 错误码 | 说明 |
| --- | --- |
| 0 | 成功 |
| 40001 | 参数错误 |
| 40100 | 未登录或登录已过期 |
| 40300 | 无权限 |
| 40400 | 数据不存在 |
| 40900 | 数据状态冲突 |
| 50000 | 系统异常 |

## 2. 用户端接口

### 2.1 微信登录

```text
POST /api/app/auth/wechat-login
```

请求参数：

```json
{
  "code": "微信登录 code",
  "nickname": "用户昵称",
  "avatarUrl": "头像地址"
}
```

响应数据：

```json
{
  "token": "jwt-token",
  "user": {
    "id": 1,
    "nickname": "张三",
    "avatarUrl": "https://example.com/avatar.png",
    "phone": "13800000000"
  }
}
```

### 2.2 获取当前用户信息

```text
GET /api/app/users/me
```

响应数据：

```json
{
  "id": 1,
  "nickname": "张三",
  "avatarUrl": "https://example.com/avatar.png",
  "phone": "13800000000"
}
```

### 2.3 绑定手机号

```text
POST /api/app/users/bind-phone
```

请求参数：

```json
{
  "phoneCode": "微信手机号授权 code"
}
```

响应数据：

```json
{
  "phone": "13800000000"
}
```

### 2.4 个人中心统计

```text
GET /api/app/auth/profile-stats
```

响应数据：

```json
{
  "orderCount": 5,
  "totalSpent": 128.00,
  "rewardPoints": 256
}
```

## 3. 地址接口

### 3.1 地址列表

```text
GET /api/app/addresses
```

响应数据：

```json
[
  {
    "id": 1,
    "receiverName": "张三",
    "receiverPhone": "13800000000",
    "province": "广东省",
    "city": "深圳市",
    "district": "南山区",
    "detail": "科技园",
    "houseNumber": "A 座 1001",
    "isDefault": true
  }
]
```

### 3.2 新增地址

```text
POST /api/app/addresses
```

请求参数：

```json
{
  "receiverName": "张三",
  "receiverPhone": "13800000000",
  "province": "广东省",
  "city": "深圳市",
  "district": "南山区",
  "detail": "科技园",
  "houseNumber": "A 座 1001",
  "isDefault": true
}
```

### 3.3 修改地址

```text
PUT /api/app/addresses/{id}
```

请求参数同新增地址。

### 3.4 删除地址

```text
DELETE /api/app/addresses/{id}
```

### 3.5 设置默认地址

```text
POST /api/app/addresses/{id}/default
```

## 4. 店铺和商品接口

### 4.1 获取店铺信息

```text
GET /api/app/shop
```

响应数据：

```json
{
  "id": 1,
  "name": "示例外卖店",
  "logoUrl": "https://example.com/logo.png",
  "notice": "欢迎下单",
  "phone": "13800000000",
  "address": "示例地址",
  "minOrderAmount": 20.00,
  "deliveryFee": 3.00,
  "businessStatus": 1
}
```

### 4.2 商品分类和商品列表

```text
GET /api/app/dishes
```

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| shopId | 否 | 店铺 ID，第一版可默认 1 |

响应数据：

```json
[
  {
    "id": 1,
    "name": "热销",
    "sort": 1,
    "dishes": [
      {
        "id": 1,
        "name": "招牌鸡腿饭",
        "imageUrl": "https://example.com/dish.png",
        "description": "招牌套餐",
        "price": 22.00,
        "stock": 100,
        "salesCount": 30,
        "status": 1
      }
    ]
  }
]
```

### 4.3 商品详情

```text
GET /api/app/dishes/{id}
```

响应数据：

```json
{
  "id": 1,
  "shopId": 1,
  "categoryId": 1,
  "name": "招牌鸡腿饭",
  "imageUrl": "https://example.com/dish.png",
  "description": "招牌套餐",
  "price": 22.00,
  "stock": 100,
  "salesCount": 30,
  "status": 1
}
```

## 5. 购物车接口

### 5.1 获取购物车

```text
GET /api/app/cart
```

响应数据：

```json
{
  "items": [
    {
      "id": 1,
      "dishId": 1,
      "dishName": "招牌鸡腿饭",
      "dishImageUrl": "https://example.com/dish.png",
      "dishPrice": 22.00,
      "quantity": 2,
      "selected": true,
      "size": "普通",
      "spice": "不辣",
      "notes": "不要洋葱",
      "subtotalAmount": 44.00
    }
  ],
  "goodsAmount": 44.00,
  "deliveryFee": 3.00,
  "payAmount": 47.00
}
```

### 5.2 添加购物车

```text
POST /api/app/cart/items
```

请求参数：

```json
{
  "shopId": 1,
  "dishId": 1,
  "quantity": 1,
  "size": "普通",
  "spice": "不辣",
  "notes": "不要洋葱"
}
```

### 5.3 修改购物车数量

```text
PUT /api/app/cart/items/{id}
```

请求参数：

```json
{
  "quantity": 3,
  "selected": true
}
```

### 5.4 删除购物车商品

```text
DELETE /api/app/cart/items/{id}
```

### 5.5 清空购物车

```text
DELETE /api/app/cart
```

## 6. 订单接口

### 6.1 提交订单

```text
POST /api/app/orders
```

请求参数：

```json
{
  "shopId": 1,
  "addressId": 1,
  "cartItemIds": [1, 2],
  "remark": "不要辣"
}
```

响应数据：

```json
{
  "orderId": 1,
  "orderNo": "202606230001",
  "payAmount": 47.00,
  "orderStatus": 10
}
```

业务规则：

- 店铺休息时不能提交订单。
- 商品下架或库存不足时不能提交订单。
- 商品金额低于起送价时不能提交订单。
- 订单创建后购物车中对应商品应删除。

### 6.2 订单列表

```text
GET /api/app/orders
```

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| status | 否 | 订单状态 |
| page | 否 | 页码，默认 1 |
| pageSize | 否 | 每页数量，默认 10 |

### 6.3 订单详情

```text
GET /api/app/orders/{id}
```

响应数据：

```json
{
  "id": 1,
  "orderNo": "202606230001",
  "shopId": 1,
  "shopName": "示例外卖店",
  "receiverName": "张三",
  "receiverPhone": "13800000000",
  "receiverAddress": "广东省深圳市南山区科技园 A 座 1001",
  "remark": "不要辣",
  "goodsAmount": 44.00,
  "deliveryFee": 3.00,
  "discountAmount": 0.00,
  "payAmount": 47.00,
  "payStatus": 0,
  "orderStatus": 10,
  "items": [
    {
      "dishId": 1,
      "dishName": "招牌鸡腿饭",
      "dishImageUrl": "https://example.com/dish.png",
      "dishPrice": 22.00,
      "quantity": 2,
      "subtotalAmount": 44.00
    }
  ],
  "createdAt": "2026-06-23 10:00:00"
}
```

### 6.4 取消订单

```text
POST /api/app/orders/{id}/cancel
```

请求参数：

```json
{
  "reason": "不想要了"
}
```

### 6.5 创建支付

```text
POST /api/app/orders/{id}/pay
```

响应数据：

```json
{
  "paymentNo": "PAY202606230001",
  "timeStamp": "1782180000",
  "nonceStr": "random-string",
  "package": "prepay_id=wx123",
  "signType": "RSA",
  "paySign": "wechat-pay-sign"
}
```

### 6.6 支付回调

```text
POST /api/app/payments/wechat/callback
```

说明：

- 该接口由微信支付平台调用。
- 后端需要验签。
- 回调处理必须幂等。
- 支付成功后订单状态改为 `20 已支付待接单`。

### 6.7 模拟支付

```text
POST /api/app/orders/{id}/mock-pay
```

说明：

- 仅用于本地开发或未申请微信支付时测试。
- 生产环境应关闭。

## 7. 商家端接口

### 7.1 商家登录

```text
POST /api/merchant/auth/login
```

请求参数：

```json
{
  "username": "merchant",
  "password": "123456"
}
```

响应数据：

```json
{
  "token": "jwt-token",
  "user": {
    "id": 2,
    "username": "merchant",
    "role": "MERCHANT",
    "shopId": 1
  }
}
```

### 7.2 获取店铺信息

```text
GET /api/merchant/shop
```

### 7.3 修改店铺信息

```text
PUT /api/merchant/shop
```

请求参数：

```json
{
  "name": "示例外卖店",
  "notice": "欢迎下单",
  "phone": "13800000000",
  "address": "示例地址",
  "minOrderAmount": 20.00,
  "deliveryFee": 3.00,
  "businessStatus": 1
}
```

### 7.4 分类列表

```text
GET /api/merchant/categories
```

### 7.5 新增分类

```text
POST /api/merchant/categories
```

请求参数：

```json
{
  "name": "热销",
  "sort": 1,
  "status": 1
}
```

### 7.6 修改分类

```text
PUT /api/merchant/categories/{id}
```

### 7.7 删除分类

```text
DELETE /api/merchant/categories/{id}
```

### 7.8 商品列表

```text
GET /api/merchant/dishes
```

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| categoryId | 否 | 分类 ID |
| status | 否 | 上下架状态 |
| keyword | 否 | 商品名称关键词 |
| page | 否 | 页码 |
| pageSize | 否 | 每页数量 |

### 7.9 新增商品

```text
POST /api/merchant/dishes
```

请求参数：

```json
{
  "categoryId": 1,
  "name": "招牌鸡腿饭",
  "imageUrl": "https://example.com/dish.png",
  "description": "招牌套餐",
  "price": 22.00,
  "stock": 100,
  "status": 1
}
```

### 7.10 修改商品

```text
PUT /api/merchant/dishes/{id}
```

### 7.11 商品上下架

```text
POST /api/merchant/dishes/{id}/status
```

请求参数：

```json
{
  "status": 1
}
```

### 7.12 删除商品

```text
DELETE /api/merchant/dishes/{id}
```

### 7.13 商家订单列表

```text
GET /api/merchant/orders
```

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| status | 否 | 订单状态 |
| orderNo | 否 | 订单号 |
| startTime | 否 | 开始时间 |
| endTime | 否 | 结束时间 |
| page | 否 | 页码 |
| pageSize | 否 | 每页数量 |

### 7.14 商家订单详情

```text
GET /api/merchant/orders/{id}
```

### 7.15 接单

```text
POST /api/merchant/orders/{id}/accept
```

业务规则：

- 只有 `20 已支付待接单` 状态可以接单。
- 接单后状态变为 `30 商家已接单`。

### 7.16 拒单

```text
POST /api/merchant/orders/{id}/reject
```

请求参数：

```json
{
  "reason": "商品已售罄"
}
```

业务规则：

- 拒单后订单进入取消或退款流程。

### 7.17 更新订单状态

```text
POST /api/merchant/orders/{id}/status
```

请求参数：

```json
{
  "status": 40
}
```

允许的状态流转：

```text
30 商家已接单 -> 40 制作中
40 制作中 -> 50 配送中
50 配送中 -> 60 已完成
```

### 7.18 商家数据统计

```text
GET /api/merchant/statistics/overview
```

响应数据：

```json
{
  "todayOrderCount": 20,
  "todaySalesAmount": 1200.00,
  "pendingOrderCount": 3,
  "dishCount": 30
}
```

## 8. 平台管理端接口

### 8.1 管理员登录

```text
POST /api/admin/auth/login
```

请求参数：

```json
{
  "username": "admin",
  "password": "123456"
}
```

### 8.2 用户列表

```text
GET /api/admin/users
```

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| keyword | 否 | 昵称或手机号 |
| status | 否 | 用户状态 |
| page | 否 | 页码 |
| pageSize | 否 | 每页数量 |

### 8.3 禁用或启用用户

```text
POST /api/admin/users/{id}/status
```

请求参数：

```json
{
  "status": 1
}
```

### 8.4 商家列表

```text
GET /api/admin/shops
```

### 8.5 新增商家

```text
POST /api/admin/shops
```

请求参数：

```json
{
  "name": "示例外卖店",
  "phone": "13800000000",
  "address": "示例地址",
  "minOrderAmount": 20.00,
  "deliveryFee": 3.00,
  "status": 1
}
```

### 8.6 修改商家

```text
PUT /api/admin/shops/{id}
```

### 8.7 禁用或启用商家

```text
POST /api/admin/shops/{id}/status
```

请求参数：

```json
{
  "status": 1
}
```

### 8.8 全部订单列表

```text
GET /api/admin/orders
```

查询参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| orderNo | 否 | 订单号 |
| userId | 否 | 用户 ID |
| shopId | 否 | 店铺 ID |
| status | 否 | 订单状态 |
| startTime | 否 | 开始时间 |
| endTime | 否 | 结束时间 |
| page | 否 | 页码 |
| pageSize | 否 | 每页数量 |

### 8.9 全部订单详情

```text
GET /api/admin/orders/{id}
```

### 8.10 平台统计

```text
GET /api/admin/statistics/overview
```

响应数据：

```json
{
  "userCount": 1000,
  "shopCount": 10,
  "todayOrderCount": 120,
  "todaySalesAmount": 8800.00
}
```

## 9. 文件上传接口

### 9.1 上传图片

```text
POST /api/merchant/files/images
```

请求格式：

```text
multipart/form-data
```

请求参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| file | 是 | 图片文件 |

响应数据：

```json
{
  "url": "https://example.com/uploads/2026/06/dish.png"
}
```

说明：

- 第一版可先上传到本地服务器。
- 后续可改为对象存储。

## 10. 订单状态常量

| 值 | 状态 |
| --- | --- |
| 10 | 待支付 |
| 20 | 已支付待接单 |
| 30 | 商家已接单 |
| 40 | 制作中 |
| 50 | 配送中 |
| 60 | 已完成 |
| 70 | 已取消 |
| 80 | 退款中 |
| 90 | 已退款 |

## 11. 支付状态常量

| 值 | 状态 |
| --- | --- |
| 0 | 未支付 |
| 1 | 已支付 |
| 2 | 已退款 |
