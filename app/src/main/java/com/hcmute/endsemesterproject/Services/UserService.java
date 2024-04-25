package com.hcmute.endsemesterproject.Services;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmute.endsemesterproject.Models.UserDetails;

import java.util.ArrayList;
import java.util.List;

public class UserService {
    private DatabaseReference usersRef;

    public UserService() {
        // Initialize Firebase Database reference for users
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public void getUserDetails(String userId, final UserFetchListener listener) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
                    listener.onUserFetched(userDetails);
                } else {
                    listener.onFetchFailed(new Exception("User not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFetchFailed(databaseError.toException());
            }
        });
    }

    public void getUsersDetails(List<String> userIds, final UsersFetchListener listener) {
        final List<UserDetails> userDetailsList = new ArrayList<>();

        for (String userId : userIds) {
            getUserDetails(userId, new UserFetchListener() {
                @Override
                public void onUserFetched(UserDetails userDetails) {
                    userDetailsList.add(userDetails);
                    if (userDetailsList.size() == userIds.size()) {
                        listener.onUsersFetched(userDetailsList);
                    }
                }

                @Override
                public void onFetchFailed(Exception e) {
                    listener.onFetchFailed(e);
                }
            });
        }
    }

    // Define interfaces for the callback
    public interface UserFetchListener {
        void onUserFetched(UserDetails userDetails);

        void onFetchFailed(Exception e);
    }

    public interface UsersFetchListener {
        void onUsersFetched(List<UserDetails> userDetailsList);

        void onFetchFailed(Exception e);
    }
}
