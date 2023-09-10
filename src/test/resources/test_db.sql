DROP DATABASE IF EXISTS boq_manager_test;

CREATE DATABASE boq_manager_test;
\c boq_manager_test

CREATE TABLE users
(
    id    SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    role  VARCHAR(100) NOT NULL
);

INSERT INTO users (email, role)
VALUES ('admin@jhuengineering.com', 'admin'),
       ('pm@jhuengineering.com', 'project-manager'),
       ('john@client.com', 'client');