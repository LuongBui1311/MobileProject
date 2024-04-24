package com.hcmute.endsemesterproject.Services;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmute.endsemesterproject.Models.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupService {
    private DatabaseReference groupsRef;

    public GroupService() {
        // Initialize Firebase Database reference for groups
        groupsRef = FirebaseDatabase.getInstance().getReference().child("beta-groups");
    }

    public void retrieveGroups(String userId, final GroupsFetchListener listener) {
        List<Group> allGroups = new ArrayList<>();

        // Add a listener for retrieving all groups
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot groupTypeSnapshot : dataSnapshot.getChildren()) {
                    String groupType = groupTypeSnapshot.getKey(); // "private" or "public"
                    for (DataSnapshot groupSnapshot : groupTypeSnapshot.getChildren()) {
                        String groupName = groupSnapshot.getKey(); // Group name
                        boolean isPublic = groupType.equals("public");

                        // Check if the user is a member of the group
                        boolean isMember = false;
                        for (DataSnapshot memberSnapshot : groupSnapshot.child("members").getChildren()) {
                            if (memberSnapshot.getValue(String.class).equals(userId)) {
                                isMember = true;
                                break;
                            }
                        }

                        if (isPublic) {
                            Group group = new Group();
                            group.setName(groupName);
                            group.setPublic(isPublic);
                            group.setNumberOfMembers(groupSnapshot.child("members").getChildrenCount()); // Get the count of members
                            group.setDescription("This is default group description.");
                            if (isMember) {
                                allGroups.add(0, group);
                            } else {
                                allGroups.add(group);
                            }
                        } else {
                            if (isMember) {
                                // Construct Group object using the group name, public/private status, and number of members
                                Group group = new Group();
                                group.setName(groupName);
                                group.setPublic(isPublic);
                                group.setNumberOfMembers(groupSnapshot.child("members").getChildrenCount()); // Get the count of members
                                group.setDescription("This is default group description.");
                                allGroups.add(group);
                            }
                        }

                    }
                }

                // Filter the groups list to get private then public groups
                List<Group> privateGroups = filterGroups(allGroups, false);
                List<Group> publicGroups = filterGroups(allGroups, true);

                // Combine the lists of private and public groups
                allGroups.clear(); // Clear the list before adding filtered groups
                allGroups.addAll(privateGroups); // Add private groups first
                allGroups.addAll(publicGroups); // Add public groups

                // Notify the listener with the retrieved groups
                listener.onGroupsFetched(allGroups);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Notify the listener if there's an error
                listener.onFetchFailed(databaseError.toException());
            }
        });
    }

    private List<Group> filterGroups(List<Group> groups, boolean isPublic) {
        List<Group> filteredGroups = new ArrayList<>();
        for (Group group : groups) {
            if (group.isPublic() == isPublic) {
                filteredGroups.add(group);
            }
        }
        return filteredGroups;
    }

    // Define an interface for the callback
    public interface GroupsFetchListener {
        void onGroupsFetched(List<Group> groups);
        void onFetchFailed(Exception e);
    }

    public void removeUserFromGroup(String groupName, String userId, GroupOperationListener listener) {
        DatabaseReference groupMembersRef = groupsRef.child("private").child(groupName).child("members");
        // Find the key corresponding to the user ID in the group's members list
        groupMembersRef.orderByValue().equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get the key of the user ID in the group's members list
                    String key = snapshot.getKey();

                    // Remove the member using the key
                    groupMembersRef.child(key).removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Notify the listener about the success
                                    listener.onGroupOperationSuccess("You have left the group.");
                                } else {
                                    // Notify the listener about the failure
                                    listener.onGroupOperationFailure("Failed to leave the group. Please try again.");
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Notify the listener about the cancellation
                listener.onGroupOperationFailure("Error leaving group: " + databaseError.getMessage());
            }
        });
    }

    // Define an interface for group operation callbacks
    public interface GroupOperationListener {
        void onGroupOperationSuccess(String message);
        void onGroupOperationFailure(String errorMessage);
    }
}
