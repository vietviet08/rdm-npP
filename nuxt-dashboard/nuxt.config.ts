// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
    compatibilityDate: "2024-04-03",
    devtools: { enabled: true },

    modules: ["@nuxtjs/tailwindcss", "@pinia/nuxt"],

    css: ["~/assets/css/main.css"],

    runtimeConfig: {
        // Private keys (only available on server-side)
        databaseUrl: process.env.DATABASE_URL,
        databaseHost: process.env.DATABASE_HOST || "localhost",
        databasePort: parseInt(process.env.DATABASE_PORT || "5432"),
        databaseName: process.env.DATABASE_NAME || "rdm_platform",
        databaseUser: process.env.DATABASE_USER || "rdm_user",
        databasePassword: process.env.DATABASE_PASSWORD || "rdm_password",
        jwtSecret: process.env.JWT_SECRET || "your-secret-key",
        jwtExpiresIn: process.env.JWT_EXPIRES_IN || "7d",
        sessionSecret: process.env.SESSION_SECRET || "your-session-secret",
        sessionTimeout: parseInt(process.env.SESSION_TIMEOUT || "1800000"),

        // Public keys (exposed to client-side)
        public: {
            appUrl: process.env.APP_URL || "http://localhost:3000",
            apiUrl: process.env.API_URL || "http://localhost:8080/api",
            guacamoleUrl:
                process.env.GUACAMOLE_URL || "http://localhost:8080/guacamole",
        },
    },

    server: {
        port: parseInt(process.env.NUXT_PORT || "3000"),
        host: process.env.NUXT_HOST || "0.0.0.0",
    },
});
