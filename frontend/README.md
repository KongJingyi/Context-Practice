# 语境智练 · 前端（ContextPractice-frontend）

基于 **uni-app + Vue 3 + Vite + TypeScript** 的跨端应用（H5 / 微信小程序等），与后端 API 通过 `VITE_API_BASE` 对接。

## 快速开始

```bash
npm install
npm run dev:h5          # H5 开发
npm run dev:mp-weixin   # 微信小程序（产物在 dist/dev/mp-weixin）
npm run type-check      # TypeScript 检查
```

环境变量见项目根目录 `.env`（示例：`VITE_API_BASE=/api`，开发时代理到 `localhost:8080`）。

---

## 目录结构说明

整体采用 **「页面薄壳 + 业务组件 + 按领域拆分」** 的组织方式：路由页只负责挂载，复杂 UI 与逻辑放在 `components/` 下按功能分目录；接口、类型、工具与业务域一一对应，便于协作与后期拆包。

```
frontend/
├── index.html
├── vite.config.ts          # Vite + uni 插件、@ 别名、/api 代理
├── tailwind.config.cjs     # Tailwind（部分页面使用，关键页多为 scoped CSS）
├── static/                 # 静态资源（tab 图标等，部分项目在 src/static）
└── src/
    ├── main.ts             # 应用入口
    ├── App.vue             # 根组件、全局样式（含 tailwind.css）
    ├── pages.json          # uni-app 路由与 tabBar（页面 path 须与此一致）
    ├── manifest.json       # 应用 manifest
    │
    ├── pages/              # 【路由层】仅做页面壳，不写大块业务
    │   ├── auth/login.vue
    │   ├── index/index.vue           # 首页 Tab
    │   ├── scenes/scenes.vue         # 场景广场 Tab
    │   ├── coach-hall/coach-hall.vue # 陪练大厅
    │   ├── expert-booking/...        # 专家预约
    │   ├── my-orders/my-orders.vue   # 我的订单 Tab
    │   ├── room/room.vue             # 训练房间（非 Tab，navigateTo 进入）
    │   └── profile/profile.vue       # 我的 Tab（个人看板）
    │
    ├── components/         # 【视图层】可复用业务组件，按功能分子目录
    │   ├── common/         # 通用 UI（如 RippleButton）
    │   ├── home/           # 首页：Hero、成长引擎画布
    │   ├── scene/          # 场景广场：ScenePlaza、SceneItem
    │   ├── coach-hall/     # 陪练大厅：CoachHall + 筛选/卡片/弹窗
    │   ├── booking/        # 专家详情与预约 ExpertBooking
    │   ├── orders/         # 我的订单 MyOrders
    │   └── dashboard/      # 个人主页 UserDashboard、GrowthLineChart
    │
    ├── api/                # 【接口层】
    │   ├── request.js      # uni.request 封装、Token、baseURL
    │   └── modules/        # 按业务域划分的 API（失败时可回落 mock）
    │       ├── auth.js
    │       ├── coach.js
    │       ├── expert.js
    │       ├── orders.js
    │       ├── dashboard.js    # 个人看板（原 userDashboard）
    │       └── user.js
    │
    ├── types/              # 【类型层】与 api/modules、components 域对应
    │   ├── home/           # hero、growthEngine
    │   ├── scene/          # plaza（场景广场嵌套数据）
    │   ├── coach/          # hall（陪练员列表）
    │   ├── booking/        # expert（预约）
    │   ├── orders/         # 订单列表项
    │   └── dashboard/      # 个人看板、成长曲线、勋章
    │
    ├── data/               # 【静态/默认数据】后端未就绪时的占位数据
    │   └── defaults/
    │       ├── scenePlaza.ts
    │       └── growthEngine.ts
    │
    ├── store/              # Pinia 状态（如 user 登录态）
    │   └── user.js
    │
    ├── utils/              # 【工具层】无 UI 的纯函数
    │   ├── auth/           # token、验证码、登录限流
    │   ├── scene/          # 场景解析、卡片模型转换
    │   ├── common/         # query 解码、用户名脱敏
    │   └── training/       # Tab 页传参 bridge（训练房间上下文）
    │
    └── styles/
        └── tailwind.css
```

---

## 设计原则

| 层级 | 职责 | 示例 |
|------|------|------|
| `pages/*` | 注册路由、解析 `onLoad` 参数、挂载一个主组件 | `coach-hall.vue` → `<CoachHall />` |
| `components/<feature>/` | 该功能的完整 UI 与交互 | `orders/MyOrders.vue` |
| `api/modules/*` | HTTP 请求；先调真实接口，失败再用 mock | `fetchOrders('active')` |
| `types/<domain>/` | 与接口、组件 props 共享的 TS 类型 | `types/scene/plaza.ts` |
| `data/defaults/` | 可替换的默认 JSON/常量，便于 props 注入 | `SCENE_PLAZA_DATA` |
| `utils/<domain>/` | 跨页面复用的非 UI 逻辑 | `resolveSceneById` |

**为何 `pages` 不按功能再套一层？**  
uni-app 要求 `pages.json` 中的 `path` 与 `src/pages` 下文件路径一致，因此路由目录保持扁平；业务复杂度上移到 `components/`。

**为何组件用 scoped CSS 为主？**  
uni-app 的 `<view>` / `<text>` 与 Tailwind 工具类在部分端上不稳定，核心业务页（订单、预约、看板）使用 **语义化 class + scoped** 保证 H5 与小程序表现一致。

---

## 主要页面与组件映射

| Tab / 路由 | 页面文件 | 主组件 |
|------------|----------|--------|
| 首页 | `pages/index/index` | `home/HeroSection`、`home/GrowthEngineCanvas` |
| 场景 | `pages/scenes/scenes` | `scene/ScenePlaza` |
| 我的订单 | `pages/my-orders/my-orders` | `orders/MyOrders` |
| 我的 | `pages/profile/profile` | `dashboard/UserDashboard`（需登录） |
| 陪练大厅 | `pages/coach-hall/coach-hall` | `coach-hall/CoachHall` |
| 专家预约 | `pages/expert-booking/expert-booking` | `booking/ExpertBooking` |
| 训练房间 | `pages/room/room` | 页内实现 + `utils/training/bridge` 上下文 |

---

## 路径别名

`@/` → `src/`（见 `vite.config.ts`）。引用示例：

```ts
import { fetchOrders } from "@/api/modules/orders.js";
import type { MyOrderItem } from "@/types/orders";
import MyOrders from "@/components/orders/MyOrders.vue";
```

---

## 依赖说明

- **echarts**：个人看板成长曲线（H5 下 `components/dashboard/GrowthLineChart.vue`）
- **motion-v**：首页等 H5 动效（条件编译）
- **pinia**：全局用户状态

---

## 仓库与后端

本目录为 Gerrit 仓库 **ContextPractice-frontend**。后端接口约定前缀为 `/api`（如 `/api/orders?status=active`），具体字段以各 `api/modules/*.js` 内 JSDoc 与 `types/` 为准。
