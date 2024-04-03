package com.hcmute.endsemesterproject.Controllers;

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

    private void retrieveGroups() {
        // Create tasks for retrieving private and public groups
        Task<List<Group>> privateGroupsTask = retrievePrivateGroupsFromDatabase();
        Task<List<Group>> publicGroupsTask = retrievePublicGroupsFromDatabase();

        // Combine both tasks into a single task
        Task<List<Group>> combinedTask = Tasks.whenAllSuccess(privateGroupsTask, publicGroupsTask);

        // Add an onCompleteListener to the combined task
        combinedTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Group> privateGroups = privateGroupsTask.getResult();
                List<Group> publicGroups = publicGroupsTask.getResult();

                // Combine the lists of private and public groups
                allGroups.addAll(privateGroups); // Add private groups first
                allGroups.addAll(publicGroups); // Add public groups

                // Now you have the combined list of groups with private groups on top and public at the bottom
                // You can proceed to display the list

                // TODO: Display the list of groups in the ListView
                displayGroups(allGroups);
            } else {
                // Handle task failure
                Exception exception = task.getException();
                if (exception != null) {
                    Log.e("BetaGroupFragment", "Error retrieving groups: " + exception.getMessage());
                }
            }
        });
    }

    private void displayGroups(List<Group> allGroups) {
        // Update the data in the adapter
        groupAdapter.notifyDataSetChanged(); // Notify the adapter that the dataset has changed
    }




    public Task<List<Group>> retrievePrivateGroupsFromDatabase() {
        // Create a new task to retrieve private groups from the database
        TaskCompletionSource<List<Group>> taskCompletionSource = new TaskCompletionSource<>();

        // Get the current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the "private" node under "beta-groups"
        DatabaseReference privateGroupsRef = betaGroupsRef.child("private");

        // Add a listener for retrieving private groups
        privateGroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Group> privateGroups = new ArrayList<>();
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    boolean joined = false;
                    // Check if the user is a member of the group
                    for (DataSnapshot child : groupSnapshot.child("members").getChildren()) {
                        // Print the key (node name) of the child
                        System.out.println("Key: " + child.getKey());
                        System.out.println("Value: " + child.getValue());
                        if (userId.equals(child.getValue())) {
                            joined = true;
                            break;
                        }
                    }
                    if (joined) {
                        String groupName = groupSnapshot.getKey();
                        Group group = new Group();
                        group.setName(groupName);
                        group.setPublic(false);
                        group.setDescription("This is default description.");
                        group.setNumberOfMembers(groupSnapshot.child("members").getChildrenCount());
                        privateGroups.add(group);
                    }
                }
                // Set the result of the task
                taskCompletionSource.setResult(privateGroups);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Set an exception for the task
                taskCompletionSource.setException(databaseError.toException());
            }
        });

        // Return the task associated with this listener
        return taskCompletionSource.getTask();
    }

    private Task<List<Group>> retrievePublicGroupsFromDatabase() {
        // Create a new task to retrieve public groups from the database
        TaskCompletionSource<List<Group>> taskCompletionSource = new TaskCompletionSource<>();

        // Reference to the "public" node under "beta-groups"
        DatabaseReference publicGroupsRef = betaGroupsRef.child("public");

        // Add a listener for retrieving public groups
        publicGroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Group> publicGroups = new ArrayList<>();
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve the group name
                    String groupName = groupSnapshot.getKey();
                    Group group = new Group();
                    group.setName(groupName);
                    group.setPublic(true);
                    group.setDescription("This is default description.");
                    group.setNumberOfMembers(groupSnapshot.child("members").getChildrenCount());

                    // Add the group to the list of public groups
                    publicGroups.add(group);
                }
                // Set the result of the task
                taskCompletionSource.setResult(publicGroups);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Set an exception for the task
                taskCompletionSource.setException(databaseError.toException());
            }
        });

        // Return the task associated with this listener
        return taskCompletionSource.getTask();
    }

}