# 贡献指南

TakeOut 由三个主要端组成：

- `miniapp/`：用户端微信小程序。
- `admin-web/`：Vue 3 商家后台和平台控制台。
- `server/`：Spring Boot 后端服务。

## 本地验证

请根据本次修改范围运行对应检查。

后端测试：

```powershell
cd server
mvn test
```

管理后台构建：

```powershell
cd admin-web
npm install
npm run build
```

小程序 JavaScript 语法检查：

```powershell
Get-ChildItem -LiteralPath miniapp -Recurse -File -Filter *.js | ForEach-Object { node --check $_.FullName }
```

部署配置检查：

```powershell
docker compose -f deploy/docker/docker-compose.yml config --quiet
```

## Pull Request 要求

- 尽量保持后端、管理后台、小程序的修改边界清晰。
- 涉及界面变化时，请附上截图或说明影响页面。
- 不要提交构建产物、本地数据库、日志、`.env`、微信开发者工具私有配置。
- `.env.example` 只作为公开变量示例，真实密钥不要进入仓库。
