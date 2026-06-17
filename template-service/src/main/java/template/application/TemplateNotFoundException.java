package template.application;

import com.notification.common.exception.NotFoundException;

/**
 * Exception thrown when a template cannot be found or is not active.
 */
public class TemplateNotFoundException extends NotFoundException {

    public TemplateNotFoundException(String message) {
        super(message);
    }

    public TemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
