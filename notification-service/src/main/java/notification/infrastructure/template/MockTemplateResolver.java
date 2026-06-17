package notification.infrastructure.template;

import lombok.extern.slf4j.Slf4j;
import notification.application.service.TemplateResolutionException;
import notification.application.service.TemplateResolver;

import java.util.Map;

/**
 * Mock implementation of TemplateResolver.
 *
 * In a full system, this would call the template-service REST API.
 * For now, we mock template resolution with simple string formatting.
 */
@Slf4j
public class MockTemplateResolver implements TemplateResolver {

    @Override
    public String resolveTemplate(String templateId, Map<String, Object> payload) {
        try {
            log.debug("[TEMPLATE] Resolving template: templateId={}", templateId);

            // Mock template resolution
            // In production, this would:
            // 1. Call template-service REST API
            // 2. Fetch template by ID
            // 3. Render with payload

            String template = getTemplate(templateId);
            return renderTemplate(template, payload);

        } catch (Exception e) {
            log.error("[ERROR] Failed to resolve template: templateId={}", templateId, e);
            throw new TemplateResolutionException("Failed to resolve template: " + templateId, e);
        }
    }

    @Override
    public boolean templateExists(String templateId) {
        return getTemplate(templateId) != null;
    }

    /**
     * Get template by ID (mocked)
     */
    private String getTemplate(String templateId) {
        return switch (templateId) {
            case "order-confirm" -> "Your order has been confirmed. Order ID: {{orderId}}, Amount: ${{amount}}";
            case "order-shipped" -> "Your order has been shipped. Tracking: {{trackingNumber}}";
            case "password-reset" -> "Password reset link: {{resetLink}}";
            case "account-created" -> "Welcome {{userName}}! Your account is now active.";
            case "payment-failed" -> "Payment failed for order {{orderId}}. Please retry at {{retryUrl}}";
            case "user-invited" -> "{{inviterName}} invited you to join. Link: {{joinLink}}";
            case "promotion-alert" -> "Special offer: {{promotionTitle}} - {{promotionDescription}}";
            case "fraud-alert" -> "ALERT: Suspicious activity detected on your account. Please verify at {{verifyUrl}}";
            case "order-delivered" -> "Your order has been delivered on {{deliveryDate}}";
            case "system-alert" -> "System notification: {{alertMessage}}";
            default -> null;
        };
    }

    /**
     * Render template with payload (simple replacement)
     */
    private String renderTemplate(String template, Map<String, Object> payload) {
        if (template == null) {
            throw new TemplateResolutionException("Template not found");
        }

        String result = template;
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }

        return result;
    }
}

