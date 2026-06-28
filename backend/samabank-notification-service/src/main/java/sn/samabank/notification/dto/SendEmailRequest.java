package sn.samabank.notification.dto;

public record SendEmailRequest(
    String recipientEmail,
    String recipientFirstName,
    String type,
    String subject,
    String htmlBody
) {}
