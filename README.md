# NeoApp

多端全栈应用，涵盖管理后台、Web 前端、微信小程序、Android/iOS/Harmony 移动端。

## 技术栈

| 模块 | 技术 |
|------|------|
| **后端** | Java 21, Spring Boot 3.4.1, MyBatis-Plus 3.5.7, Spring Security, JWT, MySQL 8.0 |
| **管理端 (admin)** | Vue 3, Vite, TDesign Vue Next, Quill 富文本编辑器, Lucide Icons |
| **Web 前端 (web)** | Vue 3, Vite, TDesign Vue Next, Lucide Icons |
| **存储** | 腾讯云 COS 对象存储 |
| **支付** | 微信支付 API v3（企业付款到零钱） |
| **登录** | 微信 OAuth2 登录 |

## 项目结构

```
neoapp/
├── backend/          # Spring Boot 后端 (端口 8080)
├── admin/            # 管理后台 (Vue 3)
├── web/              # Web 前端 (Vue 3)
├── wxmini/           # 微信小程序
├── android/          # Android
├── ios/              # iOS
├── harmony/          # HarmonyOS
├── dev-log/          # 开发日志
├── 待开发/            # 需求文档
└── README.md
```

## 快速启动

### 1. 数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS neoapp DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p neoapp < backend/src/main/resources/db/schema.sql
```

### 2. 后端

```bash
cd backend
# 修改 application-dev.yml 中的数据库密码和 COS/微信配置
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

服务启动于 `http://localhost:8080`

### 3. 前端

```bash
# 管理端
cd admin && npm install && npm run dev

# Web 前端
cd web && npm install && npm run dev
```

## 默认账号

| 端 | 账号 | 密码 |
|------|------|------|
| 管理后台 | admin | admin123 |
| C端用户 | 18868725001 | 123456 |

## 功能模块

### 用户系统
- 手机号注册/登录，JWT 无状态认证
- 微信 OAuth2 登录（自动注册 + 绑定）
- 个人资料编辑、头像上传（COS）
- 收货地址管理（增删改查、默认地址）
- 邀请码机制（注册绑定上级 + 奖励积分）

### 管理后台
- **仪表盘** — 数据概览
- **管理员管理** — 后台管理员 CRUD、最后登录/IP
- **用户管理** — C 端用户列表、默认地址、最后登录IP
- **积分管理** — 用户积分列表 + 积分流水
- **邀请管理** — 邀请关系 + 奖励记录
- **任务管理** — 发布/编辑/上下架任务，富文本详情
- **审核订单** — 审核用户提交、通过/驳回、授权打款
- **打款日志** — 微信支付打款流水
- **系统设置** — sys_config 动态配置

### Web 前端
- 任务中心（浏览/领取/提交/提现）
- 个人中心（资料编辑、头像上传、地址管理、积分、邀请）
- 微信登录/绑定

### 积分系统
- 注册送积分（`sys_config.point_register` 可配置）
- 邀请送积分（`sys_config.point_invite` 可配置）
- 积分流水记录

### 任务系统
- 管理员发布任务（标题/封面/富文本详情/奖励/24小时数量）
- 用户领取任务（同任务不重复，IP限制）
- 用户提交截图+订单号
- 管理员审核通过/驳回 + 授权打款
- 用户提现 → 微信商户付款到零钱

### 微信支付
- 企业付款到零钱（API v3）
- 支付回调处理
- 打款日志追踪

## API 接口文档

### 统一响应格式

```json
{ "code": 200, "msg": "成功", "data": {} }
```

| code | 说明 |
|------|------|
| 200  | 成功 |
| 400  | 参数错误 |
| 401  | 未登录 |
| 403  | 无权限 |
| 404  | 不存在 |
| 500  | 服务器错误 |

### API 汇总

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 健康检查 | GET | `/api/health` | 服务状态 |
| **C端认证** | | | |
| 注册 | POST | `/api/auth/register` | 手机号注册 |
| 登录 | POST | `/api/auth/login` | 手机号登录 |
| 微信授权URL | GET | `/api/auth/wechat/url` | OAuth授权地址 |
| 微信登录 | POST | `/api/auth/wechat/login` | code登录/注册 |
| 微信绑定 | POST | `/api/auth/wechat/bind` | 绑定微信 |
| 管理端登录 | POST | `/api/auth/admin/login` | 管理员登录 |
| **用户信息** | | | |
| 个人信息 | GET | `/api/user/info` | 当前用户 |
| 更新资料 | PUT | `/api/user/profile` | 昵称/性别/头像 |
| 上传头像 | POST | `/api/user/upload/avatar` | COS上传 |
| **收货地址** | | | |
| 列表 | GET | `/api/user/addresses` | 我的地址 |
| 新增 | POST | `/api/user/addresses` | 添加地址 |
| 更新 | PUT | `/api/user/addresses/{id}` | 修改地址 |
| 删除 | DELETE | `/api/user/addresses/{id}` | 删除地址 |
| 设默认 | PUT | `/api/user/addresses/{id}/default` | 默认地址 |
| **积分** | | | |
| 我的积分 | GET | `/api/user/points` | 积分余额 |
| 积分流水 | GET | `/api/user/points/logs` | 流水记录 |
| **邀请** | | | |
| 我的邀请 | GET | `/api/user/invites` | 邀请码+人数 |
| **任务(C端)** | | | |
| 任务列表 | GET | `/api/user/tasks` | 上架任务 |
| 任务详情 | GET | `/api/user/tasks/{id}` | 任务详情 |
| 领取任务 | POST | `/api/user/tasks/{id}/claim` | 领取 |
| 我的订单 | GET | `/api/user/tasks/orders` | 订单列表 |
| 提交订单 | PUT | `/api/user/tasks/orders/{id}/submit` | 提交截图 |
| 领取奖励 | POST | `/api/user/tasks/orders/{id}/withdraw` | 提现打款 |
| 打款日志 | GET | `/api/user/tasks/pay-logs` | 我的日志 |
| **管理端-用户** | | | |
| 用户列表 | GET | `/api/admin/users` | 分页+搜索 |
| 用户详情 | GET | `/api/admin/users/{id}` | 详情 |
| 更新用户 | PUT | `/api/admin/users/{id}` | 编辑 |
| 删除用户 | DELETE | `/api/admin/users/{id}` | 逻辑删除 |
| **管理端-管理员** | | | |
| 管理员列表 | GET | `/api/admin/admins` | 分页 |
| 新增管理员 | POST | `/api/admin/admins` | 创建 |
| 更新管理员 | PUT | `/api/admin/admins/{id}` | 编辑 |
| 删除管理员 | DELETE | `/api/admin/admins/{id}` | 删除 |
| **管理端-积分** | | | |
| 积分列表 | GET | `/api/admin/points` | 用户积分 |
| 积分流水 | GET | `/api/admin/points/logs` | 流水 |
| **管理端-邀请** | | | |
| 邀请关系 | GET | `/api/admin/invites` | 关系列表 |
| 奖励记录 | GET | `/api/admin/invites/rewards` | 奖励列表 |
| **管理端-任务** | | | |
| 任务列表 | GET | `/api/admin/tasks` | 分页+搜索 |
| 创建任务 | POST | `/api/admin/tasks` | 发布 |
| 更新任务 | PUT | `/api/admin/tasks/{id}` | 编辑 |
| 删除任务 | DELETE | `/api/admin/tasks/{id}` | 删除 |
| 审核订单 | GET | `/api/admin/tasks/orders` | 订单列表 |
| 审核操作 | PUT | `/api/admin/tasks/orders/{id}/audit` | 通过/驳回 |
| 授权打款 | PUT | `/api/admin/tasks/orders/{id}/grant` | 授权 |
| 打款日志 | GET | `/api/admin/tasks/pay-logs` | 日志 |
| **管理端-配置** | | | |
| 配置列表 | GET | `/api/admin/configs` | sys_config |
| 创建配置 | POST | `/api/admin/configs` | 新增 |
| 更新配置 | PUT | `/api/admin/configs/{id}` | 编辑 |
| 删除配置 | DELETE | `/api/admin/configs/{id}` | 删除 |
| **微信回调** | | | |
| 支付回调 | POST | `/api/public/wechat-pay/callback` | 微信通知 |

## 数据库表结构

### 业务用户模块（C端）
| 表名 | 说明 |
|------|------|
| `user_info` | 用户主表（昵称/头像/手机号/性别/邀请码/上级ID/最后登录IP） |
| `user_wechat` | 微信绑定（openid/unionid） |
| `user_address` | 收货地址（省市区/详情/默认标记） |

### 管理后台模块
| 表名 | 说明 |
|------|------|
| `admin_user` | 后台管理员（用户名/密码/最后登录IP） |
| `admin_role` | 后台角色 |
| `admin_user_role` | 管理员角色关联 |

### 积分模块
| 表名 | 说明 |
|------|------|
| `point_user` | 用户积分（累计/可用） |
| `point_log` | 积分流水（变动/余额/来源/备注） |

### 邀请模块
| 表名 | 说明 |
|------|------|
| `invite_user` | 邀请关系（上级/邀请人数） |
| `invite_reward_log` | 邀请奖励记录 |

### 任务模块
| 表名 | 说明 |
|------|------|
| `task_info` | 任务配置（标题/封面/详情/奖励/24小时数量） |
| `task_user_order` | 用户订单（领取→提交→审核→提现全流程） |
| `task_pay_log` | 微信打款日志 |

### 系统配置
| 表名 | 说明 |
|------|------|
| `sys_config` | 系统配置（key-value，含积分规则/IP限制等） |

## 系统配置项

| 键 | 说明 | 默认值 |
|------|------|------|
| `point_register` | 注册送积分 | 10 |
| `point_invite` | 邀请送积分 | 10 |
| `task_ip_days` | 任务IP限制天数 | 7 |

> 完整建表语句见 `backend/src/main/resources/db/schema.sql`
