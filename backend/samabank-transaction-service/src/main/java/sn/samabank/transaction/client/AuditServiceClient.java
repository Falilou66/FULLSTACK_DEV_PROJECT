package sn.samabank.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sn.samabank.transaction.client.dto.CreateAuditEventRequest;

@FeignClient(name = "samabank-audit-service")
public interface AuditServiceClient {

    @PostMapping("/internal/audit")
    void createAuditEvent(@RequestBody CreateAuditEventRequest request);
}
