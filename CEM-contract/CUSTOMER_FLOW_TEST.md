# Customer Account Creation and Contract Signing Flow Test Guide

## Overview
This document describes the complete flow for testing customer account creation after manager signs a contract, and subsequent customer login and contract signing.

## Prerequisites
1. All services running (CEM-authentication, CEM-Customer, CEM-contract)
2. Database with proper roles seeded
3. A contract with a customer that has a valid email address

## Flow Steps

### 1. Verify Roles Are Properly Seeded

**Endpoint:** `GET /v1/auth/admin/roles`
**Headers:** `Authorization: Bearer <MANAGER_TOKEN>`

**Expected Response:** Should include CUSTOMER role:
```json
{
  "data": [
    {"id": 1, "name": "USER", "description": "Standard user with basic access"},
    {"id": 2, "name": "ADMIN", "description": "Administrator with full access"},
    {"id": 3, "name": "STAFF", "description": "Staff member who works directly with customers"},
    {"id": 4, "name": "MANAGER", "description": "Manager with permission to oversee operations and make strategic decisions"},
    {"id": 5, "name": "CUSTOMER", "description": "Customer with access to view and sign contracts"},
    ...
  ]
}
```

### 2. Create a Contract with Valid Customer

**Endpoint:** `POST /v1/contracts`
**Headers:** `Authorization: Bearer <MANAGER_TOKEN>`

**Request Body:**
```json
{
  "title": "Test Contract for Customer Flow",
  "description": "Testing customer account creation",
  "customerId": 1, // Ensure this customer exists and has email
  "totalValue": 1000000,
  "startDate": "2025-01-15",
  "endDate": "2025-12-31",
  "paymentMethod": "BANK_TRANSFER",
  "contractDetails": [
    {
      "description": "Test Product",
      "quantity": 1,
      "unitPrice": 1000000
    }
  ]
}
```

### 3. Verify Customer Has Email

**Endpoint:** `GET /v1/customers/{customerId}`
**Headers:** `Authorization: Bearer <MANAGER_TOKEN>`

**Expected:** Customer must have a valid email address for account creation to work.

### 4. Manager Signs Contract (Triggers Customer Account Creation)

**Endpoint:** `POST /{contractId}/digital-signature`
**Headers:** `Authorization: Bearer <MANAGER_TOKEN>`

**Request Body:**
```json
{
  "signerType": "MANAGER",
  "signerName": "Test Manager",
  "signerEmail": "manager@test.com",
  "signatureData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==",
  "pageNumber": 1,
  "signatureX": 100,
  "signatureY": 100,
  "signatureWidth": 200,
  "signatureHeight": 50,
  "reason": "Manager approval",
  "location": "Head Office"
}
```

**Expected Result:**
- Contract status changes to `PENDING_CUSTOMER_SIGNATURE`
- SellerSignedEvent is fired
- Customer account is created automatically
- Email is sent to customer with login credentials

### 5. Check Logs for Customer Account Creation

Look for these log entries:
```
Successfully created user for contract X: customer@email.com (User ID: Y)
CUSTOMER MAPPING: Customer ID Z -> User ID Y (Email: customer@email.com)
Sent contract signed notification email to customer@email.com for contract CONTRACT_NUMBER
```

### 6. Test Customer Login

**Endpoint:** `POST /v1/auth/login`

**Request Body:**
```json
{
  "email": "customer@email.com", // Customer's email from step 3
  "password": "TEMP_PASSWORD_FROM_EMAIL"
}
```

**Expected Response:**
```json
{
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresIn": 86400000,
    "user": {
      "id": Y,
      "email": "customer@email.com",
      "firstName": "Customer",
      "lastName": "Name",
      "role": {
        "id": 5,
        "name": "CUSTOMER"
      }
    }
  }
}
```

### 7. Customer Views Their Contracts

**Endpoint:** `GET /` (contracts root)
**Headers:** `Authorization: Bearer <CUSTOMER_TOKEN>`

**Expected:** Should return only contracts where customer email matches the authenticated user's email.

### 8. Customer Views Specific Contract

**Endpoint:** `GET /{contractId}`
**Headers:** `Authorization: Bearer <CUSTOMER_TOKEN>`

**Expected:** Should return contract details if customer email matches.

### 9. Customer Signs Contract

**Endpoint:** `POST /{contractId}/digital-signature`
**Headers:** `Authorization: Bearer <CUSTOMER_TOKEN>`

**Request Body:**
```json
{
  "signerType": "CUSTOMER",
  "signerName": "Customer Name",
  "signerEmail": "customer@email.com",
  "signatureData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==",
  "pageNumber": 1,
  "signatureX": 300,
  "signatureY": 200,
  "signatureWidth": 200,
  "signatureHeight": 50,
  "reason": "Customer acceptance",
  "location": "Customer Location"
}
```

**Expected Result:**
- Contract status changes to `ACTIVE` (fully signed)
- Contract becomes legally binding

### 10. Verify Final Contract Status

**Endpoint:** `GET /{contractId}`
**Headers:** `Authorization: Bearer <MANAGER_TOKEN>` or `Authorization: Bearer <CUSTOMER_TOKEN>`

**Expected:** Contract status should be `ACTIVE`.

## Test Helper Endpoints

### Trigger Customer Creation Manually (For Testing)

**Endpoint:** `POST /api/test/signature/test-customer-creation-flow/{contractId}`

This endpoint manually triggers the SellerSignedEvent for testing purposes.

### Check Role by Name

**Endpoint:** `GET /v1/auth/admin/roles/by-name/CUSTOMER`
**Headers:** `Authorization: Bearer <MANAGER_TOKEN>`

Verifies that CUSTOMER role exists and returns its ID.

## Troubleshooting

### Customer Account Not Created
1. Check if CUSTOMER role exists in auth service
2. Verify customer has valid email address
3. Check SellerSignedEvent is fired (logs)
4. Verify MANAGER has permission to create users
5. Check ExternalService can reach auth service

### Customer Cannot See Contracts
1. Verify customer login returns CUSTOMER role
2. Check email matching logic in ContractService
3. Ensure customer email in CEM-Customer matches auth user email

### Customer Cannot Sign Contract
1. Verify CUSTOMER role has permission for digital signature endpoint
2. Check contract is in PENDING_CUSTOMER_SIGNATURE status
3. Ensure customer email matches contract customer email

## Important Notes

1. **Email Matching:** The system links customers to users through email address matching.
2. **Role Security:** CUSTOMER role is automatically assigned when account is created through contract signing flow.
3. **Contract Access:** Customers can only see contracts where their email matches the customer email in the contract.
4. **Signing Permission:** Customers can only sign contracts in PENDING_CUSTOMER_SIGNATURE status.

## Production Readiness Checklist

- [ ] All roles properly seeded in production database
- [ ] Email service configured and working
- [ ] SMTP settings correct in application.yml
- [ ] Customer service accessible from contract service
- [ ] Authentication service accessible from contract service
- [ ] Proper error handling for failed email sends
- [ ] Logging configured for monitoring account creation
- [ ] Security permissions verified for all endpoints 