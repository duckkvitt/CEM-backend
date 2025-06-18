# CEM Authentication Service API Documentation

## Overview
This document describes the API endpoints for the CEM (Construction Equipment Management) Authentication Service.

## Base URL
- Development: `http://localhost:8081/api/auth`
- Production: `https://your-domain.com/api/auth`

## Authentication
Most endpoints require authentication using Bearer tokens. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Endpoints

### 1. User Login
**POST** `/v1/auth/login`

Authenticate user with email and password.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "userpassword"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "fullName": "John Doe",
      "phone": "+84123456789",
      "role": {
        "id": 1,
        "name": "USER",
        "description": "Standard user with basic access"
      },
      "status": "ACTIVE",
      "emailVerified": true,
      "createdBy": "admin@cem.com",
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    }
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/login"
}
```

### 2. Refresh Token
**POST** `/v1/auth/refresh-token`

Generate new access token using refresh token.

**Headers:**
```
Authorization: Bearer <refresh-token>
```

**Response:**
```json
{
  "success": true,
  "message": "Token refresh successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000,
    "user": {
      // User object same as login
    }
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/refresh-token"
}
```

### 3. User Logout
**POST** `/v1/auth/logout`

**Authentication Required:** Yes

Logout current user.

**Response:**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null,
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/logout"
}
```

### 4. Forgot Password
**POST** `/v1/auth/forgot-password`

**Authentication Required:** No

Send password reset email to user. The email will contain a reset token valid for 15 minutes.

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "If an account with this email exists, a password reset email has been sent.",
  "data": null,
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/forgot-password"
}
```

### 5. Reset Password
**POST** `/v1/auth/reset-password`

**Authentication Required:** No

Reset user password using the reset token received via email.

**Request Body:**
```json
{
  "email": "user@example.com",
  "resetToken": "ABC123DE",
  "newPassword": "NewSecurePassword123!"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Password has been reset successfully. You can now login with your new password.",
  "data": null,
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/reset-password"
}
```

### 6. Change Password
**POST** `/v1/auth/change-password`

**Authentication Required:** Yes

Change current user's password. User must provide current password for verification.

**Request Body:**
```json
{
  "currentPassword": "CurrentPassword123!",
  "newPassword": "NewSecurePassword123!"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null,
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/change-password"
}
```

### 7. Get User Profile
**GET** `/v1/auth/profile`

**Authentication Required:** Yes

Get current authenticated user's profile information.

**Response:**
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "fullName": "John Doe",
    "phone": "+84123456789",
    "role": {
      "id": 1,
      "name": "USER",
      "description": "Standard user with basic access"
    },
    "status": "ACTIVE",
    "emailVerified": true,
    "createdBy": "admin@cem.com",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/profile"
}
```

### 8. Update User Profile
**PUT** `/v1/auth/profile`

**Authentication Required:** Yes

Update current authenticated user's profile information. Only firstName, lastName, and phone can be updated.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "phone": "+84987654321"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Smith",
    "fullName": "John Smith",
    "phone": "+84987654321",
    "role": {
      "id": 1,
      "name": "USER",
      "description": "Standard user with basic access"
    },
    "status": "ACTIVE",
    "emailVerified": true,
    "createdBy": "admin@cem.com",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/profile"
}
```

### 9. Create User Account (Admin Only)
**POST** `/v1/auth/admin/create-user`

**Authentication Required:** Yes (ADMIN or SUPER_ADMIN role)

Create a new user account. An email with login credentials will be sent to the user.

**Request Body:**
```json
{
  "email": "newuser@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+84987654321",
  "roleId": 1,
  "emailVerified": true
}
```

**Response:**
```json
{
  "success": true,
  "message": "User account created successfully. Login credentials have been sent to the user's email.",
  "data": {
    "id": 2,
    "email": "newuser@example.com",
    "firstName": "Jane",
    "lastName": "Smith",
    "fullName": "Jane Smith",
    "phone": "+84987654321",
    "role": {
      "id": 1,
      "name": "USER",
      "description": "Standard user with basic access"
    },
    "status": "ACTIVE",
    "emailVerified": true,
    "createdBy": "admin@cem.com",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/admin/create-user"
}
```

### 10. Get All Roles (Admin Only)
**GET** `/v1/auth/admin/roles`

**Authentication Required:** Yes (ADMIN or SUPER_ADMIN role)

Retrieve all available roles for user assignment.

**Response:**
```json
{
  "success": true,
  "message": "Roles retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "USER",
      "description": "Standard user with basic access",
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    },
    {
      "id": 2,
      "name": "ADMIN",
      "description": "Administrator with full access",
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    },
    {
      "id": 3,
      "name": "MODERATOR",
      "description": "Moderator with limited administrative access",
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    },
    {
      "id": 4,
      "name": "SUPER_ADMIN",
      "description": "Super administrator with ultimate access",
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    }
  ],
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/admin/roles"
}
```

## Error Responses

All endpoints return consistent error response format:

### Validation Errors (400 Bad Request)
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Please provide a valid email address",
    "password": "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character"
  },
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/login",
  "status": 400
}
```

### Authentication Errors (401 Unauthorized)
```json
{
  "success": false,
  "message": "Invalid email or password",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/login",
  "status": 401
}
```

### Authorization Errors (403 Forbidden)
```json
{
  "success": false,
  "message": "Account is temporarily locked due to multiple failed login attempts",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/login",
  "status": 403
}
```

### Resource Not Found (404 Not Found)
```json
{
  "success": false,
  "message": "User not found with email: user@example.com",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/forgot-password",
  "status": 404
}
```

### Business Logic Errors (409 Conflict)
```json
{
  "success": false,
  "message": "Email already exists",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/admin/create-user",
  "status": 409
}
```

### Server Errors (500 Internal Server Error)
```json
{
  "success": false,
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/v1/auth/login",
  "status": 500
}
```

## Default Admin Account

For initial setup, a default super admin account is created:

- **Email:** `admin@cem.com`
- **Password:** `AdminCEM@2024`
- **Role:** SUPER_ADMIN

**⚠️ Important:** Change this password immediately after first login in production!

## Email Notifications

When an admin creates a user account, the following email is automatically sent to the new user:

**Subject:** Welcome to CEM - Your Account Has Been Created

**Content:**
```
Dear [First Name] [Last Name],

Welcome to CEM (Construction Equipment Management) System!

Your account has been successfully created by an administrator. Please find your login credentials below:

Email: [email]
Temporary Password: [generated_password]

IMPORTANT SECURITY NOTICE:
- This is a temporary password generated for your account
- Please change your password after your first login for security purposes
- Do not share these credentials with anyone

You can now access the CEM system using these credentials.

If you have any questions or need assistance, please contact your system administrator.

Best regards,
CEM System Team

---
This is an automated message. Please do not reply to this email.
```

## Security Features

1. **JWT Authentication:** Secure token-based authentication
2. **Role-Based Access Control:** Different permission levels
3. **Account Locking:** Automatic account lock after 5 failed login attempts
4. **Secure Password Generation:** Strong temporary passwords for new accounts
5. **Async Email Processing:** Email sending doesn't block API responses
6. **CORS Configuration:** Configurable cross-origin support

## Swagger Documentation

Interactive API documentation is available at:
- Development: `http://localhost:8081/api/auth/swagger-ui.html`
- API Docs: `http://localhost:8081/api/auth/api-docs`

## Rate Limiting

Production deployment should implement rate limiting:
- Login attempts: 5 per minute per IP
- Password reset: 3 per hour per email
- API calls: 100 per minute per authenticated user

## Environment Configuration

### Required Environment Variables
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/cem_auth
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Email
spring.mail.host=${MAIL_HOST}
spring.mail.port=${MAIL_PORT}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## Production Readiness Checklist

- ✅ **Security**: JWT authentication, password hashing, input validation
- ✅ **Error Handling**: Comprehensive exception handling and user-friendly messages
- ✅ **Logging**: Structured logging with appropriate levels
- ✅ **Validation**: Bean validation on all request DTOs
- ✅ **Documentation**: Complete API documentation with examples
- ✅ **Async Processing**: Background email sending with thread pool
- ✅ **Database**: Proper entity relationships and constraints
- ✅ **Testing**: Ready for unit and integration tests
- ⚠️ **Rate Limiting**: Should be implemented at gateway level
- ⚠️ **Monitoring**: Health checks and metrics endpoints available
- ⚠️ **Caching**: Consider Redis for session management in production 