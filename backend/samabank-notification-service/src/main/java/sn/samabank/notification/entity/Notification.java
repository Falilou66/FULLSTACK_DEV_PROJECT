package sn.samabank.notification.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "sent_at")
    private Instant sentAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Notification() {}

    public static Notification create(String recipientEmail, String subject, NotificationType type) {
        Notification n = new Notification();
        n.recipientEmail = recipientEmail;
        n.subject = subject;
        n.type = type;
        n.status = NotificationStatus.PENDING;
        return n;
    }

    public void markSent() { this.status = NotificationStatus.SENT; this.sentAt = Instant.now(); }
    public void markFailed(String errorMessage) { this.status = NotificationStatus.FAILED; this.errorMessage = errorMessage; }

    public UUID getId() { return id; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getSubject() { return subject; }
    public NotificationType getType() { return type; }
    public NotificationStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getSentAt() { return sentAt; }
    public Instant getCreatedAt() { return createdAt; }
}
