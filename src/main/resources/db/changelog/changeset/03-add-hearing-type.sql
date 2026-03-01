-- liquibase formatted sql

-- changeset vbanasevych:3
ALTER TABLE hearings ADD COLUMN type VARCHAR(50) NOT NULL DEFAULT 'CONSULTATION';