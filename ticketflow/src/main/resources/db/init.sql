-- ============================================
-- TicketFlow 数据库初始化脚本
-- ============================================
CREATE DATABASE IF NOT EXISTS ticketflow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ticketflow;

-- ============================================
-- 1. 系统用户表
-- ============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ============================================
-- 2. 系统角色表
-- ============================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code_tenant` (`role_code`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- ============================================
-- 3. 系统权限表
-- ============================================
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `permission_type` TINYINT DEFAULT 1 COMMENT '权限类型: 1-菜单 2-按钮 3-接口',
    `url` VARCHAR(200) DEFAULT NULL COMMENT '请求URL',
    `method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法: GET/POST/PUT/DELETE',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '菜单图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code_tenant` (`permission_code`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- ============================================
-- 4. 用户角色关联表
-- ============================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================
-- 5. 角色权限关联表
-- ============================================
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================
-- 6. 商家表
-- ============================================
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `merchant_name` VARCHAR(100) NOT NULL COMMENT '商家名称',
    `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `description` TEXT DEFAULT NULL COMMENT '商家描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- ============================================
-- 7. 活动/演出表
-- ============================================
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `event_name` VARCHAR(200) NOT NULL COMMENT '活动名称',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `venue` VARCHAR(200) NOT NULL COMMENT '演出场馆',
    `description` TEXT DEFAULT NULL COMMENT '活动描述',
    `poster_url` VARCHAR(500) DEFAULT NULL COMMENT '海报URL',
    `start_time` DATETIME NOT NULL COMMENT '演出开始时间',
    `end_time` DATETIME NOT NULL COMMENT '演出结束时间',
    `on_sale_time` DATETIME NOT NULL COMMENT '开售时间',
    `total_seats` INT DEFAULT 0 COMMENT '总座位数',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-未开售 1-热卖中 2-已售罄 3-已结束 4-已取消',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动/演出表';

-- ============================================
-- 8. 座位表
-- ============================================
DROP TABLE IF EXISTS `seat`;
CREATE TABLE `seat` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `event_id` BIGINT NOT NULL COMMENT '活动ID',
    `section_name` VARCHAR(50) NOT NULL COMMENT '区域名称 (如: A区/VIP/看台)',
    `row_num` VARCHAR(10) NOT NULL COMMENT '排号',
    `seat_num` VARCHAR(10) NOT NULL COMMENT '座位号',
    `price` DECIMAL(10,2) NOT NULL COMMENT '票价',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-可售 1-已锁定 2-已售出',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_event_id` (`event_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位表';

-- ============================================
-- 9. 订单表
-- ============================================
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `event_id` BIGINT NOT NULL COMMENT '活动ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `seat_count` INT DEFAULT 1 COMMENT '座位数量',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待支付 1-已支付 2-已取消 3-已退款',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_event_id` (`event_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================
-- 10. 订单明细表
-- ============================================
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `seat_id` BIGINT NOT NULL COMMENT '座位ID',
    `price` DECIMAL(10,2) NOT NULL COMMENT '购买时票价',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ============================================
-- 11. 票务表
-- ============================================
DROP TABLE IF EXISTS `ticket`;
CREATE TABLE `ticket` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `ticket_no` VARCHAR(32) NOT NULL COMMENT '票号',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `event_id` BIGINT NOT NULL COMMENT '活动ID',
    `seat_id` BIGINT NOT NULL COMMENT '座位ID',
    `qr_code` VARCHAR(500) DEFAULT NULL COMMENT '二维码',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-未使用 1-已使用 2-已退款',
    `check_time` DATETIME DEFAULT NULL COMMENT '检票时间',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_no` (`ticket_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_event_id` (`event_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票务表';

-- ============================================
-- 12. 字典类型表
-- ============================================
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
    `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type_tenant` (`dict_type`, `tenant_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- ============================================
-- 13. 字典数据表
-- ============================================
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `dict_type_id` BIGINT NOT NULL COMMENT '字典类型ID',
    `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签',
    `dict_value` VARCHAR(100) NOT NULL COMMENT '字典值',
    `css_class` VARCHAR(100) DEFAULT NULL COMMENT '样式属性',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_dict_type_id` (`dict_type_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- ============================================
-- 测试数据
-- ============================================

-- 管理员用户 (密码: admin123, BCrypt加密)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `phone`, `status`, `tenant_id`) VALUES
(1, 'admin', '$2a$10$KubRrykRMW9PkJK/tsByFuydnLvJOPbKoutSMBysIRnE1Yoieaoj6', '系统管理员', 'admin@ticketflow.com', '13800000001', 1, 1),
(2, 'merchant1', '$2a$10$xO2pS5elw4m9Vs16EZMycuT0RX0kC.SpVahe1.cISFF.2iyuSJckq', '商家用户1', 'merchant1@ticketflow.com', '13800000002', 1, 1);

-- 角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `sort`, `status`, `tenant_id`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1, 1, 1),
(2, '商家', 'MERCHANT', '商家管理权限', 2, 1, 1),
(3, '普通用户', 'USER', '普通用户权限', 3, 1, 1);

-- 权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `url`, `method`, `sort`, `status`, `tenant_id`) VALUES
(1, 0, '用户管理', 'user:manage', 1, '/api/user/**', NULL, 1, 1, 1),
(2, 1, '用户列表', 'user:list', 3, '/api/user/list', 'GET', 1, 1, 1),
(3, 1, '新增用户', 'user:add', 3, '/api/user', 'POST', 2, 1, 1),
(4, 0, '角色管理', 'role:manage', 1, '/api/role/**', NULL, 2, 1, 1),
(5, 0, '活动管理', 'event:manage', 1, '/api/event/**', NULL, 3, 1, 1),
(6, 0, '订单管理', 'order:manage', 1, '/api/order/**', NULL, 4, 1, 1),
(7, 0, '秒杀抢票', 'order:seckill', 3, '/api/order/seckill/**', 'POST', 5, 1, 1),
(8, 0, '座位管理', 'seat:manage', 1, '/api/seat/**', NULL, 6, 1, 1),
(9, 0, '票务管理', 'ticket:manage', 1, '/api/ticket/**', NULL, 7, 1, 1),
(10, 0, '商家管理', 'merchant:manage', 1, '/api/merchant/**', NULL, 8, 1, 1),
(11, 0, '统计查询', 'statistics:view', 1, '/api/statistics/**', 'GET', 9, 1, 1);

-- 用户-角色关联
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES
(1, 1, 1),
(2, 2, 2);

-- 角色-权限关联 (超级管理员拥有所有权限)
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`) VALUES
(1, 1, 1), (2, 1, 2), (3, 1, 3), (4, 1, 4),
(5, 1, 5), (6, 1, 6), (7, 1, 7), (8, 1, 8),
(9, 1, 9), (10, 1, 10), (11, 1, 11),
(12, 2, 5), (13, 2, 6), (14, 2, 7), (15, 2, 8), (16, 2, 9),
(17, 3, 7);

-- 商家
INSERT INTO `merchant` (`id`, `merchant_name`, `contact_name`, `contact_phone`, `contact_email`, `address`, `description`, `status`, `tenant_id`) VALUES
(1, '星光演艺公司', '张三', '13800000003', 'zhangsan@starlight.com', '北京市朝阳区文化大厦10层', '专业演出策划公司', 1, 1);

-- 活动 (开售时间设为当前时间之前，方便测试)
INSERT INTO `event` (`id`, `event_name`, `merchant_id`, `venue`, `description`, `start_time`, `end_time`, `on_sale_time`, `total_seats`, `status`, `tenant_id`) VALUES
(1, '周杰伦2026世界巡回演唱会-北京站', 1, '国家体育场(鸟巢)', '华语乐坛天王周杰伦2026世界巡回演唱会北京站，经典歌曲一次听够！',
 '2026-08-15 19:30:00', '2026-08-15 22:30:00', '2026-06-01 10:00:00', 100, 1, 1);

-- 座位 (100个座位，4个区域)
-- A区 VIP 前排 (前20个座位，票价1280)
INSERT INTO `seat` (`id`, `event_id`, `section_name`, `row_num`, `seat_num`, `price`, `status`, `tenant_id`) VALUES
(1, 1, 'VIP区', '1', '1', 1280.00, 0, 1),
(2, 1, 'VIP区', '1', '2', 1280.00, 0, 1),
(3, 1, 'VIP区', '1', '3', 1280.00, 0, 1),
(4, 1, 'VIP区', '1', '4', 1280.00, 0, 1),
(5, 1, 'VIP区', '1', '5', 1280.00, 0, 1),
(6, 1, 'VIP区', '2', '1', 1280.00, 0, 1),
(7, 1, 'VIP区', '2', '2', 1280.00, 0, 1),
(8, 1, 'VIP区', '2', '3', 1280.00, 0, 1),
(9, 1, 'VIP区', '2', '4', 1280.00, 0, 1),
(10, 1, 'VIP区', '2', '5', 1280.00, 0, 1),
(11, 1, 'VIP区', '3', '1', 1280.00, 0, 1),
(12, 1, 'VIP区', '3', '2', 1280.00, 0, 1),
(13, 1, 'VIP区', '3', '3', 1280.00, 0, 1),
(14, 1, 'VIP区', '3', '4', 1280.00, 0, 1),
(15, 1, 'VIP区', '3', '5', 1280.00, 0, 1),
(16, 1, 'VIP区', '4', '1', 1280.00, 0, 1),
(17, 1, 'VIP区', '4', '2', 1280.00, 0, 1),
(18, 1, 'VIP区', '4', '3', 1280.00, 0, 1),
(19, 1, 'VIP区', '4', '4', 1280.00, 0, 1),
(20, 1, 'VIP区', '4', '5', 1280.00, 0, 1),
-- A区 (30个座位，票价880)
(21, 1, 'A区', '5', '1', 880.00, 0, 1),
(22, 1, 'A区', '5', '2', 880.00, 0, 1),
(23, 1, 'A区', '5', '3', 880.00, 0, 1),
(24, 1, 'A区', '5', '4', 880.00, 0, 1),
(25, 1, 'A区', '5', '5', 880.00, 0, 1),
(26, 1, 'A区', '6', '1', 880.00, 0, 1),
(27, 1, 'A区', '6', '2', 880.00, 0, 1),
(28, 1, 'A区', '6', '3', 880.00, 0, 1),
(29, 1, 'A区', '6', '4', 880.00, 0, 1),
(30, 1, 'A区', '6', '5', 880.00, 0, 1),
(31, 1, 'A区', '7', '1', 880.00, 0, 1),
(32, 1, 'A区', '7', '2', 880.00, 0, 1),
(33, 1, 'A区', '7', '3', 880.00, 0, 1),
(34, 1, 'A区', '7', '4', 880.00, 0, 1),
(35, 1, 'A区', '7', '5', 880.00, 0, 1),
(36, 1, 'A区', '8', '1', 880.00, 0, 1),
(37, 1, 'A区', '8', '2', 880.00, 0, 1),
(38, 1, 'A区', '8', '3', 880.00, 0, 1),
(39, 1, 'A区', '8', '4', 880.00, 0, 1),
(40, 1, 'A区', '8', '5', 880.00, 0, 1),
(41, 1, 'A区', '9', '1', 880.00, 0, 1),
(42, 1, 'A区', '9', '2', 880.00, 0, 1),
(43, 1, 'A区', '9', '3', 880.00, 0, 1),
(44, 1, 'A区', '9', '4', 880.00, 0, 1),
(45, 1, 'A区', '9', '5', 880.00, 0, 1),
(46, 1, 'A区', '10', '1', 880.00, 0, 1),
(47, 1, 'A区', '10', '2', 880.00, 0, 1),
(48, 1, 'A区', '10', '3', 880.00, 0, 1),
(49, 1, 'A区', '10', '4', 880.00, 0, 1),
(50, 1, 'A区', '10', '5', 880.00, 0, 1),
-- B区 (25个座位，票价580)
(51, 1, 'B区', '11', '1', 580.00, 0, 1),
(52, 1, 'B区', '11', '2', 580.00, 0, 1),
(53, 1, 'B区', '11', '3', 580.00, 0, 1),
(54, 1, 'B区', '11', '4', 580.00, 0, 1),
(55, 1, 'B区', '11', '5', 580.00, 0, 1),
(56, 1, 'B区', '12', '1', 580.00, 0, 1),
(57, 1, 'B区', '12', '2', 580.00, 0, 1),
(58, 1, 'B区', '12', '3', 580.00, 0, 1),
(59, 1, 'B区', '12', '4', 580.00, 0, 1),
(60, 1, 'B区', '12', '5', 580.00, 0, 1),
(61, 1, 'B区', '13', '1', 580.00, 0, 1),
(62, 1, 'B区', '13', '2', 580.00, 0, 1),
(63, 1, 'B区', '13', '3', 580.00, 0, 1),
(64, 1, 'B区', '13', '4', 580.00, 0, 1),
(65, 1, 'B区', '13', '5', 580.00, 0, 1),
(66, 1, 'B区', '14', '1', 580.00, 0, 1),
(67, 1, 'B区', '14', '2', 580.00, 0, 1),
(68, 1, 'B区', '14', '3', 580.00, 0, 1),
(69, 1, 'B区', '14', '4', 580.00, 0, 1),
(70, 1, 'B区', '14', '5', 580.00, 0, 1),
(71, 1, 'B区', '15', '1', 580.00, 0, 1),
(72, 1, 'B区', '15', '2', 580.00, 0, 1),
(73, 1, 'B区', '15', '3', 580.00, 0, 1),
(74, 1, 'B区', '15', '4', 580.00, 0, 1),
(75, 1, 'B区', '15', '5', 580.00, 0, 1),
-- C区 (25个座位，票价380)
(76, 1, 'C区', '16', '1', 380.00, 0, 1),
(77, 1, 'C区', '16', '2', 380.00, 0, 1),
(78, 1, 'C区', '16', '3', 380.00, 0, 1),
(79, 1, 'C区', '16', '4', 380.00, 0, 1),
(80, 1, 'C区', '16', '5', 380.00, 0, 1),
(81, 1, 'C区', '17', '1', 380.00, 0, 1),
(82, 1, 'C区', '17', '2', 380.00, 0, 1),
(83, 1, 'C区', '17', '3', 380.00, 0, 1),
(84, 1, 'C区', '17', '4', 380.00, 0, 1),
(85, 1, 'C区', '17', '5', 380.00, 0, 1),
(86, 1, 'C区', '18', '1', 380.00, 0, 1),
(87, 1, 'C区', '18', '2', 380.00, 0, 1),
(88, 1, 'C区', '18', '3', 380.00, 0, 1),
(89, 1, 'C区', '18', '4', 380.00, 0, 1),
(90, 1, 'C区', '18', '5', 380.00, 0, 1),
(91, 1, 'C区', '19', '1', 380.00, 0, 1),
(92, 1, 'C区', '19', '2', 380.00, 0, 1),
(93, 1, 'C区', '19', '3', 380.00, 0, 1),
(94, 1, 'C区', '19', '4', 380.00, 0, 1),
(95, 1, 'C区', '19', '5', 380.00, 0, 1),
(96, 1, 'C区', '20', '1', 380.00, 0, 1),
(97, 1, 'C区', '20', '2', 380.00, 0, 1),
(98, 1, 'C区', '20', '3', 380.00, 0, 1),
(99, 1, 'C区', '20', '4', 380.00, 0, 1),
(100, 1, 'C区', '20', '5', 380.00, 0, 1);

-- 字典类型
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `tenant_id`) VALUES
(1, '活动状态', 'event_status', 1, 1),
(2, '订单状态', 'order_status', 1, 1),
(3, '座位状态', 'seat_status', 1, 1);

-- 字典数据
INSERT INTO `sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `sort`, `status`, `tenant_id`) VALUES
(1, 1, '未开售', '0', 1, 1, 1),
(2, 1, '热卖中', '1', 2, 1, 1),
(3, 1, '已售罄', '2', 3, 1, 1),
(4, 1, '已结束', '3', 4, 1, 1),
(5, 2, '待支付', '0', 1, 1, 1),
(6, 2, '已支付', '1', 2, 1, 1),
(7, 2, '已取消', '2', 3, 1, 1),
(8, 3, '可售', '0', 1, 1, 1),
(9, 3, '已锁定', '1', 2, 1, 1),
(10, 3, '已售出', '2', 3, 1, 1);
