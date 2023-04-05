CREATE DATABASE crud;
\connect crud;

CREATE TABLE patient
(
    name         TEXT NOT NULL,
    lastname     TEXT NOT NULL,
    patronymic   TEXT,
    birthday     DATE,
    gender       TEXT,
    address      TEXT,
    insurance_id INT8 PRIMARY KEY
);

CREATE INDEX patient_name_idx
    ON patient
        USING btree (name);

CREATE INDEX patient_lastname_idx
    ON patient
        USING btree (lastname);

CREATE INDEX patient_patronymic_idx
    ON patient
        USING btree (patronymic);