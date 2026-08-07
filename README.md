# Encore

Encore is a distributed event ticketing system built as a portfolio project to demonstrate polyglot microservice architecture. The .NET API Gateway handles authentication and routes incoming ticket requests onto a Kafka event bus, where a suite of Java Spring Boot services take over — managing order state, reserving seats with Redis-backed hold logic, generating QR-coded tickets, and dispatching booking notifications. Designed to handle the high-concurrency spike of a live ticket sale, with dead letter queue handling for fault tolerance.


## Getting Started

## Prerequisites

## Docker

**Corporate SSL Inspection**

If you're on a corporate network using Palo Alto GlobalProtect (or similar), Docker containers won't trust your company's SSL certificates, causing dependency restoration to fail during builds.
To fix it, export your machine's certificates and make them available to the Docker build:

```bash
security find-certificate -a -p /Library/Keychains/System.keychain > api/ca-certs.pem
```

The Dockerfile handles the rest. Note that ca-certs.pem is gitignored and should never be committed.