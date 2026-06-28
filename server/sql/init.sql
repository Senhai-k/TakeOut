CREATE DATABASE IF NOT EXISTS takeout
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE takeout;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` VARCHAR(64) NOT NULL COMMENT '微信 openid',
  `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信 unionid',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 正常，0 禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_openid` (`openid`),
  KEY `idx_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货手机号',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区',
  `detail` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `house_number` VARCHAR(100) DEFAULT NULL COMMENT '门牌号',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认地址：0 否，1 是',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`),
  KEY `idx_user_address_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址表';

CREATE TABLE IF NOT EXISTS `shop` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
  `logo_url` VARCHAR(255) DEFAULT NULL COMMENT '店铺 Logo',
  `notice` VARCHAR(500) DEFAULT NULL COMMENT '店铺公告',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '店铺地址',
  `min_order_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '起送价',
  `delivery_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送费',
  `business_status` TINYINT NOT NULL DEFAULT 1 COMMENT '营业状态：1 营业，0 休息',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态：1 启用，0 禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` BIGINT NOT NULL COMMENT '店铺 ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`),
  KEY `idx_category_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

CREATE TABLE IF NOT EXISTS `dish` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` BIGINT NOT NULL COMMENT '店铺 ID',
  `category_id` BIGINT NOT NULL COMMENT '分类 ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
  `sales_count` INT NOT NULL DEFAULT 0 COMMENT '销量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 上架，0 下架',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`),
  KEY `idx_dish_shop_id` (`shop_id`),
  KEY `idx_dish_category_id` (`category_id`),
  KEY `idx_dish_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

CREATE TABLE IF NOT EXISTS `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `shop_id` BIGINT NOT NULL COMMENT '店铺 ID',
  `dish_id` BIGINT NOT NULL COMMENT '商品 ID',
  `dish_name` VARCHAR(100) NOT NULL COMMENT '商品名称快照',
  `dish_image_url` VARCHAR(255) DEFAULT NULL COMMENT '商品图片快照',
  `dish_price` DECIMAL(10,2) NOT NULL COMMENT '商品价格快照',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `selected` TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中：1 是，0 否',
  `size_option` VARCHAR(50) DEFAULT NULL COMMENT '规格',
  `spice_option` VARCHAR(50) DEFAULT NULL COMMENT '辣度',
  `notes` VARCHAR(255) DEFAULT NULL COMMENT '商品备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_user_shop_dish` (`user_id`, `shop_id`, `dish_id`),
  KEY `idx_cart_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

CREATE TABLE IF NOT EXISTS `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `shop_id` BIGINT NOT NULL COMMENT '店铺 ID',
  `address_id` BIGINT DEFAULT NULL COMMENT '地址 ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人快照',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货手机号快照',
  `receiver_address` VARCHAR(500) NOT NULL COMMENT '收货地址快照',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '用户备注',
  `goods_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品金额',
  `delivery_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送费',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0 未支付，1 已支付，2 已退款',
  `order_status` TINYINT NOT NULL DEFAULT 10 COMMENT '订单状态：10 待支付，20 已支付待接单，30 商家已接单，40 制作中，50 配送中，60 已完成，70 已取消，80 退款中，90 已退款',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `accept_time` DATETIME DEFAULT NULL COMMENT '接单时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_order_no` (`order_no`),
  KEY `idx_orders_user_id` (`user_id`),
  KEY `idx_orders_shop_id` (`shop_id`),
  KEY `idx_orders_status` (`order_status`),
  KEY `idx_orders_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT NOT NULL COMMENT '订单 ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `dish_id` BIGINT NOT NULL COMMENT '商品 ID',
  `dish_name` VARCHAR(100) NOT NULL COMMENT '商品名称快照',
  `dish_image_url` VARCHAR(255) DEFAULT NULL COMMENT '商品图片快照',
  `dish_price` DECIMAL(10,2) NOT NULL COMMENT '下单单价',
  `quantity` INT NOT NULL COMMENT '数量',
  `size_option` VARCHAR(50) DEFAULT NULL COMMENT '规格',
  `spice_option` VARCHAR(50) DEFAULT NULL COMMENT '辣度',
  `notes` VARCHAR(255) DEFAULT NULL COMMENT '商品备注',
  `subtotal_amount` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_item_order_id` (`order_id`),
  KEY `idx_order_item_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

CREATE TABLE IF NOT EXISTS `payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT NOT NULL COMMENT '订单 ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `payment_no` VARCHAR(64) NOT NULL COMMENT '系统支付流水号',
  `transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '微信支付流水号',
  `channel` VARCHAR(20) NOT NULL DEFAULT 'WECHAT' COMMENT '支付渠道',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0 待支付，1 成功，2 失败，3 已退款',
  `paid_at` DATETIME DEFAULT NULL COMMENT '支付成功时间',
  `callback_content` TEXT COMMENT '回调内容',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_payment_no` (`payment_no`),
  UNIQUE KEY `uk_payment_transaction_id` (`transaction_id`),
  KEY `idx_payment_order_id` (`order_id`),
  KEY `idx_payment_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

CREATE TABLE IF NOT EXISTS `admin_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` BIGINT DEFAULT NULL COMMENT '店铺 ID，平台管理员可为空',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：ADMIN 平台管理员，MERCHANT 商家',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 正常，0 禁用',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最近登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_user_username` (`username`),
  KEY `idx_admin_user_shop_id` (`shop_id`),
  KEY `idx_admin_user_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台用户表';

INSERT INTO `shop` (`name`, `notice`, `phone`, `address`, `min_order_amount`, `delivery_fee`, `business_status`, `status`)
VALUES
  ('玛利亚披萨', '招牌披萨现烤出餐，高峰期请预留配送时间。', '13800000000', '广东省深圳市南山区科技园', 20.00, 4.00, 1, 1),
  ('南山拉面馆', '汤面默认分装，辣度可在菜品详情中备注。', '13800000000', '广东省深圳市南山区粤海街道', 20.00, 4.00, 1, 1),
  ('炭火烧烤铺', '烧烤现点现烤，满 ¥68 赠送冰柠檬茶。', '13800000000', '广东省深圳市南山区后海', 20.00, 5.00, 1, 1);

INSERT INTO `user_address` (`user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail`, `house_number`, `is_default`)
VALUES (1, '张三', '13800000000', '广东省', '深圳市', '南山区', '科技园', 'A 座 1001', 1);

INSERT INTO `admin_user` (`shop_id`, `username`, `password_hash`, `real_name`, `phone`, `role`, `status`)
VALUES (NULL, 'admin', '$2a$10$replace_with_real_bcrypt_hash', '平台管理员', '13800000000', 'ADMIN', 1);
