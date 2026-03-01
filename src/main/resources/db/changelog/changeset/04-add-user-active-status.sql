-- liquibase formatted sql

-- changeset vbanasevych:4
ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT TRUE;