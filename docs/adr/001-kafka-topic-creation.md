# ADR 001 — Kafka Topic Creation Strategy

## Status
Accepted

## Context
The system requires several Kafka topics to exist before services can produce or consume messages.
A strategy is needed for how and where these topics are created, both for local development and
with future production environments in mind.

## Options Considered

### 1. Auto-creation (`auto.create.topics.enable`)
Kafka can create topics automatically on first use. Simple but dangerous — typos silently create
unintended topics, and there is no control over partition count or replication factor.

### 2. Docker Compose init container (chosen)
A dedicated init container runs `kafka-topics --create` commands on startup. Topics are treated
as infrastructure, defined alongside the rest of the local infrastructure config.

### 3. Application code
Services create topics programmatically on startup via the Kafka admin client. Keeps topic
definitions close to the code that uses them, but couples infrastructure concerns to application
logic.

## Decision
Topics are created via a Docker Compose init container. This keeps topic management separate
from application code, automates local setup with a single `docker compose up`, and reflects
how topics would be managed in production (via an infrastructure-as-code tool).

## Consequences
- Local setup is fully automated
- Topic definitions are visible in one place
- A separate production strategy (e.g. Terraform, Confluent Control Center) will be needed
  when the project moves beyond local development