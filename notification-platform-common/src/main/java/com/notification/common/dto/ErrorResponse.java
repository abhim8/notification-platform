package com.notification.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    String traceId,
    List<ValidationError> details
) {
    public static ErrorResponse of(HttpStatus httpStatus, String message, String path, List<ValidationError> details) {
        return new ErrorResponse(
            LocalDateTime.now(),
            httpStatus.value(),
            httpStatus.getReasonPhrase(),
            message,
            path,
            null,
            details
        );
    }

    public static ErrorResponse of(HttpStatus httpStatus, String message, String path) {
        return of(httpStatus, message, path, null);
    }
}
