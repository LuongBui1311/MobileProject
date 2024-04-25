package com.hcmute.endsemesterproject.Controllers;

import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hcmute.endsemesterproject.Adapters.MembersAdapter;
import com.hcmute.endsemesterproject.Models.Group;
import com.hcmute.endsemesterproject.Models.UserDetails;
import com.hcmute.endsemesterproject.R;
import com.hcmute.endsemesterproject.Services.GroupService;
import com.hcmute.endsemesterproject.Services.UserService;

import java.util.ArrayList;
import java.util.List;

public class GroupMembersViewActivity extends AppCompatActivity {

    private ListView listViewMembers;
    private MembersAdapter adapter;
    private List<UserDetails> membersList;
    private List<UserDetails> dumpSelectedListForInterfaceCompatible;
    private Group currentGroup;
    private GroupService groupService;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members_view);

        // Retrieve the currentGroup object from the intent extras
        currentGroup = (Group) getIntent().getSerializableExtra("groupObject");

        groupService = new GroupService();
        userService = new UserService();

        listViewMembers = findViewById(R.id.listViewMembers);
        membersList = new ArrayList<>();
        dumpSelectedListForInterfaceCompatible = new ArrayList<>();

        // Retrieve all user IDs of the current group
        groupService.getUserIdsInGroup(currentGroup.getId(), new GroupService.UserIdsFetchListener() {
            @Override
            public void onUserIdsFetched(List<String> userIds) {
                // Get all user details from the retrieved user ID list
                userService.getUsersDetails(userIds, new UserService.UsersFetchListener() {
                    @Override
                    public void onUsersFetched(List<UserDetails> userDetailsList) {
                        // Populate the member list with user details
                        membersList.clear();
                        membersList.addAll(userDetailsList);
                        // Update the adapter
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFetchFailed(Exception e) {
                        // Handle fetch failure
                    }
                });
            }

            @Override
            public void onFetchFailed(Exception e) {
                // Handle fetch failure
            }
        });

        // Create the adapter
        adapter = new MembersAdapter(this, R.layout.group_member_layout, membersList, dumpSelectedListForInterfaceCompatible);

        // Set the adapter to the ListView
        listViewMembers.setAdapter(adapter);
    }
}
