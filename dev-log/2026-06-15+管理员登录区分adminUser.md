# 管理员登录修复 — 区分 admin_user 和 user_info 认证

## 问题
管理员登录调用了 C 端用户登录接口 `POST /api/auth/login`，该接口查询 `user_info` 表（手机号登录），而非 `admin_user` 表（用户名+BCrypt密码）。

## 修复内容

### 1. JWT 增加类型标识
`JwtUtil.java`
- `generateToken()` 增加 `type: "user"` claim
- 新增 `generateAdminToken(Long adminId, String username)` → `type: "admin"` + `username` claim

### 2. 管理员专用登录接口
`UserAuthController.java` 新增 `POST /api/auth/admin/login`
- 接收 `{ username, password }` → AdminLoginRequest
- 查询 `admin_user` 表（按 username）
- BCrypt 密码校验（`passwordEncoder.matches()`）
- 禁用账号返回 403，密码错误返回 400
- 签发 type=admin 的 JWT

### 3. 管理员登录页面改回用户名
`admin/src/views/Login.vue`
- 表单 `phone` → `username`
- 图标 `Phone` → `User`
- 请求 `POST /api/auth/login` → `POST /api/auth/admin/login`

### 4. DTO 新增
`AdminLoginRequest.java` — `{ username, password }`

### 5. schema.sql 密码哈希修复
由于重建库时 BCrypt 种子不同，更新为已验证正确的哈希值。

## 认证流程对比

| | C 端用户 | 管理后台 |
|------|------|------|
| 登录接口 | `POST /api/auth/login` | `POST /api/auth/admin/login` |
| 认证表 | `user_info` | `admin_user` |
| 凭据 | phone + (password) | username + password(BCrypt) |
| JWT type | `user` | `admin` |

## 测试验证
- ✅ admin/admin123 正确登录 → JWT 签发
- ✅ 错误密码 → 400 "用户名或密码错误"
- ✅ 禁用账号 → 403 "账号已被禁用"
- ✅ Admin JWT 可访问 `/api/admin/*` 接口
