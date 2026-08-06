# BidStream Microservices - Complete Project Structure

**Project Type**: Spring Boot Microservices with Spring Cloud Gateway, Eureka, and Kafka  
**Total Services**: 7 (6 microservices + 1 frontend mentioned)  
**Build Tool**: Maven  
**Java Version**: 21+  
**Spring Boot**: 4.1.0  
**Spring Cloud**: 2025.1.2

---

## Complete Project Tree

```
BidStream/
│
├── 📁 EurekaServer/                          # Service Discovery & Registration
│   ├── pom.xml
│   ├── src/main/java/com/example/
│   │   └── EurekaServerApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── src/test/java/...
│
├── 📁 UserService/                           # User Management & Authentication
│   ├── pom.xml
│   ├── src/main/java/com/example/
│   │   ├── UserServiceApplication.java
│   │   ├── controller/
│   │   │   └── UserController.java
│   │   ├── service/
│   │   │   ├── UserService.java (interface)
│   │   │   └── UserServiceImpl.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── model/
│   │   │   └── User.java (JPA Entity)
│   │   ├── DTO/
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── AuthResponse.java
│   │   │   ├── UserResponse.java
│   │   │   └── ErrorResponse.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── PasswordEncoderConfig.java
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       ├── UserNotFoundException.java
│   │       ├── UserAlreadyExistsException.java
│   │       └── InvalidCredentialsException.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── src/test/java/...
│
├── 📁 AuctionService/                        # Auction Management
│   ├── pom.xml
│   ├── src/main/java/com/example/
│   │   ├── AuctionServiceApplication.java
│   │   ├── controller/
│   │   │   └── AuctionController.java
│   │   ├── service/
│   │   │   ├── AuctionService.java (interface)
│   │   │   └── AuctionServiceImpl.java
│   │   ├── repository/
│   │   │   └── AuctionRepository.java
│   │   ├── model/
│   │   │   └── Auction.java (JPA Entity)
│   │   ├── DTO/
│   │   │   ├── CreateAuctionRequest.java
│   │   │   ├── UpdateAuctionRequest.java
│   │   │   ├── AuctionResponse.java
│   │   │   └── ErrorResponse.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── KafkaProducerConfig.java
│   │   │   └── JwtConfig.java
│   │   ├── event/
│   │   │   ├── AuctionCreatedEvent.java
│   │   │   ├── AuctionStartedEvent.java
│   │   │   └── AuctionClosedEvent.java
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       ├── AuctionNotFoundException.java
│   │       └── InvalidAuctionStateException.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── src/test/java/...
│
├── 📁 BID-PROCESSING-SERVICE/                # Bid Management & Validation
│   ├── pom.xml
│   ├── src/main/java/com/example/
│   │   ├── BidProcessingServiceApplication.java
│   │   ├── controller/
│   │   │   └── BidController.java
│   │   ├── service/
│   │   │   ├── BidService.java (interface)
│   │   │   └── BidServiceImpl.java
│   │   ├── repository/
│   │   │   └── BidRepository.java
│   │   ├── model/
│   │   │   └── Bid.java (JPA Entity)
│   │   ├── DTO/
│   │   │   ├── CreateBidRequest.java
│   │   │   ├── BidResponse.java
│   │   │   └── ErrorResponse.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── KafkaConsumerConfig.java
│   │   │   └── KafkaProducerConfig.java
│   │   ├── listener/
│   │   │   ├── AuctionEventListener.java
│   │   │   └── BidEventListener.java
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       ├── BidNotFoundException.java
│   │       ├── InvalidBidException.java
│   │       └── AuctionNotFoundException.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── src/test/java/...
│
├── 📁 NOTIFICATION-SERVICE/                  # Email & Notification Management
│   ├── pom.xml
│   ├── src/main/java/com/example/
│   │   ├── NotificationServiceApplication.java
│   │   ├── controller/
│   │   │   └── NotificationController.java
│   │   ├── service/
│   │   │   ├── NotificationService.java (interface)
│   │   │   ├── NotificationServiceImpl.java
│   │   │   ├── EmailService.java (interface)
│   │   │   └── EmailServiceImpl.java
│   │   ├── repository/
│   │   │   └── NotificationRepository.java
│   │   ├── model/
│   │   │   └── Notification.java (JPA Entity)
│   │   ├── DTO/
│   │   │   ├── CreateNotificationRequest.java
│   │   │   ├── NotificationResponse.java
│   │   │   └── ErrorResponse.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── KafkaConsumerConfig.java
│   │   │   ├── KafkaProducerConfig.java
│   │   │   └── EmailConfig.java
│   │   ├── listener/
│   │   │   ├── NotificationEventListener.java
│   │   │   └── BidEventListener.java
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       ├── NotificationNotFoundException.java
│   │       └── InvalidNotificationException.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── src/test/java/...
│
└── 📁 ApiGateway/                             # API Gateway & Routing
    ├── ApiGateway/                           # Note: Nested structure
    │   ├── pom.xml
    │   ├── src/main/java/com/gl/app/ApiGateway/
    │   │   ├── ApiGatewayApplication.java
    │   │   ├── config/
    │   │   │   ├── GateWayconfig.java
    │   │   │   ├── RedisConfig.java
    │   │   │   └── SecurityConfig.java
    │   │   └── filter/
    │   │       ├── JwtAuthenticationFilter.java
    │   │       └── RateLimitingFilter.java
    │   ├── src/main/resources/
    │   │   └── application.properties
    │   └── src/test/java/...
    └── [pom.xml]
```

---

## Service Details & Responsibilities

### 1️⃣ EurekaServer (Port 8761)
**Purpose**: Service Discovery & Registration  
**Key Files**:
- `EurekaServerApplication.java` - Entry point with `@EnableEurekaServer`

**Configuration**:
- Registers all microservices automatically
- Provides service location lookup
- Health check monitoring

---

### 2️⃣ UserService (Port 8081)
**Purpose**: User Management, Authentication, JWT Token Generation  

**Key Components**:

| Layer | File | Purpose |
|-------|------|---------|
| **Controller** | `UserController.java` | Handles `/auth` and `/api/users` endpoints |
| **Service** | `UserServiceImpl.java` | Business logic for register/login |
| **Repository** | `UserRepository.java` | Database access for User entity |
| **Entity** | `User.java` | JPA entity mapping to `users` table |
| **DTO** | `RegisterRequest.java` | Validation for registration |
| | `LoginRequest.java` | Validation for login |
| | `AuthResponse.java` | Response with JWT token |
| **Config** | `SecurityConfig.java` | Spring Security setup |
| | `JwtTokenProvider.java` | JWT token generation/validation |
| **Exception** | `GlobalExceptionHandler.java` | Centralized error handling |

**Endpoints**:
```
POST   /api/auth/register     → 201 Created
POST   /api/auth/login        → 200 OK
GET    /api/users/{id}        → 200 OK
DELETE /api/users/{id}        → 204 No Content
```

---

### 3️⃣ AuctionService (Port 8082)
**Purpose**: Auction CRUD Operations, State Management, Event Publishing  

**Key Components**:

| Layer | File | Purpose |
|-------|------|---------|
| **Controller** | `AuctionController.java` | Handles `/api/auctions` endpoints |
| **Service** | `AuctionServiceImpl.java` | Business logic for auction operations |
| **Repository** | `AuctionRepository.java` | Database access for Auction entity |
| **Entity** | `Auction.java` | JPA entity mapping to `auctions` table |
| **DTO** | `CreateAuctionRequest.java` | Validation for auction creation |
| | `UpdateAuctionRequest.java` | Validation for status updates |
| | `AuctionResponse.java` | Response format |
| **Event** | `AuctionCreatedEvent.java` | Published when auction created |
| | `AuctionStartedEvent.java` | Published when auction started |
| | `AuctionClosedEvent.java` | Published when auction closed |
| **Config** | `SecurityConfig.java` | Spring Security |
| | `KafkaProducerConfig.java` | Kafka topic publisher |
| | `@Scheduled` in `AuctionServiceImpl` | Auto state transitions (PENDING→ACTIVE→CLOSED, PENDING→EXPIRED) |
| **Exception** | `GlobalExceptionHandler.java` | Error handling |

**Endpoints**:
```
POST   /api/auctions                 → 201 Created
GET    /api/auctions                 → 200 OK (paginated)
GET    /api/auctions/{id}            → 200 OK
GET    /api/auctions/stats/active-count → 200 OK
PUT    /api/auctions/{id}            → 200 OK
POST   /api/auctions/{id}/start      → 200 OK
```

---

### 4️⃣ BID-PROCESSING-SERVICE (Port 8083)
**Purpose**: Bid Validation, Processing, State Management  

**Key Components**:

| Layer | File | Purpose |
|-------|------|---------|
| **Controller** | `BidController.java` | Handles `/api/bids` endpoints |
| **Service** | `BidServiceImpl.java` | Business logic for bid processing |
| **Repository** | `BidRepository.java` | Database access for Bid entity |
| **Entity** | `Bid.java` | JPA entity mapping to `bids` table |
| **DTO** | `CreateBidRequest.java` | Validation for bid placement |
| | `BidResponse.java` | Response format |
| **Listener** | `AuctionEventListener.java` | Consumes auction events from Kafka |
| | `BidEventListener.java` | Consumes bid events from Kafka |
| **Config** | `SecurityConfig.java` | Spring Security |
| | `KafkaConsumerConfig.java` | Kafka topic consumer |
| | `KafkaProducerConfig.java` | Kafka topic publisher |
| **Exception** | `GlobalExceptionHandler.java` | Error handling |

**Endpoints**:
```
POST   /api/bids                              → 201 Created
GET    /api/bids/{bidId}                      → 200 OK
GET    /api/bids/auction/{auctionId}          → 200 OK
GET    /api/bids/bidder/{bidderId}            → 200 OK
GET    /api/bids/auction/{auctionId}/highest  → 200 OK
```

**Kafka Topics Consumed**:
- `auction-created` - New auction created
- `auction-started` - Auction is now active
- `auction-closed` - Auction ended

**Kafka Topics Published**:
- `bid-placed` - New bid received
- `bid-outbid` - Previous bidder outbid

---

### 5️⃣ NOTIFICATION-SERVICE (Port 8084)
**Purpose**: Notifications, Email Sending, Event Listening  

**Key Components**:

| Layer | File | Purpose |
|-------|------|---------|
| **Controller** | `NotificationController.java` | Handles `/api/notifications` endpoints |
| **Service** | `NotificationServiceImpl.java` | Notification CRUD operations |
| | `EmailServiceImpl.java` | Email sending logic |
| **Repository** | `NotificationRepository.java` | Database access |
| **Entity** | `Notification.java` | JPA entity mapping to `notifications` table |
| **DTO** | `CreateNotificationRequest.java` | Validation for notification creation |
| | `NotificationResponse.java` | Response format |
| **Listener** | `NotificationEventListener.java` | Consumes auction events |
| | `BidEventListener.java` | Consumes bid events |
| **Config** | `SecurityConfig.java` | Spring Security |
| | `KafkaConsumerConfig.java` | Kafka consumer |
| | `KafkaProducerConfig.java` | Kafka producer |
| | `EmailConfig.java` | Email service setup (SMTP) |
| **Exception** | `GlobalExceptionHandler.java` | Error handling |

**Endpoints**:
```
POST   /api/notifications                    → 201 Created
GET    /api/notifications/{id}               → 200 OK
GET    /api/notifications/user/me            → 200 OK
GET    /api/notifications/user/me/unread     → 200 OK
GET    /api/notifications/user/me/count      → 200 OK
PUT    /api/notifications/{id}/read          → 200 OK
PUT    /api/notifications/user/me/read-all   → 200 OK
DELETE /api/notifications/{id}               → 200 OK
```

**Kafka Topics Consumed**:
- `auction-created` - Send notification about new auction
- `auction-started` - Send notification when auction starts
- `auction-closed` - Send notification when auction ends
- `bid-placed` - Send notification about new bid
- `bid-outbid` - Send notification to outbid bidder

**Note**: `@EnableKafka` on `NotificationServiceApplication` is required for the `@KafkaListener` consumers to start.

---

### 6️⃣ ApiGateway (Port 8080)
**Purpose**: Routing, Rate Limiting, JWT Validation, Load Balancing  

**Key Components**:

| Component | File | Purpose |
|-----------|------|---------|
| **Application** | `ApiGatewayApplication.java` | Spring Cloud Gateway entry point |
| **Config** | `GateWayconfig.java` | Route configuration and predicates |
| | `RedisConfig.java` | Redis for rate limiting |
| | `SecurityConfig.java` | Gateway security |
| **Filter** | `JwtAuthenticationFilter.java` | JWT validation for all requests |
| | `RateLimitingFilter.java` | Rate limiting using Redis |

**Route Configuration** (defined in `GateWayconfig.java` via `RouteLocator`, NOT in `application.properties`):
```
Routes → Services Mapping:
/api/users/**     → UserService (8081)
/api/auth/**      → UserService (8081)
/api/auctions/**  → AuctionService (8082)
/api/bids/**      → BidService (8083)
/api/notifications/** → NotificationService (8084)
```

**Features**:
- ✅ Load balancing with Eureka (using `lb://service-name`)
- ✅ JWT token validation (sets SecurityContext so `authenticated()` passes downstream)
- ✅ CORS configuration
- ✅ Routes forward full `/api/...` paths (no `stripPrefix`)

---

## Technology Stack by Layer

### Backend Services
```
Spring Boot 4.1.0
├── Spring Data JPA (ORM)
├── Spring Security (Authentication)
├── Spring Cloud (Microservices)
├── Spring Cloud Gateway (Routing)
├── Spring Cloud Eureka (Service Discovery)
├── Spring Kafka (Event Messaging)
├── Validation (javax.validation)
├── Lombok (Boilerplate reduction)
└── MySQL/PostgreSQL Driver (Database)
```

### Database
```
PostgreSQL
├── users (User credentials & profile)
├── auctions (Auction listings & state)
├── bids (Bid records)
└── notifications (Notification history)
```

### Messaging
```
Apache Kafka
├── Topic: auction-created
├── Topic: auction-started
├── Topic: auction-closed
├── Topic: bid-placed
└── Topic: bid-outbid
```

### Infrastructure
```
Eureka Server → Service Discovery
Redis → Rate Limiting & Caching
SMTP → Email Notifications
JWT → Token Authentication
```

---

## Build & Dependencies

### Parent POM (if exists)
- Spring Boot Starter Parent 4.1.0
- Java 21+

### Common Dependencies (All Services)
```xml
<!-- Spring Cloud -->
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<!-- Database -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Security -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Kafka -->
<dependency>
  <groupId>org.springframework.kafka</groupId>
  <artifactId>spring-kafka</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <scope>provided</scope>
</dependency>
```

---

## Configuration Files Summary

### Each Service Has: `application.properties`

```properties
# Service Name & Port
spring.application.name={service-name}
server.port={port}

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/bidstream
spring.datasource.username=postgres
spring.datasource.password=****
spring.jpa.hibernate.ddl-auto=update

# Eureka Registration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true

# Kafka (if needed)
spring.kafka.bootstrap-servers=localhost:9092

# JWT Configuration
jwt.secret=bidstream-secret-key
jwt.expiration-ms=3600000

# Logging
logging.level.root=INFO
logging.level.org.springframework.cloud.gateway=DEBUG
```

---

## Data Models (Entity Relationships)

```
User (1) ─── (Many) Auction
  ↓
  └─ Seller creates Auctions
  
Auction (1) ─── (Many) Bid
  ├─ Has starting price
  ├─ Has status (PENDING, ACTIVE, EXPIRED, CLOSED)
  └─ Tracks highest bidder

User (Many) ─── (Many) Bid
  └─ Users place Bids on Auctions

Notification (Many) ─── (1) User
  └─ Users receive Notifications
```

---

## Deployment Order

```
1. Start EurekaServer (8761) - FIRST
   ↓
2. Start UserService (8081)
   ↓
3. Start AuctionService (8082)
   ↓
4. Start BidService (8083)
   ↓
5. Start NotificationService (8084)
   ↓
6. Start ApiGateway (8080) - LAST
```

---

## Environment Prerequisites

```
✅ Java 21+
✅ Maven 3.8+
✅ PostgreSQL (Database)
✅ Apache Kafka (Event Streaming)
✅ Redis (Optional - Rate Limiting)
✅ SMTP Server (Email Notifications)
```

---

## Build Command (For Each Service)

```bash
cd <service-directory>
mvn clean package -DskipTests
java -jar target/<service-name>.jar
```

---

## Project Statistics

| Metric | Count |
|--------|-------|
| Total Microservices | 6 |
| Total Endpoints | 23 |
| Database Tables | 4 |
| Kafka Topics | 5 |
| Java Classes | ~80+ |
| Configuration Classes | ~15+ |
| DTO Classes | ~20+ |
| Exception Classes | ~15+ |

---

## Common Patterns Used

### 1. Controller → Service → Repository Pattern
```
UserController
    ↓
UserService (interface)
    ↓
UserServiceImpl (implementation)
    ↓
UserRepository (JPA)
    ↓
Database
```

### 2. Global Exception Handling
```
@ControllerAdvice
GlobalExceptionHandler
├─ Catches all exceptions
├─ Returns standardized ErrorResponse
└─ HTTP status code mapping
```

### 3. Kafka Event Listening
```
BidEventListener
├─ Consumes "bid-placed" topic
├─ Sends notification
└─ Updates local state
```

### 4. JWT Authentication
```
JwtAuthenticationFilter
├─ Extracts token from header
├─ Validates signature
├─ Extracts userId
└─ Sets SecurityContext
```

---

## Key Files Locations

| File | Location |
|------|----------|
| User Entity | `UserService/src/main/java/com/example/model/User.java` |
| Auction Entity | `AuctionService/src/main/java/com/example/model/Auction.java` |
| Bid Entity | `BID-PROCESSING-SERVICE/src/main/java/com/example/model/Bid.java` |
| Notification Entity | `NOTIFICATION-SERVICE/src/main/java/com/example/model/Notification.java` |
| User Controller | `UserService/src/main/java/com/example/controller/UserController.java` |
| Auction Controller | `AuctionService/src/main/java/com/example/controller/AuctionController.java` |
| Bid Controller | `BID-PROCESSING-SERVICE/src/main/java/com/example/controller/BidController.java` |
| Notification Controller | `NOTIFICATION-SERVICE/src/main/java/com/example/controller/NotificationController.java` |
| Gateway Config | `ApiGateway/ApiGateway/src/main/java/com/gl/app/ApiGateway/config/GateWayconfig.java` |

