# NeoApp

多端全栈应用，涵盖管理后台、Web 前端、微信小程序、Android/iOS/Harmony 移动端。

## 技术栈

| 模块 | 技术 |
|------|------|
| **后端** | Java 21, Spring Boot 3.4.1, MyBatis-Plus 3.5.7, Spring Security, JWT, MySQL 8.0 |
| **管理端 (admin)** | Vue 3, Vite, TDesign Vue Next, Quill 富文本编辑器, Lucide Icons |
| **Web 前端 (web)** | Vue 3, Vite, TDesign Vue Next, Lucide Icons |
| **存储** | 腾讯云 COS 对象存储 |
| **支付** | 微信支付 API v3（企业付款到零钱 + H5支付） |
| **登录** | 手机号登录 + 微信 OAuth2 登录 |

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
| 菜单 | 路由 | 功能 |
|------|------|------|
| 仪表盘 | `/dashboard` | 用户/任务/订单/打款实时统计 |
| 管理员管理 | `/admins` | 后台管理员 CRUD + 最后登录IP |
| 用户管理 | `/users` | C端用户列表 + 默认地址 + 最后登录IP |
| 用户积分 | `/points` | 用户积分列表 + 搜索 + 查看流水 |
| 积分流水 | `/point-logs` | 积分变动记录 + 类型筛选 |
| 邀请管理 | `/invites` | 邀请关系 + 奖励记录 |
| 任务管理 | `/tasks` | 发布/编辑任务（富文本+24h数量） |
| 审核订单 | `/task-orders` | 审核通过/驳回/授权打款 |
| 打款日志 | `/task-pay-logs` | 微信支付打款流水 |
| 套餐管理 | `/members` | 会员套餐 CRUD |
| 用户会员 | `/member-users` | 用户会员状态 + 到期时间 |
| 系统设置 | `/settings` | sys_config 动态配置 |

### Web 前端
- 首页任务展示 + 导航（任务中心/会员中心/个人中心/我的任务）
- 任务中心（领取→查看详情→提交截图+订单号→审核状态→提现）
- 会员中心（购买套餐 → 微信 H5 支付）
- 个人中心（资料编辑、头像上传、地址管理、积分、邀请、绑定微信）

### 积分系统
- 注册送积分（`sys_config.point_register` 可配置）
- 邀请送积分（`sys_config.point_invite` 可配置）
- 购买会员送积分（每个套餐独立配置）
- 积分流水记录（来源：register/invite/member）

### 任务系统
- 管理员发布任务（标题/封面/富文本详情/奖励/24小时格子数量）
- 用户领取任务（同任务不重复，IP限制天数额可配）
- 用户提交截图（最多3张）+ 订单号
- 管理员审核通过/驳回（已通过也可驳回）
- 管理员授权打款（与审核独立操作）
- 用户提现 → 微信商户付款到零钱

### 会员套餐
- 管理员管理套餐（名称/价格/天数/赠积分/排序/状态）
- 用户浏览套餐、购买会员
- 微信 H5 支付（未配置时直接开通）
- 购买赠送积分

### 微信支付
- 企业付款到零钱（任务提现）
- H5 支付（会员购买）
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
| **通用** | | | |
| 健康检查 | GET | `/api/health` | 服务状态 |
| 仪表盘 | GET | `/api/admin/dashboard/stats` | 统计数据 |
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
| 新增 | POST | `/api/user/addresses` | 添加 |
| 更新 | PUT | `/api/user/addresses/{id}` | 修改 |
| 删除 | DELETE | `/api/user/addresses/{id}` | 删除 |
| 设默认 | PUT | `/api/user/addresses/{id}/default` | 默认地址 |
| **积分(C端)** | | | |
| 我的积分 | GET | `/api/user/points` | 余额 |
| 积分流水 | GET | `/api/user/points/logs` | 记录 |
| **积分(管理端)** | | | |
| 积分列表 | GET | `/api/admin/points` | 用户积分 |
| 积分流水 | GET | `/api/admin/points/logs` | 流水 |
| **邀请** | | | |
| 我的邀请 | GET | `/api/user/invites` | 邀请码+人数 |
| 邀请关系 | GET | `/api/admin/invites` | 管理端列表 |
| 奖励记录 | GET | `/api/admin/invites/rewards` | 管理端列表 |
| **任务(C端)** | | | |
| 任务列表 | GET | `/api/user/tasks` | 上架任务 |
| 任务详情 | GET | `/api/user/tasks/{id}` | 详情 |
| 领取任务 | POST | `/api/user/tasks/{id}/claim` | 领取 |
| 我的订单 | GET | `/api/user/tasks/orders` | 订单列表 |
| 提交订单 | PUT | `/api/user/tasks/orders/{id}/submit` | 提交截图+订单号 |
| 领取奖励 | POST | `/api/user/tasks/orders/{id}/withdraw` | 提现 |
| 打款日志 | GET | `/api/user/tasks/pay-logs` | 我打款日志 |
| **任务(管理端)** | | | |
| 任务列表 | GET | `/api/admin/tasks` | 分页+搜索 |
| 创建任务 | POST | `/api/admin/tasks` | 发布 |
| 更新任务 | PUT | `/api/admin/tasks/{id}` | 编辑 |
| 删除任务 | DELETE | `/api/admin/tasks/{id}` | 删除 |
| 审核订单 | GET | `/api/admin/tasks/orders` | 订单列表 |
| 审核操作 | PUT | `/api/admin/tasks/orders/{id}/audit` | 通过/驳回 |
| 授权打款 | PUT | `/api/admin/tasks/orders/{id}/grant` | 授权 |
| 打款日志 | GET | `/api/admin/tasks/pay-logs` | 日志 |
| **会员(C端)** | | | |
| 套餐列表 | GET | `/api/user/member/packages` | 可用套餐 |
| 会员状态 | GET | `/api/user/member/info` | 我的会员 |
| 购买套餐 | POST | `/api/user/member/buy/{id}` | 发起支付 |
| 支付回调 | POST | `/api/user/member/pay-callback` | 确认开通 |
| **会员(管理端)** | | | |
| 套餐列表 | GET | `/api/admin/member/packages` | 套餐 |
| 创建套餐 | POST | `/api/admin/member/packages` | 新增 |
| 更新套餐 | PUT | `/api/admin/member/packages/{id}` | 编辑 |
| 删除套餐 | DELETE | `/api/admin/member/packages/{id}` | 删除 |
| 用户会员 | GET | `/api/admin/member/users` | 列表+搜索 |
| **用户管理** | | | |
| 用户列表 | GET | `/api/admin/users` | 分页+搜索 |
| 用户详情 | GET | `/api/admin/users/{id}` | 详情 |
| 更新用户 | PUT | `/api/admin/users/{id}` | 编辑 |
| 删除用户 | DELETE | `/api/admin/users/{id}` | 逻辑删除 |
| **管理员管理** | | | |
| 管理员列表 | GET | `/api/admin/admins` | 分页 |
| 创建 | POST | `/api/admin/admins` | 新增 |
| 更新 | PUT | `/api/admin/admins/{id}` | 编辑 |
| 删除 | DELETE | `/api/admin/admins/{id}` | 删除 |
| **系统配置** | | | |
| 配置列表 | GET | `/api/admin/configs` | sys_config |
| 创建 | POST | `/api/admin/configs` | 新增 |
| 更新 | PUT | `/api/admin/configs/{id}` | 编辑 |
| 删除 | DELETE | `/api/admin/configs/{id}` | 删除 |
| **微信回调** | | | |
| 支付回调 | POST | `/api/public/wechat-pay/callback` | 微信通知 |

## 部署说明

### 环境要求

| 依赖 | 版本 |
|------|------|
| JDK | 21+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Nginx | 可选 |

### 配置

`backend/src/main/resources/application-dev.yml` 需要配置：

```yaml
spring.datasource     # 数据库连接
cos                   # 腾讯云 COS（头像/图片上传）
wechat.oauth          # 微信登录（AppID/AppSecret/回调URL）
wechat.pay            # 微信支付（商户号/APIv3密钥/证书/回调URL）
jwt.secret            # 生产环境务必修改为强随机字符串
```

### 构建部署

```bash
# 后端打包
cd backend && mvn clean package -DskipTests
java -jar target/neoapp-backend-*.jar --spring.profiles.active=prod

# 前端构建
cd admin && npm run build    # 产出 dist/
cd web && npm run build      # 产出 dist/
```

### Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / { root /path/to/web/dist/; try_files $uri /index.html; }
    location /api/ { proxy_pass http://127.0.0.1:8080; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_addr; }
}
```

> ⚠️ **生产环境**务必修改 `jwt.secret` 为强随机字符串，关闭 MyBatis SQL 日志。

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
| `task_info` | 任务配置（标题/封面/富文本/奖励/24h数量） |
| `task_user_order` | 用户订单（领取→提交→审核→提现） |
| `task_pay_log` | 微信打款日志 |

### 会员模块
| 表名 | 说明 |
|------|------|
| `member_package` | 会员套餐（名称/价格/天数/赠积分） |
| `member_user` | 用户会员（是否会员/到期时间） |

### 系统配置
| 表名 | 说明 |
|------|------|
| `sys_config` | 系统配置（key-value） |

## 系统配置项

| 键 | 说明 | 默认值 |
|------|------|------|
| `point_register` | 注册送积分 | 10 |
| `point_invite` | 邀请送积分 | 10 |
| `task_ip_days` | 任务IP限制天数 | 7 |

> 完整建表语句见 `backend/src/main/resources/db/schema.sql`
