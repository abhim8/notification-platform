package template.domain;

public class Template {
    private final String templateId;
    private final String name;
    private final String content;
    private final String channel;
    private final long createdAt;

    public Template(String templateId, String name, String content, String channel) {
        this.templateId = templateId;
        this.name = name;
        this.content = content;
        this.channel = channel;
        this.createdAt = System.currentTimeMillis();
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getChannel() {
        return channel;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}

