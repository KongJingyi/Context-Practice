# 语境智练

前后端分离仓库：**后端 API** 与 **前端应用** 分目录维护，独立构建与部署。

## 目录说明

| 目录 | 说明 |
|------|------|
| [`backend/`](backend/) | Spring Boot 服务端（JDK 17、Maven），REST API |
| [`frontend/`](frontend/) | **Vue 3 + Uni-app（Vite）**：同一套代码发布 **H5**（沉浸式 Web）与 **微信小程序**（预约/复盘）；详见 `frontend` 内 `package.json` 脚本 |

## 协作方式

- 后端默认端口见 `backend/src/main/resources/application.yml`（当前 `8080`）；前端 H5 开发时 `vite.config.ts` 将 `/api` 代理到该地址（可在 `.env.development` 调整）。
- 详细后端说明、包结构与接口列表见 [`backend/README.md`](backend/README.md)。
