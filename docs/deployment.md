# 部署说明

建议部署组件：

- Nginx
- 后端服务
- MySQL
- Redis
- HTTPS 证书

## Docker 部署

仓库当前提供了后端服务的 Docker 配置：

```bash
docker compose -f deploy/docker/docker-compose.yml up --build -d
```

访问健康检查：

```text
http://localhost:8080/api/health
```

容器编排和负载均衡建议使用标准探针：

```text
GET /actuator/health/liveness
GET /actuator/health/readiness
```

仅公开 Actuator `health`，并隐藏数据库等组件详情。`info`、`env`、`metrics` 等端点默认不对外暴露。

## 请求日志

后端会为每个请求生成或透传 `X-Request-Id`，并输出单行访问日志，包含请求方法、路径、响应状态和耗时。反向代理应透传该响应头；集中日志系统可按 `requestId` 聚合同一请求的应用日志。访问日志不记录请求体、Authorization 或 Cookie。

注意：

- 当前后端默认使用 H2 文件数据库，并通过 Docker volume 持久化数据，未启用 `AUTO_SERVER`。
- 如果要做生产部署，建议把数据库改成 MySQL，再单独加 Nginx 做反向代理。

## MySQL + Flyway 联调

MySQL 联调配置会等待数据库健康检查通过，再启动后端并自动执行 Flyway 迁移：

```bash
export TAKEOUT_JWT_SECRET='replace-with-a-long-random-secret'
export MYSQL_PASSWORD='replace-with-a-long-random-password'
export MYSQL_ROOT_PASSWORD='replace-with-another-long-random-password'
docker compose -f deploy/docker/docker-compose.mysql.yml up --build -d
```

停止服务：

```bash
docker compose -f deploy/docker/docker-compose.mysql.yml down
```

首次联调默认写入开发种子数据。生产环境必须设置强随机 `MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD`、`TAKEOUT_JWT_SECRET`，并设置 `TAKEOUT_SEED_ENABLED=false`。Flyway 迁移文件位于 `server/src/main/resources/db/migration/`，已经执行过的迁移文件不可修改，后续结构变更应新增更高版本迁移。

## MySQL 备份与恢复

创建备份：

```powershell
.\scripts\mysql-backup.ps1 -Database takeout -Username takeout
```

备份默认写入被 Git 忽略的 `backups/mysql/`，并生成同名 `.sha256` 文件。建议将 SQL 和校验文件一起复制到受权限控制的异地存储。

恢复会覆盖目标数据库中的同名对象，执行前必须核对环境、数据库名和备份时间，并显式确认：

```powershell
.\scripts\mysql-restore.ps1 -BackupPath .\backups\mysql\takeout-yyyyMMdd-HHmmss.sql -Database takeout -Username takeout -ConfirmRestore
```

恢复脚本要求 `.sha256` 校验文件存在且匹配。正式发布前应在隔离数据库执行恢复演练，并核对 Flyway 历史表、订单数量和关键业务数据。

## API 限流

后端默认对登录、Token 刷新和图片上传执行单实例内存限流。可通过以下环境变量调整：

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `TAKEOUT_RATE_LIMIT_ENABLED` | `true` | 是否启用限流 |
| `TAKEOUT_RATE_LIMIT_WINDOW_SECONDS` | `60` | 固定窗口秒数 |
| `TAKEOUT_RATE_LIMIT_AUTH_MAX_REQUESTS` | `20` | 每个客户端每个窗口允许的认证请求数 |
| `TAKEOUT_RATE_LIMIT_UPLOAD_MAX_REQUESTS` | `30` | 每个客户端每个窗口允许的上传请求数 |
| `TAKEOUT_RATE_LIMIT_TRUST_FORWARDED_HEADERS` | `false` | 是否信任反向代理传入的 `X-Forwarded-For` |

仅在应用部署于可信反向代理之后时启用 `TAKEOUT_RATE_LIMIT_TRUST_FORWARDED_HEADERS`。默认 `TAKEOUT_RATE_LIMIT_PROVIDER=memory`；多实例部署可设置为 `redis`，Redis 实现通过 Lua 原子执行计数和过期时间，故障时默认失败关闭。

MySQL Compose 叠加 Redis 限流：

```bash
export TAKEOUT_JWT_SECRET='replace-with-a-long-random-secret'
export MYSQL_PASSWORD='replace-with-a-long-random-password'
export MYSQL_ROOT_PASSWORD='replace-with-another-long-random-password'
export REDIS_PASSWORD='replace-with-a-long-random-password'
docker compose -f deploy/docker/docker-compose.mysql.yml -f deploy/docker/docker-compose.redis.yml up --build -d
```

可用 `TAKEOUT_RATE_LIMIT_REDIS_FAIL_OPEN=true` 改为 Redis 故障时放行，但认证入口可能失去防护，生产环境不建议启用。Redis provider 启用后会注册专用健康检查；内存模式不会尝试连接 Redis。

## 管理员登录保护

管理员账号默认连续输错密码 5 次后锁定 15 分钟。成功登录和平台管理员重置密码会清零失败计数并解除锁定。可通过 `TAKEOUT_ADMIN_MAX_FAILED_ATTEMPTS` 和 `TAKEOUT_ADMIN_LOCK_MINUTES` 调整；生产环境不建议关闭该保护。

MySQL profile 会强制关闭 H2 Console。生产环境不应重新启用 `/h2-console`，也不应使用开发种子数据。

## 微信登录

后端默认 `TAKEOUT_WECHAT_MODE=dev`。启用官方登录需要设置：

```text
TAKEOUT_WECHAT_MODE=wechat
WECHAT_APP_ID=<小程序 AppID>
WECHAT_APP_SECRET=<小程序 AppSecret>
```

同时将 `miniapp/app.js` 中的 `wechatLoginEnabled` 设置为 `true`，并把 `baseUrl` 改为已配置微信合法域名的 HTTPS 地址。AppSecret 只能保存在后端环境变量或密钥管理系统中，不能写入小程序代码。
