-- liquibase formatted sql
-- changeset vbanasevych:6

ALTER TABLE hearings DROP COLUMN result;

ALTER TABLE legal_cases ADD COLUMN result VARCHAR(50);