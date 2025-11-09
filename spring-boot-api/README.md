# RDM Platform API

Spring Boot REST API cho Remote Desktop Management Platform.

## Features

- **Authentication**: JWT-based authentication
- **Device Management**: CRUD operations với RBAC
- **User Management**: CRUD operations với role management
- **RBAC**: Role-based access control (Admin, Operator, Viewer)
- **Audit Logging**: Log tất cả user actions
- **API Documentation**: Swagger/OpenAPI

## Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 15+

## Configuration

Cấu hình trong `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rdm_platform
    username: rdm_user
    password: rdm_password
  jpa:
    properties:
      hibernate:
        default_schema: app
  security:
    jwt:
      secret: your-secret-key
      expiration: 604800000

server:
  port: 8080
```

## Building

```bash
mvn clean package
```

## Running

```bash
mvn spring-boot:run
```

Hoặc với Docker:

```bash
docker-compose up spring-boot-api
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout
- `GET /api/auth/me` - Get current user

### Devices (Admin only)
- `GET /api/devices` - List devices
- `GET /api/devices/{id}` - Get device
- `POST /api/devices` - Create device
- `PUT /api/devices/{id}` - Update device
- `DELETE /api/devices/{id}` - Delete device

### Users (Admin only)
- `GET /api/users` - List users
- `GET /api/users/{id}` - Get user
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `PUT /api/users/{id}/role` - Update user role

## API Documentation

Swagger UI available tại: http://localhost:8080/swagger-ui.html

## Health Check

Health endpoint: http://localhost:8080/actuator/health

## Testing

```bash
mvn test
```

## Security

- JWT tokens expire sau 7 days (configurable)
- Passwords được hash bằng BCrypt
- CORS configured cho development
- RBAC enforcement với @PreAuthorize

