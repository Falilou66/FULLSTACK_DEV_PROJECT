package sn.samabank.transaction.client.dto;

import java.util.Map;
import java.util.UUID;

public record CreateAuditEventRequest(
    String eventType,
    UUID actorId,
    String actorRole,
    String resourceType,
    UUID resourceId,
    UUID correlationId,
    String ipAddress,
    String channel,
    Map<String, Object> payload
) {}
