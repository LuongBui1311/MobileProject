package com.hcmute.endsemesterproject.Services;

import android.provider.ContactsContract;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmute.endsemesterproject.Models.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupService {
    private DatabaseReference groupsRef;

    public GroupService() {
        // Initialize Firebase Database reference for groups
        groupsRef = FirebaseDatabase.getInstance().getReference().child("beta-groups");
    }

    public void createGroup(String name, String description, boolean isPublic, String ownerId, List<String> memberIds, GroupOperationListener listener) {
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("beta-groups");

        DatabaseReference newGroupRef = groupsRef.push();
        String groupId = newGroupRef.getKey();

        // Create the group structure
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("name", name);
        groupData.put("description", description);
        groupData.put("owner-id", ownerId);
        groupData.put("is-public", isPublic);

        // Add the members to the group structure
        Map<String, Object> membersData = new HashMap<>();
        for (int i = 0; i < memberIds.size(); i++) {
            membersData.put(String.valueOf(i), memberIds.get(i));
        }
        groupData.put("members", membersData);

        // Set the group data to the database
        newGroupRef.setValue(groupData)
                .addOnSuccessListener(aVoid -> listener.onGroupOperationSuccess("Group created successfully."))
                .addOnFailureListener(e -> listener.onGroupOperationFailure("Failed to create group: " + e.getMessage()));
    }

    public void retrieveGroups(String userId, final GroupsFetchListener listener) {
        List<Group> groups = new ArrayList<>();

        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    String groupId = groupSnapshot.getKey();
                    String name = groupSnapshot.child("name").getValue(String.class);
                    String description = groupSnapshot.child("description").getValue(String.class);
                    String ownerId = groupSnapshot.child("owner-id").getValue(String.class);
                    boolean isPublic = groupSnapshot.child("is-public").getValue(Boolean.class);

                    // Check if the user is a member of the group
                    boolean isMember = false;
                    for (DataSnapshot memberSnapshot : groupSnapshot.child("members").getChildren()) {
                        if (memberSnapshot.getValue(String.class).equals(userId)) {
                            isMember = true;
                            break;
                        }
                    }

                    Group group = new Group(groupId, name, description, (int) groupSnapshot.child("members").getChildrenCount(), isPublic, ownerId);
                    if (isPublic) {
                        if (isMember) {
                            groups.add(0, group);
                        } else {
                            groups.add(group);
                        }
                    } else {
                        if (isMember) {
                            groups.add(group);
                        }
                    }
                    // Filter the groups list to get private then public groups
                    List<Group> privateGroups = filterGroups(groups, false);
                    List<Group> publicGroups = filterGroups(groups, true);

                    // Combine the lists of private and public groups
                    groups.clear(); // Clear the list before adding filtered groups
                    groups.addAll(privateGroups); // Add private groups first
                    groups.addAll(publicGroups); // Add public groups

                    // Notify the listener with the retrieved groups
                    listener.onGroupsFetched(groups);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFetchFailed(error.toException());
            }
        });
    }

    private List<Group> filterGroups(List<Group> groups, boolean isPublic) {
        List<Group> filteredGroups = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);
            if (group.isPublic() == isPublic) {
                filteredGroups.add(group);
            }
        }
        return filteredGroups;
    }

    public void getUserIdsInGroup(String groupId, UserIdsFetchListener listener) {
        DatabaseReference groupMembersRef = groupsRef.child(groupId).child("members");

        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> userIds = new ArrayList<>();
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    String userId = memberSnapshot.getValue(String.class);
                    userIds.add(userId);
                }
                listener.onUserIdsFetched(userIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFetchFailed(databaseError.toException());
            }
        });
    }

    // Define an interface for the callback
    public interface UserIdsFetchListener {
        void onUserIdsFetched(List<String> userIds);
        void onFetchFailed(Exception e);
    }

    // Define an interface for the callback
    public interface GroupsFetchListener {
        void onGroupsFetched(List<Group> groups);
        void onFetchFailed(Exception e);
    }

    public void removeUserFromGroup(String groupId, String userId, GroupOperationListener listener) {
        DatabaseReference groupRef = groupsRef.child(groupId).child("members");

        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean userRemoved = false;
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    if (memberSnapshot.getValue(String.class).equals(userId)) {
                        // Remove the member from the group
                        memberSnapshot.getRef().removeValue();
                        userRemoved = true;
                        break; // No need to continue if the user is found and removed
                    }
                }

                if (userRemoved) {
                    listener.onGroupOperationSuccess("User removed from group successfully.");
                } else {
                    listener.onGroupOperationFailure("User not found in group.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onGroupOperationFailure("Error removing user from group: " + databaseError.getMessage());
            }
        });
    }

    public void updateGroupName(String groupId, String newName, GroupOperationListener listener) {
        DatabaseReference groupRef = groupsRef.child(groupId).child("name");

        // Update the group name in the database
        groupRef.setValue(newName)
                .addOnSuccessListener(aVoid -> listener.onGroupOperationSuccess("Group name updated successfully."))
                .addOnFailureListener(e -> listener.onGroupOperationFailure("Failed to update group name: " + e.getMessage()));
    }

    public void updateGroupDescription(String groupId, String newDescription, GroupOperationListener listener) {
        DatabaseReference groupRef = groupsRef.child(groupId).child("description");

        // Update the group description in the database
        groupRef.setValue(newDescription)
                .addOnSuccessListener(aVoid -> listener.onGroupOperationSuccess("Group description updated successfully."))
                .addOnFailureListener(e -> listener.onGroupOperationFailure("Failed to update group description: " + e.getMessage()));
    }

    public void addMembersToGroup(String groupId, List<String> userIds, GroupOperationListener listener) {
        DatabaseReference groupRef = groupsRef.child(groupId).child("members");

        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int maxIndex = -1; // Initialize with -1, assuming indices start from 0
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    int index = Integer.parseInt(memberSnapshot.getKey());
                    if (index > maxIndex) {
                        maxIndex = index;
                    }
                }

                // Loop through the list of user IDs and add them to the group if not already present
                for (String userId : userIds) {
                    // Check if the user ID is already in the group
                    boolean userExists = false;
                    for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                        if (memberSnapshot.getValue(String.class).equals(userId)) {
                            userExists = true;
                            break;
                        }
                    }

                    if (!userExists) {
                        // Increment the maximum index for the new member
                        int newIndex = maxIndex + 1;
                        groupRef.child(String.valueOf(newIndex)).setValue(userId);
                        maxIndex = newIndex; // Update the maximum index
                    }
                }

                // Notify the listener about the success
                listener.onGroupOperationSuccess("Members added to group successfully.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Notify the listener about the failure
                listener.onGroupOperationFailure("Failed to add members to group: " + databaseError.getMessage());
            }
        });
    }



    // Define an interface for group operation callbacks
    public interface GroupOperationListener {
        void onGroupOperationSuccess(String message);
        void onGroupOperationFailure(String errorMessage);
    }

    public void getAllMemberIdsInGroup(String groupId, AllMemberIdsFetchListener listener) {
        DatabaseReference groupMembersRef = groupsRef.child(groupId).child("members");

        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> memberIds = new ArrayList<>();
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    String memberId = memberSnapshot.getValue(String.class);
                    memberIds.add(memberId);
                }
                listener.onAllMemberIdsFetched(memberIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFetchFailed(databaseError.toException());
            }
        });
    }

    // Define an interface for the callback
    public interface AllMemberIdsFetchListener {
        void onAllMemberIdsFetched(List<String> memberIds);
        void onFetchFailed(Exception e);
    }

    public void checkMembership(String groupId, String userId, MembershipCheckListener listener) {
        DatabaseReference groupMembersRef = groupsRef.child(groupId).child("members");

        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isMember = false;
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    String memberId = memberSnapshot.getValue(String.class);
                    if (memberId.equals(userId)) {
                        isMember = true;
                        break;
                    }
                }
                listener.onMembershipChecked(isMember);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onCheckFailed(databaseError.toException());
            }
        });
    }

    // Define an interface for the callback
    public interface MembershipCheckListener {
        void onMembershipChecked(boolean isMember);
        void onCheckFailed(Exception e);
    }

    public void getAllMessageIdsInGroup(String groupId, MessageIdsFetchListener listener) {
        DatabaseReference groupMessagesRef = groupsRef.child(groupId).child("messages");

        groupMessagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> messageIds = new ArrayList<>();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String messageId = messageSnapshot.getKey();
                    messageIds.add(messageId);
                }
                listener.onMessageIdsFetched(messageIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFetchFailed(databaseError.toException());
            }
        });
    }

    // Define an interface for the callback
    public interface MessageIdsFetchListener {
        void onMessageIdsFetched(List<String> messageIds);
        void onFetchFailed(Exception e);
    }


}
