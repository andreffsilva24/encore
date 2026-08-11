# ADR 002 — How the Notification Service resolves user contact details

## Status
Accepted

## Context
The Notification Service needs to send emails to users when order events occur (confirmed, cancelled, seat hold failed, ticket issued). Kafka events carry a `userId` but not the user's email address. A strategy is needed to resolve contact details at notification time.

## Options Considered

### 1. Event enrichment
Include `userEmail` in every Kafka event payload at publish time. Simple to implement but carries downsides: increases payload size, introduces PII into Kafka topics (a GDPR concern), and violates single responsibility — events carry data irrelevant to their primary consumers.

### 2. Query a User Service via REST (chosen)
The Notification Service calls `GET /api/users/{userId}` on a dedicated User Service to resolve the email at notification time. Keeps events lean, isolates PII to the User Service, and follows the same service-to-service REST pattern already established between the API Gateway and Order Service.

### 3. Notification Service maintains its own user store
Duplicate user data in the Notification Service's own database. Avoids the runtime dependency but introduces data duplication and consistency challenges.

## Decision
The Notification Service queries a User Service via REST to resolve user contact details. This keeps Kafka events free of PII, respects single responsibility, and introduces a User Service that will also serve as the foundation for authentication in a future iteration.