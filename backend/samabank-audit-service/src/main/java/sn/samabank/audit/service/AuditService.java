package sn.samabank.audit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sn.samabank.audit.dto.AuditEventResponse;
import sn.samabank.audit.dto.CreateAuditEventRequest;
import sn.samabank.audit.entity.AuditEvent;
import sn.samabank.audit.repository.AuditRepository;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createEvent(CreateAuditEventRequest request) {
        try {
            AuditEvent event = AuditEvent.create(
                request.eventType(), request.actorId(), request.actorRole(),
                request.resourceType(), request.resourceId(), request.correlationId(),
                request.ipAddress(), request.channel(), request.payload()
            );
            auditRepository.save(event);
            log.info("[AUDIT] {} — actor:{} resource:{}/{}", request.eventType(), request.actorId(), request.resourceType(), request.resourceId());
        } catch (Exception e) {
            log.error("[AUDIT_ERROR] Echec persistance — eventType:{} — {}", request.eventType(), e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> findAll(String eventType, UUID actorId, Instant from, Instant to, Pageable pageable) {
        return auditRepository.findWithFilters(eventType, actorId, from, to, pageable).map(AuditEventResponse::from);
    }

    @Transactional(readOnly = true)
    public AuditEventResponse findById(UUID id) {
        return auditRepository.findById(id)
            .map(AuditEventResponse::from)
            .orElseThrow(() -> new RuntimeException("AuditEvent not found: " + id));
    }
}
