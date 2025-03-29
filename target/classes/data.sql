-- 清理表数据
DELETE FROM user_roles;
DELETE FROM users;

-- 插入管理员用户
INSERT INTO users (
    id, username, password, email, phone, nickname, 
    enabled, account_non_expired, account_non_locked, credentials_non_expired,
    create_time, update_time, email_verified, phone_verified
) VALUES (
    1, 'admin', 
    '$2a$10$rDbl4jFq8L0j5nqxW1gGXO6QQCaQxBHtg0LFyqjlnqtFGzFvh0Pru', -- 密码: admin123
    'admin@lushan-garden.com',
    '13800138000',
    '系统管理员',
    true, true, true, true,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    true, true
);

-- 插入管理员角色
INSERT INTO user_roles (user_id, role) VALUES (1, 'ADMIN');
INSERT INTO user_roles (user_id, role) VALUES (1, 'USER');

-- 插入测试用户
INSERT INTO users (
    id, username, password, email, phone, nickname,
    enabled, account_non_expired, account_non_locked, credentials_non_expired,
    create_time, update_time, email_verified, phone_verified
) VALUES (
    2, 'testuser',
    '$2a$10$6KDklkImZMyYPgGxx4jZeOoXkh8TMT/egwJ4c8oNBrDkN0MAc7ylW', -- 密码: test123
    'test@lushan-garden.com',
    '13800138001',
    '测试用户',
    true, true, true, true,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    true, true
);

-- 插入测试用户角色
INSERT INTO user_roles (user_id, role) VALUES (2, 'USER');

-- 插入未验证用户
INSERT INTO users (
    id, username, password, email, phone, nickname,
    enabled, account_non_expired, account_non_locked, credentials_non_expired,
    create_time, update_time, email_verified, phone_verified
) VALUES (
    3, 'unverified',
    '$2a$10$6KDklkImZMyYPgGxx4jZeOoXkh8TMT/egwJ4c8oNBrDkN0MAc7ylW', -- 密码: test123
    'unverified@lushan-garden.com',
    '13800138002',
    '未验证用户',
    true, true, true, true,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    false, false
);

-- 插入未验证用户角色
INSERT INTO user_roles (user_id, role) VALUES (3, 'USER');

-- 插入禁用用户
INSERT INTO users (
    id, username, password, email, phone, nickname,
    enabled, account_non_expired, account_non_locked, credentials_non_expired,
    create_time, update_time, email_verified, phone_verified
) VALUES (
    4, 'disabled',
    '$2a$10$6KDklkImZMyYPgGxx4jZeOoXkh8TMT/egwJ4c8oNBrDkN0MAc7ylW', -- 密码: test123
    'disabled@lushan-garden.com',
    '13800138003',
    '禁用用户',
    false, true, true, true,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    true, true
);

-- 插入禁用用户角色
INSERT INTO user_roles (user_id, role) VALUES (4, 'USER');

-- 设置自增序列
ALTER SEQUENCE users_seq RESTART WITH 1000;