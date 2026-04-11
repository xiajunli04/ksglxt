-- =============================================
-- 广软课室申请系统 - 数据库初始化脚本
-- =============================================
ALTER DATABASE ksglxt CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- ----------------------------
-- 1. 用户信息表（扩展字段）
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_name        VARCHAR(64)  NOT NULL COMMENT '用户名',
    nick_name        VARCHAR(64)  DEFAULT '' COMMENT '昵称',
    email            VARCHAR(128) DEFAULT '' COMMENT '邮箱',
    phone            VARCHAR(20)  DEFAULT '' COMMENT '手机号',
    sex              CHAR(1)      DEFAULT '0' COMMENT '性别 0=男 1=女 2=未知',
    avatar           VARCHAR(255) DEFAULT '' COMMENT '头像地址',
    password         VARCHAR(128) NOT NULL COMMENT '密码',
    status           CHAR(1)      DEFAULT '0' COMMENT '状态 0=正常 1=停用',
    login_fail_count INT          DEFAULT 0 COMMENT '登录失败次数',
    lock_time        DATETIME     DEFAULT NULL COMMENT '锁定时间',
    del_flag         CHAR(1)      DEFAULT '0' COMMENT '删除标志 0=未删除 1=已删除',
    create_by        VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark           VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='用户信息表';

-- ----------------------------
-- 2. 角色信息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_name   VARCHAR(64)  NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(64)  NOT NULL COMMENT '角色编码',
    status      CHAR(1)      DEFAULT '0' COMMENT '状态 0=正常 1=停用',
    del_flag    CHAR(1)      DEFAULT '0' COMMENT '删除标志 0=未删除 1=已删除',
    create_by   VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark      VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='角色信息表';

-- ----------------------------
-- 3. 菜单权限表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id   BIGINT       DEFAULT 0 COMMENT '父菜单ID',
    menu_name   VARCHAR(64)  NOT NULL COMMENT '菜单名称',
    menu_type   CHAR(1)      DEFAULT 'M' COMMENT '菜单类型 M=目录 C=菜单 F=按钮',
    path        VARCHAR(255) DEFAULT '' COMMENT '路由地址',
    component   VARCHAR(255) DEFAULT '' COMMENT '组件路径',
    permission  VARCHAR(128) DEFAULT '' COMMENT '权限标识',
    icon        VARCHAR(64)  DEFAULT '' COMMENT '菜单图标',
    sort        INT          DEFAULT 0 COMMENT '排序',
    visible     CHAR(1)      DEFAULT '0' COMMENT '是否可见 0=可见 1=隐藏',
    status      CHAR(1)      DEFAULT '0' COMMENT '状态 0=正常 1=停用',
    del_flag    CHAR(1)      DEFAULT '0' COMMENT '删除标志 0=未删除 1=已删除',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='菜单权限表';

-- ----------------------------
-- 4. 用户角色关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- ----------------------------
-- 5. 角色菜单关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='角色菜单关联表';

-- ----------------------------
-- 6. 课室信息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS classroom (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    room_code   VARCHAR(32)  NOT NULL COMMENT '课室编号',
    room_name   VARCHAR(64)  NOT NULL COMMENT '课室名称',
    building    VARCHAR(64)  DEFAULT '' COMMENT '所属教学楼',
    floor       INT          DEFAULT 1 COMMENT '楼层',
    capacity    INT          DEFAULT 0 COMMENT '容纳人数',
    room_type   VARCHAR(32)  DEFAULT '' COMMENT '课室类型',
    facilities  TEXT         COMMENT '设施设备',
    status      CHAR(1)      DEFAULT '0' COMMENT '状态 0=可用 1=停用 2=维修中',
    image       VARCHAR(255) DEFAULT '' COMMENT '课室图片',
    del_flag    CHAR(1)      DEFAULT '0' COMMENT '删除标志 0=未删除 1=已删除',
    create_by   VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark      VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='课室信息表';

-- ----------------------------
-- 7. 时间段表
-- ----------------------------
CREATE TABLE IF NOT EXISTS time_slot (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    slot_name   VARCHAR(32) NOT NULL COMMENT '时段名称',
    start_time  TIME        NOT NULL COMMENT '开始时间',
    end_time    TIME        NOT NULL COMMENT '结束时间',
    sort_order  INT         DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='时间段表';

-- ----------------------------
-- 8. 预约申请表
-- ----------------------------
CREATE TABLE IF NOT EXISTS booking (
    id                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    applicant_id         BIGINT       NOT NULL COMMENT '申请人ID',
    classroom_id         BIGINT       NOT NULL COMMENT '课室ID',
    booking_date         DATE         NOT NULL COMMENT '预约日期',
    time_slot_id         BIGINT       NOT NULL COMMENT '时间段ID',
    start_time           TIME         NOT NULL COMMENT '开始时间',
    end_time             TIME         NOT NULL COMMENT '结束时间',
    purpose              VARCHAR(255) DEFAULT '' COMMENT '使用目的',
    expected_attendance  INT          DEFAULT 0 COMMENT '预计人数',
    contact_phone        VARCHAR(20)  DEFAULT '' COMMENT '联系电话',
    attachment_url       VARCHAR(500) DEFAULT '' COMMENT '附件地址',
    status               CHAR(1)      DEFAULT '0' COMMENT '状态 0=待审批 1=通过 2=驳回 3=取消 4=完成',
    reject_reason        VARCHAR(500) DEFAULT '' COMMENT '驳回原因',
    approver_id          BIGINT       DEFAULT NULL COMMENT '审批人ID',
    approve_time         DATETIME     DEFAULT NULL COMMENT '审批时间',
    del_flag             CHAR(1)      DEFAULT '0' COMMENT '删除标志 0=未删除 1=已删除',
    create_by            VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time          DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by            VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='预约申请表';

-- ----------------------------
-- 9. 公告表
-- ----------------------------
CREATE TABLE IF NOT EXISTS announcement (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(128) NOT NULL COMMENT '公告标题',
    content      TEXT         COMMENT '公告内容',
    publisher_id BIGINT       NOT NULL COMMENT '发布人ID',
    is_top       CHAR(1)      DEFAULT '0' COMMENT '是否置顶 0=否 1=是',
    status       CHAR(1)      DEFAULT '0' COMMENT '状态 0=草稿 1=已发布',
    del_flag     CHAR(1)      DEFAULT '0' COMMENT '删除标志 0=未删除 1=已删除',
    create_by    VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='公告表';

-- ----------------------------
-- 10. 通知表
-- ----------------------------
CREATE TABLE IF NOT EXISTS notification (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '接收用户ID',
    title       VARCHAR(128) NOT NULL COMMENT '通知标题',
    content     TEXT         COMMENT '通知内容',
    type        CHAR(1)      DEFAULT '3' COMMENT '类型 1=审批通过 2=审批驳回 3=系统提醒',
    is_read     CHAR(1)      DEFAULT '0' COMMENT '是否已读 0=未读 1=已读',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='通知表';

-- ----------------------------
-- 11. 操作日志表
-- ----------------------------
CREATE TABLE IF NOT EXISTS operation_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       DEFAULT NULL COMMENT '操作用户ID',
    operation   VARCHAR(128) DEFAULT '' COMMENT '操作描述',
    method      VARCHAR(255) DEFAULT '' COMMENT '请求方法',
    params      TEXT         COMMENT '请求参数',
    ip          VARCHAR(64)  DEFAULT '' COMMENT 'IP地址',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='操作日志表';

-- ----------------------------
-- 12. 字典数据表
-- ----------------------------
CREATE TABLE IF NOT EXISTS dict_data (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    dict_type   VARCHAR(64)  NOT NULL COMMENT '字典类型',
    dict_label  VARCHAR(128) DEFAULT '' COMMENT '字典标签',
    dict_value  VARCHAR(128) DEFAULT '' COMMENT '字典值',
    sort        INT          DEFAULT 0 COMMENT '排序',
    status      CHAR(1)      DEFAULT '0' COMMENT '状态 0=正常 1=停用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    remark      VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='字典数据表';


-- =============================================
-- 初始数据
-- =============================================

-- ----------------------------
-- 初始角色
-- ----------------------------
INSERT IGNORE INTO sys_role (id, role_name, role_code, status, remark) VALUES
(1, '管理员', 'ADMIN', '0', '系统管理员'),
(2, '教师', 'TEACHER', '0', '教师角色'),
(3, '学生', 'STUDENT', '0', '学生角色');

-- ----------------------------
-- 初始用户（密码统一 123456）
-- ----------------------------
INSERT IGNORE INTO sys_user (id, user_name, nick_name, email, password, status) VALUES
(1, 'admin', '管理员', 'admin@xjl.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0'),
(2, 'zhangsan', '张三', 'zhangsan@xjl.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0'),
(3, 'xiajunli', 'XJL', 'xiajunli@xjl.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '0');

-- ----------------------------
-- 用户角色关联
-- ----------------------------
INSERT IGNORE INTO sys_user_role (id, user_id, role_id) VALUES
(1, 1, 1),
(2, 2, 2),
(3, 3, 3);

-- ----------------------------
-- 菜单数据
-- ----------------------------
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort, visible, status) VALUES
-- 一级菜单
(1,  0, '首页',       'M', '/dashboard',      'dashboard/index',      '',                  'dashboard', 1, '0', '0'),
(2,  0, '课室管理',   'M', '/classroom',      'classroom/index',      '',                  'classroom', 2, '0', '0'),
(3,  0, '预约管理',   'M', '/booking',        'booking/index',        '',                  'booking',   3, '0', '0'),
(4,  0, '审批管理',   'M', '/approval',       'approval/index',       '',                  'approval',  4, '0', '0'),
(5,  0, '公告管理',   'M', '/announcement',   'announcement/index',   '',                  'notice',    5, '0', '0'),
(6,  0, '用户管理',   'M', '/user',           'user/index',           '',                  'user',      6, '0', '0'),
(7,  0, '个人中心',   'M', '/profile',        'profile/index',        '',                  'profile',   7, '0', '0'),

-- 课室管理 - 按钮
(20, 2, '课室查询',   'F', '', '', 'classroom:list',   '', 1, '0', '0'),
(21, 2, '课室新增',   'F', '', '', 'classroom:add',    '', 2, '0', '0'),
(22, 2, '课室修改',   'F', '', '', 'classroom:edit',   '', 3, '0', '0'),
(23, 2, '课室删除',   'F', '', '', 'classroom:remove', '', 4, '0', '0'),

-- 预约管理 - 按钮
(30, 3, '预约查询',   'F', '', '', 'booking:list',     '', 1, '0', '0'),
(31, 3, '预约新增',   'F', '', '', 'booking:add',      '', 2, '0', '0'),
(32, 3, '预约修改',   'F', '', '', 'booking:edit',     '', 3, '0', '0'),
(33, 3, '预约取消',   'F', '', '', 'booking:cancel',   '', 4, '0', '0'),

-- 审批管理 - 按钮
(40, 4, '审批查询',   'F', '', '', 'approval:list',    '', 1, '0', '0'),
(41, 4, '审批通过',   'F', '', '', 'approval:approve', '', 2, '0', '0'),
(42, 4, '审批驳回',   'F', '', '', 'approval:reject',  '', 3, '0', '0'),

-- 公告管理 - 按钮
(50, 5, '公告查询',   'F', '', '', 'announcement:list',   '', 1, '0', '0'),
(51, 5, '公告新增',   'F', '', '', 'announcement:add',    '', 2, '0', '0'),
(52, 5, '公告修改',   'F', '', '', 'announcement:edit',   '', 3, '0', '0'),
(53, 5, '公告删除',   'F', '', '', 'announcement:remove', '', 4, '0', '0'),

-- 用户管理 - 按钮
(60, 6, '用户查询',   'F', '', '', 'user:list',   '', 1, '0', '0'),
(61, 6, '用户新增',   'F', '', '', 'user:add',    '', 2, '0', '0'),
(62, 6, '用户修改',   'F', '', '', 'user:edit',   '', 3, '0', '0'),
(63, 6, '用户删除',   'F', '', '', 'user:remove', '', 4, '0', '0');

-- ----------------------------
-- 角色-菜单关联（ADMIN 全部权限）
-- ----------------------------
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
-- ADMIN 全部菜单权限
(1, 1),  (1, 2),  (1, 3),  (1, 4),  (1, 5),  (1, 6),  (1, 7),
(1, 20), (1, 21), (1, 22), (1, 23),
(1, 30), (1, 31), (1, 32), (1, 33),
(1, 40), (1, 41), (1, 42),
(1, 50), (1, 51), (1, 52), (1, 53),
(1, 60), (1, 61), (1, 62), (1, 63),

-- TEACHER 部分权限（首页、课室查询、预约、审批、公告查看、个人中心）
(2, 1),
(2, 2),  (2, 20),
(2, 3),  (2, 30), (2, 31), (2, 32), (2, 33),
(2, 4),  (2, 40),
(2, 5),  (2, 50),
(2, 7),

-- STUDENT 部分权限（首页、课室查询、预约、公告查看、个人中心）
(3, 1),
(3, 2),  (3, 20),
(3, 3),  (3, 30), (3, 31), (3, 33),
(3, 5),  (3, 50),
(3, 7);

-- ----------------------------
-- 时间段
-- ----------------------------
INSERT IGNORE INTO time_slot (id, slot_name, start_time, end_time, sort_order) VALUES
(1, '第1-2节', '08:00:00', '09:40:00', 1),
(2, '第3-4节', '10:00:00', '11:40:00', 2),
(3, '第5-6节', '14:00:00', '15:40:00', 3),
(4, '第7-8节', '16:00:00', '17:40:00', 4),
(5, '第9-10节', '19:00:00', '20:40:00', 5);

-- ----------------------------
-- 示例课室
-- ----------------------------
INSERT IGNORE INTO classroom (id, room_code, room_name, building, floor, capacity, room_type, facilities, status) VALUES
(1, 'A101', 'A栋101多媒体教室', 'A栋教学楼', 1, 120, '多媒体教室', '投影仪,音响系统,空调,白板', '0'),
(2, 'A201', 'A栋201普通教室',   'A栋教学楼', 2, 80,  '普通教室',   '空调,白板', '0'),
(3, 'B301', 'B栋301实验室',     'B栋教学楼', 3, 60,  '实验室',     '电脑60台,投影仪,空调', '0'),
(4, 'B401', 'B栋401会议室',     'B栋教学楼', 4, 30,  '会议室',     '投影仪,视频会议系统,空调', '0'),
(5, 'C102', 'C栋102阶梯教室',   'C栋教学楼', 1, 200, '阶梯教室',   '投影仪,音响系统,空调,麦克风', '0');
