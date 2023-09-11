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

CREATE TABLE projects
(
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    active BOOLEAN NOT NULL,
    created_on timestamptz NOT NULL DEFAULT NOW()
);

CREATE TABLE project_locations
(
    project_id INTEGER REFERENCES projects (id),

    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    created_on timestamptz NOT NULL DEFAULT NOW()
);