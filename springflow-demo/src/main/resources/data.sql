-- Sample data for SpringFlow Demo Application
-- This data is automatically loaded on application startup

-- Categories
INSERT INTO categories (id, name, description, parent_id) VALUES
    (1, 'Electronics', 'Electronic devices and gadgets', NULL),
    (2, 'Books', 'Books and publications', NULL),
    (3, 'Clothing', 'Apparel and accessories', NULL),
    (4, 'Smartphones', 'Mobile phones and accessories', 1),
    (5, 'Laptops', 'Portable computers', 1),
    (6, 'Fiction', 'Fiction books', 2),
    (7, 'Non-Fiction', 'Non-fiction books', 2);

-- Products
INSERT INTO products (id, name, description, price, stock, category_id, active, created_at, updated_at) VALUES
    (1, 'iPhone 15 Pro', 'Latest Apple smartphone with A17 Pro chip', 999.99, 50, 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Samsung Galaxy S24', 'Premium Android smartphone', 899.99, 45, 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'MacBook Pro 16"', 'Professional laptop with M3 Max chip', 2499.99, 20, 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'Dell XPS 15', 'High-performance Windows laptop', 1799.99, 30, 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'The Great Gatsby', 'Classic American novel by F. Scott Fitzgerald', 12.99, 100, 6, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, '1984', 'Dystopian novel by George Orwell', 14.99, 80, 6, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 'Sapiens', 'A brief history of humankind by Yuval Noah Harari', 18.99, 60, 7, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 'Men''s T-Shirt', 'Comfortable cotton t-shirt', 19.99, 200, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9, 'Women''s Jeans', 'Classic blue denim jeans', 49.99, 150, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (10, 'Wireless Earbuds', 'Bluetooth earbuds with noise cancellation', 149.99, 75, 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Users
INSERT INTO users (id, username, email, password, first_name, last_name, birth_date, phone_number, role, active, created_at) VALUES
    (1, 'admin', 'admin@springflow.io', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Admin', 'User', '1990-01-01', '+1234567890', 'ADMIN', true, CURRENT_TIMESTAMP),
    (2, 'john_doe', 'john@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'John', 'Doe', '1985-05-15', '+1234567891', 'USER', true, CURRENT_TIMESTAMP),
    (3, 'jane_smith', 'jane@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Jane', 'Smith', '1992-08-20', '+1234567892', 'MODERATOR', true, CURRENT_TIMESTAMP),
    (4, 'bob_wilson', 'bob@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Bob', 'Wilson', '1988-03-10', '+1234567893', 'USER', true, CURRENT_TIMESTAMP),
    (5, 'alice_brown', 'alice@example.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Alice', 'Brown', '1995-11-25', '+1234567894', 'USER', true, CURRENT_TIMESTAMP);

-- Reset auto-increment sequences to avoid primary key conflicts
-- H2 syntax: ALTER TABLE tablename ALTER COLUMN id RESTART WITH next_value
ALTER TABLE categories ALTER COLUMN id RESTART WITH 8;
ALTER TABLE products ALTER COLUMN id RESTART WITH 11;
ALTER TABLE users ALTER COLUMN id RESTART WITH 6;
