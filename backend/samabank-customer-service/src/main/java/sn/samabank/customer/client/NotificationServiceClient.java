package sn.samabank.customer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sn.samabank.customer.client.dto.SendEmailRequest;

@FeignClient(name = "samabank-notification-service")
public interface NotificationServiceClient {

    @PostMapping("/internal/notifications/email")
    void sendEmail(@RequestBody SendEmailRequest request);
}
