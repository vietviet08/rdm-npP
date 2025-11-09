#!/usr/bin/env node

/**
 * Seed script to create admin user with properly hashed password
 * Usage: node database/seeds/seed-admin.js
 */

const { Pool } = require("pg");
const bcrypt = require("bcryptjs");

const pool = new Pool({
    host: process.env.DATABASE_HOST || "localhost",
    port: process.env.DATABASE_PORT || 5432,
    database: process.env.DATABASE_NAME || "rdm_platform",
    user: process.env.DATABASE_USER || "postgres",
    password: process.env.DATABASE_PASSWORD || "postgres",
});

async function seedAdmin() {
    const client = await pool.connect();

    try {
        await client.query("BEGIN");
        await client.query("SET search_path TO app, public");

        // Default admin credentials
        const username = "admin";
        const email = "admin@rdm.local";
        const password = "admin123"; // Change this in production!
        const passwordHash = await bcrypt.hash(password, 10);

        // Check if admin user already exists
        const existingUser = await client.query(
            "SELECT id FROM app.users WHERE username = $1",
            [username]
        );

        if (existingUser.rows.length > 0) {
            console.log("Admin user already exists. Updating password...");
            await client.query(
                "UPDATE app.users SET password_hash = $1, email = $2 WHERE username = $3",
                [passwordHash, email, username]
            );
        } else {
            console.log("Creating admin user...");
            const result = await client.query(
                `INSERT INTO app.users (username, email, password_hash, role, is_active)
         VALUES ($1, $2, $3, 'admin', TRUE)
         RETURNING id`,
                [username, email, passwordHash]
            );

            const userId = result.rows[0].id;

            // Create audit log entry
            await client.query(
                `INSERT INTO app.audit_logs (user_id, action, resource_type, resource_id, details, ip_address)
         VALUES ($1, 'create'::audit_action, 'user', $1, $2, '127.0.0.1'::inet)`,
                [
                    userId,
                    JSON.stringify({
                        message: "Admin user created via seed script",
                        username,
                    }),
                ]
            );
        }

        await client.query("COMMIT");
        console.log("✅ Admin user seeded successfully!");
        console.log(`   Username: ${username}`);
        console.log(`   Password: ${password}`);
        console.log("   ⚠️  Please change the password after first login!");
    } catch (error) {
        await client.query("ROLLBACK");
        console.error("❌ Error seeding admin user:", error);
        process.exit(1);
    } finally {
        client.release();
        await pool.end();
    }
}

seedAdmin();
