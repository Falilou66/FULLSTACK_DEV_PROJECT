package sn.samabank.account.client.dto;

public class SendEmailRequest {

    private String recipientEmail;
    private String recipientFirstName;
    private String type;
    private String subject;
    private String htmlBody;

    public SendEmailRequest() {}

    public SendEmailRequest(String recipientEmail, String recipientFirstName,
                            String type, String subject, String htmlBody) {
        this.recipientEmail     = recipientEmail;
        this.recipientFirstName = recipientFirstName;
        this.type               = type;
        this.subject            = subject;
        this.htmlBody           = htmlBody;
    }

    public String getRecipientEmail()           { return recipientEmail; }
    public void setRecipientEmail(String v)     { this.recipientEmail = v; }
    public String getRecipientFirstName()       { return recipientFirstName; }
    public void setRecipientFirstName(String v) { this.recipientFirstName = v; }
    public String getType()                     { return type; }
    public void setType(String v)               { this.type = v; }
    public String getSubject()                  { return subject; }
    public void setSubject(String v)            { this.subject = v; }
    public String getHtmlBody()                 { return htmlBody; }
    public void setHtmlBody(String v)           { this.htmlBody = v; }
}
