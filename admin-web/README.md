# 管理后台

Vue 3 + Vite 单页应用，同时提供商家后台和平台控制台。登录后根据账号角色进入对应界面，接口统一通过 `/api` 代理到后端。

## 本地运行

```powershell
npm install
npm run dev
```

默认地址为 `http://127.0.0.1:5173`。开发代理配置见 `vite.config.js`。

## 生产构建

```powershell
npm run build
```

构建产物位于 `dist/`，不提交到仓库。

## 代码结构

- `src/App.vue`：登录与商家后台
- `src/PlatformApp.vue`：平台控制台
- `src/api/admin.js`：后台接口客户端和令牌刷新
- `src/styles.css`：共享界面样式
