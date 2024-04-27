package com.hcmute.endsemesterproject.Controllers;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hcmute.endsemesterproject.Adapters.GroupMessageAdapter;
import com.hcmute.endsemesterproject.Models.BetaGroupMessage;
import com.hcmute.endsemesterproject.Models.Group;
import com.hcmute.endsemesterproject.Models.UserDetails;
import com.hcmute.endsemesterproject.R;
import com.hcmute.endsemesterproject.Services.BetaMessageService;
import com.hcmute.endsemesterproject.Services.GroupService;
import com.hcmute.endsemesterproject.Services.UserService;

import java.util.ArrayList;
import java.util.List;

public class BetaGroupChatActivity extends AppCompatActivity {
    private Group currentGroup;
    private Button sendMessageButton;
    private ImageButton sendFileButton;
    private EditText messageInput;
    private RecyclerView messageRecyclerView;
    private List<BetaGroupMessage> messageList;
    private TextView groupTitle;

    private LinearLayout chatPromtLayout;

    private DatabaseReference betaGroupsRef;
    private DatabaseReference messagesRef;
    private GroupMessageAdapter messageAdapter;

    private static final int PICK_FILE_REQUEST = 1;
    private static final String[] SUPPORTED_MIME_TYPES = {
            "image/jpeg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/pdf"
    };

    private Uri selectedFileUri;
    private ImageView selectedFileImageView;
    private TextView selectedFileTextView;
    private LinearLayout selectedFilePlaceholder;
    private ImageButton dotsButton;
    private TextView chatPromptTextView;
    private Button joinGroupButton;

    private GroupService groupService;
    private boolean isGroupMember = false;
    private UserService userService;
    private BetaMessageService betaMessageService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beta_group_chat);

        betaGroupsRef = FirebaseDatabase.getInstance().getReference().child("beta-groups");
        userService = new UserService();
        groupService = new GroupService();
        betaMessageService = new BetaMessageService();
        loadGroupInfo();

        sendMessageButton = findViewById(R.id.send_message_button);
        sendFileButton = findViewById(R.id.sendFileButton);
        messageInput = findViewById(R.id.message_input);
        chatPromptTextView = findViewById(R.id.chat_prompt);
        joinGroupButton = findViewById(R.id.join_group_button);
        messageRecyclerView = findViewById(R.id.messages_list);
        groupTitle = findViewById(R.id.group_title);
        groupTitle.setText(currentGroup.getName());
        chatPromtLayout = findViewById(R.id.chat_prompt_layout);
        dotsButton = findViewById(R.id.dots_button);

        sendFileButton.setEnabled(false);
        sendMessageButton.setEnabled(false);
        messageInput.setEnabled(false);

        // Determine the messages reference based on the group's privacy status
        if (currentGroup != null) {
            if (currentGroup.isPublic()) {
                messagesRef = betaGroupsRef.child("public").child(currentGroup.getName()).child("messages");
                // check if this user is member of current group, if so hide the chat promt layout
                checkMembership();

            } else {
                chatPromtLayout.setVisibility(View.GONE);
                messagesRef = betaGroupsRef.child("private").child(currentGroup.getName()).child("messages");
            }
        }


        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> userIdList = new ArrayList<>();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                userIdList.add(uid);

                if (!isGroupMember) {
                    groupService.addMembersToGroup(currentGroup.getId(), userIdList, new GroupService.GroupOperationListener() {
                        @Override
                        public void onGroupOperationSuccess(String message) {
                            checkMembership();
                        }

                        @Override
                        public void onGroupOperationFailure(String errorMessage) {
                            Log.d("join public group error", errorMessage);
                        }
                    });
                } else {
                    groupService.removeUserFromGroup(currentGroup.getId(), uid, new GroupService.GroupOperationListener() {
                        @Override
                        public void onGroupOperationSuccess(String message) {
                            checkMembership();
                        }

                        @Override
                        public void onGroupOperationFailure(String errorMessage) {
                            Log.d("leave public group error", errorMessage);

                        }
                    });
                }

            }
        });


        // Initialize message list
        messageList = new ArrayList<>();

        // Initialize message adapter
        messageAdapter = new GroupMessageAdapter(messageList, FirebaseAuth.getInstance().getCurrentUser().getUid());
        messageRecyclerView.setAdapter(messageAdapter);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        dotsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start GroupInteractionActivity
                Intent intent = new Intent(BetaGroupChatActivity.this, GroupInteractionActivity.class);
                intent.putExtra("groupObject", currentGroup);
                startActivity(intent);
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        selectedFileImageView = findViewById(R.id.selected_file_image_view);
        selectedFileTextView = findViewById(R.id.selected_file_text_view);
        selectedFilePlaceholder = findViewById(R.id.selected_file_placeholder);

        ImageButton sendFileButton = findViewById(R.id.sendFileButton);
        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFilePickerIntent();
            }
        });

        ImageButton removeFileButton = findViewById(R.id.remove_file_button);
        removeFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSelectedFile();
            }
        });

        loadMessagesFromFirebase();
    }

    private void checkMembership() {
        groupService.checkMembership(currentGroup.getId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), new GroupService.MembershipCheckListener() {
            @Override
            public void onMembershipChecked(boolean isMember) {
                isGroupMember = isMember;

                // Update UI based on membership status
                if (isMember) {
                    // User is a member of the group
                    chatPromptTextView.setText("You are a member of this group.");
                    joinGroupButton.setText("Leave Group");
                    joinGroupButton.setBackgroundColor(getResources().getColor(R.color.red));

                    sendFileButton.setEnabled(true);
                    sendMessageButton.setEnabled(true);
                    messageInput.setEnabled(true);
                } else {
                    // User is not a member of the group
                    chatPromptTextView.setText("To chat, please join this group.");
                    joinGroupButton.setText("Join Group");
                    joinGroupButton.setBackgroundColor(getResources().getColor(R.color.blue));
                }
            }

            @Override
            public void onCheckFailed(Exception e) {
                Log.e("BetaGroupChatActivity", "Failed to check membership: " + e.getMessage());

            }
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();

        if (!messageText.isEmpty()) {
            sendMessageButton.setEnabled(false);
            sendTextMessage(messageText);
        }

        if (selectedFileUri != null) {
            sendMessageButton.setEnabled(false);
            sendFileMessage();
        }
    }

    private void startFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, SUPPORTED_MIME_TYPES);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedFileUri = data.getData();
            displaySelectedFile();
        }
    }

    private void displaySelectedFile() {
        if (selectedFileUri != null) {
            String fileName = getFileNameFromUri(selectedFileUri);
            selectedFileTextView.setText(fileName);
            selectedFilePlaceholder.setVisibility(View.VISIBLE);

            String mimeType = getContentResolver().getType(selectedFileUri);
            if (mimeType != null && (mimeType.startsWith("image/") || mimeType.startsWith("application/msword") || mimeType.startsWith("application/vnd.ms-powerpoint") || mimeType.startsWith("application/vnd.ms-excel") || mimeType.equals("application/pdf"))) {
                selectedFileImageView.setImageURI(selectedFileUri);
            } else {
                selectedFileImageView.setImageResource(R.drawable.ic_file_placeholder);
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            fileName = cursor.getString(displayNameIndex);
            cursor.close();
        }
        return fileName;
    }

    private void clearSelectedFile() {
        selectedFileUri = null;
        selectedFileImageView.setImageDrawable(null);
        selectedFileTextView.setText("");
        selectedFilePlaceholder.setVisibility(View.GONE);
    }

    private void loadMessagesFromFirebase() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear(); // Clear existing messages
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Deserialize GroupMessage from Firebase snapshot
                    BetaGroupMessage message = snapshot.getValue(BetaGroupMessage.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged(); // Notify adapter about data change
                if (!messageList.isEmpty()) {
                    messageRecyclerView.smoothScrollToPosition(messageList.size() - 1);
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

    private void sendTextMessage(String messageText) {
        if (currentGroup == null || currentGroup.getName() == null) {
            Log.e("BetaGroupChatActivity", "Invalid group information");
            return;
        }

        String messageId = messagesRef.push().getKey();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        // Fetch sender's name from Firebase Realtime Database
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        userService.getUserDetails(userId, new UserService.UserFetchListener() {
            @Override
            public void onUserFetched(UserDetails userDetails) {
                // Create and send the message
                BetaGroupMessage message = new BetaGroupMessage(messageId,
                        userId,
                        userDetails.getName(),
                        timestamp,
                        messageText,
                        "text",
                        "", // Pass an empty string for fileUrl
                        "",
                        "normal");
                betaMessageService.createNewMessage(message, currentGroup.getId(), new BetaMessageService.OnMessageAddListener() {
                    @Override
                    public void onMessageAdded(String messageId) {
                        Log.d("BetaGroupChatActivity", "Text message sent successfully");
                        messageInput.setText("");
                        sendMessageButton.setEnabled(true);
                    }

                    @Override
                    public void onAddFailure(String errorMessage) {
                        Log.e("BetaGroupChatActivity", "Failed to send text message: " + errorMessage);
                        sendMessageButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void onFetchFailed(Exception e) {
                Log.e("BetaGroupChatActivity", "Failed to fetch user data: " + e.getMessage());
                sendMessageButton.setEnabled(true);
            }
        });
    }

    private void sendFileMessage() {
        String storageName = "beta-group-files/" + currentGroup.getName() + "/" + selectedFileUri.getLastPathSegment();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(storageName);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String messageId = messagesRef.push().getKey();
        long timestamp = System.currentTimeMillis();
        sendMessageButton.setEnabled(false);

        userService.getUserDetails(userId, new UserService.UserFetchListener() {
            @Override
            public void onUserFetched(UserDetails userDetails) {
                // Perform file upload to Firebase Storage
                storageReference.putFile(selectedFileUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                sendMessageButton.setEnabled(false);

                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri downloadUri) {
                                        // Create the message object for file message
                                        BetaGroupMessage message = new BetaGroupMessage(messageId,
                                                userId,
                                                userDetails.getName(), // Use the fetched sender's name
                                                timestamp,
                                                null,
                                                getFileType(selectedFileUri),
                                                downloadUri.toString(),
                                                getFileNameFromUri(selectedFileUri),
                                                "normal");
                                        betaMessageService.createNewMessage(message, currentGroup.getId(), new BetaMessageService.OnMessageAddListener() {
                                            @Override
                                            public void onMessageAdded(String messageId) {
                                                Log.d("BetaGroupChatActivity", "File message sent successfully");
                                                clearSelectedFile();
                                                sendMessageButton.setEnabled(true);
                                            }

                                            @Override
                                            public void onAddFailure(String errorMessage) {
                                                Log.e("BetaGroupChatActivity", "Failed to send file message: " + errorMessage);
                                                sendMessageButton.setEnabled(true);
                                            }
                                        });
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("BetaGroupChatActivity", "Failed to upload file: " + e.getMessage());
                                sendMessageButton.setEnabled(true);
                            }
                        });
            }

            @Override
            public void onFetchFailed(Exception e) {
                Log.e("BetaGroupChatActivity", "User data does not exist for ID: " + userId);
                sendMessageButton.setEnabled(true);
            }
        });
    }


    private String getFileType(Uri fileUri) {
        String mimeType = getContentResolver().getType(fileUri);
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return "image";
            } else if (mimeType.equals("application/pdf")) {
                return "pdf";
            } else if (mimeType.startsWith("application/msword") || mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                return "doc";
            } else if (mimeType.startsWith("application/vnd.ms-powerpoint") || mimeType.startsWith("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                return "ppt";
            } else if (mimeType.startsWith("application/vnd.ms-excel") || mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                return "xls";
            }
        }
        return "other";
    }
}
