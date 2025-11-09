-- Phase 1: Initial Database Schema
-- Create application schema and tables

-- Create app schema
CREATE SCHEMA IF NOT EXISTS app;

-- Set search path to app schema
SET search_path TO app, public;

-- Create enum types
CREATE TYPE user_role AS ENUM ('admin', 'operator', 'viewer');
CREATE TYPE device_protocol AS ENUM ('rdp', 'vnc', 'ssh');
CREATE TYPE device_status AS ENUM ('online', 'offline', 'unknown');
CREATE TYPE permission_type AS ENUM ('read', 'write', 'control', 'view');
CREATE TYPE connection_status AS ENUM ('success', 'failed', 'timeout');
CREATE TYPE audit_action AS ENUM ('create', 'update', 'delete', 'connect', 'login', 'logout');

-- Users table
CREATE TABLE app.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'viewer',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE
);

-- User Groups table
CREATE TABLE app.user_groups (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Group Members table
CREATE TABLE app.group_members (
    group_id INTEGER NOT NULL REFERENCES app.user_groups(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES app.users(id) ON DELETE CASCADE,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, user_id)
);

-- Devices table
CREATE TABLE app.devices (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    protocol device_protocol NOT NULL,
    username VARCHAR(255),
    password_encrypted TEXT,
    private_key TEXT,
    guacamole_conn_id VARCHAR(255),
    status device_status DEFAULT 'unknown',
    tags JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES app.users(id),
    is_active BOOLEAN DEFAULT TRUE
);

-- User Devices (permissions) table
CREATE TABLE app.user_devices (
    user_id INTEGER NOT NULL REFERENCES app.users(id) ON DELETE CASCADE,
    device_id INTEGER NOT NULL REFERENCES app.devices(id) ON DELETE CASCADE,
    permission permission_type NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    granted_by INTEGER REFERENCES app.users(id),
    PRIMARY KEY (user_id, device_id)
);

-- Group Devices (permissions) table
CREATE TABLE app.group_devices (
    group_id INTEGER NOT NULL REFERENCES app.user_groups(id) ON DELETE CASCADE,
    device_id INTEGER NOT NULL REFERENCES app.devices(id) ON DELETE CASCADE,
    permission permission_type NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, device_id)
);

-- Connection Logs table
CREATE TABLE app.connection_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES app.users(id),
    device_id INTEGER NOT NULL REFERENCES app.devices(id),
    connection_start TIMESTAMP WITH TIME ZONE NOT NULL,
    connection_end TIMESTAMP WITH TIME ZONE,
    duration INTEGER, -- in seconds
    status connection_status NOT NULL,
    ip_address INET,
    user_agent TEXT
);

-- Audit Logs table
CREATE TABLE app.audit_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES app.users(id),
    action audit_action NOT NULL,
    resource_type VARCHAR(50) NOT NULL, -- user, device, connection, etc.
    resource_id INTEGER,
    details JSONB,
    ip_address INET,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_users_username ON app.users(username);
CREATE INDEX idx_users_email ON app.users(email);
CREATE INDEX idx_users_role ON app.users(role);
CREATE INDEX idx_devices_host ON app.devices(host);
CREATE INDEX idx_devices_protocol ON app.devices(protocol);
CREATE INDEX idx_devices_status ON app.devices(status);
CREATE INDEX idx_user_devices_user_id ON app.user_devices(user_id);
CREATE INDEX idx_user_devices_device_id ON app.user_devices(device_id);
CREATE INDEX idx_connection_logs_user_id ON app.connection_logs(user_id);
CREATE INDEX idx_connection_logs_device_id ON app.connection_logs(device_id);
CREATE INDEX idx_connection_logs_connection_start ON app.connection_logs(connection_start);
CREATE INDEX idx_audit_logs_user_id ON app.audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON app.audit_logs(action);
CREATE INDEX idx_audit_logs_resource_type ON app.audit_logs(resource_type);
CREATE INDEX idx_audit_logs_timestamp ON app.audit_logs(timestamp);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION app.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON app.users
    FOR EACH ROW EXECUTE FUNCTION app.update_updated_at_column();

CREATE TRIGGER update_devices_updated_at BEFORE UPDATE ON app.devices
    FOR EACH ROW EXECUTE FUNCTION app.update_updated_at_column();

-- Grant permissions (will be executed after user creation)
-- Note: PostgreSQL user needs to be created separately or use default postgres user

COMMENT ON SCHEMA app IS 'Application schema for Remote Desktop Management Platform';
COMMENT ON TABLE app.users IS 'System users with roles (admin, operator, viewer)';
COMMENT ON TABLE app.devices IS 'Remote devices (RDP, VNC, SSH)';
COMMENT ON TABLE app.user_devices IS 'User permissions for devices';
COMMENT ON TABLE app.group_devices IS 'Group permissions for devices';
COMMENT ON TABLE app.connection_logs IS 'Connection history and logs';
COMMENT ON TABLE app.audit_logs IS 'Audit trail for all system actions';

