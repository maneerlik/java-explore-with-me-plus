-- liquibase formatted sql

-- changeset belove:002-alter-ip-type.sql
ALTER TABLE hits ALTER COLUMN ip TYPE VARCHAR(21) USING host(ip)::VARCHAR(21);