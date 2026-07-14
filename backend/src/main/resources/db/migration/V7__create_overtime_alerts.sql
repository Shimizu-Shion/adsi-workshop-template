CREATE TABLE overtime_alerts (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    year_month VARCHAR(7) NOT NULL,
    total_overtime_minutes INT NOT NULL,
    notified_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_overtime_alerts_employee_month UNIQUE (employee_id, year_month)
);
