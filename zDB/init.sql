CREATE TABLE IF NOT EXISTS farmstand (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    init_date DATE NOT NULL,
    shutdown_date DATE
);
