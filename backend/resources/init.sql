CREATE DATABASE crud;
\connect crud;

CREATE TABLE IF NOT EXISTS patient
(
    name         TEXT NOT NULL,
    lastname     TEXT NOT NULL,
    patronymic   TEXT,
    birthday     DATE,
    gender       TEXT,
    address      TEXT,
    insurance_id INT8 PRIMARY KEY
);

CREATE INDEX IF NOT EXISTS patient_name_idx
    ON patient
        USING btree (name);

CREATE INDEX IF NOT EXISTS patient_lastname_idx
    ON patient
        USING btree (lastname);

CREATE INDEX IF NOT EXISTS patient_patronymic_idx
    ON patient
        USING btree (patronymic);
