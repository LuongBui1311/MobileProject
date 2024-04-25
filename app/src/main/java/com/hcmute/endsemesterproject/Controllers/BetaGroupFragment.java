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
import com.hcmute.endsemesterproject.Services.GroupService;

import java.util.ArrayList;
import java.util.List;

public class BetaGroupFragment extends Fragment {

    private View betaGroupFragmentView;
    private ListView groupsListView;
    private DatabaseReference betaGroupsRef;
    private GroupAdapter groupAdapter;

    private List<Group> allGroups;

    private GroupService groupService;
    public BetaGroupFragment() {
        // Required empty public constructor
        groupService = new GroupService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        betaGroupFragmentView = inflater.inflate(R.layout.fragment_beta_group, container, false);

        groupsListView = (ListView) betaGroupFragmentView.findViewById(R.id.groupsListView);
        betaGroupsRef =  FirebaseDatabase.getInstance().getReference().child("beta-groups");
        allGroups = new ArrayList<Group>();

        groupAdapter = new GroupAdapter(requireContext(), allGroups);
        groupsListView.destroyDrawingCache();
        groupsListView.setVisibility(ListView.INVISIBLE);
        groupsListView.setVisibility(ListView.VISIBLE);
        groupsListView.setAdapter(groupAdapter);

        retrieveGroups();


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

    private void retrieveGroups() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        groupService.retrieveGroups(userId, new GroupService.GroupsFetchListener() {
            @Override
            public void onGroupsFetched(List<Group> groups) {
                allGroups.clear();
                allGroups.addAll(groups);
                groupAdapter.setGroups(allGroups);
                groupsListView.setAdapter(groupAdapter);
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFetchFailed(Exception e) {
                Log.d("group fetch error", e.getMessage());
            }
        });
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
                groupService.removeUserFromGroup(group.getName(), userId, new GroupService.GroupOperationListener() {
                    @Override
                    public void onGroupOperationSuccess(String message) {
                        retrieveGroups();
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onGroupOperationFailure(String errorMessage) {
                        // Handle leaving group failure
                        Log.e("BetaGroupFragment", "Error leaving group: " + errorMessage);
                        Toast.makeText(requireContext(), "Failed to leave the group. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
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

}