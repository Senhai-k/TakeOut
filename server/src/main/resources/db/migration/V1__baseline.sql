-- Flyway connects to the target database configured by SPRING_DATASOURCE_URL.

CREATE TABLE IF NOT EXISTS `app_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid` VARCHAR(64) NOT NULL COMMENT '微信 openid',
  `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信 unionid',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1 正常，0 禁用',
  `token_version` INT NOT NULL DEFAULT 0 COMMENT '令牌版本，用于刷新轮换和注销',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_user_openid` (`openid`),
  KEY `idx_app_user_phone` (`phone`)
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
  `is_default` INT NOT NULL DEFAULT 0 COMMENT '是否默认地址：0 否，1 是',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
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
  `business_status` INT NOT NULL DEFAULT 1 COMMENT '营业状态：1 营业，0 休息',
  `status` INT NOT NULL DEFAULT 1 COMMENT '启用状态：1 启用，0 禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` BIGINT NOT NULL COMMENT '店铺 ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1 启用，0 禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
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
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `sales_count` INT NOT NULL DEFAULT 0 COMMENT '销量',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1 上架，0 下架',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
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
  `selected` INT NOT NULL DEFAULT 1 COMMENT '是否选中：1 是，0 否',
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
  `idempotency_key` VARCHAR(64) DEFAULT NULL COMMENT '用户下单幂等键',
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
  `pay_status` INT NOT NULL DEFAULT 0 COMMENT '支付状态：0 未支付，1 已支付，2 已退款',
  `order_status` INT NOT NULL DEFAULT 10 COMMENT '订单状态：10 待支付，20 已支付待接单，30 商家已接单，40 制作中，50 配送中，60 已完成，70 已取消，80 退款中，90 已退款',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `accept_time` DATETIME DEFAULT NULL COMMENT '接单时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `is_deleted` INT NOT NULL DEFAULT 0 COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_order_no` (`order_no`),
  UNIQUE KEY `uk_order_user_idempotency` (`user_id`, `idempotency_key`),
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

CREATE TABLE IF NOT EXISTS `order_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT NOT NULL COMMENT '订单 ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `shop_id` BIGINT NOT NULL COMMENT '店铺 ID',
  `rating` INT NOT NULL COMMENT '评分：1-5',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_review_order_id` (`order_id`),
  KEY `idx_order_review_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单评价表';

CREATE TABLE IF NOT EXISTS `payment_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `request_id` VARCHAR(64) NOT NULL COMMENT '支付请求幂等号',
  `order_id` BIGINT NOT NULL COMMENT '订单 ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `channel` VARCHAR(20) NOT NULL COMMENT '支付渠道',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `status` VARCHAR(20) NOT NULL COMMENT '支付状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_request_id` (`request_id`),
  KEY `idx_payment_transaction_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

CREATE TABLE IF NOT EXISTS `refund` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `request_id` VARCHAR(64) NOT NULL COMMENT '退款请求幂等号',
  `refund_no` VARCHAR(32) NOT NULL COMMENT '系统退款单号',
  `order_id` BIGINT NOT NULL COMMENT '订单 ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '退款金额',
  `source` VARCHAR(20) NOT NULL COMMENT '退款来源',
  `status` VARCHAR(20) NOT NULL COMMENT '退款状态',
  `reason` VARCHAR(255) NOT NULL COMMENT '退款原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_request_id` (`request_id`),
  UNIQUE KEY `uk_refund_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';

CREATE TABLE IF NOT EXISTS `system_config` (
  `config_key` VARCHAR(100) NOT NULL,
  `config_value` VARCHAR(500) NOT NULL,
  `description` VARCHAR(200) NOT NULL,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `admin_id` BIGINT NOT NULL,
  `role` VARCHAR(50) NOT NULL,
  `action` VARCHAR(50) NOT NULL,
  `target_type` VARCHAR(50) NOT NULL,
  `target_id` VARCHAR(100) DEFAULT NULL,
  `detail` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_audit_log_created_at` (`created_at`),
  KEY `idx_audit_log_admin_id` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台审计日志';

CREATE TABLE IF NOT EXISTS `admin_account` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `shop_id` BIGINT DEFAULT NULL COMMENT '店铺 ID，平台管理员可为空',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password_hash` VARCHAR(100) NOT NULL COMMENT '密码哈希',
  `display_name` VARCHAR(50) NOT NULL COMMENT '显示名称',
  `role` VARCHAR(50) NOT NULL COMMENT 'MERCHANT_ADMIN 或 PLATFORM_ADMIN',
  `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺或平台名称',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1 正常，0 禁用',
  `token_version` INT NOT NULL DEFAULT 0 COMMENT '令牌版本，用于刷新轮换和注销',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_account_username` (`username`),
  KEY `idx_admin_account_shop_id` (`shop_id`),
  KEY `idx_admin_account_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台用户表';
