package com.hcmute.endsemesterproject.Models;

import java.util.HashMap;

public class GroupMessage {
    private String messageId;
    private String senderId;
    private String message;
    private long timestamp;
    private HashMap<String, Integer> reactions; // Map of reaction types and counts

    public GroupMessage() {
        // Default constructor required for Firebase
    }

    public GroupMessage(String messageId, String senderId, String message, long timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.reactions = new HashMap<>();
    }

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public HashMap<String, Integer> getReactions() {
        return reactions;
    }

    public void setReactions(HashMap<String, Integer> reactions) {
        this.reactions = reactions;
    }

    public void addReaction(String reactionType) {
        // Add a reaction to the message
        reactions.put(reactionType, reactions.getOrDefault(reactionType, 0) + 1);
    }

    public void removeReaction(String reactionType) {
        // Remove a reaction from the message
        if (reactions.containsKey(reactionType)) {
            int count = reactions.get(reactionType);
            if (count > 1) {
                reactions.put(reactionType, count - 1);
            } else {
                reactions.remove(reactionType);
            }
        }
    }
}
