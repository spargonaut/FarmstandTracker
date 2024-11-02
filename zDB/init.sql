CREATE TABLE IF NOT EXISTS farmstand (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    init_date DATE NOT NULL,
    shutdown_date DATE
);

CREATE TABLE IF NOT EXISTS measurement (
    id SERIAL PRIMARY KEY,
    farmstand_id INTEGER,
    measurement_date DATE,
    context VARCHAR(20),
    ph FLOAT,
    temp_value INTEGER,
    temp_metric VARCHAR(10),
    ec FLOAT,
    notes VARCHAR(255),
    CONSTRAINT fk_customer
        FOREIGN KEY(farmstand_id)
            REFERENCES farmstand(id)
);
