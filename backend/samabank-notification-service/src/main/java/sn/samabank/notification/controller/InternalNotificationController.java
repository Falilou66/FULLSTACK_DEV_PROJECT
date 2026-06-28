package sn.samabank.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.samabank.notification.dto.SendEmailRequest;
import sn.samabank.notification.service.NotificationService;

@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {

    private final NotificationService notificationService;

    public InternalNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> send(@RequestBody SendEmailRequest request) {
        notificationService.send(request);
        return ResponseEntity.accepted().build();
    }
}
