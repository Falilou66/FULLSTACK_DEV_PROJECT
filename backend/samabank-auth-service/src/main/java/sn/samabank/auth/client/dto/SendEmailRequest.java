package sn.samabank.auth.client.dto;

public class SendEmailRequest {

    private String recipientEmail;
    private String recipientFirstName;
    private String type;
    private String subject;
    private String htmlBody;

    public SendEmailRequest() {}

    public SendEmailRequest(String recipientEmail, String recipientFirstName,
                            String type, String subject, String htmlBody) {
        this.recipientEmail = recipientEmail;
        this.recipientFirstName = recipientFirstName;
        this.type = type;
        this.subject = subject;
        this.htmlBody = htmlBody;
    }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getRecipientFirstName() { return recipientFirstName; }
    public void setRecipientFirstName(String recipientFirstName) { this.recipientFirstName = recipientFirstName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getHtmlBody() { return htmlBody; }
    public void setHtmlBody(String htmlBody) { this.htmlBody = htmlBody; }
}
