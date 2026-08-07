# Encore

--------
Encore is a distributed event ticketing system built as a portfolio project to demonstrate polyglot microservice architecture. The .NET API Gateway handles authentication and routes incoming ticket requests onto a Kafka event bus, where a suite of Java Spring Boot services take over — managing order state, reserving seats with Redis-backed hold logic, generating QR-coded tickets, and dispatching booking notifications. Designed to handle the high-concurrency spike of a live ticket sale, with dead letter queue handling for fault tolerance.