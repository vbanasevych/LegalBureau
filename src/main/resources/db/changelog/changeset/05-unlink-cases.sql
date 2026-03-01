-- liquibase formatted sql

-- changeset vbanasevych:5
ALTER TABLE legal_cases ALTER COLUMN client_id DROP NOT NULL;