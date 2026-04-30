-- SwasthyaSetu Database Schema Reset
-- Run this in MySQL Workbench or CLI to start fresh

DROP DATABASE IF EXISTS swasthyasetu;
CREATE DATABASE swasthyasetu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE swasthyasetu;

-- Hibernate will auto-create all tables on next Spring Boot startup
-- The DataInitializer will seed users, appointments, and medicines
