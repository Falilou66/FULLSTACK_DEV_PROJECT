package sn.samabank.audit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.samabank.audit.dto.CreateAuditEventRequest;
import sn.samabank.audit.service.AuditService;

@RestController
@RequestMapping("/internal/audit")
public class InternalAuditController {

    private final AuditService auditService;

    public InternalAuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<Void> createAuditEvent(@RequestBody CreateAuditEventRequest request) {
        auditService.createEvent(request);
        return ResponseEntity.accepted().build();
    }
}
