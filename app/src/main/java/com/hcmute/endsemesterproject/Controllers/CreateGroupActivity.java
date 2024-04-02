package com.hcmute.endsemesterproject.Controllers;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmute.endsemesterproject.Adapters.ContactAdapter;
import com.hcmute.endsemesterproject.Models.Contacts;
import com.hcmute.endsemesterproject.R;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity {
    private ImageView imageViewVerify;
    private ProgressBar progressBarVerify;
    private EditText editTextGroupName;
    private TextView textViewValidation;
    private Button buttonCreate;
    private Button buttonCancel;
    private ListView listViewContacts;
    private DatabaseReference groupsRef;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private List<String> contactIdList;
    private List<Contacts> contactsList;
    private ContactAdapter contactAdapter;
    private RadioButton publicRadioButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mAuth = FirebaseAuth.getInstance();
        groupsRef = FirebaseDatabase.getInstance().getReference().child("beta-groups");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        imageViewVerify  = findViewById(R.id.imageViewVerify);
        progressBarVerify = findViewById(R.id.progressBarVerify);
        editTextGroupName = findViewById(R.id.editTextGroupName);
        textViewValidation = findViewById(R.id.textViewValidation);
        buttonCreate = findViewById(R.id.buttonCreate);
        buttonCreate.setEnabled(false);
        buttonCancel = findViewById(R.id.buttonCancel);
        publicRadioButton = findViewById(R.id.publicRadioButton);

        contactIdList = new ArrayList<String>();
        contactsList = new ArrayList<Contacts>();

        loadContactIdList();

        listViewContacts = findViewById(R.id.listViewContacts);
        contactAdapter = new ContactAdapter(this, R.layout.contact_item, contactsList);
        listViewContacts.setAdapter(contactAdapter);


        textViewValidation.setText("");


        // Add a text change listener to the group name edit text
        editTextGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Reset the creatable flag whenever there is a change in the group name
                buttonCreate.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Nothing to do here
            }
        });

        imageViewVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the green tick ImageView
                imageViewVerify.setVisibility(View.INVISIBLE);

                // Show the ProgressBar (spinner)
                progressBarVerify.setVisibility(View.VISIBLE);

                // Get the group name from the EditText
                String groupName = editTextGroupName.getText().toString();

                // Verify the group name
                if (!groupName.isEmpty()) {
                    verifyGroupName(groupName);
                } else {
                    // If group name is empty, display a message
                    Toast.makeText(CreateGroupActivity.this, "Please enter a group name", Toast.LENGTH_SHORT).show();

                    // Hide the ProgressBar and show the green tick ImageView again
                    progressBarVerify.setVisibility(View.INVISIBLE);
                    imageViewVerify.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(CreateGroupActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });

        listViewContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                contactAdapter.toggleSelection(position);
            }
        });

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
    }

    private void createGroup() {
        String groupName = editTextGroupName.getText().toString();
        String groupType = publicRadioButton.isChecked() ? "public" : "private";
        List<Contacts> contactsList1= contactAdapter.getSelectedContacts();
        List<String> userIdList = new ArrayList<>();
        userIdList.add(mAuth.getCurrentUser().getUid());
        for (Contacts contacts : contactsList1) {
            userIdList.add(contacts.getId());
        }
        // do the firebase stuff here
        DatabaseReference groupsCategoryRef = FirebaseDatabase.getInstance().getReference().child("beta-groups").child(groupType);
        DatabaseReference groupRef = groupsCategoryRef.child(groupName);
        groupRef.child("messages");
        groupRef.child("members").setValue(userIdList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Group and members added successfully
                        Toast.makeText(CreateGroupActivity.this, "Group created successfully", Toast.LENGTH_SHORT).show();
                        // Optionally, navigate to another activity or perform additional actions
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to create group or add members
                        Toast.makeText(CreateGroupActivity.this, "Failed to create group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        buttonCreate.setEnabled(false);
    }

    private void loadContactInformations() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Assuming you have a list of contact IDs stored in contactIdList
            for (String contactId : contactIdList) {
                // Get the reference to the contact node in the Users database
                DatabaseReference contactRef = usersRef.child(contactId);

                // Read the data from the contact node
                contactRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Extract the contact details from the dataSnapshot
                            String name = dataSnapshot.child("name").getValue(String.class);
                            String status = dataSnapshot.child("status").getValue(String.class);
                            String image = dataSnapshot.child("image").getValue(String.class);

                            // Construct the Contact model using the retrieved details
                            Contacts contact = new Contacts(name, status, image);
                            contact.setId(contactId);

                            // Now you can use the contact object as needed (e.g., add it to a list)
                            contactsList.add(contact);
                            contactAdapter.notifyDataSetChanged();
                            // Print debug information
                            Log.d("cd", "ContactDebug - Name: " + name + ", Status: " + status + ", Image: " + image);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle any errors
                        Log.d("cd", "ContactDebug - Error loading contact: " + databaseError.getMessage());

                    }
                });
            }
            contactAdapter.notifyDataSetChanged();

        }
    }

    private void loadContactIdList() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserId = currentUser.getUid();
        DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot contactSnapshot : dataSnapshot.getChildren()) {
                    // Iterate through each child of the current user's node, which represents a contact
                    String contactId = contactSnapshot.getKey();
                    // Add the contact ID to the list
                    contactIdList.add(contactId);
                }
                // After loading the contact ID list, you can perform any necessary operations
                // For example, you may want to update the UI or trigger another action
                // Here, I'll simply print the contact ID list
                loadContactInformations();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(CreateGroupActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void verifyGroupName(String groupName) {
        groupsRef.child("public").child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The group name already exists in the public category
                    showGroupNameExistsMessage(groupName);
                } else {
                    // Check if the group name exists in the private category
                    checkPrivateGroupName(groupName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(CreateGroupActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressBar();
            }
        });
    }

    private void checkPrivateGroupName(String groupName) {
        groupsRef.child("private").child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The group name already exists in the private category
                    showGroupNameExistsMessage(groupName);
                } else {
                    // The group name does not exist in either category
                    showGroupAvailableMessage(groupName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(CreateGroupActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressBar();
            }
        });
    }

    private void showGroupNameExistsMessage(String groupName) {
        Toast.makeText(CreateGroupActivity.this, "Group name already exists", Toast.LENGTH_SHORT).show();
        textViewValidation.setText("Group named " + groupName + " already exists. Please choose another name.");
        textViewValidation.setTextColor(Color.rgb(228, 8, 10));
        buttonCreate.setEnabled(false);
        hideProgressBar();
    }

    private void showGroupAvailableMessage(String groupName) {
        Toast.makeText(CreateGroupActivity.this, "Group name available", Toast.LENGTH_SHORT).show();
        textViewValidation.setText("Group named " + groupName + " available. Let's go.");
        textViewValidation.setTextColor(Color.rgb(125, 218, 88));
        buttonCreate.setEnabled(true);
        hideProgressBar();
    }

    private void hideProgressBar() {
        progressBarVerify.setVisibility(View.INVISIBLE);
        imageViewVerify.setVisibility(View.VISIBLE);
    }


}