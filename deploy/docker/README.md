# Docker 部署

仓库同时提供 H2 文件库和 MySQL + Flyway 两种 Compose 配置。

## 构建并启动

在仓库根目录执行：

```bash
docker compose -f deploy/docker/docker-compose.yml up --build -d
```

启动后访问：

```text
http://localhost:8080/api/health
```

## 停止

```bash
docker compose -f deploy/docker/docker-compose.yml down
```

## MySQL 联调

```bash
export TAKEOUT_JWT_SECRET='replace-with-a-long-random-secret'
export MYSQL_PASSWORD='replace-with-a-long-random-password'
export MYSQL_ROOT_PASSWORD='replace-with-another-long-random-password'
docker compose -f deploy/docker/docker-compose.mysql.yml up --build -d
```

多实例限流需要叠加 Redis 配置：

```bash
export TAKEOUT_JWT_SECRET='replace-with-a-long-random-secret'
export MYSQL_PASSWORD='replace-with-a-long-random-password'
export MYSQL_ROOT_PASSWORD='replace-with-another-long-random-password'
export REDIS_PASSWORD='replace-with-a-long-random-password'
docker compose -f deploy/docker/docker-compose.mysql.yml -f deploy/docker/docker-compose.redis.yml up --build -d
```

## 说明

- 这个镜像会在构建阶段执行 `mvn package`，再把生成的 JAR 放进运行时镜像。
- 目前配置使用 H2 文件数据库，并通过 Docker volume 持久化数据，未启用 `AUTO_SERVER`。
- MySQL 配置会自动执行 Flyway 迁移；生产部署还需要替换默认密码、关闭开发种子数据并增加 Nginx 和 HTTPS。
