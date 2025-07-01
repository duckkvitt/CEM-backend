# CEM Contract Service

## Overview
The Contract Management Service is part of the Customer & Equipment Management System (CEMS) for Thanh Dat Trading and Manufacturing Co., Ltd. This microservice handles contract creation, management, and tracking operations.

## Service Information
- **Port**: 8084
- **Context Path**: `/api/contract`
- **Database**: PostgreSQL (Neon Cloud)
- **Architecture**: Spring Boot 3.5.0 with Java 17

## Features Implemented
- ✅ Contract creation and management
- ✅ Customer and staff association
- ✅ Contract details (line items) management
- ✅ Contract number auto-generation
- ✅ Pagination and sorting
- ✅ JWT authentication integration
- ✅ Database migration with Flyway
- ✅ REST API with OpenAPI documentation
- ✅ Exception handling and validation

## Database Schema
The service uses 4 main tables:
- `contracts` - Main contract information
- `contract_details` - Line items/services in contracts
- `contract_signatures` - Signature tracking
- `contract_history` - Audit trail

## API Endpoints

### Contract Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/contracts/test` | Test service health |
| POST | `/api/contracts` | Create new contract |
| GET | `/api/contracts/{id}` | Get contract by ID |
| GET | `/api/contracts/number/{contractNumber}` | Get contract by contract number |
| GET | `/api/contracts` | Get all contracts (paginated) |
| GET | `/api/contracts/customer/{customerId}` | Get contracts by customer |
| GET | `/api/contracts/staff/{staffId}` | Get contracts by staff |

### API Documentation
- Swagger UI: `http://localhost:8084/api/contract/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8084/api/contract/api-docs`

## Request/Response Examples

### Create Contract
```json
POST /api/contracts
{
  "customerId": 1,
  "title": "Equipment Installation Contract",
  "description": "Installation and setup of industrial equipment",
  "totalValue": 50000000,
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "contractDetails": [
    {
      "workCode": "INST001",
      "serviceName": "Equipment Installation",
      "description": "Installation of conveyor system",
      "quantity": 1,
      "unitPrice": 30000000,
      "warrantyMonths": 12
    }
  ]
}
```

### Contract Response
```json
{
  "success": true,
  "data": {
    "id": 1,
    "contractNumber": "HD2025070001",
    "customerId": 1,
    "staffId": 1,
    "title": "Equipment Installation Contract",
    "status": "UNSIGNED",
    "totalValue": 50000000,
    "createdAt": "2025-07-01T13:00:00Z"
  },
  "timestamp": "2025-07-01T13:00:00Z"
}
```

## Configuration

### Database Connection
```yaml
spring:
  datasource:
    url: jdbc:postgresql://ep-royal-bird-a1m9xsdr.ap-southeast-1.aws.neon.tech:5432/CEM_DB?sslmode=require
    username: default
    password: USIBWX1Y4Lui
```

### Service URLs
- Customer Service: `http://localhost:8082/api/customer`
- Device Service: `http://localhost:8083/api/device`
- Auth Service: `http://localhost:8081/api/auth`

## Contract Statuses
- `UNSIGNED` - Contract created but not signed
- `PAPER_SIGNED` - Contract signed on paper
- `DIGITALLY_SIGNED` - Contract signed digitally
- `CANCELLED` - Contract cancelled
- `EXPIRED` - Contract expired

## Security
- JWT token authentication required
- Authorization header: `Bearer {token}`
- Role-based access control integrated

## Development Status
✅ **COMPLETE** - Basic contract management functionality is implemented and working

### Next Steps (Future Enhancements)
- Contract signing workflow
- Email notifications
- File upload/download
- Advanced search and filtering
- Contract statistics and reporting
- Integration with external services

## Build and Run

### Local Development
```bash
mvn clean compile
mvn spring-boot:run
```

### Production Build
```bash
mvn clean package
java -jar target/CEM-contract-0.0.1-SNAPSHOT.jar
```

## Integration with Other Services
The Contract Service integrates with:
- **Authentication Service** (port 8081) - User authentication and JWT validation
- **Customer Service** (port 8082) - Customer information
- **Device Service** (port 8083) - Device information
- **Gateway Service** (port 8080) - API routing

## Monitoring
- Health check: `http://localhost:8084/api/contract/actuator/health`
- Metrics: `http://localhost:8084/api/contract/actuator/metrics`
- Logs: Available in `logs/contract-service.log` 