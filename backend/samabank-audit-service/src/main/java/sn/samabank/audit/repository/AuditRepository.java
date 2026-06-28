package sn.samabank.audit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.samabank.audit.entity.AuditEvent;

import java.time.Instant;
import java.util.UUID;

public interface AuditRepository extends JpaRepository<AuditEvent, UUID> {

    Page<AuditEvent> findByResourceTypeAndResourceIdOrderByOccurredAtDesc(
        String resourceType, UUID resourceId, Pageable pageable);

    @Query(value = """
        SELECT a FROM AuditEvent a
        WHERE (:eventType IS NULL OR a.eventType = :eventType)
          AND (:actorId IS NULL OR a.actorId = :actorId)
          AND (CAST(:from AS timestamp) IS NULL OR a.occurredAt >= :from)
          AND (CAST(:to AS timestamp) IS NULL OR a.occurredAt <= :to)
        """,
        countQuery = """
        SELECT COUNT(a) FROM AuditEvent a
        WHERE (:eventType IS NULL OR a.eventType = :eventType)
          AND (:actorId IS NULL OR a.actorId = :actorId)
          AND (CAST(:from AS timestamp) IS NULL OR a.occurredAt >= :from)
          AND (CAST(:to AS timestamp) IS NULL OR a.occurredAt <= :to)
        """)
    Page<AuditEvent> findWithFilters(
        @Param("eventType") String eventType,
        @Param("actorId") UUID actorId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        Pageable pageable);
}
