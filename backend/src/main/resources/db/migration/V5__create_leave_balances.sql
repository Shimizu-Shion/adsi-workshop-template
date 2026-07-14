CREATE TABLE leave_balances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    fiscal_year INT NOT NULL,
    total_days INT NOT NULL DEFAULT 20,
    used_days INT NOT NULL DEFAULT 0,
    carried_over INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_leave_balances_employee_year UNIQUE (employee_id, fiscal_year)
);
