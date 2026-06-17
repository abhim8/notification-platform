package template.adapter.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import template.adapter.rest.dto.TemplateResponse;
import template.application.TemplateUseCase;
import template.application.TemplateNotFoundException;
import template.domain.Template;

import java.util.List;
import java.util.Map;

/**
 * REST controller for template operations.
 */
@RestController
@RequestMapping("/api/v1/templates")
@Tag(name = "Templates", description = "Notification template management")
@Slf4j
@RequiredArgsConstructor
public class TemplateController {
    private final TemplateUseCase templateUseCase;

    /**
     * Get all templates
     */
    @GetMapping
    @Operation(summary = "Get all templates", description = "Retrieves all templates in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Templates retrieved successfully")
    })
    public ResponseEntity<List<TemplateResponse>> getAllTemplates() {
        log.debug("GET /api/v1/templates");

        List<Template> templates = templateUseCase.getAllTemplates();

        List<TemplateResponse> responses = templates.stream()
                .map(t -> new TemplateResponse(
                        t.id(), t.eventType(), t.name(),
                        t.subject(), t.body(), t.version(),
                        t.active(), t.createdAt(), t.updatedAt()))
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Get a template by ID
     */
    @GetMapping("/{templateId}")
    @Operation(summary = "Get template by ID", description = "Retrieves a template without rendering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template found"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    public ResponseEntity<TemplateResponse> getTemplate(
            @Parameter(description = "Template ID", required = true)
            @PathVariable String templateId) {

        try {
            log.debug("GET /api/v1/templates/{} ", templateId);

            Template template = templateUseCase.getTemplate(templateId);

            TemplateResponse response = new TemplateResponse(
                    template.id(),
                    template.eventType(),
                    template.name(),
                    template.subject(),
                    template.body(),
                    template.version(),
                    template.active(),
                    template.createdAt(),
                    template.updatedAt()
            );

            return ResponseEntity.ok(response);

        } catch (TemplateNotFoundException e) {
            log.warn("Template not found: {}", templateId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving template: {}", templateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get and render a template with payload
     */
    @PostMapping("/{templateId}/render")
    @Operation(summary = "Render template", description = "Retrieves and renders a template with the provided payload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template rendered successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public ResponseEntity<RenderResponse> renderTemplate(
            @Parameter(description = "Template ID", required = true)
            @PathVariable String templateId,
            @Parameter(description = "Payload for template rendering", required = true)
            @RequestBody Map<String, Object> payload) {

        try {
            log.debug("POST /api/v1/templates/{}/render", templateId);

            if (payload == null) {
                payload = Map.of();
            }

            String rendered = templateUseCase.getAndRenderTemplate(templateId, payload);

            RenderResponse response = new RenderResponse(templateId, rendered);
            return ResponseEntity.ok(response);

        } catch (TemplateNotFoundException e) {
            log.warn("Template not found : {}", templateId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error rendering template: {}", templateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Check if a template exists
     */
    @RequestMapping(value = "/{templateId}", method = org.springframework.web.bind.annotation.RequestMethod.HEAD)
    @Operation(summary = "Check template exists", description = "Returns 200 if template exists, 404 otherwise")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template exists"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    public ResponseEntity<Void> templateExists(
            @PathVariable @Parameter(description = "Template ID", required = true) String templateId) {

        if (templateUseCase.templateExists(templateId)) {
            return ResponseEntity.ok().build();
        } else {
            log.warn("Template not-found: {}", templateId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Response DTO for rendered template
     */
    public record RenderResponse(String templateId, String content) {}
}


