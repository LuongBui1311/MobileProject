package com.hcmute.endsemesterproject.Models;

public class MessageReaction {
    private String messageId;
    private String userId;
    private String reactType;

    public MessageReaction(String messageId, String userId, String reactType) {
        this.messageId = messageId;
        this.userId = userId;
        this.reactType = reactType;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReactType() { return reactType; }
    public void setReactType(String reactType) {
        this.reactType = reactType;
    }
}
