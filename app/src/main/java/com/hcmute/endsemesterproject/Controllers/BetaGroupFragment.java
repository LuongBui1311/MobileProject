package com.hcmute.endsemesterproject.Controllers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmute.endsemesterproject.Adapters.GroupAdapter;
import com.hcmute.endsemesterproject.Models.Group;
import com.hcmute.endsemesterproject.R;

import java.util.ArrayList;
import java.util.List;

public class BetaGroupFragment extends Fragment {

    private View betaGroupFragmentView;
    private ListView groupsListView;
    private DatabaseReference betaGroupsRef;
    private GroupAdapter groupAdapter;

    private List<Group> allGroups;

    public BetaGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        betaGroupFragmentView = inflater.inflate(R.layout.fragment_beta_group, container, false);

        groupsListView = (ListView) betaGroupFragmentView.findViewById(R.id.groupsListView);
        betaGroupsRef =  FirebaseDatabase.getInstance().getReference().child("beta-groups");
        allGroups = new ArrayList<Group>();

        retrieveGroups();

        groupAdapter = new GroupAdapter(requireContext(), allGroups);
        groupsListView.setAdapter(groupAdapter);

        groupAdapter.setLeaveGroupCallback(new GroupAdapter.LeaveGroupCallback() {
            @Override
            public void onLeaveGroup(Group group) {
                leaveGroup(group);
            }
        });

        groupsListView.setItemsCanFocus(false);


        // Set item click listener for the ListView
        groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Group clickedGroup = allGroups.get(position);
                Intent intent = new Intent(requireContext(), BetaGroupChatActivity.class);
                intent.putExtra("groupObject", clickedGroup);
                Log.d("item click debug", "item clicked");
                startActivity(intent);
            }
        });

        return betaGroupFragmentView;
    }

    private void leaveGroup(Group group) {
        // Build the confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Leave Group");
        builder.setMessage("Are you sure you want to leave this group?");

        // Add "Yes" button to confirm leaving the group
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Proceed with leaving the group
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference groupMembersRef = FirebaseDatabase.getInstance().getReference()
                        .child("beta-groups")
                        .child("private")
                        .child(group.getName())
                        .child("members");
                removeCurrentUserFromGroup(groupMembersRef, userId);
            }
        });

        // Add "No" button to cancel leaving the group
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        // Show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void removeCurrentUserFromGroup(DatabaseReference groupMembersRef, String userId) {
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
                                    // Optionally, you can refresh the group list after leaving
                                    retrieveGroups();
                                    Toast.makeText(requireContext(), "You have left the group.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Handle leaving group failure
                                    Log.e("BetaGroupFragment", "Error leaving group: " + task.getException().getMessage());
                                    Toast.makeText(requireContext(), "Failed to leave the group. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
    }

    private void retrieveGroups() {
        allGroups.clear();

        // Get the current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the "beta-groups" node
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("beta-groups");

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


                // Filter the groups list to get private then public groups
                List<Group> privateGroups = filterGroups(allGroups, false);
                List<Group> publicGroups = filterGroups(allGroups, true);

                // Combine the lists of private and public groups
                allGroups.clear(); // Clear the list before adding filtered groups
                allGroups.addAll(privateGroups); // Add private groups first
                allGroups.addAll(publicGroups); // Add public groups

                // TODO: Display the list of groups in the ListView
                displayGroups(allGroups);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
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



    private void displayGroups(List<Group> allGroups) {
        // Update the data in the adapter
        groupAdapter.notifyDataSetChanged(); // Notify the adapter that the dataset has changed
    }

}