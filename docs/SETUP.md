# Setup Guide

Hướng dẫn chi tiết để setup và chạy Remote Desktop Management Platform.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Initial Setup](#initial-setup)
3. [Database Setup](#database-setup)
4. [Guacamole Configuration](#guacamole-configuration)
5. [Nuxt Application Setup](#nuxt-application-setup)
6. [Verification](#verification)
7. [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Software

- **Docker**: Version 20.10+
- **Docker Compose**: Version 2.0+
- **Node.js**: Version 18.0+
- **npm**: Version 9.0+

### System Requirements

- **RAM**: Tối thiểu 4GB (khuyến nghị 8GB)
- **Disk**: Tối thiểu 10GB free space
- **OS**: Linux, macOS, hoặc Windows với WSL2

### Verify Installation

```bash
docker --version
docker-compose --version
node --version
npm --version
```

## Initial Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd project
```

### 2. Environment Configuration

Tạo file `.env` từ template:

```bash
cp .env.example .env
```

Chỉnh sửa `.env` với các giá trị phù hợp:

```env
# PostgreSQL Database Configuration
POSTGRES_DB=rdm_platform
POSTGRES_USER=rdm_user
POSTGRES_PASSWORD=rdm_password  # Thay đổi trong production!

# Nuxt Application Configuration
NUXT_PORT=3000
NUXT_HOST=0.0.0.0
NODE_ENV=development

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRES_IN=7d

# Database Connection
DATABASE_URL=postgresql://rdm_user:rdm_password@postgres:5432/rdm_platform
DATABASE_HOST=postgres
DATABASE_PORT=5432
DATABASE_NAME=rdm_platform
DATABASE_USER=rdm_user
DATABASE_PASSWORD=rdm_password

# Guacamole Configuration
GUACAMOLE_URL=http://localhost:8080/guacamole
GUACAMOLE_API_URL=http://guacamole:8080/guacamole

# Session Configuration
SESSION_SECRET=your-session-secret-change-this-in-production
SESSION_TIMEOUT=1800000

# Application URLs
APP_URL=http://localhost:3000
API_URL=http://localhost:3000/api
```

### 3. Start Docker Services

Khởi động tất cả services:

```bash
docker-compose up -d
```

Kiểm tra status:

```bash
docker-compose ps
```

Xem logs:

```bash
docker-compose logs -f
```

## Database Setup

### Automatic Setup

Database schema sẽ được tự động tạo khi PostgreSQL container khởi động lần đầu. Migration scripts trong `database/migrations/` sẽ được chạy tự động.

### Manual Setup

Nếu cần chạy migrations thủ công:

```bash
# Connect to PostgreSQL container
docker-compose exec postgres psql -U rdm_user -d rdm_platform

# Run migration
\i /docker-entrypoint-initdb.d/001_initial_schema.sql
```

### Verify Database Setup

Kiểm tra schemas đã được tạo:

```bash
docker-compose exec postgres psql -U rdm_user -d rdm_platform -c "\dn"
```

Kiểm tra tables:

```bash
docker-compose exec postgres psql -U rdm_user -d rdm_platform -c "\dt app.*"
```

### Seed Data

Admin user mặc định sẽ được tạo tự động. Để tạo thủ công:

```bash
docker-compose exec postgres psql -U rdm_user -d rdm_platform -f /docker-entrypoint-initdb.d/../seeds/001_admin_user.sql
```

## Guacamole Configuration

### Initial Setup

Guacamole sẽ tự động khởi tạo database schema khi khởi động lần đầu. Kiểm tra logs:

```bash
docker-compose logs guacamole
```

### Access Guacamole

Guacamole web interface sẽ có sẵn tại:
- Direct: `http://localhost:8080/guacamole`
- Through Nginx: `http://localhost/guacamole/`

### Default Credentials

Guacamole sử dụng database authentication. Bạn cần tạo user trong Guacamole hoặc sử dụng API để quản lý connections.

## Nuxt Application Setup

### 1. Install Dependencies

```bash
cd nuxt-dashboard
npm install
```

### 2. Environment Configuration

Đảm bảo file `.env` trong thư mục root đã được cấu hình đúng. Nuxt sẽ đọc các biến môi trường từ file này.

### 3. Run Development Server

```bash
npm run dev
```

Application sẽ chạy tại `http://localhost:3000`

### 4. Verify Database Connection

Kiểm tra database connection bằng cách tạo một API endpoint test:

```bash
curl http://localhost:3000/api/health
```

## Verification

### 1. Check Services

Tất cả services phải đang chạy:

```bash
docker-compose ps
```

Expected output:
```
NAME                STATUS          PORTS
rdm-postgres        Up (healthy)    0.0.0.0:5432->5432/tcp
rdm-guacd           Up              0.0.0.0:4822->4822/tcp
rdm-guacamole       Up (healthy)    0.0.0.0:8080->8080/tcp
rdm-nginx           Up              0.0.0.0:80->80/tcp
```

### 2. Check Database

```bash
docker-compose exec postgres psql -U rdm_user -d rdm_platform -c "SELECT COUNT(*) FROM app.users;"
```

### 3. Check Guacamole

Truy cập `http://localhost:8080/guacamole` và kiểm tra interface.

### 4. Check Nuxt Application

Truy cập `http://localhost:3000` và kiểm tra application.

## Troubleshooting

### PostgreSQL Issues

**Problem**: PostgreSQL container không khởi động

**Solution**:
```bash
# Check logs
docker-compose logs postgres

# Check if port is already in use
netstat -tulpn | grep 5432

# Remove and recreate container
docker-compose down
docker-compose up -d postgres
```

**Problem**: Database connection errors

**Solution**:
1. Kiểm tra credentials trong `.env`
2. Kiểm tra network connectivity:
   ```bash
   docker-compose exec nuxt-dashboard ping postgres
   ```
3. Kiểm tra database đã được tạo:
   ```bash
   docker-compose exec postgres psql -U rdm_user -l
   ```

### Guacamole Issues

**Problem**: Guacamole không kết nối được đến database

**Solution**:
1. Kiểm tra Guacamole logs:
   ```bash
   docker-compose logs guacamole
   ```
2. Kiểm tra database credentials trong `docker/guacamole/guacamole.properties`
3. Kiểm tra guacd đang chạy:
   ```bash
   docker-compose ps guacd
   ```

**Problem**: Guacamole không khởi tạo schema

**Solution**:
1. Xóa volumes và recreate:
   ```bash
   docker-compose down -v
   docker-compose up -d
   ```
2. Kiểm tra Guacamole có quyền tạo schema:
   ```bash
   docker-compose exec postgres psql -U rdm_user -d rdm_platform -c "CREATE SCHEMA IF NOT EXISTS guacamole;"
   ```

### Nuxt Application Issues

**Problem**: Nuxt không kết nối được đến database

**Solution**:
1. Kiểm tra database connection string trong `.env`
2. Kiểm tra Nuxt có thể resolve hostname `postgres`:
   - Trong Docker: sử dụng service name `postgres`
   - Local development: sử dụng `localhost` hoặc `127.0.0.1`
3. Kiểm tra database pool connection:
   ```bash
   docker-compose exec postgres psql -U rdm_user -d rdm_platform -c "SELECT count(*) FROM pg_stat_activity;"
   ```

**Problem**: Port 3000 đã được sử dụng

**Solution**:
1. Thay đổi port trong `.env`:
   ```env
   NUXT_PORT=3001
   ```
2. Hoặc kill process đang sử dụng port:
   ```bash
   lsof -ti:3000 | xargs kill -9
   ```

### Network Issues

**Problem**: Containers không thể giao tiếp với nhau

**Solution**:
1. Kiểm tra network:
   ```bash
   docker network ls
   docker network inspect project_rdm-network
   ```
2. Recreate network:
   ```bash
   docker-compose down
   docker-compose up -d
   ```

## Next Steps

Sau khi setup thành công, bạn có thể:

1. **Đăng nhập**: Sử dụng admin credentials để đăng nhập
2. **Tạo devices**: Thêm thiết bị remote vào hệ thống
3. **Quản lý users**: Tạo và quản lý người dùng
4. **Kết nối**: Kết nối đến thiết bị thông qua Guacamole

Xem [Phase 2 Implementation](../README.md#phase-2) để biết thêm về các tính năng tiếp theo.

## Additional Resources

- [Nuxt.js Documentation](https://nuxt.com/docs)
- [Apache Guacamole Documentation](https://guacamole.apache.org/doc/gug/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

