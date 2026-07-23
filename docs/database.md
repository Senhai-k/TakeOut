# 数据模型

## 数据库策略

- 本地开发默认使用 H2 文件数据库，由 Hibernate `ddl-auto=update` 维护，并通过 `db/h2-upgrade.sql` 兼容旧数据文件。
- MySQL 环境只使用 Flyway 管理结构，应用以 `ddl-auto=validate` 校验实体和表结构。
- MySQL 基线位于 `server/src/main/resources/db/migration/V1__baseline.sql`，后续结构变更必须新增迁移文件，不得修改已发布迁移。

## 核心表

| 表 | 职责 | 关键约束 |
| --- | --- | --- |
| `app_user` | 小程序用户身份、手机号和令牌版本 | `openid` 唯一 |
| `user_address` | 用户收货地址 | 按 `user_id` 查询 |
| `shop` | 商家资料、营业与启停状态 | 商家数据的归属根节点 |
| `category` | 商家菜单分类 | 按 `shop_id` 隔离 |
| `dish` | 菜品、价格、库存与上下架状态 | 乐观锁 `version` |
| `cart` | 用户购物车项 | 用户、商家、菜品组合唯一 |
| `orders` | 订单主数据、金额、状态和收货快照 | 订单号唯一；用户幂等键唯一；乐观锁 `version` |
| `order_item` | 下单时的菜品快照 | 按订单 ID 和订单号索引 |
| `order_review` | 完成订单的用户评价 | 每个订单最多一条评价 |
| `payment_transaction` | 支付请求和结果 | 支付请求号唯一 |
| `refund` | 退款请求、退款单号和结果 | 请求号、订单 ID 分别唯一 |
| `admin_account` | 平台与商家管理员账号 | 用户名唯一；商家账号关联 `shop_id` |
| `system_config` | 可在线维护的运行参数 | 配置键为主键 |
| `audit_log` | 平台管理写操作记录 | 按时间和管理员索引 |

## 数据隔离

- 用户地址、购物车、订单和评价从认证上下文取得用户 ID，不接受客户端指定归属。
- 商家分类、菜品、订单、统计和店铺设置从管理员令牌取得商家 ID。
- 平台管理员的 `shop_id` 为空，仅平台接口允许跨商家查询。

## 一致性约束

- 创建订单时对用户购物车加锁，在同一事务内校验库存、扣减库存并创建订单与明细。
- 用户取消、商家拒单、平台取消和超时关闭统一恢复库存。
- 支付、退款和创建订单分别使用请求号或幂等键防止重复执行。
- 订单与菜品使用乐观锁处理并发更新；需要串行化的用户和管理员操作使用悲观锁。

## 迁移验证

`DatabaseMigrationSmokeTests` 会在空数据库执行全部 Flyway 迁移，再启动 JPA `validate`。修改实体或数据库结构后必须运行：

```powershell
cd server
mvn test
```
