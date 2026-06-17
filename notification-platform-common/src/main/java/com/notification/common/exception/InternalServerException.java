package com.notification.common.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends BaseApiException {

    public InternalServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message, cause);
    }
}
