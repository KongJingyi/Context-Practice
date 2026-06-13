# 语境智练 · 后端

基于 **JDK 17**、**Spring Boot 3**、**Maven**、**MyBatis-Plus**、**MySQL** 的陪练平台服务端骨架。

## 模块职责（包结构）

| 包路径 | 职责 |
|--------|------|
| `com.team13.context.common` | 通用能力：统一响应体、错误码、工具类等 |
| `com.team13.context.config` | Spring 与三方组件配置（如 MyBatis-Plus、Redis、安全） |
| `com.team13.context.controller` | HTTP 接入层，参数校验与路由，不写业务规则 |
| `com.team13.context.service` | 业务编排与事务边界，组合 mapper、领域逻辑与外部服务 |
| `com.team13.context.mapper` | MyBatis-Plus Mapper，数据库访问 |
| `com.team13.context.entity` | 与表结构对应的持久化实体（POJO） |
| `com.team13.context.dto` | 入参 / 出参、视图对象，与 entity 解耦 |
| `com.team13.context.ai` | AI 相关抽象：报告生成、逻辑纠错、Prompt 与模型调用封装 |

## 运行

1. 本地创建数据库 `context_practice`，并按 entity 建表（或后续接入 Flyway）。
2. 修改 `src/main/resources/application-dev.yml` 中的 MySQL、Redis 连接信息。
3. 在 `backend` 目录执行：`mvn spring-boot:run`

## API（训练 / 房间 / 订单）

- `POST /api/auth/login` — 登录（返回 token、userId、roles）
- `GET /api/v1/user/me` — 当前用户信息
- `GET /api/v1/orders` — 用户订单列表
- `GET /api/v1/orders/{orderId}` — 订单详情
- `GET /api/v1/coach/orders` — 陪练订单列表
- `POST /api/v1/training/start` — 开始训练（返回 roomId、trainingId）
- `GET /api/v1/rooms/{roomId}/join-info` — 进房 TRTC 凭证
- `POST /api/v1/rooms/{roomId}/join|leave|end` — 进房 / 离开 / 结束
- `GET /api/v1/rooms/{roomId}` — 房间状态
- `POST /api/v1/training/end` — 结束训练并生成 AI 报告
- `POST /api/v1/training/report` — 按训练记录 ID 查询/补生成报告

完整接口文档见 `docs/api-v1-video-conference.md`。

## 大模型报告（DeepSeek / Kimi 等 OpenAI 兼容）

1. 申请 API Key（如 [DeepSeek](https://platform.deepseek.com/)）。  
2. 在 **`ContextPractice-backend`** 目录复制 `.env.example` 为 **`.env`**，填入密钥：

   ```env
   DEEPSEEK_API_KEY=sk-你的密钥
   ```

   项目已引入 `spring-boot-starter-dotenv`，启动时会自动加载 `.env`（无需再手动 `export`）。  
3. `application-dev.yml` 中通过 `api-key: ${DEEPSEEK_API_KEY:}` 引用；`app.ai.enabled: true` 时生效。  
4. **切勿将 `.env` 提交到 Git**（已在 `.gitignore` 中忽略）。  
5. 切换 Kimi：`base-url: https://api.moonshot.cn/v1`，`model: moonshot-v1-8k`，`.env` 中使用 `MOONSHOT_API_KEY` 并改 yml 中的变量名。
