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

## API 占位

- `POST /api/v1/training/start` — 开始训练  
- `POST /api/v1/training/end` — 结束训练  
- `POST /api/v1/training/report` — 生成 AI 训练报告（当前为 `StubAiReportFacade` 占位实现）
