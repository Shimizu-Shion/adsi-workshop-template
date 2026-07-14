CREATE TABLE calendar_holidays (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    name VARCHAR(100) NOT NULL,
    "year" INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_calendar_holidays_date UNIQUE (date)
);

CREATE INDEX idx_calendar_holidays_year ON calendar_holidays("year");
