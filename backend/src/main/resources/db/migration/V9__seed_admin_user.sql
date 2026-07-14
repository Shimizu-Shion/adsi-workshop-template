-- password: admin123 (BCrypt encoded)
INSERT INTO employees (employee_code, name, email, password_hash, role, department_id, active)
VALUES ('admin', '管理者', 'admin@example.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'ADMIN', 1, TRUE);
