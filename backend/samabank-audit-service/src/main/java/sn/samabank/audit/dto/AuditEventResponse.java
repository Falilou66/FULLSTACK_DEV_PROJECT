package sn.samabank.audit.dto;

import sn.samabank.audit.entity.AuditEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditEventResponse(
    UUID id,
    String eventType,
    UUID actorId,
    String actorRole,
    String resourceType,
    UUID resourceId,
    UUID correlationId,
    String ipAddress,
    String channel,
    Map<String, Object> payload,
    Instant occurredAt,
    Long sequenceNum
) {
    public static AuditEventResponse from(AuditEvent e) {
        return new AuditEventResponse(
            e.getId(), e.getEventType(), e.getActorId(), e.getActorRole(),
            e.getResourceType(), e.getResourceId(), e.getCorrelationId(),
            e.getIpAddress(), e.getChannel(), e.getPayload(),
            e.getOccurredAt(), e.getSequenceNum()
        );
    }
}
