# 语境智练（CONTEXT-Practise）

面向语言学习场景的 **三客户端 + 统一后端** 平台：学员预约陪练、视频实战训练、AI 复盘报告与运营管理。

## 目录结构

| 目录 | 说明 | 默认端口 |
|------|------|----------|
| [backend/](backend/) | Spring Boot 3 API（MySQL + Redis + Flyway） | 8080 |
| [frontend/](frontend/) | 学员 H5（uni-app）+ 管理端（`admin/`） | 5173 / 5176 |
| [coach/](coach/) | 陪练 PC 端（Vue 3 + Vite） | 5175 |

## 快速启动

详见根目录 **[项目运行说明书.md](项目运行说明书.md)**（含演示账号、环境配置、双机视频联调）。

```bash
# 1. 后端
cd backend && copy .env.example .env   # 填入 MySQL / TRTC 等
mvn spring-boot:run

# 2. 学员端
cd frontend && npm install && npm run dev:h5

# 3. 陪练端
cd coach && npm install && npm run dev

# 4. 管理端（可选）
cd frontend && npm run dev:admin
```

开发环境登录验证码：**888888**（`SMS_PROVIDER=dev`）。

## 技术栈

- 后端：JDK 17、Spring Boot 3、MyBatis-Plus、Flyway
- 前端：Vue 3、uni-app、Vite、Element Plus
- 实时音视频：腾讯云 TRTC
- AI 报告：DeepSeek（OpenAI 兼容 API）

## 仓库说明

本仓库为 **monorepo**，汇总学员端、陪练端与后端，便于答辩展示与 GitHub 备份。团队日常开发亦可使用 Gerrit 分仓库协作。
