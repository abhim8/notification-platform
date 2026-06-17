package notification.infrastructure.template;

import lombok.extern.slf4j.Slf4j;
import notification.application.service.TemplateResolutionException;
import notification.application.service.TemplateResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
public class RestTemplateResolver implements TemplateResolver {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RestTemplateResolver(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public String resolveTemplate(String templateId, Map<String, Object> payload) {
        try {
            log.debug("[TEMPLATE] Resolving template via REST: templateId={}", templateId);
            String url = baseUrl + "/api/v1/templates/{templateId}/render";
            ResponseEntity<RenderResponse> response = restTemplate.postForEntity(
                    url, payload, RenderResponse.class, templateId);
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
            String url = baseUrl + "/api/v1/templates/{templateId}";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, templateId);
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
