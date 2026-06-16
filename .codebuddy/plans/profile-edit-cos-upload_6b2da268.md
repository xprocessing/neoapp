---
name: profile-edit-cos-upload
overview: 为 /profile 页面添加编辑资料和头像上传功能，头像使用腾讯云 COS 存储。涉及后端 COS SDK 集成、上传接口、用户资料更新接口，以及前端 Profile 页面改造。
design:
  architecture:
    framework: vue
    component: tdesign
  styleKeywords:
    - 极简卡片
    - 双模式切换
    - 微动效
  fontSystem:
    fontFamily: PingFang SC
    heading:
      size: 24px
      weight: 600
    subheading:
      size: 16px
      weight: 500
    body:
      size: 14px
      weight: 400
  colorSystem:
    primary:
      - "#0052D9"
      - "#618DFF"
    background:
      - "#FFFFFF"
      - "#F5F7FA"
    text:
      - "#1D2129"
      - "#4E5969"
      - "#86909C"
    functional:
      - "#00A870"
      - "#E34D59"
todos:
  - id: cos-deps-config
    content: 后端添加 COS SDK 依赖和配置：pom.xml 添加 cos_api，application-dev.yml 添加 COS 参数，创建 CosProperties 配置类
    status: completed
  - id: cos-service
    content: 创建 CosService 服务类，封装 COSClient 初始化和文件上传方法，支持 MultipartFile 流式上传并返回 URL
    status: completed
    dependencies:
      - cos-deps-config
  - id: user-profile-api
    content: UserController 新增 PUT /api/user/profile 自更新端点和 POST /api/user/upload/avatar 头像上传端点，UserService 新增 updateProfile 方法
    status: completed
    dependencies:
      - cos-service
  - id: profile-vue-rewrite
    content: 重写 Profile.vue：实现查看/编辑双模式，集成 t-form 表单编辑昵称性别，集成头像上传功能
    status: completed
    dependencies:
      - user-profile-api
  - id: dev-log
    content: 写入 dev-log 记录本次修改内容
    status: completed
    dependencies:
      - profile-vue-rewrite
---

## 用户需求

个人中心页面 `/profile` 支持用户自助编辑资料和上传头像。

## 核心功能

- 个人信息编辑：支持修改昵称、性别
- 头像上传：点击头像选择图片，上传至腾讯云 COS，即时预览
- 编辑/查看模式切换：默认查看模式，点击编辑按钮进入可编辑状态
- 图片存储：所有用户上传的头像存储于腾讯云对象存储 COS

## COS 配置

- SECRET_ID: AKID0tabQI5kLlyMfrtiKky1Z9eyIWmy0Ie7
- SECRET_KEY: Ws59JUXP5bwELH1oUzJRQHxRmxxnRxIR
- REGION: ap-guangzhou
- BUCKET: neoimage-1251198067

## 技术栈

- **后端**：Spring Boot 3.4.1 + Java 21 + MyBatis-Plus 3.5.7 + 腾讯云 COS Java SDK
- **前端**：Vue 3 + TDesign Vue Next 1.20.1 + Tailwind CSS + Lucide Icons
- **存储**：腾讯云 COS（对象存储）

## 实现方案

### 上传架构：后端中转模式

前端选择文件 → 通过 `POST /api/user/upload/avatar` (multipart/form-data) 发送到后端 → 后端 CosService 将文件流上传至 COS → 返回 COS 访问 URL → 前端更新头像预览并保存

选择后端中转而非前端直传的原因：

1. 避免 SecretKey 暴露在前端代码中
2. 后端统一控制文件大小、格式校验
3. 实现简单，前端无需集成 COS JS SDK

### 后端新增

#### COS SDK 集成

- Maven 依赖：`com.qcloud:cos_api:5.6.227`
- 配置类：`CosProperties` 读取 yml 中的 COS 参数
- 服务类：`CosService` 封装 `COSClient`，提供 `upload(MultipartFile)` 方法返回 URL

#### 新增接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| PUT | `/api/user/profile` | 用户自助更新资料（昵称、性别） |
| POST | `/api/user/upload/avatar` | 上传头像图片（multipart） |


两个端点均在 SecurityConfig 中 `/api/user/**` 路径已被设为 `authenticated`，无需额外配置。

#### 头像上传流程

1. 接收 MultipartFile，校验文件类型（仅限 image/jpeg, image/png, image/gif, image/webp）和大小（最大 5MB）
2. 生成 COS 对象 Key：`avatar/{userId}/{uuid}.{ext}`
3. 调用 COSClient.putObject 上传到 bucket
4. 返回完整 URL：`https://{bucket}.cos.{region}.myqcloud.com/{key}`
5. 前端拿到 URL 后调用 PUT /api/user/profile 保存 avatar 到数据库

### 前端改造

#### Profile.vue 重构

- **查看模式**：保留现有用户信息卡片展示
- **编辑模式**：点击编辑按钮切换，展示 t-form 表单
- 头像区域：点击触发文件选择（隐藏的 input[type=file]），选择后即时调用上传接口，显示 loading 状态，上传成功后更新预览
- 昵称：t-input 文本输入
- 性别：t-radio-group 单选（男/女/未知）
- 保存/取消按钮

### 性能与可靠性

- COS 文件上传设置 15 秒超时（与前端的 request.js timeout 一致）
- 上传失败展示错误提示，不阻塞表单编辑
- 编辑时先上传头像获得 URL，再整体保存资料，原子性由前端保证
- 若头像已更新但资料保存失败，头像已存在于 COS（可接受，下次覆盖）

### 日志

- 后端上传成功/失败记录 debug/error 级别日志（禁止打印 SecretKey）
- 复用项目已有的 logback 配置，遵循现有日志模式

## 设计风格

沿用现有 Profile 页面的极简卡片风格，在保持视觉一致性的前提下增强交互体验。

## 页面设计（单页，查看/编辑双模式）

### 查看模式（默认）

继承现有设计：渐变头部区域展示头像+昵称+状态标签，下方详情列表展示手机号、性别、邀请码、注册时间，底部退出登录按钮。新增悬浮在头像上的"编辑"图标引导进入编辑模式，卡片右上角新增编辑按钮。

### 编辑模式

保留卡片整体结构，头部区域的头像增加上传交互：鼠标悬停时显示半透明遮罩+相机图标，点击触发文件选择。头像下方以表单形式展示可编辑字段：

- 昵称：t-input 组件，带字数限制提示
- 性别：t-radio-group 组件，选项为「未知」「男」「女」
- 底部操作区：保存（t-button primary）和取消（t-button outline）

### 交互细节

- 头像上传中显示 t-loading 覆盖层
- 表单校验：昵称不为空、不超过 50 字符
- 保存成功显示 MessagePlugin.success 并切回查看模式
- 取消编辑不保存任何更改

### 响应式

页面宽度 max-w-[640px] mx-auto，与现有 Profile 保持一致，移动端自适应良好。

## SubAgent

- **code-explorer**
- Purpose: 验证现有代码路径和文件结构，确保新增文件与现有架构一致
- Expected outcome: 确认所有目标文件路径存在，验证 Service/Controller 模式一致性