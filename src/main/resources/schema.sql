-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    membership_type VARCHAR(20),
    membership_expire_date TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(50),
    reset_password_token VARCHAR(100),
    reset_password_token_expire_time TIMESTAMP,
    email_verification_token VARCHAR(100),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verification_token VARCHAR(100),
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE
);

-- 用户角色表
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 序列生成器
CREATE SEQUENCE IF NOT EXISTS users_seq START WITH 1 INCREMENT BY 1;

-- 索引
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_reset_token ON users(reset_password_token);
CREATE INDEX IF NOT EXISTS idx_users_email_token ON users(email_verification_token);
CREATE INDEX IF NOT EXISTS idx_users_phone_token ON users(phone_verification_token);

-- 花园表
CREATE TABLE IF NOT EXISTS gardens (
    id BIGINT PRIMARY KEY,
    name_zh VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    description_zh TEXT,
    description_en TEXT,
    location_zh VARCHAR(255),
    location_en VARCHAR(255),
    area DECIMAL(10,2),
    plant_count INTEGER,
    established_date DATE,
    image_url VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    sort_order INTEGER DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 花园序列生成器
CREATE SEQUENCE IF NOT EXISTS gardens_seq START WITH 1 INCREMENT BY 1;

-- 花园分类表
CREATE TABLE IF NOT EXISTS garden_categories (
    id BIGINT PRIMARY KEY,
    name_zh VARCHAR(50) NOT NULL,
    name_en VARCHAR(50),
    description_zh TEXT,
    description_en TEXT,
    sort_order INTEGER DEFAULT 0,
    parent_id BIGINT,
    FOREIGN KEY (parent_id) REFERENCES garden_categories(id)
);

-- 花园分类序列生成器
CREATE SEQUENCE IF NOT EXISTS garden_categories_seq START WITH 1 INCREMENT BY 1;

-- 花园分类关联表
CREATE TABLE IF NOT EXISTS garden_category_relations (
    garden_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (garden_id, category_id),
    FOREIGN KEY (garden_id) REFERENCES gardens(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES garden_categories(id) ON DELETE CASCADE
);

-- 图片表
CREATE TABLE IF NOT EXISTS images (
    id BIGINT PRIMARY KEY,
    url VARCHAR(255) NOT NULL,
    title VARCHAR(100),
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    width INTEGER,
    height INTEGER,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 图片序列生成器
CREATE SEQUENCE IF NOT EXISTS images_seq START WITH 1 INCREMENT BY 1;

-- 花园图片关联表
CREATE TABLE IF NOT EXISTS garden_images (
    garden_id BIGINT NOT NULL,
    image_id BIGINT NOT NULL,
    sort_order INTEGER DEFAULT 0,
    PRIMARY KEY (garden_id, image_id),
    FOREIGN KEY (garden_id) REFERENCES gardens(id) ON DELETE CASCADE,
    FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE CASCADE
);

-- 评论表
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT PRIMARY KEY,
    content TEXT NOT NULL,
    garden_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (garden_id) REFERENCES gardens(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (parent_id) REFERENCES comments(id)
);

-- 评论序列生成器
CREATE SEQUENCE IF NOT EXISTS comments_seq START WITH 1 INCREMENT BY 1;

-- 访问日志表
CREATE TABLE IF NOT EXISTS access_logs (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    ip_address VARCHAR(50) NOT NULL,
    user_agent VARCHAR(255),
    request_url VARCHAR(255) NOT NULL,
    request_method VARCHAR(10) NOT NULL,
    response_status INTEGER NOT NULL,
    request_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    response_time BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 访问日志序列生成器
CREATE SEQUENCE IF NOT EXISTS access_logs_seq START WITH 1 INCREMENT BY 1;