package com.OnlineToyStore.Sllit.model;

public class ContactMessage extends Feedback {

    private String messageId;
    private String username;
    private String email;
    private String subject;
    private String reply;
    private String status;

    public ContactMessage() {
        super();
        this.status = "OPEN";
    }

    public ContactMessage(String userId, String username, String email,
                          String subject, String comment) {
        super(userId, comment);
        this.username = username;
        this.email = email;
        this.subject = subject;
        this.status = "OPEN";
    }

    @Override
    public String getSummary() {
        return subject + " - " + getComment();
    }

    public String toFileString() {
        return safe(messageId) + "|" +
                safe(getUserId()) + "|" +
                safe(username) + "|" +
                safe(email) + "|" +
                safe(subject) + "|" +
                safe(getComment()) + "|" +
                safe(getDate()) + "|" +
                safe(reply) + "|" +
                safe(status);
    }

    public static ContactMessage fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        ContactMessage message = new ContactMessage();
        message.setMessageId(p.length > 0 ? p[0] : "");
        message.setUserId(p.length > 1 ? p[1] : "");
        message.setUsername(p.length > 2 ? p[2] : "");
        message.setEmail(p.length > 3 ? p[3] : "");
        message.setSubject(p.length > 4 ? p[4] : "");
        message.setComment(p.length > 5 ? p[5] : "");
        message.setDate(p.length > 6 ? p[6] : "");
        message.setReply(p.length > 7 ? p[7] : "");
        message.setStatus(p.length > 8 ? p[8] : "OPEN");
        return message;
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("|", " ");
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
