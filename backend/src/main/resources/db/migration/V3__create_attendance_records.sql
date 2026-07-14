CREATE TABLE attendance_records (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    date DATE NOT NULL,
    clock_in TIMESTAMP NOT NULL,
    clock_out TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attendance_employee_date ON attendance_records(employee_id, date);
