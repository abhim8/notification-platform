package com.notification.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseApiException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, cause);
    }
}
