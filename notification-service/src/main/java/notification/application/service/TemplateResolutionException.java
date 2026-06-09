package notification.application.service;

/**
 * Exception thrown when template resolution/rendering fails.
 */
public class TemplateResolutionException extends RuntimeException {

    public TemplateResolutionException(String message) {
        super(message);
    }

    public TemplateResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

