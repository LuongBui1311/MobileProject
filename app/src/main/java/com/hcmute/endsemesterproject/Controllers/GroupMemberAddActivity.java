package com.hcmute.endsemesterproject.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.hcmute.endsemesterproject.Adapters.MembersAdapter;
import com.hcmute.endsemesterproject.Models.Group;
import com.hcmute.endsemesterproject.Models.UserDetails;
import com.hcmute.endsemesterproject.R;
import com.hcmute.endsemesterproject.Services.ContactService;
import com.hcmute.endsemesterproject.Services.GroupService;
import com.hcmute.endsemesterproject.Services.UserService;

import java.util.ArrayList;
import java.util.List;

public class GroupMemberAddActivity extends AppCompatActivity {

    private List<UserDetails> allUsersList; // List of all users
    private List<UserDetails> selectedUsersList; // List of selected users
    private ListView listViewUsers;
    private Button acceptButton;
    private Button cancelButton;
    private MembersAdapter adapter;
    private GroupService groupService;
    private ContactService contactService;
    private UserService userService;
    private Group currentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member_add);

        currentGroup = (Group) getIntent().getSerializableExtra("groupObject");


        listViewUsers = findViewById(R.id.listViewUsers);
        acceptButton = findViewById(R.id.acceptButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Initialize lists
        selectedUsersList = new ArrayList<>();

        // Initialize lists
        allUsersList = new ArrayList<>(); // Initialize with your user data

        // Create adapter
        adapter = new MembersAdapter(this, R.layout.group_member_layout, allUsersList, selectedUsersList);
        listViewUsers.setAdapter(adapter);

        // Fetch users already in the group
        groupService = new GroupService();
        groupService.getUserIdsInGroup(currentGroup.getId(), new GroupService.UserIdsFetchListener() {
            @Override
            public void onUserIdsFetched(List<String> groupUserIds) {
                // Fetch contacts of the current user
                contactService = new ContactService();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                contactService.getContactsByUserId(userId, new ContactService.ContactsFetchListener() {
                    @Override
                    public void onContactsFetched(List<String> contactUserIds) {
                        // Remove group members from contacts
                        contactUserIds.removeAll(groupUserIds);

                        // Fetch user details for remaining contact ids
                        userService = new UserService();
                        for (String userId : contactUserIds) {
                            userService.getUserDetails(userId, new UserService.UserFetchListener() {
                                @Override
                                public void onUserFetched(UserDetails userDetails) {
                                    allUsersList.add(userDetails);
                                    adapter.notifyDataSetChanged(); // Update list view
                                }

                                @Override
                                public void onFetchFailed(Exception e) {
                                    // Handle error
                                }
                            });
                        }
                    }

                    @Override
                    public void onFetchFailed(Exception e) {
                        // Handle error
                    }
                });
            }

            @Override
            public void onFetchFailed(Exception e) {
                // Handle error
            }
        });

        // List view item click listener to handle user selection
        listViewUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserDetails clickedUser = allUsersList.get(position);
                if (selectedUsersList.contains(clickedUser)) {
                    selectedUsersList.remove(clickedUser); // Deselect user
                } else {
                    selectedUsersList.add(clickedUser); // Select user
                }
                adapter.notifyDataSetChanged(); // Update list view
            }
        });

        // Accept button click listener
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return selected users list to calling activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedUsers", new ArrayList<>(selectedUsersList));
                setResult(RESULT_OK, resultIntent);
                finish(); // Close this activity
            }
        });

        // Cancel button click listener
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return nothing to calling activity
                setResult(RESULT_CANCELED);
                finish(); // Close this activity
            }
        });
    }
}
