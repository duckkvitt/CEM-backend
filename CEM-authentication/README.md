# CEM Authentication Service

A Spring Boot microservice that provides authentication and user management functionality for the Construction Equipment Management (CEM) system.

## Features

- ✅ JWT-based authentication
- ✅ Role-based access control (RBAC)
- ✅ Admin user creation with email notifications
- ✅ Async email processing
- ✅ Account locking after failed attempts
- ✅ Secure password generation
- ✅ Database migrations with Flyway
- ✅ Comprehensive API documentation with Swagger
- ✅ CORS configuration
- ✅ Security headers and best practices

## Tech Stack

- **Java 17**
- **Spring Boot 3.5.0**
- **Spring Security 6**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway** (Database Migration)
- **JWT** (JSON Web Tokens)
- **Spring Mail** (Email Service)
- **Swagger/OpenAPI 3** (API Documentation)
- **Maven** (Build Tool)

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- SMTP server for email functionality

### 1. Clone and Navigate

```bash
git clone <repository-url>
cd CEM-authentication
```

### 2. Configure Database

Update `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your_database
    username: your_username
    password: your_password
```

### 3. Configure Email (Optional)

Update email settings in `application.yml`:

```yaml
spring:
  mail:
    host: smtp.your-provider.com
    port: 587
    username: your-email@domain.com
    password: your-app-password
```

### 4. Build and Run

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

The service will start on `http://localhost:8081/api/auth`

### 5. Verify Installation

- **Health Check:** `GET http://localhost:8081/api/auth/actuator/health`
- **Swagger UI:** `http://localhost:8081/api/auth/swagger-ui.html`
- **API Docs:** `http://localhost:8081/api/auth/api-docs`

## Default Admin Account

A default super admin account is automatically created:

- **Email:** `admin@cem.com`
- **Password:** `AdminCEM@2024`
- **Role:** SUPER_ADMIN

⚠️ **Security Warning:** Change this password immediately in production!

## API Usage

### 1. Login

```bash
curl -X POST http://localhost:8081/api/auth/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@cem.com",
    "password": "AdminCEM@2024"
  }'
```

### 2. Create User (Admin Only)

```bash
curl -X POST http://localhost:8081/api/auth/v1/auth/admin/create-user \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "email": "newuser@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+84123456789",
    "roleId": 1,
    "emailVerified": true
  }'
```

### 3. Get Available Roles

```bash
curl -X GET http://localhost:8081/api/auth/v1/auth/admin/roles \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Configuration

### Environment Variables

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/cem_db
DATABASE_USERNAME=cem_user
DATABASE_PASSWORD=cem_password

# JWT
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://your-frontend.com
```

### Application Profiles

- **Development:** `application.yml`
- **Production:** `application-prod.yml`
- **Testing:** `application-test.yml`

```bash
# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Database Schema

The service uses Flyway for database migrations. Schema is automatically created on startup.

### Tables Created:

- `roles` - User roles (USER, ADMIN, MODERATOR, SUPER_ADMIN)
- `users` - User accounts with authentication details
- `flyway_schema_history` - Migration history

### Migration Files:

- `V001__Create_initial_schema.sql` - Creates tables and indexes
- `V002__Insert_default_data.sql` - Inserts default roles and admin user

## Security Features

### Authentication & Authorization

- JWT tokens with configurable expiration
- Role-based access control
- Secure password hashing (BCrypt)
- Account locking after failed attempts

### Email Security

- Secure temporary password generation
- Async email processing (non-blocking)
- Email templates with security notices

### Security Headers

- CORS configuration
- Content Security Policy ready
- Secure cookie settings

## Monitoring & Observability

### Health Checks

```bash
# Service health
GET /actuator/health

# Database connectivity
GET /actuator/health/db

# Email service status
GET /actuator/health/mail
```

### Metrics

```bash
# Application metrics
GET /actuator/metrics

# Prometheus metrics
GET /actuator/prometheus
```

### Logging

- Structured logging with SLF4J/Logback
- Configurable log levels
- File and console appenders
- Request/response logging

## Development

### Project Structure

```
src/main/java/com/g47/cem/cemauthentication/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA entities
├── exception/      # Custom exceptions
├── repository/     # Data access layer
├── service/        # Business logic
└── util/           # Utility classes

src/main/resources/
├── db/migration/   # Flyway migration scripts
├── application.yml # Configuration file
└── static/        # Static resources
```

### Building

```bash
# Clean build
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Skip tests (for faster builds)
mvn package -DskipTests
```

### Docker Support

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/CEM-authentication-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Build Docker image
docker build -t cem-auth:latest .

# Run container
docker run -p 8081:8081 cem-auth:latest
```

## Production Deployment

### 1. Environment Setup

```bash
# Set production profile
export SPRING_PROFILES_ACTIVE=prod

# Set database connection
export DATABASE_URL=jdbc:postgresql://prod-db:5432/cem_prod
export DATABASE_USERNAME=cem_prod_user
export DATABASE_PASSWORD=your-secure-password

# Set JWT secret (256-bit)
export JWT_SECRET=your-super-secure-256-bit-secret-key

# Set email configuration
export MAIL_HOST=smtp.your-provider.com
export MAIL_USERNAME=noreply@your-domain.com
export MAIL_PASSWORD=your-secure-app-password

# Set CORS origins
export CORS_ALLOWED_ORIGINS=https://your-frontend.com
```

### 2. Database Migration

```bash
# Run Flyway migration manually if needed
mvn flyway:migrate
```

### 3. Build Production JAR

```bash
mvn clean package -DskipTests -Pprod
```

### 4. Run Application

```bash
java -jar target/CEM-authentication-*.jar \
  --spring.profiles.active=prod \
  --server.port=8081
```

### 5. Health Check

```bash
curl http://your-domain:8081/api/auth/actuator/health
```

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   ```bash
   # Check database connectivity
   pg_isready -h localhost -p 5432 -U your_username
   ```

2. **Email Service Not Working**
   ```bash
   # Check SMTP settings in application.yml
   # Verify app password for Gmail
   # Check firewall settings
   ```

3. **JWT Token Invalid**
   ```bash
   # Verify JWT_SECRET is properly set
   # Check token expiration
   # Validate token format
   ```

4. **Migration Errors**
   ```bash
   # Check Flyway history
   mvn flyway:info
   
   # Repair if needed
   mvn flyway:repair
   ```

### Logs

```bash
# View application logs
tail -f logs/auth-service.log

# Increase log level for debugging
export LOGGING_LEVEL_COM_G47_CEM=DEBUG
```

## API Documentation

Comprehensive API documentation is available at:
- [API Documentation](./API_DOCUMENTATION.md)
- [Swagger UI](http://localhost:8081/api/auth/swagger-ui.html)

## Support

For issues and questions:
- Create an issue in the repository
- Contact: dev@cem.com
- Documentation: [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details. 