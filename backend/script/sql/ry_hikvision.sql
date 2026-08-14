-- --------------------------------------------------------
-- 海康 SDK 对接模块数据库脚本
-- 依赖：RuoYi-Vue-Plus 基础库 ry_vue.sql 已执行
-- --------------------------------------------------------

DROP TABLE IF EXISTS `sys_hik_device`;
CREATE TABLE `sys_hik_device` (
  `device_id`       bigint       NOT NULL COMMENT '设备主键',
  `device_name`     varchar(100) NOT NULL COMMENT '设备名称',
  `device_ip`       varchar(50)  NOT NULL COMMENT '设备IP',
  `port`            int          NOT NULL DEFAULT 8000 COMMENT '端口号',
  `username`        varchar(64)  NOT NULL DEFAULT 'admin' COMMENT '登录账号',
  `password`        varchar(128) NOT NULL DEFAULT '' COMMENT '登录密码',
  `device_type`     int          DEFAULT NULL COMMENT '设备类型',
  `manufacturer`    varchar(50)  DEFAULT 'hikvision' COMMENT '厂家',
  `serial_number`   varchar(64)  DEFAULT NULL COMMENT '序列号',
  `channel_num`     int          DEFAULT 0 COMMENT '模拟通道数',
  `ip_channel_num`  int          DEFAULT 0 COMMENT '数字通道数',
  `status`          varchar(20)  DEFAULT 'offline' COMMENT '在线状态 online/offline',
  `remark`          varchar(500) DEFAULT NULL COMMENT '备注',
  `create_dept`     bigint       DEFAULT NULL COMMENT '创建部门',
  `create_by`       bigint       DEFAULT NULL COMMENT '创建者',
  `create_time`     datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`       bigint       DEFAULT NULL COMMENT '更新者',
  `update_time`     datetime     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`device_id`),
  KEY `idx_device_ip` (`device_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='海康设备表';

DROP TABLE IF EXISTS `sys_hik_channel`;
CREATE TABLE `sys_hik_channel` (
  `channel_id`    bigint       NOT NULL COMMENT '通道主键',
  `device_id`     bigint       NOT NULL COMMENT '设备主键',
  `channel_no`    int          NOT NULL COMMENT '通道号',
  `channel_name`  varchar(100) DEFAULT NULL COMMENT '通道名称',
  `channel_type`  int          DEFAULT 1 COMMENT '通道类型 1模拟 2数字 3零通道',
  `status`        varchar(20)  DEFAULT 'offline' COMMENT '在线状态',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  `create_dept`   bigint       DEFAULT NULL COMMENT '创建部门',
  `create_by`     bigint       DEFAULT NULL COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`     bigint       DEFAULT NULL COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`channel_id`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='海康设备通道表';

DROP TABLE IF EXISTS `sys_hik_alarm_record`;
CREATE TABLE `sys_hik_alarm_record` (
  `id`            bigint       NOT NULL COMMENT '主键',
  `device_id`     bigint       DEFAULT NULL COMMENT '设备主键',
  `command`       int          DEFAULT NULL COMMENT '报警命令类型',
  `command_name`  varchar(100) DEFAULT NULL COMMENT '报警命令名称',
  `device_ip`     varchar(50)  DEFAULT NULL COMMENT '报警设备IP',
  `alarm_time`    datetime     DEFAULT NULL COMMENT '报警时间',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  `create_dept`   bigint       DEFAULT NULL COMMENT '创建部门',
  `create_by`     bigint       DEFAULT NULL COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`     bigint       DEFAULT NULL COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_alarm_time` (`alarm_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='海康报警记录表';

-- --------------------------------------------------------
-- 菜单 SQL（如需使用前端页面，请执行以下语句，父菜单ID按实际调整）
-- 权限标识：hikvision:device:list/query/add/edit/remove
-- --------------------------------------------------------
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query_param`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_dept`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
(6000, '海康SDK', 0, 10, 'hikvision', NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'camera', 103, 1, NOW(), NULL, NULL, '海康SDK对接目录'),
(6001, '设备管理', 6000, 1, 'device', 'hikvision/device/index', NULL, 1, 0, 'C', '0', '0', 'hikvision:device:list', 'monitor', 103, 1, NOW(), NULL, NULL, '设备管理菜单'),
(6002, '设备查询', 6001, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'hikvision:device:query', '#', 103, 1, NOW(), NULL, NULL, ''),
(6003, '设备新增', 6001, 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'hikvision:device:add', '#', 103, 1, NOW(), NULL, NULL, ''),
(6004, '设备修改', 6001, 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'hikvision:device:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
(6005, '设备删除', 6001, 4, '', NULL, NULL, 1, 0, 'F', '0', '0', 'hikvision:device:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
(6006, '实时预览', 6000, 2, 'preview', 'hikvision/preview/index', NULL, 1, 0, 'C', '0', '0', 'hikvision:preview:list', 'video', 103, 1, NOW(), NULL, NULL, '实时预览菜单'),
(6007, '录像回放', 6000, 3, 'playback', 'hikvision/playback/index', NULL, 1, 0, 'C', '0', '0', 'hikvision:playback:list', 'time', 103, 1, NOW(), NULL, NULL, '录像回放菜单'),
(6008, '报警记录', 6000, 4, 'alarm', 'hikvision/alarm/index', NULL, 1, 0, 'C', '0', '0', 'hikvision:alarm:list', 'bell', 103, 1, NOW(), NULL, NULL, '报警记录菜单'),
(6009, '门禁调试', 6000, 5, 'access', 'hikvision/access/index', NULL, 1, 0, 'C', '0', '0', 'hikvision:device:query', 'lock', 103, 1, NOW(), NULL, NULL, '门禁与ISAPI调试菜单');
