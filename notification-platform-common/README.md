# notification-platform-common Module

This module serves as a shared library for the Notification Platform, encapsulating common functionalities, domain models, and utilities that are reused across all microservices. Its primary goal is to promote consistency, reduce code duplication, and establish a common ground for platform-wide concerns.

## Why the Common Module Exists

The `notification-platform-common` module was created to:
*   **Centralize Domain Enums:** Define core business enums (e.g., `Channel`, `EventType`, `DeliveryStatus`) in a single, accessible location.
*   **Establish a Unified Exception Hierarchy:** Provide a consistent set of custom exceptions (`BaseApiException`, `NotFoundException`, `BadRequestException`, etc.) for standardized error handling across services.
*   **Share Common DTOs:** Define reusable Data Transfer Objects (DTOs) for common API responses and internal data structures, preventing redundant definitions.
*   **Standardize Logging & Tracing:** Offer shared resources and configurations for logging, including MDC (Mapped Diagnostic Context) to ensure `traceId` propagation across service boundaries.
*   **Provide Auto-Configuration:** Offer common Spring Boot auto-configurations that can be easily included by other services.

## What Belongs in It

*   **Domain Enums:** `Channel`, `EventType`, `DeliveryStatus`, and any other enum representing a core domain concept used by multiple services.
*   **Custom Exception Classes:** Extend `BaseApiException` for specific business exceptions (`NotFoundException`, `BadRequestException`, `ConflictException`, `InternalServerException`, `UnauthorizedException`).
*   **Common DTOs:** `ErrorResponse`, `ValidationError`, and other DTOs used in standard API interactions or inter-service communication.
*   **Utility Classes:** Small, stateless utility classes that provide common helper functions (e.g., string manipulation, date formatting) that are genuinely generic and not specific to any single service's business logic.
*   **Spring `Configuration` Classes:** General auto-configuration components or filters (e.g., `MdcFilter`, `CommonAutoConfiguration`).
*   **Shared Resources:** `log-layout.json` or other common configuration files.

## What Should NOT Belong in It

*   **Service-Specific Business Logic:** Any code that implements a feature unique to a single microservice (e.g., Kafka consumer logic, template rendering, database persistence for `DeliveryAttempt`).
*   **Controller/Service/Repository Implementations:** Avoid placing concrete implementations of business services or data access layers.
*   **External Library Dependencies (unless widely used):** Only include dependencies that are truly common across *most* or *all* services and provide fundamental utilities (e.g., Lombok, Jackson annotations). Avoid pulling in niche libraries.
*   **Database Entities/Repositories:** These are specific to a service's persistence layer.
*   **UI-Related Components:** No UI-specific code.

## Shared Enums

*   **`com.notification.common.domain.Channel`**: Defines the supported notification channels (EMAIL, SMS, PUSH, WEBHOOK). Includes `@JsonCreator` and `@JsonValue` for flexible JSON serialization/deserialization.
*   **`com.notification.common.domain.EventType`**: Defines various types of events that trigger notifications (e.g., ORDER_PLACED, PASSWORD_RESET).
*   **`com.notification.common.domain.DeliveryStatus`**: Represents the status of a delivery attempt (e.g., PENDING, SENT, FAILED, RETRYING).

## Shared Exception Hierarchy

All custom exceptions in the platform extend `com.notification.common.exception.BaseApiException`, which provides a consistent structure for error responses.

*   `BaseApiException`
*   `BadRequestException` (HTTP 400)
*   `UnauthorizedException` (HTTP 401)
*   `NotFoundException` (HTTP 404)
*   `ConflictException` (HTTP 409)
*   `InternalServerException` (HTTP 500)

The `com.notification.common.handler.GlobalExceptionHandler` catches these exceptions and maps them to appropriate HTTP status codes and `ErrorResponse` DTOs.

## Shared DTOs

*   **`com.notification.common.dto.ErrorResponse`**: Standardized structure for error messages returned by APIs.
*   **`com.notification.common.dto.ValidationError`**: Used within `ErrorResponse` to detail specific field validation errors.

## Shared Logging Resources

*   **`com.notification.common.config.MdcFilter`**: A Spring `Filter` that automatically extracts or generates a `traceId` and places it into the MDC (Mapped Diagnostic Context). This ensures that a unique `traceId` is present in all log messages for a given request across service boundaries.
*   **`log-layout.json`**: A Logback configuration fragment defining a consistent JSON logging format, facilitating centralized log collection and analysis.

## Any Auto-Configuration Provided

*   **`com.notification.common.config.CommonAutoConfiguration`**: Configures general beans and components common to all services, such as the `MdcFilter` and potentially other platform-wide settings.
