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

CREATE TABLE project_products
(
    project_id INTEGER REFERENCES projects (id),

    id SERIAL PRIMARY KEY,
    product_name TEXT NOT NULL,

    created_on timestamptz NOT NULL DEFAULT NOW()
);

CREATE TABLE location_boq_entries
(
    location_id INTEGER REFERENCES project_locations (id),

    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES project_products (id),

    quantity_in_store INTEGER NOT NULL,
    quantity_to_install INTEGER NOT NULL,

    created_on timestamptz NOT NULL DEFAULT NOW()
);