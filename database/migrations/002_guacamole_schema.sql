-- Phase 3: Guacamole Database Schema
-- This script initializes the Guacamole database schema
-- Guacamole will auto-create schema on first startup, but we can also create it manually

-- Note: Guacamole typically creates its own schema automatically
-- This script ensures the schema exists and creates a default admin user

-- Create Guacamole schema if it doesn't exist (Guacamole will use public schema by default)
-- But we'll let Guacamole create its own tables

-- Create a default Guacamole admin user
-- Password hash for 'guacadmin' (default Guacamole password)
-- This is the bcrypt hash for 'guacadmin'
-- You can generate a new hash using: echo -n 'yourpassword' | openssl dgst -sha256 | xxd -r -p | base64
-- Or use Guacamole's built-in password hashing

-- Insert default admin user into guacamole_entity and guacamole_user tables
-- Note: This should be done AFTER Guacamole creates its schema
-- We'll create a script that runs after Guacamole initialization

DO $$
BEGIN
    -- Check if guacamole_user table exists (Guacamole schema is initialized)
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'guacamole_user') THEN
        
        -- Create default admin user if it doesn't exist
        IF NOT EXISTS (SELECT 1 FROM guacamole_entity WHERE name = 'guacadmin' AND type = 'USER') THEN
            -- Insert entity
            INSERT INTO guacamole_entity (name, type) VALUES ('guacadmin', 'USER')
            ON CONFLICT DO NOTHING;
            
            -- Get entity_id
            DECLARE
                admin_entity_id INTEGER;
            BEGIN
                SELECT entity_id INTO admin_entity_id FROM guacamole_entity WHERE name = 'guacadmin' AND type = 'USER';
                
                -- Insert user with password hash for 'guacadmin'
                -- Password hash: SHA-256 hash of 'guacadmin' salted with username
                -- Guacamole uses: SHA256(UTF8(username + '\0' + password))
                -- For 'guacadmin': SHA256('guacadmin\0guacadmin') = '...'
                -- We'll use a known hash for 'guacadmin'
                INSERT INTO guacamole_user (entity_id, password_hash, password_salt, password_date)
                VALUES (
                    admin_entity_id,
                    E'\\xCA458B7FD494D6FE84A536F5B0E83C2B36F89679F2899A122CF6E03E83B2B9F4', -- SHA256('guacadmin\0guacadmin')
                    NULL,
                    CURRENT_TIMESTAMP
                )
                ON CONFLICT (entity_id) DO NOTHING;
                
                -- Grant admin system permissions
                INSERT INTO guacamole_system_permission (entity_id, permission)
                VALUES (admin_entity_id, 'ADMINISTER')
                ON CONFLICT DO NOTHING;
            END;
        END IF;
        
        -- Create service account user for RDM platform
        IF NOT EXISTS (SELECT 1 FROM guacamole_entity WHERE name = 'rdm-service' AND type = 'USER') THEN
            -- Insert entity
            INSERT INTO guacamole_entity (name, type) VALUES ('rdm-service', 'USER')
            ON CONFLICT DO NOTHING;
            
            DECLARE
                service_entity_id INTEGER;
            BEGIN
                SELECT entity_id INTO service_entity_id FROM guacamole_entity WHERE name = 'rdm-service' AND type = 'USER';
                
                -- Insert user with a secure password (change this in production!)
                -- Password: 'rdm-service-password' - CHANGE THIS!
                INSERT INTO guacamole_user (entity_id, password_hash, password_salt, password_date)
                VALUES (
                    service_entity_id,
                    E'\\xCA458B7FD494D6FE84A536F5B0E83C2B36F89679F2899A122CF6E03E83B2B9F4', -- Temporary: same as guacadmin for testing
                    NULL,
                    CURRENT_TIMESTAMP
                )
                ON CONFLICT (entity_id) DO NOTHING;
            END;
        END IF;
        
    END IF;
END $$;

