# Encore 🎵

A distributed event ticketing backend built with .NET and Java microservices, communicating asynchronously via Apache Kafka. Covers distributed seat reservations, order state management, QR ticket generation, and event-driven notifications.

## Architecture

Encore is a polyglot microservices system where services are loosely coupled through Kafka. The .NET API Gateway handles authentication and routes incoming ticket requests onto a Kafka event bus, where a suite of Java Spring Boot services take over — managing order state, reserving seats with Redis-backed hold logic, generating QR-coded tickets, and dispatching booking notifications.

```
Client → .NET API Gateway → Kafka → Order Service
                                  → Inventory Service (Redis)
                                  → Fulfillment Service (QR codes)
                                  → Notification Service (Resend)
         .NET API Gateway → REST → User Service
                                 → Order Service
```

## Services

| Service | Language | Port | Responsibility |
|---|---|---|---|
| API Gateway | .NET 9 / ASP.NET Core | 8080 | Auth, routing, Kafka publishing |
| Order Service | Java 21 / Spring Boot | 8081 | Order state machine, PostgreSQL |
| Inventory Service | Java 21 / Spring Boot | 8082 | Seat holds, Redis |
| Notification Service | Java 21 / Spring Boot | 8083 | Email via Resend |
| User Service | Java 21 / Spring Boot | 8084 | User management, BCrypt |
| Fulfillment Service | Java 21 / Spring Boot | 8085 | QR code generation |

## Tech Stack

| Layer | Technology |
|---|---|
| API Gateway | ASP.NET Core 9, JWT, Confluent.Kafka |
| Microservices | Java 21, Spring Boot 4.1, Spring Kafka |
| Messaging | Apache Kafka (Confluent) |
| Databases | PostgreSQL 16 (orders, users) |
| Cache | Redis 7 (seat holds) |
| Email | Resend |
| Observability | Kafbat UI, pgAdmin |
| Infrastructure | Docker, Docker Compose |

## Kafka Topics

| Topic | Producer | Consumer(s) |
|---|---|---|
| `ticket.order.requested` | API Gateway | Order Service |
| `ticket.seat.hold.requested` | Order Service | Inventory Service |
| `ticket.seat.held` | Inventory Service | Order Service |
| `ticket.seat.hold.failed` | Inventory Service | Order Service, Notification Service |
| `ticket.order.confirmed` | Order Service | Fulfillment Service, Notification Service |
| `ticket.order.cancelled` | Order Service | Inventory Service, Notification Service |
| `ticket.issued` | Fulfillment Service | Notification Service |
| `ticket.events.dlq` | Any (retry exhausted) | DLQ monitor |

## Prerequisites

- Docker and Docker Compose
- .NET 9 SDK (for local API development)
- Java 21 + Maven (for local service development)
- A [Resend](https://resend.com) account and API key

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/encore.git
cd encore
```

### 2. Create the environment file

```bash
cp .env.example .env
```

Edit `.env` and fill in the required values:

```env
RESEND_API_KEY=your_resend_api_key
JWT_SECRET=your_generated_secret
```

Generate a secure JWT secret with:

```bash
openssl rand -base64 64
```

### 3. Start the full stack

```bash
docker compose up -d --build
```

This starts all services, creates all Kafka topics automatically, and runs database migrations.

### 4. Verify everything is running

```bash
docker compose ps
```

All containers should show as `Up`.

## API Endpoints

All endpoints except auth require a `Authorization: Bearer <token>` header.

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive a JWT |

### Users
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users/{userId}` | Get user by ID |

### Orders
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders` | Submit a ticket order |
| GET | `/api/orders/{orderId}` | Get order status |

### Swagger UI

Available at `http://localhost:8080/swagger` when running locally.

## Monitoring

| Tool | URL | Description |
|---|---|---|
| Kafbat | http://localhost:9000 | Kafka topic browser |
| pgAdmin | http://localhost:5050 | PostgreSQL UI |

## Troubleshooting

### Corporate SSL Inspection

If you are behind a corporate network using Palo Alto GlobalProtect or similar SSL inspection, Docker containers won't trust your company's certificates, causing dependency restoration to fail during builds.

Export your machine's certificates:

```bash
security find-certificate -a -p /Library/Keychains/System.keychain > api/ca-certs.pem
cp api/ca-certs.pem services/notification-service/ca-certs.pem
cp api/ca-certs.pem services/fulfillment-service/ca-certs.pem
```

The Dockerfiles are already configured to inject these certificates. `ca-certs.pem` is gitignored and should never be committed.

## Architecture Decision Records

| ADR | Decision |
|---|---|
| [ADR 001](docs/adr/001-kafka-topic-creation.md) | Kafka topic creation via Docker Compose init container |
| [ADR 002](docs/adr/002-notification-user-resolution.md) | Notification Service resolves user contact details via User Service REST |

## Project Structure

```
encore/
├── docker-compose.yml
├── .env.example
├── README.md
├── docs/
│   └── adr/
├── api/                        # .NET API Gateway
│   ├── Dockerfile
│   └── src/
└── services/
    ├── order-service/
    ├── inventory-service/
    ├── notification-service/
    ├── user-service/
    └── fulfillment-service/
```
