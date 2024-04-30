package com.hcmute.endsemesterproject.Services;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactService {
    private DatabaseReference contactsRef;

    public ContactService() {
        // Initialize Firebase Database reference for contacts
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
    }

    public void getContactsByUserId(String userId, ContactsFetchListener listener) {
        DatabaseReference userContactsRef = contactsRef.child(userId);

        userContactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> contactUserIds = new ArrayList<>();
                for (DataSnapshot contactSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> contactData = (Map<String, Object>) contactSnapshot.getValue();
                    if (contactData != null && contactData.containsKey("Contact")) {
                        String state = (String) contactData.get("Contact");
                        // Check if the state is "Saved"
                        if ("Saved".equals(state)) {
                            contactUserIds.add(contactSnapshot.getKey());
                        }
                    }
                }
                listener.onContactsFetched(contactUserIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFetchFailed(databaseError.toException());
            }
        });
    }

    // Define an interface for the callback
    public interface ContactsFetchListener {
        void onContactsFetched(List<String> contactUserIds);
        void onFetchFailed(Exception e);
    }
}
