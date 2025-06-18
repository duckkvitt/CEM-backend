# CEM Customer Management Service

Customer Experience Management - Customer Service API

## Overview

This service provides comprehensive customer management functionality including creating, retrieving, searching, and managing customer data with advanced features like tagging and visibility controls.

## Features

- **Customer CRUD Operations**: Create, Read, Update customer information
- **Advanced Search**: Search customers by name, email, phone, and tags
- **Visibility Management**: Hide/show customers
- **Tagging System**: Organize customers with custom tags
- **Pagination & Sorting**: Efficient data retrieval
- **JWT Authentication**: Secure API access
- **OpenAPI Documentation**: Complete API documentation

## Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL database
- CEM Authentication Service running (for JWT validation)

## Database Configuration

The service uses PostgreSQL with automatic schema migration via Flyway. Update `application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your_database
    username: your_username
    password: your_password
```

## Quick Start

1. **Clone and build the project**:
```bash
git clone <repository-url>
cd CEM-Customer
mvn clean install
```

2. **Run the application**:
```bash
mvn spring-boot:run
```

3. **Access the API documentation**:
   - Swagger UI: http://localhost:8082/api/customer/swagger-ui.html
   - OpenAPI JSON: http://localhost:8082/api/customer/api-docs

## API Endpoints

### Authentication
All endpoints require JWT Bearer token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### Customer Management

#### Create Customer
```http
POST /api/customer/v1/customers
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "address": "123 Main St, City, Country",
  "tags": ["vip", "premium"],
  "isHidden": false
}
```

#### Get Customer by ID
```http
GET /api/customer/v1/customers/{id}
```

#### Get Customer by Email
```http
GET /api/customer/v1/customers/email/{email}
```

#### List All Customers (with pagination and filtering)
```http
GET /api/customer/v1/customers?page=0&size=20&sortBy=createdAt&sortDir=desc&name=John&email=john&isHidden=false
```

#### Get Visible Customers Only
```http
GET /api/customer/v1/customers/visible?page=0&size=20
```

#### Get Hidden Customers Only
```http
GET /api/customer/v1/customers/hidden?page=0&size=20
```

#### Hide Customer
```http
PUT /api/customer/v1/customers/{id}/hide
```

#### Show Customer (Restore from Hidden)
```http
PUT /api/customer/v1/customers/{id}/show
```

#### Get Customers by Tag
```http
GET /api/customer/v1/customers/tag/{tagName}
```

## Request/Response Examples

### Create Customer Request
```json
{
  "name": "Jane Smith",
  "email": "jane.smith@example.com",
  "phone": "+1987654321",
  "address": "456 Oak Avenue, Downtown, City",
  "tags": ["new", "potential"],
  "isHidden": false
}
```

### Customer Response
```json
{
  "success": true,
  "message": "Customer created successfully",
  "data": {
    "id": 1,
    "name": "Jane Smith",
    "email": "jane.smith@example.com",
    "phone": "+1987654321",
    "address": "456 Oak Avenue, Downtown, City",
    "tags": ["new", "potential"],
    "isHidden": false,
    "createdBy": "admin@example.com",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/customer/v1/customers",
  "status": 201
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "content": [...],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true,
        "direction": "DESC",
        "property": "createdAt"
      }
    },
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## Customer Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Customer full name (1-255 chars) |
| email | String | Yes | Customer email (unique, valid email format) |
| phone | String | No | Customer phone number (10-20 digits) |
| address | String | No | Customer address (max 1000 chars) |
| tags | Array[String] | No | Custom tags for categorization |
| isHidden | Boolean | No | Visibility status (default: false) |

## Search and Filtering

The API supports advanced filtering:

- **Name**: Partial match (case-insensitive)
- **Email**: Partial match (case-insensitive)
- **Phone**: Partial match
- **isHidden**: Exact match (true/false)
- **Tags**: Exact tag match

Example:
```http
GET /api/customer/v1/customers?name=john&email=gmail&isHidden=false&page=0&size=10
```

## Sorting

Available sort fields:
- `createdAt` (default)
- `updatedAt`
- `name`
- `email`

Sort direction: `asc` or `desc` (default: `desc`)

## Error Handling

The API returns standardized error responses:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Invalid email format",
    "name": "Name is required"
  },
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/customer/v1/customers",
  "status": 400
}
```

Common HTTP status codes:
- `200`: Success
- `201`: Created
- `400`: Bad Request (validation errors)
- `401`: Unauthorized (invalid/missing JWT)
- `403`: Forbidden (insufficient permissions)
- `404`: Not Found
- `409`: Conflict (duplicate email)
- `500`: Internal Server Error

## Security

- JWT token validation
- CORS configuration
- Input validation and sanitization
- SQL injection prevention
- Rate limiting (can be configured)

## Monitoring and Health

- Health check: `GET /api/customer/actuator/health`
- Metrics: `GET /api/customer/actuator/metrics`
- Application info: `GET /api/customer/actuator/info`

## Configuration

Key configuration properties in `application.yml`:

```yaml
server:
  port: 8082
  servlet:
    context-path: /api/customer

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/CEM_DB
    username: your_username
    password: your_password
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

jwt:
  secret: your-jwt-secret
  expiration: 86400000

app:
  cors:
    allowedOrigins: "http://localhost:3000,http://localhost:3001"
  security:
    permitAll: "/api-docs/**,/swagger-ui/**,/actuator/health"
```

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package -Pprod
```

### Docker Support
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/CEM-Customer-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Production Deployment

1. **Database Setup**: Ensure PostgreSQL is running and accessible
2. **Environment Variables**: Set production database credentials
3. **JWT Secret**: Use a strong, unique JWT secret in production
4. **CORS**: Configure appropriate CORS origins
5. **Logging**: Set appropriate log levels
6. **Health Checks**: Monitor application health endpoints

## Support

For technical support or questions, please contact the CEM development team.

## Version

Current version: 1.0.0

## License

This project is licensed under the Apache License 2.0. 