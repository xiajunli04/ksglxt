# 广软课室申请管理系统 (ksglxt)

## 项目概述

广软课室申请管理系统是一套面向高校场景的课室预约与审批管理平台。系统采用前后端分离架构，支持管理员、教师、学生三种角色，提供课室浏览、在线预约、审批管理、公告发布、消息通知等完整业务功能。

### 系统演示

![系统演示](images/exp.gif)

---

## 目录

- [系统架构](#系统架构)
- [技术栈](#技术栈)
- [项目目录结构](#项目目录结构)
- [环境要求](#环境要求)
- [快速启动](#快速启动)
- [后端详细说明](#后端详细说明)
- [前端详细说明](#前端详细说明)
- [前后端联调与调试](#前后端联调与调试)
- [数据库设计](#数据库设计)
- [API 接口文档](#api-接口文档)
- [认证与安全机制](#认证与安全机制)
- [定时任务](#定时任务)
- [预置账号](#预置账号)
- [技术报告](#技术报告)

---

## 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                      用户浏览器                          │
│                   http://localhost:5173                   │
└─────────────────────┬───────────────────────────────────┘
                      │  HTTP / WebSocket
                      ▼
┌─────────────────────────────────────────────────────────┐
│              Vite Dev Server (开发代理)                   │
│    /api/*   ──proxy──>  http://localhost:8001/api/*      │
│    /uploads/* ─proxy──>  http://localhost:8001/uploads/* │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Boot 后端 (Port: 8001)               │
│  ┌──────────┐  ┌──────────┐  ┌───────────────────────┐  │
│  │Controller│─>│ Service  │─>│ Mapper (MyBatis-Plus) │  │
│  └──────────┘  └──────────┘  └───────────┬───────────┘  │
│  ┌──────────────────┐  ┌─────────────┐   │              │
│  │ Spring Security   │  │   Redis     │   │              │
│  │ + JWT Filter      │  │   Cache     │   │              │
│  └──────────────────┘  └─────────────┘   │              │
└──────────────────────────────────────────┼──────────────┘
                                           │
                                           ▼
                                  ┌─────────────────┐
                                  │   MySQL 8.x     │
                                  │  Database:      │
                                  │   ksglxt        │
                                  └─────────────────┘
```

**架构特点：**

- **前后端分离**：前端 Vue 3 SPA 应用通过 RESTful API 与后端通信
- **开发代理**：Vite 开发服务器内置反向代理，将 `/api` 和 `/uploads` 请求转发至后端，解决跨域问题
- **生产部署**：前端构建产物为纯静态文件，可由 Nginx 托管并反向代理 API 请求至后端
- **JWT 无状态认证**：不依赖服务器端 Session，Token 存储在客户端 localStorage

---

## 技术栈

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | JDK 运行环境 |
| Spring Boot | 3.4.6 | 核心应用框架 |
| Spring Security | (随 Spring Boot) | 安全认证与授权框架 |
| MyBatis-Plus | 3.5.7 | ORM 框架（注解模式，无 XML Mapper） |
| MySQL Connector/J | (随 Spring Boot) | MySQL 数据库驱动 |
| Spring Data Redis | 3.5.7 | Redis 缓存（Lettuce 连接池） |
| JJWT (jjwt) | 0.12.6 | JWT Token 生成与验证 |
| Lombok | (随 Spring Boot) | 编译期代码生成（减少样板代码） |
| Jakarta Validation | (随 Spring Boot) | 参数校验（Hibernate Validator） |
| Maven | (Spring Boot Parent) | 项目构建与依赖管理 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | ^3.5.31 | 渐进式 JavaScript 框架（Composition API + `<script setup>`） |
| Vue Router | ^5.0.4 | 前端路由管理（HTML5 History 模式） |
| Pinia | ^3.0.4 | 新一代状态管理（替代 Vuex） |
| Element Plus | ^2.13.7 | Vue 3 企业级 UI 组件库 |
| @element-plus/icons-vue | ^2.3.2 | Element Plus 图标库（全局注册） |
| Axios | ^1.15.0 | HTTP 请求客户端 |
| TypeScript | ~6.0.0 | 类型安全的 JavaScript 超集 |
| Vite | ^8.0.3 | 新一代前端构建工具（开发服务器 + 打包） |
| Vitest | ^4.1.2 | 单元测试框架 |
| Playwright | ^1.58.2 | E2E 端到端测试框架 |
| Prettier | 3.8.1 | 代码格式化工具 |
| pnpm | (包管理器) | 高效的 Node.js 包管理器 |

### 基础设施

| 组件 | 说明 |
|------|------|
| MySQL 8.x | 关系型数据库，存储所有业务数据 |
| Redis 7.x | 内存缓存，用于登录失败计数锁定和课室查询缓存 |
| Node.js ^20.19.0 或 >=22.12.0 | 前端运行与构建环境 |

---

## 项目目录结构

```
ksglxt/
├── backend/                          # 后端 Spring Boot 项目
│   ├── pom.xml                       # Maven 依赖配置
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/xjl/
│   │   │   │   ├── BackendApplication.java          # 应用入口
│   │   │   │   ├── config/                          # 配置类
│   │   │   │   │   ├── CorsConfig.java              # 跨域配置
│   │   │   │   │   ├── MybatisPlusConfig.java       # 分页插件 + 自动填充
│   │   │   │   │   ├── RedisConfig.java             # Redis 序列化配置
│   │   │   │   │   ├── SecurityConfig.java          # Spring Security + JWT 过滤器链
│   │   │   │   │   └── WebMvcConfig.java            # 静态资源映射（上传文件访问）
│   │   │   │   ├── controller/                      # 控制器层（10 个）
│   │   │   │   │   ├── AnnouncementController.java  # 公告管理
│   │   │   │   │   ├── ApprovalController.java      # 审批管理
│   │   │   │   │   ├── AuthController.java          # 认证（登录/注册/登出）
│   │   │   │   │   ├── BookingController.java       # 预约管理
│   │   │   │   │   ├── ClassroomController.java     # 课室管理
│   │   │   │   │   ├── DashboardController.java     # 仪表盘数据
│   │   │   │   │   ├── FileController.java          # 文件上传下载
│   │   │   │   │   ├── NotificationController.java  # 通知管理
│   │   │   │   │   ├── SysUserController.java       # 用户管理
│   │   │   │   │   └── TestController.java          # 健康检查
│   │   │   │   ├── domain/                          # 数据模型
│   │   │   │   │   ├── R.java                       # 统一响应包装类
│   │   │   │   │   ├── PageResult.java              # 分页结果包装类
│   │   │   │   │   ├── entity/                      # 实体类（12 个）
│   │   │   │   │   ├── dto/request/                 # 请求参数 DTO
│   │   │   │   │   └── dto/response/                # 响应视图对象 VO
│   │   │   │   ├── exception/
│   │   │   │   │   └── GlobalExceptionHandler.java  # 全局异常处理
│   │   │   │   ├── mapper/                          # MyBatis-Plus Mapper 接口
│   │   │   │   ├── scheduler/
│   │   │   │   │   └── BookingScheduler.java        # 定时任务（自动完成/关闭/提醒）
│   │   │   │   ├── security/                        # 安全模块
│   │   │   │   │   ├── JwtAuthenticationFilter.java # JWT 过滤器
│   │   │   │   │   ├── JwtTokenUtil.java            # JWT 工具类
│   │   │   │   │   ├── LoginUser.java               # 认证用户详情
│   │   │   │   │   └── CustomUserDetailsService.java
│   │   │   │   ├── service/                         # 业务接口
│   │   │   │   └── service/Impl/                    # 业务实现
│   │   │   └── resources/
│   │   │       ├── application.yaml                 # 应用配置文件
│   │   │       └── sql/init.sql                     # 数据库初始化脚本（建表 + 种子数据）
│   │   └── test/                                    # 测试目录
│   └── uploads/                                     # 上传文件存储目录
│       └── avatars/                                 # 用户头像
│
├── frontend/                         # 前端 Vue 3 项目
│   ├── package.json                  # npm 依赖与脚本
│   ├── pnpm-lock.yaml                # pnpm 锁文件
│   ├── vite.config.ts                # Vite 构建与代理配置
│   ├── tsconfig.json                 # TypeScript 配置
│   ├── index.html                    # SPA 入口 HTML
│   ├── public/                       # 静态资源
│   │   ├── favicon.ico
│   │   ├── logo.jpg
│   │   └── 228.mp4                   # 首页背景视频
│   └── src/
│       ├── main.ts                   # 应用入口（挂载 Vue 实例、注册 Element Plus）
│       ├── App.vue                   # 根组件
│       ├── api/                      # API 请求模块（9 个）
│       │   ├── auth.ts               # 认证 API
│       │   ├── user.ts               # 用户 API
│       │   ├── classroom.ts          # 课室 API
│       │   ├── booking.ts            # 预约 API
│       │   ├── approval.ts           # 审批 API
│       │   ├── announcement.ts       # 公告 API
│       │   ├── notification.ts       # 通知 API
│       │   ├── dashboard.ts          # 仪表盘 API
│       │   └── file.ts               # 文件上传下载 API
│       ├── assets/styles/
│       │   └── variables.css         # CSS 变量（设计 Token）
│       ├── layout/
│       │   └── MainLayout.vue        # 后台布局（顶栏 + 侧边栏 + 内容区）
│       ├── router/
│       │   └── index.ts              # 路由配置 + 导航守卫
│       ├── stores/                   # Pinia 状态管理
│       │   ├── user.ts               # 用户状态（token、登录/登出）
│       │   └── notification.ts       # 通知状态（未读数轮询）
│       ├── utils/
│       │   └── request.ts            # Axios 实例（拦截器、Token 注入）
│       └── views/                    # 页面组件（16 个）
│           ├── LandingView.vue       # 着陆页
│           ├── LoginView.vue         # 登录页
│           ├── RegisterView.vue      # 注册页
│           ├── DashboardView.vue     # 仪表盘首页
│           ├── ClassroomListView.vue # 课室列表
│           ├── ClassroomDetailView.vue # 课室详情/新增/编辑
│           ├── BookingCreateView.vue # 新建预约
│           ├── BookingDetailView.vue # 预约详情
│           ├── MyBookingView.vue     # 我的预约
│           ├── ApprovalPendingView.vue # 待审批列表
│           ├── ApprovalAllView.vue   # 全部审批记录
│           ├── AnnouncementListView.vue # 公告列表
│           ├── AnnouncementDetailView.vue # 公告详情/新增/编辑
│           ├── NotificationView.vue  # 通知消息
│           ├── UserListView.vue      # 用户管理
│           └── ProfileView.vue       # 个人中心
```

---

## 环境要求

### 必要软件

| 软件 | 版本要求 | 用途 | 安装方式 |
|------|----------|------|----------|
| JDK | 17 | 运行 Java 后端 | [Adoptium](https://adoptium.net/) 或 Oracle JDK |
| Maven | 3.8+ | 后端项目构建 | [maven.apache.org](https://maven.apache.org/) |
| MySQL | 8.0+ | 关系型数据库 | [mysql.com](https://dev.mysql.com/downloads/) |
| Redis | 7.0+ | 内存缓存 | [redis.io](https://redis.io/) |
| Node.js | ^20.19.0 或 >=22.12.0 | 前端运行环境 | [nodejs.org](https://nodejs.org/) |
| pnpm | 最新版 | 前端包管理器 | `npm install -g pnpm` |

### 环境变量/配置

- MySQL 需要运行在 `localhost:3306`，默认用户名 `root`，密码 `123456`
- Redis 需要运行在 `localhost:6379`，默认密码 `123456`

---

## 快速启动

### 第一步：启动 MySQL 并创建数据库

```sql
-- 登录 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE ksglxt;
```

> 系统首次启动时会自动执行 `init.sql` 创建表结构并导入种子数据（角色、预置用户、菜单权限、时间段、示例课室）。

### 第二步：启动 Redis

```bash
# Linux / macOS
redis-server

# Windows (如果以服务安装)
net start Redis

# 或直接运行
redis-server.exe
```

> 如果 Redis 设置了密码，需确保与 `application.yaml` 中 `spring.data.redis.password` 一致。

### 第三步：启动后端

```bash
cd backend

# 方式一：Maven 命令行
mvn clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar

# 方式二：Maven 直接运行（开发推荐）
mvn spring-boot:run

# 方式三：IDE 中运行
# 在 IntelliJ IDEA / Eclipse 中打开 BackendApplication.java，右键 Run
```

启动成功后控制台输出：

```
Started BackendApplication in X.XX seconds (process running on 8001)
```

后端运行在 **http://localhost:8001**

### 第四步：启动前端

```bash
cd frontend

# 安装依赖（首次或依赖变更时）
pnpm install

# 启动开发服务器
pnpm dev
```

启动成功后控制台输出：

```
  VITE v8.x.x  ready in Xms

  ➜  Local:   http://localhost:5173/
  ➜  Network: http://192.168.x.x:5173/
```

前端运行在 **http://localhost:5173**

### 第五步：访问系统

打开浏览器访问 **http://localhost:5173**，使用预置账号登录：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| zhangsan | 123456 | 教师 |
| xiajunli | 123456 | 学生 |

---

## 后端详细说明

### 应用配置

配置文件位于 `backend/src/main/resources/application.yaml`：

```yaml
server:
  port: 8001                        # 服务端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ksglxt?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
    username: root
    password: 123456
  data:
    redis:
      host: localhost
      port: 6379
      password: 123456
  servlet:
    multipart:
      max-file-size: 5MB            # 单文件最大 5MB
      max-request-size: 10MB        # 单次请求最大 10MB

jwt:
  secret: <256位以上的密钥>
  expiration: 7200000               # Token 有效期 2 小时（毫秒）
  header: Authorization
  prefix: "Bearer "

file:
  upload-dir: <项目路径>/backend/uploads   # 文件上传存储路径
```

### 分层架构

```
Controller (接收请求，参数校验)
    │
    ▼
Service (业务逻辑，事务管理)
    │
    ▼
Mapper (MyBatis-Plus，数据库操作)
    │
    ▼
MySQL / Redis
```

**各层职责：**

- **Controller 层**：接收 HTTP 请求，使用 `@Valid` 注解校验请求参数，调用 Service 层处理业务逻辑，返回统一格式的 `R<T>` 响应
- **Service 层**：核心业务逻辑，包括预约冲突检测、审批流程、缓存管理等。接口定义在 `service/` 目录，实现在 `service/Impl/` 目录
- **Mapper 层**：继承 MyBatis-Plus 的 `BaseMapper<T>`，自动获得 CRUD 能力，无需编写 XML 映射文件。复杂查询通过 `LambdaQueryWrapper` 链式构建
- **Domain 层**：包含 Entity（数据库实体）、DTO/Request（请求参数）、VO/Response（响应视图）三类数据模型

### 统一响应格式

所有 API 返回统一的 JSON 格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 业务异常（参数错误、重复操作等） |
| 401 | 未认证（Token 缺失或过期） |
| 403 | 无权限 |

分页响应的 `data` 格式：

```json
{
  "records": [ ... ],
  "total": 100,
  "pageNum": 1,
  "pageSize": 10
}
```

### 关键设计模式

| 模式 | 实现方式 | 代码位置 |
|------|----------|----------|
| 统一响应 | `R<T>` 泛型包装类 | `domain/R.java` |
| 分页查询 | `PageQuery` 输入 + `PageResult<T>` 输出 | `domain/dto/request/PageQuery.java`、`domain/PageResult.java` |
| 逻辑删除 | `@TableLogic` 注解，`del_flag` 字段 | 所有 Entity 类 |
| 自动填充 | `MetaObjectHandler` 自动填充 `createTime`/`updateTime`/`createBy`/`updateBy` | `config/MybatisPlusConfig.java` |
| Redis 缓存 | `@Cacheable`/`@CacheEvict` 缓存课室查询 | `service/Impl/ClassroomServiceImpl.java` |
| 全局异常处理 | `@RestControllerAdvice` 统一捕获异常 | `exception/GlobalExceptionHandler.java` |
| CORS 跨域 | 允许 `http://localhost:*` 来源 | `config/CorsConfig.java` |

---

## 前端详细说明

### 路由系统

路由配置位于 `src/router/index.ts`，使用 Vue Router 5 的 HTML5 History 模式。

**公共路由（无需登录）：**

| 路径 | 页面 | 说明 |
|------|------|------|
| `/` | LandingView.vue | 系统着陆页（背景视频 + 功能介绍） |
| `/login` | LoginView.vue | 登录页（已登录自动跳转仪表盘） |
| `/register` | RegisterView.vue | 注册页（选择教师/学生角色） |

**认证路由（需要登录，使用 MainLayout 布局）：**

| 路径 | 页面 | 权限 | 说明 |
|------|------|------|------|
| `/admin/dashboard` | DashboardView.vue | 所有 | 仪表盘首页（角色感知统计） |
| `/admin/classroom` | ClassroomListView.vue | 所有 | 课室列表（筛选 + 分页） |
| `/admin/classroom/add` | ClassroomDetailView.vue | 所有 | 新增课室 |
| `/admin/classroom/:id` | ClassroomDetailView.vue | 所有 | 课室详情（时段可用性 + 快捷预约） |
| `/admin/booking/my` | MyBookingView.vue | 教师/学生 | 我的预约列表（状态筛选） |
| `/admin/booking/create` | BookingCreateView.vue | 教师/学生/管理员 | 新建预约 |
| `/admin/booking/:id` | BookingDetailView.vue | 所有 | 预约详情（审批/取消操作） |
| `/admin/approval/pending` | ApprovalPendingView.vue | 管理员 | 待审批列表 |
| `/admin/approval/all` | ApprovalAllView.vue | 管理员 | 全部审批记录 |
| `/admin/announcement` | AnnouncementListView.vue | 所有 | 公告列表 |
| `/admin/announcement/add` | AnnouncementDetailView.vue | 所有 | 新增公告 |
| `/admin/announcement/:id` | AnnouncementDetailView.vue | 所有 | 公告详情/编辑 |
| `/admin/notification` | NotificationView.vue | 所有 | 通知消息（未读标记） |
| `/admin/user/list` | UserListView.vue | 管理员 | 用户管理（搜索 + 启停用） |
| `/admin/profile` | ProfileView.vue | 所有 | 个人中心（头像上传 + 信息修改 + 改密码） |

### 导航守卫

路由守卫在 `src/router/index.ts` 中实现 `beforeEach` 全局前置守卫：

1. **动态标题**：设置页面标题为 `{页面名} - 广软课室申请系统`
2. **公开页面**：着陆页 `/` 始终可访问
3. **游客页面**：登录/注册页，已登录用户自动重定向至仪表盘
4. **认证检查**：无 Token 的用户访问受保护页面时重定向至登录页
5. **角色检查**：根据路由 `meta.roles` 校验用户角色，ADMIN 角色拥有所有权限

### 状态管理 (Pinia)

**用户 Store** (`src/stores/user.ts`)：
- 管理登录状态、Token、用户信息
- Token 和用户信息持久化至 `localStorage`
- 登录时调用 API 获取 Token，登出时清除所有本地数据

**通知 Store** (`src/stores/notification.ts`)：
- 管理未读通知数量
- 每 30 秒轮询后端获取最新未读数
- 在 `MainLayout` 挂载时启动轮询，卸载时停止

### HTTP 请求封装

`src/utils/request.ts` 创建 Axios 实例，配置如下：

- **baseURL**: `/api`（开发环境由 Vite 代理转发至后端）
- **timeout**: 15000ms（15 秒）
- **请求拦截器**：自动从 `localStorage` 读取 Token，附加到 `Authorization: Bearer <token>` 请求头
- **响应拦截器**：
  - 检查响应体中的 `code` 字段，非 200 时弹出 Element Plus 错误提示
  - 401 状态自动清除 Token 并跳转登录页
  - 403 状态提示无权限
  - 网络异常提示"网络异常，请稍后重试"

---

## 前后端联调与调试

### 开发环境联调

开发时前后端通过 **Vite 代理** 联调，无需额外配置 CORS：

```typescript
// vite.config.ts 中的代理配置
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8001',
      changeOrigin: true,
    },
    '/uploads': {
      target: 'http://localhost:8001',
      changeOrigin: true,
    },
  },
}
```

**请求流转过程：**

```
浏览器 → http://localhost:5173/api/auth/login
       → Vite Dev Server 代理转发
       → http://localhost:8001/api/auth/login
       → Spring Boot Controller 处理
       → 返回 JSON 响应
       → Vite Dev Server 转发回浏览器
```

### 后端接口调试

**方式一：浏览器开发者工具**

在浏览器 F12 Network 面板中查看所有 API 请求和响应。

**方式二：cURL 命令**

```bash
# 登录获取 Token
curl -X POST http://localhost:8001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userName":"admin","password":"123456"}'

# 使用 Token 访问接口
curl http://localhost:8001/api/user/info \
  -H "Authorization: Bearer <your-token>"

# 查看课室列表
curl http://localhost:8001/api/classroom/list?pageNum=1&pageSize=10

# 健康检查
curl http://localhost:8001/test/hello
```

**方式三：Postman / Apifox**

1. 导入接口信息或手动创建请求
2. 登录接口获取 Token
3. 在后续请求的 Header 中添加 `Authorization: Bearer <token>`

### 前端调试

**Vue DevTools**：

安装 Vue DevTools 浏览器扩展，可查看组件树、Pinia Store 状态、路由信息。

**控制台调试**：

Vite 已集成 `vite-plugin-vue-devtools`，开发时自动可用。

**SQL 日志**：

后端 `application.yaml` 配置了 MyBatis SQL 日志输出：

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

后端控制台会打印所有执行的 SQL 语句及参数。

### 生产构建与部署

```bash
# 前端构建
cd frontend
pnpm build
# 产物位于 frontend/dist/ 目录

# 后端构建
cd backend
mvn clean package -DskipTests
# 产物位于 backend/target/backend-0.0.1-SNAPSHOT.jar
```

**生产环境 Nginx 配置示例：**

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://localhost:8001/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 上传文件代理
    location /uploads/ {
        proxy_pass http://localhost:8001/uploads/;
    }
}
```

---

## 数据库设计

### ER 关系图

```
┌──────────┐     ┌───────────────┐     ┌──────────┐
│ sys_user │────<│ sys_user_role │>────│ sys_role │
└────┬─────┘     └───────────────┘     └────┬─────┘
     │                                      │
     │           ┌───────────────┐          │
     │           │ sys_role_menu │>─────────┘
     │           └───────┬───────┘
     │                   │
     │                   ▼
     │            ┌──────────┐
     │            │ sys_menu │
     │            └──────────┘
     │
     ├──────┐
     │      │
     ▼      ▼
┌────────┐  ┌──────────┐     ┌───────────┐
│booking │  │announcement│    │notification│
└───┬────┘  └──────────┘     └───────────┘
    │
    ├──> ┌───────────┐
    │    │ classroom  │
    │    └───────────┘
    │
    └──> ┌───────────┐
         │ time_slot  │
         └───────────┘
```

### 数据表清单（共 12 张）

| # | 表名 | 说明 | 核心字段 |
|---|------|------|----------|
| 1 | `sys_user` | 用户信息表 | user_name, password, nick_name, status |
| 2 | `sys_role` | 角色信息表 | role_name, role_code (ADMIN/TEACHER/STUDENT) |
| 3 | `sys_menu` | 菜单权限表 | parent_id, menu_type (M/C/F), permission |
| 4 | `sys_user_role` | 用户角色关联表 | user_id, role_id |
| 5 | `sys_role_menu` | 角色菜单关联表 | role_id, menu_id |
| 6 | `classroom` | 课室信息表 | room_code, building, capacity, room_type, status |
| 7 | `time_slot` | 时间段表 | slot_name, start_time, end_time |
| 8 | `booking` | 预约申请表 | applicant_id, classroom_id, booking_date, time_slot_id, status |
| 9 | `announcement` | 公告表 | title, content, publisher_id, is_top, status |
| 10 | `notification` | 通知表 | user_id, title, content, type, is_read |
| 11 | `operation_log` | 操作日志表 | user_id, operation, method, ip |
| 12 | `dict_data` | 字典数据表 | dict_type, dict_label, dict_value |

### 预置时间段

| 时段 | 时间范围 |
|------|----------|
| 第1-2节 | 08:00 - 09:40 |
| 第3-4节 | 10:00 - 11:40 |
| 第5-6节 | 14:00 - 15:40 |
| 第7-8节 | 16:00 - 17:40 |
| 第9-10节 | 19:00 - 20:40 |

### 状态码约定

**预约状态（booking.status）：**

| 值 | 含义 | 说明 |
|----|------|------|
| 0 | 待审批 | 用户提交预约后的初始状态 |
| 1 | 已通过 | 管理员审批通过 |
| 2 | 已驳回 | 管理员审批驳回（附驳回原因） |
| 3 | 已取消 | 用户主动取消或系统超时自动取消 |
| 4 | 已完成 | 系统在预约时间过后自动标记 |

**课室状态（classroom.status）：**

| 值 | 含义 |
|----|------|
| 0 | 可用 |
| 1 | 停用 |
| 2 | 维修中 |

**通知类型（notification.type）：**

| 值 | 含义 |
|----|------|
| 1 | 审批通过通知 |
| 2 | 审批驳回通知 |
| 3 | 系统提醒 |

---

## API 接口文档

### 认证接口 `/api/auth`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/register` | 否 | 用户注册（可选角色：TEACHER/STUDENT） |
| POST | `/api/auth/login` | 否 | 用户登录，返回 JWT Token + 用户信息 |
| POST | `/api/auth/logout` | 否 | 用户登出 |

### 课室接口 `/api/classroom`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/classroom/list` | 否 | 课室列表（支持 building/roomType/status/keyword 筛选） |
| GET | `/api/classroom/{id}` | 否 | 课室详情 |
| GET | `/api/classroom/{id}/slots?date=` | 否 | 指定日期的可用时段 |
| POST | `/api/classroom` | 管理员 | 新增课室 |
| PUT | `/api/classroom/{id}` | 管理员 | 编辑课室 |
| DELETE | `/api/classroom/{id}` | 管理员 | 删除课室（软删除，有关联预约时拒绝） |

### 预约接口 `/api/booking`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/booking` | 用户 | 创建预约（冲突检测） |
| GET | `/api/booking/my` | 用户 | 我的预约列表（status 筛选） |
| GET | `/api/booking/slot-info` | 用户 | 查看时段预约情况 |
| GET | `/api/booking/{id}` | 用户 | 预约详情 |
| PUT | `/api/booking/{id}/cancel` | 用户 | 取消预约（仅限待审批/已通过状态） |

### 审批接口 `/api/approval`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/approval/pending` | 管理员 | 待审批列表 |
| GET | `/api/approval/all` | 管理员 | 全部预约记录（支持 status/startDate/endDate/classroomId 筛选） |
| PUT | `/api/approval/{id}/approve` | 管理员 | 通过审批（自动发送通知） |
| PUT | `/api/approval/{id}/reject?reason=` | 管理员 | 驳回审批（附原因，自动发送通知） |

### 公告接口 `/api/announcement`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/announcement/list` | 否 | 公告列表（置顶优先，分页） |
| GET | `/api/announcement/{id}` | 否 | 公告详情 |
| POST | `/api/announcement` | 管理员 | 发布公告 |
| PUT | `/api/announcement/{id}` | 管理员 | 编辑公告 |
| DELETE | `/api/announcement/{id}` | 管理员 | 删除公告 |

### 用户接口 `/api/user`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/user/info` | 用户 | 获取当前用户信息及角色 |
| PUT | `/api/user/info` | 用户 | 修改个人资料 |
| PUT | `/api/user/password` | 用户 | 修改密码（需验证旧密码） |
| POST | `/api/user/avatar` | 用户 | 上传头像 |
| GET | `/api/user/list` | 管理员 | 用户列表（搜索 + 分页） |
| PUT | `/api/user/{id}/status?status=` | 管理员 | 启用/停用用户 |

### 通知接口 `/api/notification`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/notification/list` | 用户 | 通知列表（分页） |
| GET | `/api/notification/unread-count` | 用户 | 未读通知数量 |
| PUT | `/api/notification/{id}/read` | 用户 | 标记已读 |
| PUT | `/api/notification/read-all` | 用户 | 全部标记已读 |

### 仪表盘接口 `/api/dashboard`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/dashboard/stats` | 用户 | 统计数据（按角色返回不同指标） |
| GET | `/api/dashboard/recent-announcements` | 用户 | 最近 5 条公告 |
| GET | `/api/dashboard/hot-classrooms` | 用户 | 近 30 天热门课室 Top 5 |
| GET | `/api/dashboard/available-classrooms` | 用户 | 当前可用课室 Top 5 |
| GET | `/api/dashboard/recent-bookings` | 用户 | 最近 5 条预约 |

### 文件接口 `/api/file`

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/file/upload` | 用户 | 上传文件（pdf/doc/docx/jpg/png，最大 5MB） |
| GET | `/api/file/download/{filename}` | 用户 | 下载文件 |

---

## 认证与安全机制

### JWT 认证流程

```
1. 用户注册/登录
   POST /api/auth/login  { userName, password }
        │
        ▼
2. 后端验证密码（BCrypt 对比）
   检查 Redis 锁定状态（5 次失败后锁定 30 分钟）
        │
        ▼
3. 生成 JWT Token（HMAC-SHA256，有效期 2 小时）
   返回 { token, userInfo }
        │
        ▼
4. 前端存储 Token 到 localStorage
   后续请求自动附加 Authorization: Bearer <token>
        │
        ▼
5. 后端 JwtAuthenticationFilter 拦截请求
   解析 Token → 加载用户权限 → 放入 SecurityContext
        │
        ▼
6. Spring Security 权限校验
   @PreAuthorize("hasRole('ADMIN')") 等注解控制
```

### 安全特性

| 特性 | 实现方式 | 代码位置 |
|------|----------|----------|
| 密码加密 | BCrypt 哈希算法 | `service/Impl/AuthServiceImpl.java` |
| Token 生成 | JJWT 0.12.6 HMAC-SHA256 | `security/JwtTokenUtil.java` |
| 请求过滤 | `OncePerRequestFilter` 一次请求一次验证 | `security/JwtAuthenticationFilter.java` |
| 账户锁定 | Redis 记录失败次数，5 次锁定 30 分钟 | `service/Impl/AuthServiceImpl.java` |
| 权限模型 | RBAC（用户-角色-菜单权限三级模型） | `security/CustomUserDetailsService.java` |
| 文件上传限制 | 类型白名单 + 5MB 大小限制 | `util/FileUtil.java` + `application.yaml` |
| 跨域配置 | 允许 `http://localhost:*` 来源 | `config/CorsConfig.java` |

### 公开接口（无需认证）

以下接口不需要 Token 即可访问：

- `POST /api/auth/**` — 登录、注册、登出
- `GET /api/classroom/**` — 课室浏览
- `GET /api/announcement/**` — 公告查看
- `GET /uploads/**` — 上传文件访问
- `GET /test/hello` — 健康检查

---

## 定时任务

系统通过 Spring `@Scheduled` 注解实现三个自动化定时任务，代码位于 `scheduler/BookingScheduler.java`：

| 任务 | 执行频率 | 功能说明 |
|------|----------|----------|
| **自动完成** | 每小时整点 | 将已通过且时间已过的预约标记为"已完成"（status=4），并发送通知 |
| **自动关闭** | 每日 00:30 | 将超过 3 天仍为"待审批"状态的预约自动取消（status=3），并发送通知 |
| **预约提醒** | 每日 08:00 | 向当天有预约的用户发送提醒通知 |

---

## 预置账号

系统通过 `init.sql` 初始化以下账号，密码统一为 `123456`（BCrypt 加密存储）：

| 用户名 | 昵称 | 角色 | 邮箱 | 权限范围 |
|--------|------|------|------|----------|
| admin | 管理员 | ADMIN | admin@xjl.com | 全部菜单和按钮权限（`*:*:*` 通配符） |
| zhangsan | 张三 | TEACHER | zhangsan@xjl.com | 首页、课室查询、预约管理、审批查看、公告查看、个人中心 |
| xiajunli | 夏俊丽 | STUDENT | xiajunli@xjl.com | 首页、课室查询、预约管理、公告查看、个人中心 |

---

## 技术报告

### 系统功能矩阵

| 功能模块 | 管理员 | 教师 | 学生 |
|----------|--------|------|------|
| 仪表盘统计 | 全局数据 | 个人数据 | 个人数据 |
| 课室浏览 | 查看列表 + 增删改 | 查看列表 | 查看列表 |
| 课室预约 | 可创建 | 可创建 | 可创建 |
| 预约管理 | 查看所有预约 | 查看自己的预约 | 查看自己的预约 |
| 审批管理 | 审批/驳回 | 查看审批状态 | 查看审批状态 |
| 公告管理 | 增删改查 + 发布 | 查看公告 | 查看公告 |
| 用户管理 | 搜索/启停用 | 无 | 无 |
| 通知消息 | 收到审批通知 | 收到审批结果 + 提醒 | 收到审批结果 + 提醒 |
| 个人中心 | 修改资料/密码/头像 | 同左 | 同左 |

### 关键业务流程

**预约审批流程：**

```
用户提交预约 → 系统冲突检测 → 状态:待审批
                                    │
                     ┌──────────────┴──────────────┐
                     ▼                              ▼
              管理员通过                     管理员驳回
           状态:已通过                    状态:已驳回
           发送通知                        发送通知(含原因)
                │
                ▼
         预约时间到达
           状态:已完成
           发送提醒
```

**异常处理流程：**

```
超3天未审批 → 定时任务自动取消 → 发送通知
用户取消 → 状态:已取消（仅待审批/已通过可取消）
```

### 代码实现索引

| 功能 | 后端实现 | 前端实现 |
|------|----------|----------|
| 登录/注册 | `controller/AuthController.java` → `AuthServiceImpl.java` | `views/LoginView.vue`、`views/RegisterView.vue` → `api/auth.ts` |
| JWT 认证 | `security/JwtAuthenticationFilter.java` + `JwtTokenUtil.java` | `utils/request.ts`（Token 注入） + `stores/user.ts`（Token 管理） |
| 课室 CRUD | `controller/ClassroomController.java` → `ClassroomServiceImpl.java` | `views/ClassroomListView.vue`、`views/ClassroomDetailView.vue` → `api/classroom.ts` |
| 预约创建 | `controller/BookingController.java` → `BookingServiceImpl.java` | `views/BookingCreateView.vue` → `api/booking.ts` |
| 预约审批 | `controller/ApprovalController.java` → `ApprovalServiceImpl.java` | `views/ApprovalPendingView.vue`、`views/ApprovalAllView.vue` → `api/approval.ts` |
| 公告管理 | `controller/AnnouncementController.java` → `AnnouncementServiceImpl.java` | `views/AnnouncementListView.vue`、`views/AnnouncementDetailView.vue` → `api/announcement.ts` |
| 通知系统 | `controller/NotificationController.java` → `NotificationServiceImpl.java` | `views/NotificationView.vue` → `api/notification.ts` + `stores/notification.ts`（轮询） |
| 仪表盘 | `controller/DashboardController.java` → `DashboardServiceImpl.java` | `views/DashboardView.vue` → `api/dashboard.ts` |
| 用户管理 | `controller/SysUserController.java` → `SysUserServiceImpl.java` | `views/UserListView.vue` → `api/user.ts` |
| 个人中心 | `controller/SysUserController.java` | `views/ProfileView.vue` → `api/user.ts` |
| 文件上传 | `controller/FileController.java` → `FileServiceImpl.java` | `api/file.ts` |
| 定时任务 | `scheduler/BookingScheduler.java` | — |
| 权限控制 | `config/SecurityConfig.java` + `@PreAuthorize` 注解 | `router/index.ts`（`meta.roles` 路由守卫） |
| 缓存策略 | `RedisConfig.java` + `@Cacheable`/`@CacheEvict` | — |
| 全局异常 | `exception/GlobalExceptionHandler.java` | `utils/request.ts`（响应拦截器） |

### 构建与测试

```bash
# 后端测试
cd backend
mvn test

# 前端单元测试
cd frontend
pnpm test:unit

# 前端 E2E 测试
pnpm test:e2e

# 前端类型检查
pnpm type-check

# 前端代码格式化
pnpm format

# 前端构建
pnpm build
```

### 常见问题排查

| 问题 | 可能原因 | 解决方法 |
|------|----------|----------|
| 后端启动报数据库连接失败 | MySQL 未启动或密码不匹配 | 检查 MySQL 服务状态和 `application.yaml` 中的数据库配置 |
| 后端启动报 Redis 连接失败 | Redis 未启动或密码不匹配 | 检查 Redis 服务状态和 `application.yaml` 中的 Redis 配置 |
| 前端 API 请求 404 | 后端未启动或端口不对 | 确保后端运行在 8001 端口，检查 `vite.config.ts` 代理配置 |
| 前端 API 请求 401 | Token 过期或无效 | 重新登录获取新 Token |
| 文件上传失败 | 文件超过 5MB 或类型不支持 | 检查文件大小和类型（pdf/doc/docx/jpg/png） |
| 登录失败"账户已锁定" | 连续 5 次密码错误 | 等待 30 分钟或清除 Redis 中 `auth:lock:<username>` 键 |
| 数据库表不存在 | init.sql 未执行 | 检查 `application.yaml` 中 `spring.sql.init.mode=always` 配置 |
| 着陆页视频无法播放 | 228.mp4 文件不存在 | 确保 `frontend/public/228.mp4` 文件存在 |

---

## 许可证

本项目为广软课室申请管理系统，仅供学习与内部使用。
