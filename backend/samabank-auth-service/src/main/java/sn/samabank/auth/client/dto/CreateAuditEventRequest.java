package sn.samabank.auth.client.dto;

import java.util.Map;
import java.util.UUID;

public class CreateAuditEventRequest {

    private String eventType;
    private UUID actorId;
    private String actorRole;
    private String resourceType;
    private UUID resourceId;
    private UUID correlationId;
    private String ipAddress;
    private String channel;
    private Map<String, Object> payload;

    public CreateAuditEventRequest() {}

    public CreateAuditEventRequest(String eventType, UUID actorId, String actorRole,
                                   String resourceType, UUID resourceId, UUID correlationId,
                                   String ipAddress, String channel, Map<String, Object> payload) {
        this.eventType = eventType;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.correlationId = correlationId;
        this.ipAddress = ipAddress;
        this.channel = channel;
        this.payload = payload;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }
    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public UUID getResourceId() { return resourceId; }
    public void setResourceId(UUID resourceId) { this.resourceId = resourceId; }
    public UUID getCorrelationId() { return correlationId; }
    public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
