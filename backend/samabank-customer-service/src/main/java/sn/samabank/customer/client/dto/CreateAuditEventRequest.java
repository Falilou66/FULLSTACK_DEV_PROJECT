package sn.samabank.customer.client.dto;

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
        this.eventType    = eventType;
        this.actorId      = actorId;
        this.actorRole    = actorRole;
        this.resourceType = resourceType;
        this.resourceId   = resourceId;
        this.correlationId = correlationId;
        this.ipAddress    = ipAddress;
        this.channel      = channel;
        this.payload      = payload;
    }

    public String getEventType()                { return eventType; }
    public void setEventType(String v)          { this.eventType = v; }
    public UUID getActorId()                    { return actorId; }
    public void setActorId(UUID v)              { this.actorId = v; }
    public String getActorRole()                { return actorRole; }
    public void setActorRole(String v)          { this.actorRole = v; }
    public String getResourceType()             { return resourceType; }
    public void setResourceType(String v)       { this.resourceType = v; }
    public UUID getResourceId()                 { return resourceId; }
    public void setResourceId(UUID v)           { this.resourceId = v; }
    public UUID getCorrelationId()              { return correlationId; }
    public void setCorrelationId(UUID v)        { this.correlationId = v; }
    public String getIpAddress()                { return ipAddress; }
    public void setIpAddress(String v)          { this.ipAddress = v; }
    public String getChannel()                  { return channel; }
    public void setChannel(String v)            { this.channel = v; }
    public Map<String, Object> getPayload()     { return payload; }
    public void setPayload(Map<String, Object> v){ this.payload = v; }
}
