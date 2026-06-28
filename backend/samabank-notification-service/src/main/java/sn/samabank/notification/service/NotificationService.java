package sn.samabank.notification.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sn.samabank.notification.dto.SendEmailRequest;
import sn.samabank.notification.entity.Notification;
import sn.samabank.notification.entity.NotificationType;
import sn.samabank.notification.repository.NotificationRepository;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String FROM = "noreply@samabank.local";

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    public NotificationService(JavaMailSender mailSender, NotificationRepository notificationRepository) {
        this.mailSender = mailSender;
        this.notificationRepository = notificationRepository;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(SendEmailRequest request) {
        NotificationType type;
        try {
            type = NotificationType.valueOf(request.type());
        } catch (IllegalArgumentException e) {
            type = NotificationType.WELCOME;
        }

        Notification notification = Notification.create(request.recipientEmail(), request.subject(), type);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(FROM);
            helper.setTo(request.recipientEmail());
            helper.setSubject(request.subject());
            helper.setText(request.htmlBody(), true);
            mailSender.send(message);
            notification.markSent();
            log.info("[NOTIF] Envoye — type:{} to:{}", request.type(), request.recipientEmail());
        } catch (Exception e) {
            notification.markFailed(e.getMessage());
            log.error("[NOTIF_ERROR] Echec envoi — type:{} to:{} — {}", request.type(), request.recipientEmail(), e.getMessage());
        } finally {
            notificationRepository.save(notification);
        }
    }
}
