
CREATE DATABASE IF NOT EXISTS iwish_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE iwish_db;

-- Users
CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Friendships (one row per pair; user_a is always the smaller id)

CREATE TABLE IF NOT EXISTS friendships (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    user_a        INT NOT NULL,
    user_b        INT NOT NULL,
    status        ENUM('PENDING','ACCEPTED') NOT NULL DEFAULT 'PENDING',
    requested_by  INT NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_pair (user_a, user_b),
    FOREIGN KEY (user_a) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user_b) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (requested_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Catalog of items users can add to their wish lists.
-- New items can be added by an admin directly via SQL INSERT.

CREATE TABLE IF NOT EXISTS catalog_items (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL UNIQUE,
    description VARCHAR(500),
    price       DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB;

-- A user's personal wish list (an item picked from the catalog)

CREATE TABLE IF NOT EXISTS wishlist_items (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    owner_id         INT NOT NULL,
    catalog_item_id  INT NOT NULL,
    note             VARCHAR(255),
    status           ENUM('OPEN','COMPLETED') NOT NULL DEFAULT 'OPEN',
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (catalog_item_id) REFERENCES catalog_items(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Contributions made by friends toward a wishlist item's price

CREATE TABLE IF NOT EXISTS contributions (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    wishlist_item_id  INT NOT NULL,
    contributor_id    INT NOT NULL,
    amount            DECIMAL(10,2) NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wishlist_item_id) REFERENCES wishlist_items(id) ON DELETE CASCADE,
    FOREIGN KEY (contributor_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Notifications delivered to users (buyer + receiver events)

CREATE TABLE IF NOT EXISTS notifications (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    message     VARCHAR(500) NOT NULL,
    is_read     TINYINT(1) NOT NULL DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Starter catalog so the app is usable immediately.
-- Admins can add more any time with a plain INSERT statement, e.g.:
--   INSERT INTO catalog_items (name, description, price) VALUES ('New Item', 'Description', 49.99);

INSERT INTO catalog_items (name, description, price) VALUES
    ('Wireless Headphones',   'Noise-cancelling over-ear headphones',        79.99),
    ('Smart Watch',           'Fitness & notifications on your wrist',      129.99),
    ('Bluetooth Speaker',     'Portable speaker with deep bass',             45.50),
    ('E-Reader',              '6-inch glare-free e-ink reader',              99.00),
    ('Coffee Maker',          'Drip coffee maker, 12-cup capacity',          59.99),
    ('Board Game: Strategy',  'A fun strategy board game for 2-4 players',   34.99),
    ('Backpack',              'Water-resistant laptop backpack',             49.90),
    ('Sneakers',              'Comfortable everyday running sneakers',       89.00),
    ('Digital Camera',        'Compact point-and-shoot digital camera',     220.00),
    ('Cookbook',              'Bestselling cookbook, 200+ recipes',          24.99),
    ('Desk Lamp',             'LED desk lamp with adjustable brightness',    27.50),
    ('Gaming Mouse',          'Ergonomic RGB gaming mouse',                  39.99),
    ('Yoga Mat',              'Non-slip eco-friendly yoga mat',              22.00),
    ('Perfume',               'Signature eau de parfum, 50ml',               65.00),
    ('Bicycle Helmet',        'Lightweight, adjustable safety helmet',       32.00)
ON DUPLICATE KEY UPDATE name = VALUES(name);
