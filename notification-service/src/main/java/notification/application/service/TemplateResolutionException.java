package notification.application.service;

import com.notification.common.exception.InternalServerException;

/**
 * Exception thrown when template resolution/rendering fails.
 */
public class TemplateResolutionException extends InternalServerException {

    public TemplateResolutionException(String message) {
        super(message);
    }

    public TemplateResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
