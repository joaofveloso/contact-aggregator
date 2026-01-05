# Contacts API Aggregator

![Test Coverage](https://img.shields.io/badge/coverage-96%25-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![Quarkus](https://img.shields.io/badge/Quarkus-3.30-red)
![Tests](https://img.shields.io/badge/tests-60%20passing-success)

A REST API that fetches contact data from Kenect Labs API with reactive programming, circuit breaker pattern, and caching.

## Configuration

### Environment Variables

Copy the example file and add your credentials:

```bash
cp .env.example .env
```

Then edit `.env` and add the following required variable:

```text
KENECT_LABS_API_BEARER_TOKEN=your_external_api_token_here
```

**Getting the external API token:**
1. Contact the Kenect Labs API provider to obtain your bearer token
2. Add the token to the `.env` file (see example below)

**Example `.env` file:**
```text
# Kenect Labs API Authentication
KENECT_LABS_API_BEARER_TOKEN=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Important:** The `.env` file is already in `.gitignore` to prevent committing sensitive data.

## Quick Start

### Development Mode

```bash
git clone git@github.com:joaofveloso/contact-aggregator.git
cd contact-aggregator
```

```bash
./gradlew quarkusDev
```

Access Swagger UI: http://localhost:8080/q/swagger-ui

### Docker

```bash
./gradlew quarkusBuild -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false
docker buildx build --load -f src/main/docker/Dockerfile.native-micro -t contacts-aggregator .
docker run -d -p 8080:8080 --env-file .env --name contacts-aggregator contacts-aggregator
```

View logs: `docker logs -f contacts-aggregator`

Access Swagger UI: http://localhost:8080/q/swagger-ui

### Docker Compose

```bash
./gradlew quarkusBuild -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false
docker compose up -d --build
```

View logs: `docker compose logs -f contacts-aggregator`

Access Swagger UI: http://localhost:8080/q/swagger-ui

## Usage Examples

### Fetch All Contacts

```bash
curl -X GET http://localhost:8080/v1/contacts
```

**Response:**
```json
{
  "successfulRecords": 10,
  "skippedRecords": 0,
  "successfulPages": 1,
  "skippedPages": 0,
  "circuitBreakerTrippedCount": 0,
  "contacts": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "source": "KENECT_LABS",
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

### Invalidate Cache

```bash
curl -X POST http://localhost:8080/v1/contacts/invalidate-cache
```

**Response:**
```json
{
  "message": "Cache invalidated successfully"
}
```

## Architecture

Hexagonal architecture with three layers:

- **REST Layer** - HTTP endpoints (ContactResource)
- **Application Layer** - Business logic + caching (ContactApplicationService)
- **Domain Layer** - Core models (Contact, statistics)
- **Repository** - External API adapter (KenectLabsApiAdapter)

## Features

- **Circuit Breaker** - Protects against external API failures (opens after 4 failures, waits 10s, closes after 2 successes)
- **Caching** - 5-minute cache to reduce API calls
- **Reactive Programming** - Non-blocking I/O with Mutiny (Uni/Multi)
- **Error Handling** - Tracks failures, skips bad pages, returns partial results

## API Endpoints

- `GET /v1/contacts` - Fetch all contacts with statistics
- `POST /v1/contacts/invalidate-cache` - Clear the cache

## Code Quality

- **96% test coverage** (60 tests)
- **Clean Architecture** - SOLID principles, hexagonal ports/adapters
- **Test-Driven Development** - Tests written before code
- **Modern Java** - Records, streams, Java 21 features

---

```bash
docker stop contacts-aggregator
docker rm contacts-aggregator
```