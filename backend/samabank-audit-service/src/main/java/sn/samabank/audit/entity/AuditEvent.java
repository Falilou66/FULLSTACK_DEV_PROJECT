package sn.samabank.audit.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "actor_role", nullable = false, length = 20)
    private String actorRole;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(length = 20)
    private String channel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "sequence_num", insertable = false, updatable = false)
    private Long sequenceNum;

    protected AuditEvent() {}

    public static AuditEvent create(String eventType, UUID actorId, String actorRole,
                                    String resourceType, UUID resourceId, UUID correlationId,
                                    String ipAddress, String channel, Map<String, Object> payload) {
        AuditEvent e = new AuditEvent();
        e.eventType = eventType;
        e.actorId = actorId;
        e.actorRole = actorRole;
        e.resourceType = resourceType;
        e.resourceId = resourceId;
        e.correlationId = correlationId != null ? correlationId : UUID.randomUUID();
        e.ipAddress = ipAddress;
        e.channel = channel;
        e.payload = payload != null ? payload : Map.of();
        e.occurredAt = Instant.now();
        return e;
    }

    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public UUID getActorId() { return actorId; }
    public String getActorRole() { return actorRole; }
    public String getResourceType() { return resourceType; }
    public UUID getResourceId() { return resourceId; }
    public UUID getCorrelationId() { return correlationId; }
    public String getIpAddress() { return ipAddress; }
    public String getChannel() { return channel; }
    public Map<String, Object> getPayload() { return payload; }
    public Instant getOccurredAt() { return occurredAt; }
    public Long getSequenceNum() { return sequenceNum; }
}
