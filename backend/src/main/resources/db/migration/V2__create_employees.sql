CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    department_id BIGINT REFERENCES departments(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_employees_code UNIQUE (employee_code),
    CONSTRAINT uk_employees_email UNIQUE (email)
);
