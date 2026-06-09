package template.application;

public class TemplateNotFoundException extends Exception {
    private final String templateId;

    public TemplateNotFoundException(String message) {
        super(message);
        this.templateId = null;
    }

    public TemplateNotFoundException(String message, String templateId) {
        super(message);
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }
}

