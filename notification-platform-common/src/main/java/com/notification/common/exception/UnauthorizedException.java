package com.notification.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseApiException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message, cause);
    }
}
