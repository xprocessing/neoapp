---
name: update-readme-api-docs
overview: 创建/更新项目根目录 README.md，包含项目介绍、模块结构、启动说明、以及完整的 API 接口文档（8个接口，含请求/响应格式和 DTO 说明）。
todos:
  - id: create-readme
    content: 在项目根目录新建 README.md，写入项目简介、技术栈、统一响应格式和完整的 8 个 API 接口文档
    status: completed
  - id: write-devlog
    content: 将 README.md 创建记录写入 dev-log 文件夹
    status: completed
    dependencies:
      - create-readme
---

## 用户需求

将 user 模块开发完成后，把 8 个 API 接口文档更新到项目根目录的 README.md 文件中。

## 核心内容

- 项目简介（项目名称、技术栈概览、多端说明）
- 统一响应格式说明（Result&lt;T&gt; 的结构：code、msg、data）
- 健康检查接口文档
- 用户认证接口文档（注册、登录）
- 用户信息接口文档（获取当前用户信息）
- 管理端用户管理接口文档（分页查询、详情、更新、逻辑删除）
- 每个接口包含：HTTP 方法、URL 路径、认证要求、请求参数表格、响应示例 JSON

## 文件状态

- 项目根目录当前不存在 README.md，需新建
- API 信息已全部从 Controller 代码中提取确认

## 实现方式

纯文档编写任务，不需要代码实现。直接在项目根目录 `/Users/neo/neocode/neoapp/README.md` 新建文件，写入格式化 Markdown 文档。

## 文档结构

```
README.md
├── 项目简介 (project overview, multi-platform)
├── 技术栈 (Spring Boot 3, MyBatis-Plus, Vue 3, TDesign, etc.)
├── 统一响应格式 (Result<T> schema)
├── 接口文档
│   ├── 健康检查
│   ├── 用户认证 (注册/登录)
│   ├── 用户信息
│   └── 管理端用户管理 (CRUD)
├── 数据库表结构 (user_info, user_wechat, user_address)
└── 项目启动说明
```

## 实现细节

### API 接口汇总（8个端点）

| 模块 | 方法 | 路径 | 认证 | 说明 |
| --- | --- | --- | --- | --- |
| 健康检查 | GET | /api/health | 否 | 服务健康状态 |
| 用户认证 | POST | /api/auth/register | 否 | 用户注册 |
| 用户认证 | POST | /api/auth/login | 否 | 用户登录，返回JWT |
| 用户信息 | GET | /api/user/info | 是(Bearer Token) | 获取当前用户信息 |
| 用户管理 | GET | /api/admin/users | 是 | 分页查询用户列表 |
| 用户管理 | GET | /api/admin/users/{id} | 是 | 用户详情 |
| 用户管理 | PUT | /api/admin/users/{id} | 是 | 更新用户信息 |
| 用户管理 | DELETE | /api/admin/users/{id} | 是 | 逻辑删除用户 |


### 统一响应格式

所有接口返回 `Result<T>` 结构：`{ "code": 200, "msg": "成功", "data": ... }`