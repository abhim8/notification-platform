# Notification Platform Common

[![Java](https://img.shields.io/badge/Java-23-blue?logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)

> Shared library encapsulating domain primitives, exception hierarchy, common DTOs, and auto-configuration used across all Notification Platform microservices.

**Module:** `notification-platform-common` | **Navigation:** [Root](../README.md) · [Notification Service](../notification-service/README.md) · [Template Service](../template-service/README.md) · [Delivery Tracker](../delivery-tracker/README.md)

---

## Project Overview

The `notification-platform-common` module serves as a shared library for the Notification Platform, promoting consistency, reducing code duplication, and establishing a common foundation for platform-wide concerns. Every microservice depends on this module.

### Why It Exists

| Goal | Implementation |
|------|---------------|
| Centralize domain enums | Single source of truth for `Channel`, `EventType`, `DeliveryStatus` |
| Unify exception handling | Consistent `BaseApiException` hierarchy with `GlobalExceptionHandler` |
| Share common DTOs | Reusable `ErrorResponse` and `ValidationError` for all services |
| Standardize logging & tracing | MDC filter for `traceId` propagation across service boundaries |
| Provide auto-configuration | Spring Boot auto-config via `CommonAutoConfiguration` |

### What Belongs Here

- **Domain Enums** - `Channel`, `EventType`, `DeliveryStatus`
- **Exception Classes** - `BaseApiException` and its subclasses
- **Common DTOs** - `ErrorResponse`, `ValidationError`
- **Configuration** - `CommonAutoConfiguration`, `MdcFilter`, `WebConfig`, `StringToEnumIgnoringCaseConverterFactory`
- **Shared Resources** - `log-layout.json`, `log4j2.xml`

### What Does NOT Belong Here

- Service-specific business logic (Kafka consumers, channel dispatchers)
- Controllers, service implementations, or repositories
- Database entities or persistence adapters
- UI or frontend components
- Niche external library dependencies

---

## Package Structure

```
notification-platform-common/
└── src/main/java/com/notification/common/
    ├── config/
    │   ├── CommonAutoConfiguration.java            # Auto-config entry point
    │   ├── MdcFilter.java                          # traceId MDC propagation filter
    │   ├── StringToEnumIgnoringCaseConverterFactory.java  # Case-insensitive enum binding
    │   └── WebConfig.java                          # Web MVC configuration
    ├── domain/
    │   ├── Channel.java                            # EMAIL, SMS, PUSH, WEBHOOK
    │   ├── DeliveryStatus.java                     # PENDING, DELIVERED, FAILED, etc.
    │   └── EventType.java                          # Event classification enum
    ├── dto/
    │   ├── ErrorResponse.java                      # Standard error response
    │   └── ValidationError.java                    # Field validation error detail
    ├── exception/
    │   ├── BaseApiException.java                   # Base class (extends RuntimeException)
    │   ├── BadRequestException.java                # HTTP 400
    │   ├── UnauthorizedException.java              # HTTP 401
    │   ├── NotFoundException.java                  # HTTP 404
    │   ├── ConflictException.java                  # HTTP 409
    │   └── InternalServerException.java            # HTTP 500
    └── handler/
        └── GlobalExceptionHandler.java             # Maps exceptions → HTTP responses
```

---

## Shared Enums

### `Channel`

```java
public enum Channel {
    EMAIL, SMS, PUSH, WEBHOOK
}
```

Includes `@JsonCreator` and `@JsonValue` for flexible JSON serialization/deserialization with case-insensitive matching.

### `EventType`

```java
public enum EventType {
    ORDER_PLACED, PASSWORD_RESET, // ... and others
}
```

### `DeliveryStatus`

```java
public enum DeliveryStatus {
    PENDING, DELIVERED, FAILED, RETRYING, DLQ, DROPPED
}
```

---

## Exception Hierarchy

All custom exceptions extend `BaseApiException` and are handled by `GlobalExceptionHandler`:

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `BadRequestException` | 400 | Invalid request data |
| `UnauthorizedException` | 401 | Authentication failure |
| `NotFoundException` | 404 | Resource not found |
| `ConflictException` | 409 | Resource conflict |
| `InternalServerException` | 500 | Unexpected server error |

---

## Shared DTOs

| DTO | Fields | Purpose |
|-----|--------|---------|
| `ErrorResponse` | `timestamp`, `status`, `error`, `message`, `path`, `errors` (List of ValidationError) | Standardized API error response |
| `ValidationError` | `field`, `message` | Field-level validation detail |

---

## Logging & Tracing

| Component | Purpose |
|-----------|---------|
| `MdcFilter` | Servlet filter that extracts or generates a `traceId` and populates the MDC context |
| `log-layout.json` | JSON template layout for structured log output |
| `log4j2.xml` | Log4j2 configuration with JSON layout and console appender |

The MDC filter ensures that a unique `traceId` flows across all services, enabling end-to-end request tracing in centralized log aggregation systems.

---

## Auto-Configuration

`CommonAutoConfiguration` is registered via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. It configures:

- `MdcFilter` - MDC trace ID propagation
- `WebConfig` - web-related beans (CORS, converters)
- `StringToEnumIgnoringCaseConverterFactory` - case-insensitive string-to-enum conversion

---

## Documentation

| Document | Description |
|----------|-------------|
| [**Root README**](../README.md) | Platform overview, architecture, setup |
| [**Notification Service**](../notification-service/README.md) | Core orchestration service |
| [**Template Service**](../template-service/README.md) | Template management and rendering |
| [**Delivery Tracker**](../delivery-tracker/README.md) | Delivery attempt tracking |
| [**Startup Guide**](../STARTUP_GUIDE.md) | End-to-end local setup |
| [**Contributing**](../CONTRIBUTING.md) | Contribution guidelines |
| [**Security**](../SECURITY.md) | Security policy |
