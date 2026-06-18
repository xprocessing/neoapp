-- =====================================================
-- NeoApp 初始建表语句
-- =====================================================

-- =====================================================
-- 管理后台系统表 (admin 前缀，用于后台管理系统认证/授权)
-- =====================================================

-- 后台管理员表
CREATE TABLE IF NOT EXISTS `admin_user` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`        VARCHAR(64)   NOT NULL                COMMENT '用户名',
    `password`        VARCHAR(128)  NOT NULL                COMMENT '密码(BCrypt加密)',
    `nickname`        VARCHAR(64)   DEFAULT NULL            COMMENT '昵称',
    `email`           VARCHAR(128)  DEFAULT NULL            COMMENT '邮箱',
    `phone`           VARCHAR(20)   DEFAULT NULL            COMMENT '手机号',
    `avatar`          VARCHAR(256)  DEFAULT NULL            COMMENT '头像URL',
    `gender`          TINYINT       DEFAULT 0               COMMENT '性别: 0-未知, 1-男, 2-女',
    `status`          TINYINT       DEFAULT 1               COMMENT '状态: 0-禁用, 1-启用',
    `last_login_time` DATETIME      DEFAULT NULL            COMMENT '最后登录时间',
    `last_login_ip`   VARCHAR(64)   DEFAULT NULL            COMMENT '最后登录IP',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员表';

-- 后台角色表
CREATE TABLE IF NOT EXISTS `admin_role` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name`     VARCHAR(64)   NOT NULL                COMMENT '角色名称',
    `role_code`     VARCHAR(64)   NOT NULL                COMMENT '角色编码',
    `description`   VARCHAR(256)  DEFAULT NULL            COMMENT '角色描述',
    `status`        TINYINT       DEFAULT 1               COMMENT '状态: 0-禁用, 1-启用',
    `sort_order`    INT           DEFAULT 0               COMMENT '排序',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台角色表';

-- 后台管理员角色关联表
CREATE TABLE IF NOT EXISTS `admin_user_role` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT        NOT NULL                COMMENT '管理员ID(关联admin_user)',
    `role_id`       BIGINT        NOT NULL                COMMENT '角色ID(关联admin_role)',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员角色关联表';

-- 插入默认管理员 (密码: admin123, BCrypt加密)
INSERT INTO `admin_user` (`username`, `password`, `nickname`, `status`)
VALUES ('admin', '$2b$10$k.YWnGF12dms9H5zEz.SSub8TxaKTX2Ss.zJFyiNZ9x/7VzZUgOUe', '超级管理员', 1)
ON DUPLICATE KEY UPDATE `username`=`username`;

-- 插入默认角色
INSERT INTO `admin_role` (`role_name`, `role_code`, `description`)
VALUES ('超级管理员', 'ROLE_ADMIN', '系统超级管理员')
ON DUPLICATE KEY UPDATE `role_code`=`role_code`;

-- =====================================================
-- 业务用户模块表 (user_ 前缀，面向 C 端用户)
-- =====================================================

-- 用户主表
CREATE TABLE IF NOT EXISTS `user_info` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `nickname`    VARCHAR(50)  DEFAULT ''                COMMENT '用户昵称',
    `avatar`      VARCHAR(255) DEFAULT ''                COMMENT '用户头像',
    `phone`       VARCHAR(20)  DEFAULT ''                COMMENT '手机号',
    `sex`         TINYINT      DEFAULT 0                 COMMENT '性别：0未知 1男 2女',
    `status`      TINYINT      NOT NULL DEFAULT 1        COMMENT '状态：0禁用 1正常',
    `invite_code` VARCHAR(32)  DEFAULT ''                COMMENT '个人邀请码',
    `parent_id`   BIGINT       DEFAULT 0                 COMMENT '上级邀请用户ID',
    `deleted`     TINYINT      NOT NULL DEFAULT 0        COMMENT '逻辑删除',
    `last_login_time` DATETIME  DEFAULT NULL            COMMENT '最后登录时间',
    `last_login_ip`   VARCHAR(64) DEFAULT NULL          COMMENT '最后登录IP',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_invite_code` (`invite_code`),
    KEY `idx_phone` (`phone`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户主表';

-- 用户微信绑定表
CREATE TABLE IF NOT EXISTS `user_wechat` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户ID',
    `openid`      VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '微信openid',
    `unionid`     VARCHAR(100) DEFAULT ''              COMMENT '微信unionid',
    `app_type`    VARCHAR(20)  DEFAULT ''              COMMENT '应用类型',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户微信绑定表';

-- 用户收货地址表
CREATE TABLE IF NOT EXISTS `user_address` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户ID',
    `name`        VARCHAR(50)  NOT NULL DEFAULT ''     COMMENT '收货人姓名',
    `phone`       VARCHAR(20)  NOT NULL DEFAULT ''     COMMENT '收货人电话',
    `province`    VARCHAR(50)  DEFAULT ''              COMMENT '省份',
    `city`        VARCHAR(50)  DEFAULT ''              COMMENT '城市',
    `district`    VARCHAR(50)  DEFAULT ''              COMMENT '区县',
    `detail`      VARCHAR(255) DEFAULT ''              COMMENT '详细地址',
    `is_default`  TINYINT      NOT NULL DEFAULT 0      COMMENT '是否默认：0否 1是',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';

-- =====================================================
-- 积分模块表
-- =====================================================

CREATE TABLE IF NOT EXISTS `point_user` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `total_point` INT      NOT NULL DEFAULT 0      COMMENT '累计积分',
    `usable_point` INT     NOT NULL DEFAULT 0      COMMENT '可用积分',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户积分表';

CREATE TABLE IF NOT EXISTS `point_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户ID',
    `point`       INT          NOT NULL DEFAULT 0      COMMENT '变动积分',
    `balance`     INT          NOT NULL DEFAULT 0      COMMENT '变动后可用积分',
    `type`        TINYINT      NOT NULL DEFAULT 1      COMMENT '类型：1获取 2消耗',
    `source`      VARCHAR(50)  DEFAULT ''              COMMENT '来源',
    `remark`      VARCHAR(255) DEFAULT ''              COMMENT '变动备注',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_create` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水记录表';

-- =====================================================
-- 会员模块表
-- =====================================================

CREATE TABLE IF NOT EXISTS `member_package` (
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
    `package_name` VARCHAR(100)   NOT NULL DEFAULT ''     COMMENT '套餐名称',
    `price`        DECIMAL(10,2)  NOT NULL DEFAULT 0.00   COMMENT '套餐价格',
    `day_num`      INT            NOT NULL DEFAULT 0      COMMENT '有效天数',
    `give_point`   INT            NOT NULL DEFAULT 0      COMMENT '赠送积分',
    `sort`         INT            NOT NULL DEFAULT 0      COMMENT '排序',
    `status`       TINYINT        NOT NULL DEFAULT 1      COMMENT '状态：0下架 1上架',
    `deleted`      TINYINT        NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    `create_time`  DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员套餐表';

CREATE TABLE IF NOT EXISTS `member_user` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `is_member`   TINYINT  NOT NULL DEFAULT 0      COMMENT '是否会员：0否 1是',
    `expire_time` DATETIME DEFAULT NULL            COMMENT '会员过期时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开通时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会员表';

-- =====================================================
-- 邀请模块表
-- =====================================================

CREATE TABLE IF NOT EXISTS `invite_user` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `parent_id`   BIGINT   NOT NULL DEFAULT 0      COMMENT '上级用户ID',
    `invite_num`  INT      NOT NULL DEFAULT 0      COMMENT '累计邀请人数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户邀请关系表';

CREATE TABLE IF NOT EXISTS `invite_reward_log` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL                COMMENT '获奖用户ID',
    `invite_user_id`  BIGINT       NOT NULL                COMMENT '被邀请用户ID',
    `reward_type`     TINYINT      NOT NULL DEFAULT 1      COMMENT '奖励类型：1积分',
    `reward_num`      INT          NOT NULL DEFAULT 0      COMMENT '奖励数量',
    `remark`          VARCHAR(255) DEFAULT ''              COMMENT '奖励备注',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请奖励记录表';

-- =====================================================
-- 系统配置表
-- =====================================================

CREATE TABLE IF NOT EXISTS `sys_config` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key`  VARCHAR(50)  NOT NULL DEFAULT ''       COMMENT '配置键名',
    `config_name` VARCHAR(100) NOT NULL DEFAULT ''       COMMENT '配置名称',
    `config_value` VARCHAR(500) DEFAULT ''               COMMENT '配置值',
    `remark`      VARCHAR(255) DEFAULT ''               COMMENT '备注说明',
    `status`      TINYINT      NOT NULL DEFAULT 1        COMMENT '状态：0禁用 1启用',
    `deleted`     TINYINT      NOT NULL DEFAULT 0        COMMENT '逻辑删除：0正常 1删除',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统全局配置表';

-- =====================================================
-- 任务模块表
-- =====================================================

CREATE TABLE IF NOT EXISTS `task_info` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务主键ID',
    `task_title`     VARCHAR(200) NOT NULL DEFAULT ''   COMMENT '任务标题',
    `task_cover`     VARCHAR(255) DEFAULT ''            COMMENT '任务封面图',
    `task_intro`     VARCHAR(500) DEFAULT ''            COMMENT '任务简介',
    `keyword`        VARCHAR(200) DEFAULT ''            COMMENT '搜索关键词',
    `task_content`   TEXT                                COMMENT '任务操作步骤详情',
    `reward_amount`  DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '奖励金额',
    `gift`           VARCHAR(255) DEFAULT ''            COMMENT '赠品',
    `total_num`      INT          NOT NULL DEFAULT 0    COMMENT '总数量(24小时之和)',
    `hour_limits`    VARCHAR(200) DEFAULT ''            COMMENT '24小时每小时数量,逗号分隔',
    `expire_minute`  INT          NOT NULL DEFAULT 60   COMMENT '有效时长(分钟)',
    `sort`           INT          NOT NULL DEFAULT 0    COMMENT '排序',
    `status`         TINYINT      NOT NULL DEFAULT 0    COMMENT '0下架1上架',
    `deleted`        TINYINT      NOT NULL DEFAULT 0    COMMENT '逻辑删除',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_status_sort` (`status`, `sort`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务配置主表';

CREATE TABLE IF NOT EXISTS `task_user_order` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `user_id`         BIGINT       NOT NULL                COMMENT '用户ID',
    `task_id`         BIGINT       NOT NULL                COMMENT '任务ID',
    `openid`          VARCHAR(100) DEFAULT ''              COMMENT '微信openid',
    `reward_amount`   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '奖励金额',
    `gift`            VARCHAR(255) DEFAULT ''              COMMENT '赠品',
    `audit_status`    TINYINT      NOT NULL DEFAULT 1      COMMENT '1待提交2待审核3通过4驳回5过期',
    `grant_pay`       TINYINT      NOT NULL DEFAULT 0      COMMENT '授权打款0未授权1已授权',
    `withdraw_status` TINYINT      NOT NULL DEFAULT 0      COMMENT '0未提现1处理中2成功3失败',
    `expire_time`     DATETIME     NULL                    COMMENT '过期时间',
    `submit_images`   VARCHAR(1000) DEFAULT ''             COMMENT '截图',
    `submit_note`     VARCHAR(500) DEFAULT ''              COMMENT '备注/订单号',
    `audit_note`      VARCHAR(500) DEFAULT ''              COMMENT '审核备注',
    `audit_time`      DATETIME     NULL                    COMMENT '审核时间',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_task` (`user_id`, `task_id`),
    INDEX `idx_audit_status` (`audit_status`),
    INDEX `idx_user_create` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户任务订单表';

CREATE TABLE IF NOT EXISTS `task_pay_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `order_id`      BIGINT       NOT NULL                COMMENT '订单ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '用户ID',
    `task_title`    VARCHAR(200) DEFAULT ''              COMMENT '任务名称',
    `trade_no`      VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '交易单号',
    `wechat_pay_no` VARCHAR(64)  DEFAULT ''              COMMENT '微信单号',
    `pay_amount`    DECIMAL(10,2) NOT NULL DEFAULT 0.00  COMMENT '打款金额',
    `pay_status`    TINYINT      NOT NULL DEFAULT 1      COMMENT '1处理中2成功3失败',
    `fail_reason`   VARCHAR(500) DEFAULT ''              COMMENT '失败原因',
    `retry_count`   INT          NOT NULL DEFAULT 0      COMMENT '重试次数',
    `apply_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `pay_time`      DATETIME     NULL                    COMMENT '打款时间',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trade_no` (`trade_no`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_pay_status` (`pay_status`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务打款日志表';
