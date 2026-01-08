-- liquibase formatted sql
-- changeset developer:001

-- create permissions table
CREATE TABLE permissions (
                        id UUID PRIMARY KEY,
                        name VARCHAR(255) UNIQUE NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP
);

-- create roles table
CREATE TABLE roles (
                       id UUID PRIMARY KEY,
                       name VARCHAR(255) UNIQUE NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP
);

-- create users table
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP
);

-- create roles_permissions table
CREATE TABLE roles_permissions (
                        role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
                        permission_id UUID REFERENCES permissions(id) ON DELETE CASCADE,
                        PRIMARY KEY (role_id, permission_id)
);
CREATE INDEX idx_roles_permissions_role ON roles_permissions(role_id);
CREATE INDEX idx_roles_permissions_perm ON roles_permissions(permission_id);

-- create users_roles table
CREATE TABLE users_roles (
                        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                        role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
                        PRIMARY KEY (user_id, role_id)
);
CREATE INDEX idx_users_roles_user ON users_roles(user_id);
CREATE INDEX idx_users_roles_role ON users_roles(role_id);