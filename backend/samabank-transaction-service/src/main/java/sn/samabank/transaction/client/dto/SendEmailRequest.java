package sn.samabank.transaction.client.dto;

public record SendEmailRequest(String recipientEmail, String recipientFirstName, String type, String subject, String htmlBody) {}
