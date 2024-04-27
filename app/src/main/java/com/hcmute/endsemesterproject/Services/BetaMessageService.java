package com.hcmute.endsemesterproject.Services;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hcmute.endsemesterproject.Models.BetaGroupMessage;

import java.util.ArrayList;
import java.util.List;

public class BetaMessageService {
    private DatabaseReference betaMessagesRef;

    public BetaMessageService() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        betaMessagesRef = database.getReference("beta-messages");
    }

    public void createNewMessage(BetaGroupMessage message, String groupId, OnMessageAddListener listener) {
        String messageId = betaMessagesRef.push().getKey();
        if (messageId != null) {
            message.setMessageId(messageId);
            betaMessagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        // Add message ID to the group's messages reference
                        DatabaseReference groupMessagesRef = FirebaseDatabase.getInstance().getReference().child("beta-groups").child(groupId).child("messages");
                        groupMessagesRef.child(messageId).setValue(true)
                                .addOnSuccessListener(aVoid1 -> {
                                    listener.onMessageAdded(messageId);
                                })
                                .addOnFailureListener(e -> {
                                    listener.onAddFailure("Failed to add message ID to group: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Handle the case where the message couldn't be added to beta-messages
                        listener.onAddFailure("Failed to add message to beta-messages: " + e.getMessage());
                    });
        } else {
            // Handle the case where the unique key couldn't be generated
            listener.onAddFailure("Failed to generate message ID");
        }
    }


    public void getMessageFromId(String messageId, final OnMessageFetchListener listener) {
        betaMessagesRef.child(messageId).get()
                .addOnSuccessListener(snapshot -> {
                    BetaGroupMessage message = snapshot.getValue(BetaGroupMessage.class);
                    listener.onMessageFetched(message);
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    System.out.println("Error fetching message: " + e.getMessage());
                    listener.onFetchFailure(e.getMessage());
                });
    }

    public void getMessagesFromIdList(List<String> messageIdList, final OnMessageListFetchListener listener) {
        List<BetaGroupMessage> messages = new ArrayList<>();

        for (String messageId : messageIdList) {
            betaMessagesRef.child(messageId).get()
                    .addOnSuccessListener(snapshot -> {
                        BetaGroupMessage message = snapshot.getValue(BetaGroupMessage.class);
                        if (message != null) {
                            messages.add(message);
                        }
                        // Check if all messages have been fetched
                        if (messages.size() == messageIdList.size()) {
                            listener.onMessageListFetched(messages);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors
                        System.out.println("Error fetching message: " + e.getMessage());
                        listener.onFetchFailure(e.getMessage());
                    });
        }
    }


    public interface OnMessageAddListener {
        void onMessageAdded(String messageId);
        void onAddFailure(String errorMessage);
    }

    public interface OnMessageUpdateListener {
        void onMessageUpdated(String messageId);
        void onUpdateFailure(String errorMessage);
    }

    public interface OnMessageFetchListener {
        void onMessageFetched(BetaGroupMessage message);
        void onFetchFailure(String errorMessage);
    }

    public interface OnMessageListFetchListener {
        void onMessageListFetched(List<BetaGroupMessage> messages);
        void onFetchFailure(String errorMessage);
    }

}
