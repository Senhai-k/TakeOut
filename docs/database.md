# 外卖小程序数据库设计

## 1. 设计说明

数据库使用 MySQL 8，字符集使用 `utf8mb4`，存储引擎使用 `InnoDB`。

第一版数据库围绕用户下单、商家接单、商品管理和后台管理设计，暂不实现复杂骑手系统、平台分账和多级营销。

通用字段约定：

- 主键统一使用 `BIGINT`。
- 金额统一使用 `DECIMAL(10,2)`。
- 状态字段使用 `TINYINT`。
- 时间字段使用 `DATETIME`。
- 软删除字段使用 `is_deleted`，`0` 表示未删除，`1` 表示已删除。

## 2. 表关系概览

```text
user 1 -> n user_address
shop 1 -> n category
shop 1 -> n dish
category 1 -> n dish
user 1 -> n cart
user 1 -> n orders
shop 1 -> n orders
orders 1 -> n order_item
orders 1 -> 1 payment
```

## 3. 核心表

### 3.1 用户表 `user`

保存小程序用户信息。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| openid | VARCHAR(64) | 微信 openid |
| unionid | VARCHAR(64) | 微信 unionid，可为空 |
| nickname | VARCHAR(64) | 昵称 |
| avatar_url | VARCHAR(255) | 头像 |
| phone | VARCHAR(20) | 手机号 |
| status | TINYINT | 状态，1 正常，0 禁用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| is_deleted | TINYINT | 是否删除 |

索引：

- `openid` 唯一索引
- `phone` 普通索引

### 3.2 用户地址表 `user_address`

保存用户收货地址。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户 ID |
| receiver_name | VARCHAR(50) | 收货人 |
| receiver_phone | VARCHAR(20) | 收货手机号 |
| province | VARCHAR(50) | 省 |
| city | VARCHAR(50) | 市 |
| district | VARCHAR(50) | 区 |
| detail | VARCHAR(255) | 详细地址 |
| house_number | VARCHAR(100) | 门牌号 |
| is_default | TINYINT | 是否默认地址 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| is_deleted | TINYINT | 是否删除 |

### 3.3 店铺表 `shop`

保存商家店铺基础信息。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 店铺名称 |
| logo_url | VARCHAR(255) | 店铺 Logo |
| notice | VARCHAR(500) | 店铺公告 |
| phone | VARCHAR(20) | 联系电话 |
| address | VARCHAR(255) | 店铺地址 |
| min_order_amount | DECIMAL(10,2) | 起送价 |
| delivery_fee | DECIMAL(10,2) | 配送费 |
| business_status | TINYINT | 营业状态，1 营业，0 休息 |
| status | TINYINT | 启用状态，1 启用，0 禁用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| is_deleted | TINYINT | 是否删除 |

### 3.4 商品分类表 `category`

保存店铺商品分类。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| shop_id | BIGINT | 店铺 ID |
| name | VARCHAR(50) | 分类名称 |
| sort | INT | 排序值 |
| status | TINYINT | 状态，1 启用，0 禁用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| is_deleted | TINYINT | 是否删除 |

### 3.5 商品表 `dish`

保存商品信息。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| shop_id | BIGINT | 店铺 ID |
| category_id | BIGINT | 分类 ID |
| name | VARCHAR(100) | 商品名称 |
| image_url | VARCHAR(255) | 商品图片 |
| description | VARCHAR(500) | 商品描述 |
| price | DECIMAL(10,2) | 售价 |
| stock | INT | 库存 |
| sales_count | INT | 销量 |
| status | TINYINT | 状态，1 上架，0 下架 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| is_deleted | TINYINT | 是否删除 |

索引：

- `shop_id`
- `category_id`
- `status`

### 3.6 购物车表 `cart`

保存用户购物车数据。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户 ID |
| shop_id | BIGINT | 店铺 ID |
| dish_id | BIGINT | 商品 ID |
| dish_name | VARCHAR(100) | 商品名称快照 |
| dish_image_url | VARCHAR(255) | 商品图片快照 |
| dish_price | DECIMAL(10,2) | 商品价格快照 |
| quantity | INT | 数量 |
| selected | TINYINT | 是否选中 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

约束：

- 同一用户、同一店铺、同一商品在购物车中只保留一条记录。

### 3.7 订单表 `orders`

保存订单主信息。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| order_no | VARCHAR(32) | 订单号 |
| user_id | BIGINT | 用户 ID |
| shop_id | BIGINT | 店铺 ID |
| address_id | BIGINT | 地址 ID |
| receiver_name | VARCHAR(50) | 收货人快照 |
| receiver_phone | VARCHAR(20) | 收货手机号快照 |
| receiver_address | VARCHAR(500) | 收货地址快照 |
| remark | VARCHAR(255) | 用户备注 |
| goods_amount | DECIMAL(10,2) | 商品金额 |
| delivery_fee | DECIMAL(10,2) | 配送费 |
| discount_amount | DECIMAL(10,2) | 优惠金额 |
| pay_amount | DECIMAL(10,2) | 实付金额 |
| pay_status | TINYINT | 支付状态，0 未支付，1 已支付，2 已退款 |
| order_status | TINYINT | 订单状态 |
| pay_time | DATETIME | 支付时间 |
| accept_time | DATETIME | 接单时间 |
| complete_time | DATETIME | 完成时间 |
| cancel_time | DATETIME | 取消时间 |
| cancel_reason | VARCHAR(255) | 取消原因 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| is_deleted | TINYINT | 是否删除 |

订单状态：

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

### 3.8 订单明细表 `order_item`

保存订单商品明细。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| order_id | BIGINT | 订单 ID |
| order_no | VARCHAR(32) | 订单号 |
| dish_id | BIGINT | 商品 ID |
| dish_name | VARCHAR(100) | 商品名称快照 |
| dish_image_url | VARCHAR(255) | 商品图片快照 |
| dish_price | DECIMAL(10,2) | 下单单价 |
| quantity | INT | 数量 |
| subtotal_amount | DECIMAL(10,2) | 小计金额 |
| created_at | DATETIME | 创建时间 |

### 3.9 支付记录表 `payment`

保存订单支付记录。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| order_id | BIGINT | 订单 ID |
| order_no | VARCHAR(32) | 订单号 |
| payment_no | VARCHAR(64) | 系统支付流水号 |
| transaction_id | VARCHAR(64) | 微信支付流水号 |
| channel | VARCHAR(20) | 支付渠道 |
| amount | DECIMAL(10,2) | 支付金额 |
| status | TINYINT | 状态，0 待支付，1 成功，2 失败，3 已退款 |
| paid_at | DATETIME | 支付成功时间 |
| callback_content | TEXT | 回调内容 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

约束：

- `payment_no` 唯一。
- `transaction_id` 唯一，可为空。

### 3.10 后台用户表 `admin_user`

保存商家和平台管理员账号。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键 |
| shop_id | BIGINT | 店铺 ID，平台管理员可为空 |
| username | VARCHAR(50) | 用户名 |
| password_hash | VARCHAR(255) | 密码哈希 |
| real_name | VARCHAR(50) | 姓名 |
| phone | VARCHAR(20) | 手机号 |
| role | VARCHAR(20) | 角色，ADMIN 平台管理员，MERCHANT 商家 |
| status | TINYINT | 状态，1 正常，0 禁用 |
| last_login_at | DATETIME | 最近登录时间 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| is_deleted | TINYINT | 是否删除 |

## 4. 建表脚本

初始化 SQL 见：

- `server/sql/init.sql`

## 5. 后续扩展表

第二阶段可以增加：

- `coupon`：优惠券表
- `user_coupon`：用户优惠券表
- `review`：评价表
- `refund`：退款记录表
- `delivery`：配送记录表
- `operation_log`：后台操作日志表
