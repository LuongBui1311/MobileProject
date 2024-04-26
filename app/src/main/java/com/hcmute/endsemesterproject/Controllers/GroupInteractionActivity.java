package com.hcmute.endsemesterproject.Controllers;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.hcmute.endsemesterproject.Models.Group;
import com.hcmute.endsemesterproject.Models.UserDetails;
import com.hcmute.endsemesterproject.R;
import com.hcmute.endsemesterproject.Services.GroupService;

import java.util.ArrayList;
import java.util.List;

public class GroupInteractionActivity extends AppCompatActivity {
    private Group currentGroup;
    private boolean isNameEditing = false;
    private boolean isDescriptionEditing = false;

    private ImageButton editNameButton;
    private ImageButton editDescriptionButton;
    private EditText groupNameEditText;
    private EditText groupDescriptionEditText;
    private GroupService groupService;
    private Button viewMembersButton;
    private Button addMembersButton;
    private Button leaveGroupButton;
    private static final int REQUEST_CODE_ADD_MEMBERS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_interaction);
        // Retrieve the currentGroup object from the intent extras
        Group currentGroup = (Group) getIntent().getSerializableExtra("groupObject");

        groupService = new GroupService();

        editNameButton = findViewById(R.id.edit_name_button);
        editDescriptionButton = findViewById(R.id.edit_description_button);
        groupNameEditText = findViewById(R.id.group_name);
        groupDescriptionEditText = findViewById(R.id.group_description);
        viewMembersButton = findViewById(R.id.view_members_button);
        addMembersButton = findViewById(R.id.add_member_button);
        leaveGroupButton = findViewById(R.id.leave_group_button);

        groupNameEditText.setEnabled(false);
        groupDescriptionEditText.setEnabled(false);

        groupNameEditText.setText(currentGroup.getName());
        groupDescriptionEditText.setText(currentGroup.getDescription());

        editNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNameEditing) {
                    groupService.updateGroupName(currentGroup.getId(), groupNameEditText.getText().toString(), new GroupService.GroupOperationListener() {
                        @Override
                        public void onGroupOperationSuccess(String message) {
                            Drawable newDrawable = getResources().getDrawable(R.drawable.ic_edit);
                            editNameButton.setImageDrawable(newDrawable);
                            groupNameEditText.setEnabled(false);
                            isNameEditing = false;
                            Toast.makeText(GroupInteractionActivity.this, message, Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onGroupOperationFailure(String errorMessage) {
                            Toast.makeText(GroupInteractionActivity.this, "Failed: " + errorMessage, Toast.LENGTH_SHORT).show();

                        }
                    });

                } else {
                    Drawable newDrawable = getResources().getDrawable(R.drawable.ic_done);
                    editNameButton.setImageDrawable(newDrawable);
                    groupNameEditText.setEnabled(true);
                    isNameEditing = true;
                }
            }
        });

        editDescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDescriptionEditing) {
                    groupService.updateGroupDescription(currentGroup.getId(), groupDescriptionEditText.getText().toString(), new GroupService.GroupOperationListener() {
                        @Override
                        public void onGroupOperationSuccess(String message) {
                            Drawable newDrawable = getResources().getDrawable(R.drawable.ic_edit);
                            editDescriptionButton.setImageDrawable(newDrawable);
                            groupDescriptionEditText.setEnabled(false);
                            isDescriptionEditing = false;
                            Toast.makeText(GroupInteractionActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onGroupOperationFailure(String errorMessage) {
                            Toast.makeText(GroupInteractionActivity.this, "Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Drawable newDrawable = getResources().getDrawable(R.drawable.ic_done);
                    editDescriptionButton.setImageDrawable(newDrawable);
                    groupDescriptionEditText.setEnabled(true);
                    isDescriptionEditing = true;
                }
            }
        });

        viewMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the GroupMembersViewActivity and pass the current group object
                Intent intent = new Intent(GroupInteractionActivity.this, GroupMembersViewActivity.class);
                intent.putExtra("groupObject", currentGroup);
                startActivity(intent);
            }
        });

        addMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the GroupMemberAddActivity and wait for result
                Intent intent = new Intent(GroupInteractionActivity.this, GroupMemberAddActivity.class);
                intent.putExtra("groupObject", currentGroup);
                startActivityForResult(intent, REQUEST_CODE_ADD_MEMBERS);
            }
        });

        leaveGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupService.removeUserFromGroup(currentGroup.getId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), new GroupService.GroupOperationListener() {
                    @Override
                    public void onGroupOperationSuccess(String message) {
                        // Leave group successful, navigate to MainActivity
                        Intent intent = new Intent(GroupInteractionActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear back stack
                        startActivity(intent);
                    }

                    @Override
                    public void onGroupOperationFailure(String errorMessage) {
                        // Leave group failed, show toast to try again
                        Toast.makeText(GroupInteractionActivity.this, "Failed to leave group: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_MEMBERS && resultCode == RESULT_OK && data != null) {
            ArrayList<UserDetails> selectedUsers = (ArrayList<UserDetails>) data.getSerializableExtra("selectedUsers");
            // Check if the selectedUsers list is not empty
            if (selectedUsers != null && !selectedUsers.isEmpty()) {
                // Extract user IDs from selectedUsers list
                List<String> userIds = new ArrayList<>();
                for (UserDetails user : selectedUsers) {
                    userIds.add(user.getUid()); // Assuming getId() returns the user ID
                }

                currentGroup = (Group) getIntent().getSerializableExtra("groupObject");
                // Call addMembersToGroup method with the extracted user IDs and group ID
                addMembersToGroup(currentGroup.getId(), userIds);
            } else {
                Log.d("SelectedUser", "No users selected");
            }
        }
    }

    private void addMembersToGroup(String groupId, List<String> userIds) {
        // Call the addMembersToGroup method from GroupService
        GroupService groupService = new GroupService();
        groupService.addMembersToGroup(groupId, userIds, new GroupService.GroupOperationListener() {
            @Override
            public void onGroupOperationSuccess(String message) {
                // Handle success
                Log.d("AddMembersToGroup", "Members added successfully: " + message);
            }

            @Override
            public void onGroupOperationFailure(String errorMessage) {
                // Handle failure
                Log.e("AddMembersToGroup", "Failed to add members: " + errorMessage);
            }
        });
    }




}