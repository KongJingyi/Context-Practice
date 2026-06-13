# 语境智练 · 1v1 视频会议 API 接口文档

> **版本**：v1.0  
> **更新日期**：2026-05-24  
> **适用范围**：用户端（Uni-app H5 / 微信小程序）+ 陪练端（PC Web）  
> **后端负责人**：后端组  
> **联调 Base URL**：开发环境 `http://localhost:8080`，生产环境待定  

---

## 1. 通用约定

### 1.1 请求规范

| 项 | 说明 |
|----|------|
| Base Path | `/api` |
| Content-Type | `application/json`（除文件上传外） |
| 鉴权 Header | `Authorization: Bearer {token}` |
| 时间格式 | ISO 8601，如 `2026-05-24T14:00:00`（无时区，服务器本地时间） |
| 金额 | 字符串或数字均可，单位：元，保留 2 位小数 |

### 1.2 统一响应结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| code | 含义 | 前端处理建议 |
|------|------|--------------|
| 200 | 成功 | 正常渲染 `data` |
| 400 | 参数错误 / 业务校验失败 | Toast 展示 `message`，不跳转 |
| 401 | 未登录或 Token 失效 | 跳转登录页 |
| 403 | 无权限 | Toast + 返回上一页 |
| 404 | 资源不存在 | Toast + 返回列表 |
| 500 | 服务器异常 | Toast「系统繁忙，请稍后重试」 |

> **注意**：JWT 拦截器在 Token 无效时可能直接返回 HTTP 401 + `{"code":401,"message":"Unauthorized: Token missing or invalid","data":null}`，与上表 `code` 字段一致，按 `code === 401` 处理即可。

### 1.3 角色定义

| role | 说明 | 对应端 |
|------|------|--------|
| `USER` | 普通用户（训练接收方） | 用户端 |
| `COACH` | 陪练员（训练提供方） | 陪练端 PC |

角色由后端根据 JWT 中的 `userId` 与订单关系自动判定，**前端不传 role 参数**（除登录后存储展示用）。

### 1.4 TRTC 集成说明（前端必读）

- 后端**只负责签发** `sdkAppId`、`userId`（TRTC 用户 ID）、`userSig`，前端用腾讯云 TRTC SDK 进房。
- `roomId` 与 TRTC 房间号**完全一致**（32 位字符串）。
- TRTC `userId` 格式约定：`u_{userId}`（用户）、`c_{userId}`（陪练），由后端生成，前端不可自行拼接。
- 压力模式、文字聊天等实时信令 **阶段 2** 优先走 TRTC 自定义消息；阶段 1 仅音视频。
- 用户端 SDK：H5 用 `trtc-js-sdk`，小程序用 `trtc-wx-sdk`；陪练端 PC 用 `trtc-js-sdk`。

### 1.5 优先级说明

| 标记 | 含义 |
|------|------|
| **P0** | 阶段 1 MVP，必须联调 |
| **P1** | 阶段 2 核心业务 |
| **P2** | 阶段 3 体验优化 |

---

## 2. 接口总览

| # | 优先级 | 状态 | 方法 | 路径 | 调用端 | 说明 |
|---|--------|------|------|------|--------|------|
| 1 | P0 | 已实现 | POST | `/api/auth/login` | 双端 | 登录获取 Token |
| 2 | P0 | 已实现 | GET | `/api/v1/user/me` | 双端 | 当前用户信息（含角色） |
| 3 | P0 | 已实现 | GET | `/api/scenes` | 用户端 | 场景列表 |
| 4 | P0 | 已实现 | POST | `/api/v1/orders` | 用户端 | 创建订单 |
| 5 | P0 | 已实现 | POST | `/api/v1/orders/mock-pay` | 用户端 | Mock 支付 |
| 6 | P0 | 已实现 | GET | `/api/v1/orders` | 用户端 | 用户订单列表 |
| 7 | P0 | 已实现 | GET | `/api/v1/orders/{orderId}` | 双端 | 订单详情 |
| 8 | P0 | 已实现 | GET | `/api/v1/coach/orders` | 陪练端 | 陪练订单列表 |
| 9 | P0 | 已实现 | POST | `/api/v1/training/start` | 用户端 | 开始训练（创建 roomId） |
| 10 | P0 | 已实现 | GET | `/api/training/start` | 用户端 | 开始训练（GET 兼容入口） |
| 11 | P0 | 已实现 | GET | `/api/v1/rooms/{roomId}/join-info` | 双端 | **进房凭证（核心）** |
| 12 | P0 | 已实现 | POST | `/api/v1/rooms/{roomId}/join` | 双端 | 记录进房 |
| 13 | P0 | 已实现 | POST | `/api/v1/rooms/{roomId}/leave` | 双端 | 离开房间（不结束订单） |
| 14 | P0 | 已实现 | POST | `/api/v1/rooms/{roomId}/end` | 陪练端 | 结束训练（全房间） |
| 15 | P0 | 已实现 | GET | `/api/v1/rooms/{roomId}` | 双端 | 房间状态（重连/轮询） |
| 16 | P0 | 已实现 | POST | `/api/v1/training/end` | 双端 | 结束训练并生成报告 |
| 17 | P0 | 已实现 | POST | `/api/v1/training/report` | 用户端 | 查询/补生成 AI 报告 |
| 18 | P1 | 已实现 | GET | `/api/v1/rooms/{roomId}/state` | 双端 | 房间实时状态（压力模式等） |
| 19 | P1 | 已实现 | POST | `/api/v1/rooms/{roomId}/pressure/countdown` | 陪练端 | 开启/关闭倒计时 |
| 20 | P1 | 已实现 | POST | `/api/v1/rooms/{roomId}/pressure/interrupt` | 陪练端 | 一键打断 |
| 21 | P1 | 已实现 | POST | `/api/v1/rooms/{roomId}/pressure/question` | 陪练端 | 发起压力提问 |
| 22 | P1 | 已实现 | POST | `/api/v1/rooms/{roomId}/chat` | 双端 | 发送聊天（REST 兜底） |
| 23 | P1 | 已实现 | GET | `/api/v1/rooms/{roomId}/chat` | 双端 | 聊天历史 |
| 24 | P1 | 待实现 | POST | `/api/v1/rooms/{roomId}/materials` | 陪练端 | 上传训练资料 |
| 25 | P1 | 待实现 | GET | `/api/v1/rooms/{roomId}/materials` | 用户端 | 训练资料列表 |
| 26 | P1 | 待实现 | POST | `/api/v1/training/{trainingId}/coach-report` | 陪练端 | 提交结构化反馈报告 |
| 27 | P2 | 已实现 | GET | `/api/v1/training/{trainingId}/recording` | 用户端 | 回放与高光片段 |
| 28 | P2 | 待实现 | POST | `/api/v1/orders/{orderId}/rating` | 用户端 | 提交评价 |
| 29 | P2 | 待实现 | GET | `/api/v1/coach/dashboard` | 陪练端 | 服务数据统计 |

---

## 3. P0 接口详细说明（阶段 1 MVP）

### 3.1 登录

**POST** `/api/auth/login`  
**鉴权**：否  
**状态**：已实现（MVP Mock，后续扩展角色）

**Request Body**

```json
{
  "username": "test_user"
}
```

**Response `data`**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "test_user",
  "userId": 1,
  "roles": ["USER"]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| token | string | JWT，后续请求放 Authorization Header |
| userId | number | 当前用户 ID |
| roles | string[] | `USER` / `COACH` / `ADMIN`，**待实现**，联调前后端会补全 |

---

### 3.2 当前用户信息

**GET** `/api/v1/user/me`  
**鉴权**：是  
**状态**：待实现

**Response `data`**

```json
{
  "userId": 1,
  "username": "张三",
  "nickname": "张三",
  "avatar": "https://cdn.example.com/avatar/1.png",
  "roles": ["USER"],
  "phone": "138****1234"
}
```

---

### 3.3 场景列表

**GET** `/api/scenes?page=1&size=10&name=`  
**鉴权**：否  
**状态**：已实现

**Response `data`**：MyBatis-Plus 分页对象，含 `records[]`：

```json
{
  "records": [
    {
      "id": 1,
      "name": "压力面试",
      "description": "...",
      "status": 1
    }
  ],
  "total": 10,
  "size": 10,
  "current": 1,
  "pages": 1
}
```

---

### 3.4 创建订单

**POST** `/api/v1/orders`  
**鉴权**：是（USER）  
**状态**：已实现

**Request Body**

```json
{
  "productId": 1,
  "coachId": 2,
  "sceneId": 1,
  "amount": 99.00
}
```

**Response `data`**

```json
{
  "orderId": 42
}
```

---

### 3.5 Mock 支付

**POST** `/api/v1/orders/mock-pay`  
**鉴权**：是（USER，订单所属人）  
**状态**：已实现

**Request Body**

```json
{
  "orderId": 42
}
```

**Response**：`data` 为 `null`，`code: 200` 即成功。订单状态变为 `PAID`。

---

### 3.6 用户订单列表

**GET** `/api/v1/orders?status=PAID&page=1&size=10`  
**鉴权**：是（USER）  
**状态**：待实现

**Query 参数**

| 参数 | 必填 | 说明 |
|------|------|------|
| status | 否 | 筛选：`PENDING_PAY` / `PAID` / `IN_SERVICE` / `COMPLETED` 等 |
| page | 否 | 默认 1 |
| size | 否 | 默认 10，最大 50 |

**Response `data.records[]` 单项**

```json
{
  "orderId": 42,
  "status": "PAID",
  "amount": 99.00,
  "coachId": 2,
  "coachName": "李教练",
  "coachAvatar": "https://...",
  "sceneId": 1,
  "sceneName": "压力面试",
  "scheduledStart": "2026-05-24T14:00:00",
  "scheduledEnd": "2026-05-24T15:00:00",
  "roomId": null,
  "trainingStatus": null,
  "canEnterRoom": false
}
```

| 字段 | 说明 |
|------|------|
| roomId | 训练开始后非空 |
| trainingStatus | `IN_PROGRESS` / `ENDED` / `REPORT_READY` / null |
| canEnterRoom | 是否可展示「进入训练」按钮（开始前 5 分钟 ~ 结束后 15 分钟内且已支付） |

---

### 3.7 订单详情

**GET** `/api/v1/orders/{orderId}`  
**鉴权**：是（USER 本人 或 COACH 该订单陪练）  
**状态**：待实现

**Response `data`**：同 3.6 单项，额外字段：

```json
{
  "orderId": 42,
  "status": "IN_SERVICE",
  "userId": 1,
  "userName": "张三",
  "userAvatar": "https://...",
  "coachId": 2,
  "coachName": "李教练",
  "sceneName": "压力面试",
  "scheduledStart": "2026-05-24T14:00:00",
  "scheduledEnd": "2026-05-24T15:00:00",
  "roomId": "a1b2c3d4e5f6...",
  "trainingStatus": "IN_PROGRESS",
  "trainingStartedAt": "2026-05-24T14:01:00",
  "canEnterRoom": true,
  "enterDeniedReason": null
}
```

| enterDeniedReason | 当 canEnterRoom=false 时的原因，如「请在开始前 5 分钟内进入」 |

---

### 3.8 陪练订单列表

**GET** `/api/v1/coach/orders?status=PAID&page=1&size=10`  
**鉴权**：是（COACH）  
**状态**：待实现

**Response `data.records[]` 单项**

```json
{
  "orderId": 42,
  "status": "PAID",
  "userId": 1,
  "userName": "张三",
  "userAvatar": "https://...",
  "sceneName": "压力面试",
  "trainingGoal": "提升面试表达能力",
  "scheduledStart": "2026-05-24T14:00:00",
  "scheduledEnd": "2026-05-24T15:00:00",
  "roomId": null,
  "trainingStatus": null,
  "canEnterRoom": true
}
```

---

### 3.9 开始训练

**POST** `/api/v1/training/start`  
**鉴权**：是（USER，订单所属人）  
**状态**：已实现

**Request Body**

```json
{
  "orderId": 42,
  "scenarioCode": "pressure_interview"
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| orderId | 是 | 订单 ID |
| scenarioCode | 是 | 场景编码，与前端场景 ID 对应 |

**Response `data`**

```json
{
  "roomId": "a1b2c3d4e5f6789012345678901234",
  "trainingId": 100,
  "orderId": 42,
  "startedAt": "2026-05-24T14:01:00"
}
```

**业务规则**

- 订单须为 `PAID`，成功后订单变 `IN_SERVICE`
- 同一订单不可重复创建进行中会话
- **仅用户端调用**；陪练端通过 `join-info` 进已有房间，不调 start

**兼容入口（已实现，建议统一用 POST）**

**GET** `/api/training/start?orderId=42`  
Response 同上，但 `data` 仅 `{ "roomId": "..." }`

---

### 3.10 获取进房信息 ⭐ 核心

**GET** `/api/v1/rooms/{roomId}/join-info`  
**鉴权**：是（该订单 USER 或 COACH）  
**状态**：待实现

**Response `data`**

```json
{
  "roomId": "a1b2c3d4e5f6789012345678901234",
  "trainingId": 100,
  "orderId": 42,
  "trainingStatus": "IN_PROGRESS",
  "sdkAppId": 1400000000,
  "trtcUserId": "u_1",
  "userSig": "eJw1jcsOwiAQRf9leGsd...",
  "role": "USER",
  "peer": {
    "userId": 2,
    "trtcUserId": "c_2",
    "nickname": "李教练",
    "avatar": "https://cdn.example.com/avatar/2.png"
  },
  "sceneName": "压力面试",
  "scheduledStart": "2026-05-24T14:00:00",
  "scheduledEnd": "2026-05-24T15:00:00",
  "startedAt": "2026-05-24T14:01:00",
  "serverTime": "2026-05-24T14:02:30",
  "canEnter": true,
  "denyReason": null,
  "participants": [
    { "role": "USER", "userId": 1, "joined": true, "joinedAt": "2026-05-24T14:01:05" },
    { "role": "COACH", "userId": 2, "joined": false, "joinedAt": null }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| sdkAppId | number | 腾讯云 TRTC 应用 ID |
| trtcUserId | string | 当前用户 TRTC 用户 ID |
| userSig | string | TRTC 进房凭证，有效期 24h |
| role | string | `USER` / `COACH` |
| peer | object | 对方信息，用于 UI 展示 |
| canEnter | boolean | false 时**禁止**调用 TRTC join |
| denyReason | string | canEnter=false 时的原因 |

**错误示例**

```json
{ "code": 403, "message": "无权进入该训练房间", "data": null }
{ "code": 400, "message": "请在预约开始前 5 分钟内进入", "data": null }
{ "code": 400, "message": "训练已结束", "data": null }
{ "code": 404, "message": "房间不存在", "data": null }
```

**前端调用顺序**

```
1. GET join-info（canEnter=true）
2. TRTC SDK join({ roomId, sdkAppId, userId: trtcUserId, userSig })
3. POST join（通知后端记录进房）
```

---

### 3.11 记录进房

**POST** `/api/v1/rooms/{roomId}/join`  
**鉴权**：是  
**状态**：待实现

**Request Body**：`{}`（空对象即可）

**Response `data`**

```json
{
  "joinedAt": "2026-05-24T14:01:05",
  "role": "USER"
}
```

---

### 3.12 离开房间

**POST** `/api/v1/rooms/{roomId}/leave`  
**鉴权**：是  
**状态**：待实现

**说明**：仅离开 TRTC 房间，**不结束订单**，不改变训练状态。用户点「挂断/退出」用这个。

**Request Body**

```json
{
  "reason": "USER_HANGUP"
}
```

| reason 可选值 | 说明 |
|---------------|------|
| USER_HANGUP | 用户主动挂断 |
| COACH_HANGUP | 陪练离开（未结束训练） |
| NETWORK_ERROR | 网络异常 |
| PAGE_UNLOAD | 关闭页面 |

**Response `data`**

```json
{
  "leftAt": "2026-05-24T14:45:00"
}
```

---

### 3.13 结束训练（陪练端）

**POST** `/api/v1/rooms/{roomId}/end`  
**鉴权**：是（**仅 COACH**）  
**状态**：待实现

**说明**：陪练点击「结束训练」。后端会：结束 TRTC 会话、更新训练记录、订单变 `COMPLETED`、触发 AI 报告生成。

**Request Body**

```json
{
  "transcript": "用户本次表达内容（可选，语音转写）",
  "sceneName": "压力面试"
}
```

**Response `data`**

```json
{
  "trainingId": 100,
  "endedAt": "2026-05-24T15:00:00",
  "durationSeconds": 3540,
  "orderStatus": "COMPLETED",
  "reportReady": false
}
```

---

### 3.14 房间状态查询

**GET** `/api/v1/rooms/{roomId}`  
**鉴权**：是（USER 或 COACH）  
**状态**：待实现

**说明**：断线重连、轮询陪练是否进房时使用。阶段 1 返回基础状态，阶段 2 扩展压力模式字段。

**Response `data`**

```json
{
  "roomId": "a1b2c3d4...",
  "trainingId": 100,
  "orderId": 42,
  "trainingStatus": "IN_PROGRESS",
  "orderStatus": "IN_SERVICE",
  "startedAt": "2026-05-24T14:01:00",
  "endedAt": null,
  "durationSeconds": 1530,
  "serverTime": "2026-05-24T14:26:30",
  "participants": [
    { "role": "USER", "userId": 1, "joined": true, "joinedAt": "2026-05-24T14:01:05" },
    { "role": "COACH", "userId": 2, "joined": true, "joinedAt": "2026-05-24T14:01:10" }
  ]
}
```

---

### 3.15 结束训练并生成报告

**POST** `/api/v1/training/end`  
**鉴权**：是  
**状态**：已实现（阶段 1 将扩展：COACH 可通过 `/rooms/{roomId}/end` 间接触发；USER 仅 leave）

**Request Body**

```json
{
  "roomId": "a1b2c3d4e5f6789012345678901234",
  "transcript": "用户表达内容...",
  "sceneName": "压力面试"
}
```

**Response `data`**

```json
{
  "report": "{\"summary\":\"...\",\"scores\":{...}}"
}
```

> `report` 为 JSON 字符串，前端需 `JSON.parse(report)`。

---

### 3.16 查询训练报告

**POST** `/api/v1/training/report`  
**鉴权**：是（USER）  
**状态**：已实现

**Request Body**

```json
{
  "trainingRecordId": 100
}
```

**Response `data`**

```json
{
  "report": "{...}"
}
```

---

## 4. P1 接口详细说明（阶段 2）

### 4.1 房间实时状态（含压力模式）

**GET** `/api/v1/rooms/{roomId}/state`  
**鉴权**：是  
**状态**：待实现

**Response `data`**

```json
{
  "pressureMode": {
    "enabled": true,
    "countdown": { "active": true, "secondsLeft": 45, "totalSeconds": 60 },
    "lastInterrupt": { "message": "请直接给结论", "at": "2026-05-24T14:10:00" },
    "currentQuestion": { "questionId": 12, "text": "如果项目延期你怎么交代？" }
  },
  "whiteboard": { "active": false, "contentUrl": null },
  "serverTime": "2026-05-24T14:10:15"
}
```

> 实时性优先走 TRTC 自定义消息；此接口用于**重连补状态**和轮询兜底（建议 3s 间隔）。

---

### 4.2 压力模式 - 倒计时

**POST** `/api/v1/rooms/{roomId}/pressure/countdown`  
**鉴权**：是（**仅 COACH**）

**Request Body**

```json
{
  "action": "start",
  "seconds": 60
}
```

| action | 说明 |
|--------|------|
| start | 开启倒计时 |
| stop | 关闭倒计时 |
| reset | 重置 |

**Response `data`**

```json
{
  "active": true,
  "secondsLeft": 60
}
```

---

### 4.3 压力模式 - 打断

**POST** `/api/v1/rooms/{roomId}/pressure/interrupt`  
**鉴权**：是（**仅 COACH**）

**Request Body**

```json
{
  "message": "请直接给结论"
}
```

---

### 4.4 压力模式 - 压力提问

**POST** `/api/v1/rooms/{roomId}/pressure/question`  
**鉴权**：是（**仅 COACH**）

**Request Body**

```json
{
  "questionId": 12,
  "text": "如果项目延期你怎么交代？"
}
```

---

### 4.5 聊天（REST 兜底）

**POST** `/api/v1/rooms/{roomId}/chat`  
**GET** `/api/v1/rooms/{roomId}/chat?page=1&size=50`  
**鉴权**：是（双端）

POST Body: `{ "text": "你好，我们先从自我介绍开始" }`

GET Response records: `{ "fromRole", "fromUserId", "text", "createdAt" }`

---

### 4.6 训练资料

**POST** `/api/v1/rooms/{roomId}/materials` — multipart 上传  
**GET** `/api/v1/rooms/{roomId}/materials` — 列表，含签名下载 URL

---

### 4.7 陪练提交结构化反馈

**POST** `/api/v1/training/{trainingId}/coach-report`  
**鉴权**：是（**仅 COACH**）

**Request Body**

```json
{
  "scoreLogic": 4,
  "scoreFluency": 3,
  "scoreContent": 4,
  "scorePressure": 3,
  "scoreTiming": 4,
  "highlights": ["开场结论清晰", "第二点论据充分"],
  "improvements": ["结尾时间把控偏长", "被打断后主线丢失"],
  "comment": "整体表达逻辑较好，建议加强抗压时的主线回归..."
}
```

---

## 5. P2 接口详细说明（阶段 3）

### 5.1 录制回放

**GET** `/api/v1/training/{trainingId}/recording`  
**鉴权**：是（USER 或 COACH）

**Response `data`**

```json
{
  "recordingUrl": "https://...signed...",
  "durationSeconds": 3540,
  "highlights": [
    { "startSec": 120, "endSec": 135, "label": "精彩反驳", "clipUrl": "https://..." }
  ],
  "expiresAt": "2026-05-25T14:00:00"
}
```

---

### 5.2 提交评价

**POST** `/api/v1/orders/{orderId}/rating`  
**鉴权**：是（USER）

**Request Body**

```json
{
  "score": 5,
  "tags": ["专业", "耐心"],
  "comment": "很有帮助"
}
```

---

### 5.3 陪练数据统计

**GET** `/api/v1/coach/dashboard`  
**鉴权**：是（COACH）

**Response `data`**

```json
{
  "totalSessions": 128,
  "totalHours": 256.5,
  "goodReviewRate": 0.96,
  "levelName": "正式陪练",
  "levelProgress": 0.72,
  "monthIncome": 12800.00
}
```

---

## 6. 业务流程时序（阶段 1）

### 6.1 用户端

```
登录 → 创建订单 → Mock支付 → 订单列表(canEnterRoom=true)
  → POST /v1/training/start → 得到 roomId
  → 跳转 room 页
  → GET /v1/rooms/{roomId}/join-info
  → TRTC join + publish/subscribe
  → POST /v1/rooms/{roomId}/join
  → 训练中（计时用 startedAt + 本地 tick）
  → 用户挂断：POST leave + TRTC leave（不 end 订单）
  → 陪练结束：收到 SDK 对方离开 / 轮询 room 状态变 ENDED
  → 跳转报告页：POST /v1/training/report
```

### 6.2 陪练端

```
登录(COACH) → GET /v1/coach/orders → 选订单(已有 roomId 或等用户 start)
  → GET /v1/rooms/{roomId}/join-info
  → TRTC join
  → POST /v1/rooms/{roomId}/join
  → 训练中
  → POST /v1/rooms/{roomId}/end → 跳转反馈页
```

---

## 7. 状态枚举

### 7.1 订单状态 `order.status`

| 值 | 说明 |
|----|------|
| PENDING_PAY | 待支付 |
| PAID | 已支付，可开始训练 |
| IN_SERVICE | 训练中 |
| COMPLETED | 已完成 |
| CANCELLED | 已取消 |
| REFUNDING | 退款中 |
| REFUNDED | 已退款 |

### 7.2 训练状态 `training.status`

| 值 | 说明 |
|----|------|
| IN_PROGRESS | 进行中 |
| ENDED | 已结束，报告生成中 |
| REPORT_READY | 报告已就绪 |

---

## 8. 联调 Checklist

- [ ] 用户 Token（USER）+ 陪练 Token（COACH）各一个
- [ ] 测试订单：已 PAID，含 coachId、scheduledStart/End
- [ ] TRTC sdkAppId 已配置，join-info 能返回有效 userSig
- [ ] 双端用同一 roomId 能互相看到画面
- [ ] 陪练 end 后用户端 room 状态变 COMPLETED
- [ ] 403/400 错误文案前端 Toast 正常

---

## 9. 变更记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0 | 2026-05-24 | 初版，覆盖 P0/P1/P2 全量接口约定 |
