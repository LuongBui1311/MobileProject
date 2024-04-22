package com.hcmute.endsemesterproject.Models;

public class BetaGroupMessage {
    private String messageId;
    private String senderId;
    private String senderName; // Add sender name field
    private long timestamp;
    private String messageText;
    private String messageType; // "text", "image", "pdf", etc.
    private String fileUrl; // URL to the file if messageType is "image" or "pdf"
    private String originalFileName; // Add field for original filename

    public BetaGroupMessage() {
        // Default constructor required for Firebase
    }

    public BetaGroupMessage(String messageId, String senderId, String senderName, long timestamp, String messageText, String messageType, String fileUrl, String originalFileName) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.timestamp = timestamp;
        this.messageText = messageText;
        this.messageType = messageType;
        this.fileUrl = fileUrl;
        this.originalFileName = originalFileName;
    }

    // Getters and setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
}
