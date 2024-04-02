package com.hcmute.endsemesterproject.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmute.endsemesterproject.Adapters.GroupMessageAdapter;
import com.hcmute.endsemesterproject.Models.Group;
import com.hcmute.endsemesterproject.Models.GroupMessage;
import com.hcmute.endsemesterproject.R;

import java.util.ArrayList;
import java.util.List;

public class BetaGroupChatActivity extends AppCompatActivity {
    private Group currentGroup;
    private Button sendMessageButton;
    private EditText messageInput;
    private RecyclerView messageRecyclerView;
    private List<GroupMessage> messageList;
    private TextView groupTitle;

    private DatabaseReference betaGroupsRef;
    private DatabaseReference messagesRef;
    private GroupMessageAdapter messageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beta_group_chat);

        betaGroupsRef = FirebaseDatabase.getInstance().getReference().child("beta-groups");

        loadGroupInfo();

        // Determine the messages reference based on the group's privacy status
        if (currentGroup != null) {
            if (currentGroup.isPublic()) {
                messagesRef = betaGroupsRef.child("public").child(currentGroup.getName()).child("messages");
            } else {
                messagesRef = betaGroupsRef.child("private").child(currentGroup.getName()).child("messages");
            }
        }

        sendMessageButton = (Button) findViewById(R.id.send_message_button);
        messageInput = (EditText) findViewById(R.id.message_input);
        messageRecyclerView = findViewById(R.id.messages_list);
        groupTitle = (TextView) findViewById(R.id.group_title);
        groupTitle.setText(currentGroup.getName());

        // Initialize message list
        messageList = new ArrayList<>();

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Initialize message adapter
        messageAdapter = new GroupMessageAdapter(messageList, currentUserId);
        messageRecyclerView.setAdapter(messageAdapter);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        loadMessagesFromFirebase();
    }

    private void loadMessagesFromFirebase() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear(); // Clear existing messages
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Deserialize GroupMessage from Firebase snapshot
                    GroupMessage message = snapshot.getValue(GroupMessage.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged(); // Notify adapter about data change
                if (messageList.size() != 0) {
                    messageRecyclerView.smoothScrollToPosition(messageList.size()-1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e("BetaGroupChatActivity", "Failed to load messages: " + databaseError.getMessage());
            }
        });
    }

    private void loadGroupInfo() {
        Intent intent = getIntent();
        this.currentGroup = (Group) intent.getSerializableExtra("groupObject");
    }


    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();

        if (!messageText.isEmpty()) {
            // Check if the currentGroup is null or invalid
            if (currentGroup == null || currentGroup.getName() == null) {
                Log.e("BetaGroupChatActivity", "Invalid group information");
                return;
            }

            // Generate a unique message ID
            String messageId = messagesRef.push().getKey();

            // Get current user ID
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Get current timestamp
            long timestamp = System.currentTimeMillis();

            // Create the message object
            GroupMessage message = new GroupMessage(messageId, userId, messageText, timestamp);

            // Save the message to the database
            messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Message sent successfully
                            Log.d("BetaGroupChatActivity", "Message sent successfully");
                            messageInput.setText(""); // Clear the input field after sending
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to send message
                            Log.e("BetaGroupChatActivity", "Failed to send message: " + e.getMessage());
                            // Handle failure
                        }
                    });
        } else {
            // Handle case where message text is empty
            Log.e("BetaGroupChatActivity", "Message text is empty");
        }
    }

}