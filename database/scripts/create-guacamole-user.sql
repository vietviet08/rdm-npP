-- Script to create Guacamole default admin user
-- Run this AFTER Guacamole has initialized its schema
-- Usage: docker-compose exec postgres psql -U postgres -d rdm_platform -f /path/to/create-guacamole-user.sql

-- Create guacadmin user
DO $$
DECLARE
    admin_entity_id INTEGER;
    service_entity_id INTEGER;
BEGIN
    -- Check if guacamole_user table exists
    IF NOT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'guacamole_user') THEN
        RAISE EXCEPTION 'Guacamole schema not found. Please ensure Guacamole has started and created its schema.';
    END IF;

    -- Create admin entity
    INSERT INTO guacamole_entity (name, type) 
    VALUES ('guacadmin', 'USER')
    ON CONFLICT DO NOTHING;
    
    SELECT entity_id INTO admin_entity_id 
    FROM guacamole_entity 
    WHERE name = 'guacadmin' AND type = 'USER';
    
    -- Create admin user with password 'guacadmin'
    -- Password hash: SHA256('guacadmin\0guacadmin')
    -- You can verify this with: echo -n 'guacadmin\0guacadmin' | sha256sum
    INSERT INTO guacamole_user (entity_id, password_hash, password_salt, password_date)
    VALUES (
        admin_entity_id,
        E'\\xCA458B7FD494D6FE84A536F5B0E83C2B36F89679F2899A122CF6E03E83B2B9F4',
        NULL,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT (entity_id) DO UPDATE
    SET password_hash = EXCLUDED.password_hash,
        password_date = CURRENT_TIMESTAMP;
    
    -- Grant admin permissions
    INSERT INTO guacamole_system_permission (entity_id, permission)
    VALUES (admin_entity_id, 'ADMINISTER')
    ON CONFLICT DO NOTHING;
    
    -- Create service account entity
    INSERT INTO guacamole_entity (name, type) 
    VALUES ('rdm-service', 'USER')
    ON CONFLICT DO NOTHING;
    
    SELECT entity_id INTO service_entity_id 
    FROM guacamole_entity 
    WHERE name = 'rdm-service' AND type = 'USER';
    
    -- Create service user (using same password hash for now - CHANGE IN PRODUCTION!)
    INSERT INTO guacamole_user (entity_id, password_hash, password_salt, password_date)
    VALUES (
        service_entity_id,
        E'\\xCA458B7FD494D6FE84A536F5B0E83C2B36F89679F2899A122CF6E03E83B2B9F4',
        NULL,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT (entity_id) DO UPDATE
    SET password_hash = EXCLUDED.password_hash,
        password_date = CURRENT_TIMESTAMP;
    
    RAISE NOTICE 'Guacamole users created/updated successfully';
    RAISE NOTICE 'Default admin credentials: username=guacadmin, password=guacadmin';
    RAISE NOTICE 'Service account: username=rdm-service, password=guacadmin (CHANGE IN PRODUCTION!)';
END $$;

