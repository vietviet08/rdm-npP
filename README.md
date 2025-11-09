# Remote Desktop Management Platform

Nền tảng quản lý Remote Desktop tập trung, hỗ trợ RDP, VNC, và SSH thông qua trình duyệt web.

## 🚀 Features

-   **Đa giao thức**: Hỗ trợ RDP, VNC, SSH
-   **Quản lý tập trung**: Quản lý thiết bị và người dùng từ một giao diện
-   **Phân quyền RBAC**: Admin, Operator, Viewer
-   **Không cần client**: Truy cập hoàn toàn qua trình duyệt web
-   **Bảo mật**: Mã hóa kết nối, audit logs, session management

## 📋 Prerequisites

-   Docker và Docker Compose
-   Node.js 18+ và npm
-   Git

## 🛠️ Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd project
```

### 2. Environment Configuration

Copy `.env.example` to `.env` và cấu hình các biến môi trường:

```bash
cp .env.example .env
# Edit .env với các giá trị phù hợp
```

### 3. Start Services với Docker Compose

```bash
docker-compose up -d
```

Services sẽ được khởi động:

-   PostgreSQL: `localhost:5432`
-   Spring Boot API: `localhost:8080`
-   Apache Guacamole: `localhost:8081`
-   guacd: `localhost:4822`
-   Nginx: `localhost:80`

### 4. Initialize Database

Database schema sẽ được tự động tạo khi PostgreSQL container khởi động lần đầu. Migration scripts trong `database/migrations/` sẽ được chạy tự động.

### 5. Seed Admin User

```bash
node database/seeds/seed-admin.js
```

Default credentials:

-   Username: `admin`
-   Password: `admin123`

### 6. Setup Nuxt Application

```bash
cd nuxt-dashboard
npm install
npm run dev
```

Nuxt application sẽ chạy tại `http://localhost:3000`

### 7. Access Applications

-   Nuxt Frontend: http://localhost:3000
-   Spring Boot API: http://localhost:8080/api
-   Swagger UI: http://localhost:8080/swagger-ui.html
-   Guacamole: http://localhost:8081/guacamole
-   Nginx (proxied): http://localhost

## 📁 Project Structure

```
project/
├── docker-compose.yml          # Docker Compose configuration
├── .env.example                # Environment variables template
├── database/
│   ├── migrations/             # Database migration scripts
│   └── seeds/                  # Database seed scripts
├── docker/
│   ├── guacamole/              # Guacamole configuration
│   └── nginx/                  # Nginx configuration
├── spring-boot-api/            # Spring Boot API
│   ├── src/main/java/com/rdm/
│   │   ├── controller/         # REST controllers
│   │   ├── service/            # Business logic
│   │   ├── repository/         # Data access
│   │   ├── model/              # Entity models
│   │   ├── dto/                # Data transfer objects
│   │   ├── security/           # Security configuration
│   │   └── exception/          # Exception handling
│   └── src/main/resources/
│       └── application.yml     # Application configuration
├── nuxt-dashboard/             # Nuxt.js application
│   ├── composables/            # Composables (useApi, useAuth)
│   ├── stores/                 # Pinia stores
│   ├── middleware/             # Route middleware
│   ├── pages/                  # Page routes
│   ├── components/             # Vue components
│   └── types/                  # TypeScript types
└── docs/                       # Documentation
```

## 🗄️ Database

Database schema được quản lý thông qua migration scripts trong `database/migrations/`.

### Schemas

-   **app**: Application data (users, devices, permissions, logs)
-   **guacamole**: Guacamole connections và configuration (tự động tạo bởi Guacamole)

### Default Admin User

Sau khi database được khởi tạo, chạy seed script để tạo admin user:

```bash
# Từ thư mục root của project
node database/seeds/seed-admin.js
```

Default credentials:

-   Username: `admin`
-   Password: `admin123` (thay đổi ngay sau lần đăng nhập đầu tiên!)

**Lưu ý**: Script này sẽ hash password đúng cách sử dụng bcrypt.

## 🚦 Development

### Running Development Server

```bash
cd nuxt-dashboard
npm run dev
```

### Building for Production

```bash
cd nuxt-dashboard
npm run build
npm run preview
```

### Database Migrations

Migration scripts trong `database/migrations/` sẽ được chạy tự động khi PostgreSQL container khởi động lần đầu.

Để chạy seed script tạo admin user:

```bash
# Sau khi services đã chạy
node database/seeds/seed-admin.js
```

### Spring Boot API

Spring Boot API sẽ tự động start khi Docker Compose chạy. Để build và chạy manually:

```bash
cd spring-boot-api
mvn clean package
mvn spring-boot:run
```

API documentation: http://localhost:8080/swagger-ui.html

## 📚 Documentation

Xem `docs/SETUP.md` để biết thêm chi tiết về setup và troubleshooting.

## 🔒 Security

-   **Đổi mật khẩu mặc định**: Thay đổi mật khẩu admin ngay sau khi setup
-   **Environment variables**: Không commit file `.env` vào git
-   **JWT Secret**: Sử dụng secret key mạnh trong production
-   **Database credentials**: Sử dụng credentials mạnh trong production

## 🐛 Troubleshooting

### Services không khởi động

Kiểm tra logs:

```bash
docker-compose logs -f
```

### Database connection errors

Kiểm tra:

1. PostgreSQL container đang chạy: `docker-compose ps`
2. Database credentials trong `.env`
3. Network connectivity giữa containers

### Guacamole không kết nối được

Kiểm tra:

1. guacd container đang chạy
2. Guacamole configuration trong `docker/guacamole/guacamole.properties`
3. Database connection từ Guacamole

## 📝 License

[License information]

## 👥 Contributors

[Contributors information]
