package com.hcmute.endsemesterproject.Services;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hcmute.endsemesterproject.Models.MessageReaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReactionService {
    private DatabaseReference reactionsRef;
    private  Map<String, String> reactionEmojiMap = new HashMap<>();


    public ReactionService() {
        this.reactionsRef = FirebaseDatabase.getInstance().getReference().child("reactions");
        reactionEmojiMap.put("like", "👍");
        reactionEmojiMap.put("love", "❤️");
        reactionEmojiMap.put("wow", "😮");
        reactionEmojiMap.put("relax", "😌");
        reactionEmojiMap.put("cry", "😭");
        reactionEmojiMap.put("angry", "😠");
    }

    // Method to add a new reaction to reactions
    public void addReaction(String messageId, String userId, String reactType, OnReactionAddListener listener) {
        String reactionId = reactionsRef.push().getKey();
        if (reactionId != null) {
            MessageReaction reaction = new MessageReaction(reactionId, messageId, userId, reactType);
            reactionsRef.child(reactionId).setValue(reaction)
                    .addOnSuccessListener(aVoid -> listener.onReactionAdded())
                    .addOnFailureListener(e -> listener.onAddFailure(e.getMessage()));
        } else {
            listener.onAddFailure("Failed to generate reaction ID");
        }
    }

    // Method to remove a reaction from message id
    public void removeReaction(String messageId, String userId, String reactType, OnReactionRemoveListener listener) {
        Query query = reactionsRef.orderByChild("messageId").equalTo(messageId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageReaction reaction = snapshot.getValue(MessageReaction.class);
                    if (reaction != null && reaction.getUserId().equals(userId) && reaction.getReactType().equals(reactType)) {
                        snapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> listener.onReactionRemoved())
                                .addOnFailureListener(e -> listener.onRemoveFailure(e.getMessage()));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onRemoveFailure(databaseError.getMessage());
            }
        });
    }

    // Method to get all reactions id from a message id
    public void getAllReactionIds(String messageId, final OnReactionIdsFetchListener listener) {
        Query query = reactionsRef.orderByChild("messageId").equalTo(messageId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> reactionIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    reactionIds.add(snapshot.getKey());
                }
                listener.onReactionIdsFetched(reactionIds);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFetchFailure(databaseError.getMessage());
            }
        });
    }

    // Method to get all reactions from id list
    public void getReactionsFromIdList(List<String> reactionIds, final OnReactionsFetchListener listener) {
        List<MessageReaction> reactions = new ArrayList<>();
        for (String reactionId : reactionIds) {
            reactionsRef.child(reactionId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    MessageReaction reaction = dataSnapshot.getValue(MessageReaction.class);
                    if (reaction != null) {
                        reactions.add(reaction);
                    }
                    listener.onReactionsFetched(reactions);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    listener.onFetchFailure(databaseError.getMessage());
                }
            });
        }
    }

    // Method to get one reaction from id
    public void getReactionFromId(String reactionId, final OnReactionFetchListener listener) {
        reactionsRef.child(reactionId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MessageReaction reaction = dataSnapshot.getValue(MessageReaction.class);
                listener.onReactionFetched(reaction);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFetchFailure(databaseError.getMessage());
            }
        });
    }

    // Listener interface for fetching reaction IDs
    public interface OnReactionIdsFetchListener {
        void onReactionIdsFetched(List<String> reactionIds);
        void onFetchFailure(String errorMessage);
    }

    // Listener interface for fetching reactions
    public interface OnReactionsFetchListener {
        void onReactionsFetched(List<MessageReaction> reactions);
        void onFetchFailure(String errorMessage);
    }

    // Listener interface for fetching a single reaction
    public interface OnReactionFetchListener {
        void onReactionFetched(MessageReaction reaction);
        void onFetchFailure(String errorMessage);
    }

    // Listener interface for adding a reaction
    public interface OnReactionAddListener {
        void onReactionAdded();
        void onAddFailure(String errorMessage);
    }

    // Listener interface for removing a reaction
    public interface OnReactionRemoveListener {
        void onReactionRemoved();
        void onRemoveFailure(String errorMessage);
    }

    public void getAllReactionTypesForUserAndMessage(String userId, String messageId, OnReactionTypesFetchListener listener) {
        Query query = reactionsRef.orderByChild("messageId").equalTo(messageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> reactionTypes = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageReaction reaction = snapshot.getValue(MessageReaction.class);
                    if (reaction != null && reaction.getUserId().equals(userId)) {
                        reactionTypes.add(reaction.getReactType());
                    }
                }
                listener.onReactionTypesFetched(reactionTypes);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFetchFailure(databaseError.getMessage());
            }
        });
    }

    // Listener interface for fetching reaction types
    public interface OnReactionTypesFetchListener {
        void onReactionTypesFetched(List<String> reactionTypes);
        void onFetchFailure(String errorMessage);
    }

    // Method to get the top 3 reactions string for a message
    public void getTop3ReactionsString(String messageId, OnTop3ReactionsFetchListener listener) {
        Query query = reactionsRef.orderByChild("messageId").equalTo(messageId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Integer> reactionCounts = new HashMap<>();

                // Count occurrences of each reaction type
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageReaction reaction = snapshot.getValue(MessageReaction.class);
                    if (reaction != null) {
                        String reactType = reaction.getReactType();
                        reactionCounts.put(reactType, reactionCounts.getOrDefault(reactType, 0) + 1);
                    }
                }

                // Sort the reactions by count
                List<String> topReactions = getTopReactions(reactionCounts);

                // Construct the top reactions string
                String topReactionsString = constructTopReactionsString(topReactions, reactionCounts);

                // Pass the result to the listener
                listener.onTop3ReactionsFetched(topReactionsString);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFetchFailure(databaseError.getMessage());
            }
        });
    }

    // Method to get the top reactions from the reaction counts map
    private List<String> getTopReactions(Map<String, Integer> reactionCounts) {
        // Sort the reaction counts map by value
        List<Map.Entry<String, Integer>> sortedCounts = new ArrayList<>(reactionCounts.entrySet());
        sortedCounts.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Get the top 3 reactions or less if there are fewer than 3 types of reactions
        int count = Math.min(sortedCounts.size(), 3);
        List<String> topReactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            topReactions.add(sortedCounts.get(i).getKey());
        }
        return topReactions;
    }

    // Method to construct the top reactions string
    private String constructTopReactionsString(List<String> topReactions, Map<String, Integer> reactionCounts) {
        StringBuilder builder = new StringBuilder();
        for (String reaction : topReactions) {
            if (reactionEmojiMap.containsKey(reaction)) {
                builder.append(reactionEmojiMap.get(reaction)).append("+").append(reactionCounts.get(reaction)).append(" ");
            } else {
                builder.append(reaction).append("+").append(reactionCounts.get(reaction)).append(" ");
            }
        }
        // Append "more" if there are more than 3 types of reactions
        if (reactionCounts.size() > 3) {
            int moreCount = 0;
            for (int i = 3; i < topReactions.size(); i++) {
                moreCount += reactionCounts.get(topReactions.get(i));
            }
            builder.append("more(+").append(moreCount).append(")");
        }
        return builder.toString().trim();
    }


    // Listener interface for fetching the top 3 reactions string
    public interface OnTop3ReactionsFetchListener {
        void onTop3ReactionsFetched(String top3Reactions);
        void onFetchFailure(String errorMessage);
    }

}
