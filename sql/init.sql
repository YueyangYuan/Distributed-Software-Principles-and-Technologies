-- ==============================
-- 用户服务数据库
-- ==============================
CREATE DATABASE IF NOT EXISTS seckill_user DEFAULT CHARACTER SET utf8mb4;
USE seckill_user;

CREATE TABLE t_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(64) NOT NULL,
    salt        VARCHAR(16) NOT NULL,
    nickname    VARCHAR(50),
    phone       VARCHAR(20),
    email       VARCHAR(100),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==============================
-- 商品服务数据库
-- ==============================
CREATE DATABASE IF NOT EXISTS seckill_product DEFAULT CHARACTER SET utf8mb4;
USE seckill_product;

CREATE TABLE t_product (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    price               DECIMAL(10,2) NOT NULL,
    image_url           VARCHAR(500),
    status              TINYINT DEFAULT 1 COMMENT '0-下架 1-上架 2-秒杀中',
    seckill_start_time  DATETIME,
    seckill_end_time    DATETIME,
    seckill_price       DECIMAL(10,2),
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted             TINYINT DEFAULT 0,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 测试数据
INSERT INTO t_product (name, description, price, status, seckill_price, seckill_start_time, seckill_end_time) VALUES
('iPhone 16 Pro', '苹果最新旗舰手机', 8999.00, 2, 6999.00, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY)),
('MacBook Pro M4', '专业笔记本电脑', 14999.00, 2, 11999.00, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY)),
('AirPods Pro 3', '主动降噪耳机', 1899.00, 2, 1299.00, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY));

-- ==============================
-- 库存服务数据库
-- ==============================
CREATE DATABASE IF NOT EXISTS seckill_inventory DEFAULT CHARACTER SET utf8mb4;
USE seckill_inventory;

CREATE TABLE t_inventory (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id      BIGINT NOT NULL UNIQUE,
    total_stock     INT NOT NULL DEFAULT 0,
    available_stock INT NOT NULL DEFAULT 0,
    locked_stock    INT NOT NULL DEFAULT 0,
    sold_count      INT NOT NULL DEFAULT 0,
    version         INT NOT NULL DEFAULT 0,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO t_inventory (product_id, total_stock, available_stock) VALUES
(1, 100, 100),
(2, 50, 50),
(3, 200, 200);

-- ==============================
-- 订单服务数据库
-- ==============================
CREATE DATABASE IF NOT EXISTS seckill_order DEFAULT CHARACTER SET utf8mb4;
USE seckill_order;

CREATE TABLE t_order (
    id           BIGINT PRIMARY KEY COMMENT '雪花算法ID',
    user_id      BIGINT NOT NULL,
    product_id   BIGINT NOT NULL,
    product_name VARCHAR(200),
    order_price  DECIMAL(10,2),
    quantity     INT DEFAULT 1,
    status       TINYINT DEFAULT 0 COMMENT '0-待支付 1-已支付 2-已取消 3-已超时',
    pay_time     DATETIME,
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==============================
-- 读写分离: 从库配置（实际部署时用）
-- ==============================
-- CHANGE MASTER TO MASTER_HOST='mysql-master', MASTER_USER='repl', MASTER_PASSWORD='repl123';
-- START SLAVE;
