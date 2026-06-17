package notification.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import notification.application.service.TemplateResolutionException;
import notification.application.service.TemplateResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
public class TemplateServiceClient implements TemplateResolver {

    private final RestTemplate restTemplate;
    private final String getTemplateUrl;
    private final String renderTemplateUrl;

    public TemplateServiceClient(RestTemplate restTemplate, String getTemplateUrl, String renderTemplateUrl) {
        this.restTemplate = restTemplate;
        this.getTemplateUrl = getTemplateUrl;
        this.renderTemplateUrl = renderTemplateUrl;
    }

    @Override
    public String resolveTemplate(String templateId, Map<String, Object> payload) {
        try {
            log.debug("[TEMPLATE] Resolving template via REST: templateId={}", templateId);
            ResponseEntity<RenderResponse> response = restTemplate.postForEntity(
                    renderTemplateUrl, payload, RenderResponse.class, templateId);
            RenderResponse body = response.getBody();
            if (body == null || body.content() == null) {
                throw new TemplateResolutionException("Empty response from template-service for: " + templateId);
            }
            return body.content();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("[TEMPLATE] Template not found via REST: templateId={}", templateId);
            throw new TemplateResolutionException("Template not found: " + templateId, e);
        } catch (Exception e) {
            log.error("[TEMPLATE] Failed to resolve template via REST: templateId={}", templateId, e);
            throw new TemplateResolutionException("Failed to resolve template: " + templateId, e);
        }
    }

    @Override
    public boolean templateExists(String templateId) {
        try {
            log.debug("[TEMPLATE] Checking template existence via REST: templateId={}", templateId);
            ResponseEntity<String> response = restTemplate.getForEntity(getTemplateUrl, String.class, templateId);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            log.error("[TEMPLATE] Failed to check template existence via REST: templateId={}", templateId, e);
            return false;
        }
    }

    private record RenderResponse(String templateId, String content) {}
}
