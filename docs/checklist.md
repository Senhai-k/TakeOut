# 运行检查清单

本文档用于本地运行、调试和发布前快速检查项目状态。

## 环境检查

- Java 17 已安装。
- Maven 可用。
- Node.js 可用。
- 微信开发者工具已安装。
- 后端默认端口 `8080` 未被占用。
- 管理后台默认端口 `5173` 未被占用。

## 启动检查

### 后端

```powershell
cd D:\Projects\Codex\TakeOut\server
mvn spring-boot:run
```

健康检查：

```powershell
Invoke-RestMethod http://127.0.0.1:8080/api/health
```

期望结果：

```text
code = 0
status = UP
```

### 管理后台

```powershell
cd D:\Projects\Codex\TakeOut\admin-web
npm install
npm run dev
```

浏览器打开：

```text
http://localhost:5173
```

登录账号：

```text
admin / 123456
```

### 微信小程序

用微信开发者工具导入：

```text
D:\Projects\Codex\TakeOut\miniapp
```

本地联调勾选：

```text
不校验合法域名、web-view、TLS版本以及HTTPS证书
```

## 功能检查

- 后台可以登录。
- 后台可以点击“重置基础数据”。
- 经营概览显示订单、销售额、待接单、在售菜品。
- 订单管理可以筛选待接单订单。
- 订单详情弹窗可以打开。
- 订单可以接单、开始制作、开始配送、完成。
- 分类管理可以新增、编辑、删除分类。
- 菜品管理可以新增、编辑、上下架菜品。
- 菜品管理可以上传图片并预览。
- 小程序可以登录默认用户。
- 小程序可以浏览商家和菜品。
- 小程序可以加入购物车、提交订单、模拟支付。

## 验证命令

后端测试：

```powershell
cd D:\Projects\Codex\TakeOut\server
mvn test
```

后台构建：

```powershell
cd D:\Projects\Codex\TakeOut\admin-web
npm run build
```

小程序 JS 语法检查：

```powershell
cd D:\Projects\Codex\TakeOut
Get-ChildItem -LiteralPath miniapp -Recurse -File -Filter *.js | ForEach-Object { node --check $_.FullName }
```

## 发布前检查

- 不上传 `server/target/`。
- 不上传 `admin-web/dist/`。
- 不上传 `admin-web/node_modules/`。
- 不上传本地数据库文件。
- 不上传本地上传图片文件，只保留 `server/uploads/.gitkeep`。
- README 和 docs 文档与当前功能保持一致。

## 常见问题

### 后台登录过期

退出重新登录。必要时清理浏览器 localStorage。

### 小程序真机无法访问本地后端

将 `miniapp/app.js` 中的本地地址改为电脑局域网 IP。

### 后台没有基础订单

登录后台后点击“重置基础数据”。
