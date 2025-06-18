# CEM Customer API Test Collection

This file contains practical examples for testing the Customer Management API endpoints.

## Prerequisites

1. **Get JWT Token**: First, get a valid JWT token from the Authentication service:

```bash
# Login to get JWT token
curl -X POST http://localhost:8081/api/auth/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@cem.com",
    "password": "AdminCEM@2024"
  }'
```

Save the `accessToken` from the response and use it in subsequent requests.

## API Test Examples

### 1. Create Customer

```bash
curl -X POST http://localhost:8082/api/customer/v1/customers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "address": "123 Main Street, New York, NY 10001",
    "tags": ["vip", "premium", "enterprise"],
    "isHidden": false
  }'
```

### 2. Create Another Customer for Testing

```bash
curl -X POST http://localhost:8082/api/customer/v1/customers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Smith",
    "email": "jane.smith@company.com",
    "phone": "+1987654321",
    "address": "456 Oak Avenue, Los Angeles, CA 90210",
    "tags": ["new", "potential"],
    "isHidden": false
  }'
```

### 3. Get Customer by ID

```bash
curl -X GET http://localhost:8082/api/customer/v1/customers/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Get Customer by Email

```bash
curl -X GET http://localhost:8082/api/customer/v1/customers/email/john.doe@example.com \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Get All Customers (Default Pagination)

```bash
curl -X GET http://localhost:8082/api/customer/v1/customers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 6. Get Customers with Pagination and Sorting

```bash
curl -X GET "http://localhost:8082/api/customer/v1/customers?page=0&size=10&sortBy=name&sortDir=asc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 7. Search Customers by Name

```bash
curl -X GET "http://localhost:8082/api/customer/v1/customers?name=John" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 8. Search Customers by Email Pattern

```bash
curl -X GET "http://localhost:8082/api/customer/v1/customers?email=example.com" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 9. Search Customers with Multiple Filters

```bash
curl -X GET "http://localhost:8082/api/customer/v1/customers?name=John&email=example&isHidden=false" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 10. Get Only Visible Customers

```bash
curl -X GET http://localhost:8082/api/customer/v1/customers/visible \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 11. Hide a Customer

```bash
curl -X PUT http://localhost:8082/api/customer/v1/customers/1/hide \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 12. Get Hidden Customers

```bash
curl -X GET http://localhost:8082/api/customer/v1/customers/hidden \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 13. Show (Restore) a Hidden Customer

```bash
curl -X PUT http://localhost:8082/api/customer/v1/customers/1/show \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 14. Get Customers by Tag

```bash
curl -X GET http://localhost:8082/api/customer/v1/customers/tag/vip \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 15. Test Duplicate Email (Should Fail)

```bash
curl -X POST http://localhost:8082/api/customer/v1/customers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Duplicate Email Test",
    "email": "john.doe@example.com",
    "phone": "+1111111111",
    "address": "Test Address",
    "tags": ["test"],
    "isHidden": false
  }'
```

## Advanced Test Scenarios

### Large Dataset Test

Create multiple customers for pagination testing:

```bash
# Create 25 customers for pagination testing
for i in {1..25}; do
  curl -X POST http://localhost:8082/api/customer/v1/customers \
    -H "Authorization: Bearer YOUR_JWT_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"Customer $i\",
      \"email\": \"customer$i@test.com\",
      \"phone\": \"+123456789$i\",
      \"address\": \"$i Test Street, Test City\",
      \"tags\": [\"test\", \"batch$((i%5))\"],
      \"isHidden\": $((i%4==0 ? true : false))
    }"
  sleep 0.1
done
```

### Pagination Testing

```bash
# Test first page
curl -X GET "http://localhost:8082/api/customer/v1/customers?page=0&size=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Test second page
curl -X GET "http://localhost:8082/api/customer/v1/customers?page=1&size=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Test last page
curl -X GET "http://localhost:8082/api/customer/v1/customers?page=5&size=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Validation Testing

Test invalid requests:

```bash
# Missing required field (name)
curl -X POST http://localhost:8082/api/customer/v1/customers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "phone": "+1234567890"
  }'

# Invalid email format
curl -X POST http://localhost:8082/api/customer/v1/customers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "invalid-email",
    "phone": "+1234567890"
  }'

# Invalid phone format
curl -X POST http://localhost:8082/api/customer/v1/customers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "phone": "invalid-phone"
  }'
```

## Health Check and API Documentation

### Health Check

```bash
curl -X GET http://localhost:8082/api/customer/actuator/health
```

### API Documentation

```bash
# Get OpenAPI JSON
curl -X GET http://localhost:8082/api/customer/api-docs

# Access Swagger UI in browser
# http://localhost:8082/api/customer/swagger-ui.html
```

## Expected Response Formats

### Successful Create Response

```json
{
  "success": true,
  "message": "Customer created successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "address": "123 Main Street, New York, NY 10001",
    "tags": ["vip", "premium", "enterprise"],
    "isHidden": false,
    "createdBy": "admin@cem.com",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/customer/v1/customers",
  "status": 201
}
```

### Error Response Example

```json
{
  "success": false,
  "message": "Customer with email john.doe@example.com already exists",
  "timestamp": "2024-01-15T10:35:00",
  "path": "/api/customer/v1/customers",
  "status": 409
}
```

### Validation Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "name": "Name is required",
    "email": "Invalid email format"
  },
  "timestamp": "2024-01-15T10:35:00",
  "path": "/api/customer/v1/customers",
  "status": 400
}
```

## Performance Testing

For load testing, you can use tools like Apache Bench or curl in loops:

```bash
# Simple performance test (create 100 customers)
for i in {1..100}; do
  curl -X POST http://localhost:8082/api/customer/v1/customers \
    -H "Authorization: Bearer YOUR_JWT_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"Perf Test $i\",\"email\":\"perf$i@test.com\"}" &
done
wait
```

## Notes

1. Replace `YOUR_JWT_TOKEN` with actual JWT token from authentication service
2. Adjust the base URL if running on different host/port
3. For production testing, use appropriate test data
4. Some operations may require specific roles/permissions
5. Monitor application logs for detailed error information

## Troubleshooting

- **401 Unauthorized**: Check if JWT token is valid and not expired
- **403 Forbidden**: Check if user has required permissions
- **409 Conflict**: Email already exists, use different email
- **500 Internal Server Error**: Check application logs and database connectivity 