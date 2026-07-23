# deploy

部署配置统一放在 `docker/`：

- `docker-compose.yml`：后端与 H2 文件数据库
- `docker-compose.mysql.yml`：后端与 MySQL、Flyway
- `docker-compose.redis.yml`：可选 Redis 限流扩展

启动命令和环境变量见 [Docker 部署说明](docker/README.md)。
