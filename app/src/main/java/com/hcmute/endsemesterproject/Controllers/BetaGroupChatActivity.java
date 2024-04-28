package com.hcmute.endsemesterproject.Controllers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.hcmute.endsemesterproject.Services.ReactionService;
import com.hcmute.endsemesterproject.Services.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private LinearLayout reactionSelectLayout;
    private BetaGroupMessage selectedMessage;
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
    private ReactionService reactionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beta_group_chat);


        betaGroupsRef = FirebaseDatabase.getInstance().getReference().child("beta-groups");
        userService = new UserService();
        groupService = new GroupService();
        betaMessageService = new BetaMessageService();
        reactionService = new ReactionService();
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
        reactionSelectLayout = findViewById(R.id.reaction_select_layout);

        collapse(reactionSelectLayout);



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
                sendFileButton.setEnabled(true);
                sendMessageButton.setEnabled(true);
                messageInput.setEnabled(true);
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

        // Set item click listener for RecyclerView
        // Set item long click listener for RecyclerView
        messageRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(BetaGroupChatActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    View childView = messageRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    int position = messageRecyclerView.getChildAdapterPosition(childView);
                    if (position != RecyclerView.NO_POSITION) {
                        // Handle long click event here
                        onItemLongClick(position);
                    }
                }
            });

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gestureDetector.onTouchEvent(e);
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });


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

    private void highlightSelectedReactions(LinearLayout reactionSelectLayout, List<String> selectedReactions) {
        // Map reaction types to corresponding Unicode emojis
        Map<String, String> reactionEmojiMap = new HashMap<>();
        reactionEmojiMap.put("like", "👍");
        reactionEmojiMap.put("love", "❤️");
        reactionEmojiMap.put("wow", "😮");
        reactionEmojiMap.put("relax", "😌");
        reactionEmojiMap.put("cry", "😭");
        reactionEmojiMap.put("angry", "😠");

        // Iterate through child views of the reactionSelectLayout
        for (int i = 0; i < reactionSelectLayout.getChildCount(); i++) {
            View view = reactionSelectLayout.getChildAt(i);
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                String emoji = textView.getText().toString();
                boolean matched = false;
                // Iterate through selectedReactions
                for (String reactionType : selectedReactions) {
                    String emojiFromMap = reactionEmojiMap.get(reactionType);
                    if (emojiFromMap != null && emojiFromMap.equals(emoji)) {
                        // Highlight the selected reaction
                        textView.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
                        matched = true;
                        break; // Exit the loop once the reaction is found and highlighted
                    }
                }
                if (!matched) {
                    // Set background color to transparent if the reaction is not found in selectedReactions
                    textView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (reactionSelectLayout.getVisibility() == View.VISIBLE) {
                Rect outRect = new Rect();
                reactionSelectLayout.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    collapse(reactionSelectLayout);
                    selectedMessage = null;
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void onEmojiClick(View view) {
        // Handle emoji click events here
        TextView emojiTextView = (TextView) view;
        String emoji = emojiTextView.getText().toString();

        // Do something with the clicked emoji, such as sending it as a reaction to a message
        Log.d("Emoji Clicked", emoji);

        String reactionType = null;

        if (emoji.equals("\uD83D\uDC4D")) {
            reactionType = "like";
        } else if (emoji.equals("❤\uFE0F")) {
            reactionType = "love";
        } else if (emoji.equals("\uD83D\uDE2E")) {
            reactionType = "wow";
        } else if (emoji.equals("\uD83D\uDE0C")) {
            reactionType = "relax";
        } else if (emoji.equals("\uD83D\uDE2D")) {
            reactionType = "cry";
        } else if (emoji.equals("\uD83D\uDE20")) {
            reactionType = "angry";
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (selectedMessage != null && userId != null && reactionType != null) {
            if (view.getBackground() instanceof ColorDrawable) {
                // Check if the background color is blue, indicating selection
                int backgroundColor = ((ColorDrawable) view.getBackground()).getColor();
                if (backgroundColor == ContextCompat.getColor(this, R.color.blue)) {
                    // If the background color is blue, remove the reaction
                    reactionService.removeReaction(selectedMessage.getMessageId(), userId, reactionType, new ReactionService.OnReactionRemoveListener() {
                        @Override
                        public void onReactionRemoved() {
                            view.setBackgroundColor(Color.TRANSPARENT); // Set background to transparent
                            collapse(reactionSelectLayout); // Collapse the selection layout
                            Toast.makeText(BetaGroupChatActivity.this, "Reaction removed successfully.", Toast.LENGTH_SHORT).show();
                            loadMessagesFromFirebase();
                        }

                        @Override
                        public void onRemoveFailure(String errorMessage) {
                            Toast.makeText(BetaGroupChatActivity.this, "Failed to remove reaction: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // If the background color is not blue, add the reaction
                    reactionService.addReaction(selectedMessage.getMessageId(), userId, reactionType, new ReactionService.OnReactionAddListener() {
                        @Override
                        public void onReactionAdded() {
                            view.setBackgroundColor(ContextCompat.getColor(BetaGroupChatActivity.this, R.color.blue)); // Set background to blue
                            collapse(reactionSelectLayout); // Collapse the selection layout
                            Toast.makeText(BetaGroupChatActivity.this, "Reaction added successfully.", Toast.LENGTH_SHORT).show();
                            loadMessagesFromFirebase();

                        }

                        @Override
                        public void onAddFailure(String errorMessage) {
                            Toast.makeText(BetaGroupChatActivity.this, "Failed to add reaction: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // If the background is not a color drawable, it's transparent, so add the reaction
                reactionService.addReaction(selectedMessage.getMessageId(), userId, reactionType, new ReactionService.OnReactionAddListener() {
                    @Override
                    public void onReactionAdded() {
                        view.setBackgroundColor(ContextCompat.getColor(BetaGroupChatActivity.this, R.color.blue)); // Set background to blue
                        collapse(reactionSelectLayout); // Collapse the selection layout
                        Toast.makeText(BetaGroupChatActivity.this, "Reaction added successfully.", Toast.LENGTH_SHORT).show();
                        loadMessagesFromFirebase();

                    }

                    @Override
                    public void onAddFailure(String errorMessage) {
                        Toast.makeText(BetaGroupChatActivity.this, "Failed to add reaction: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Log.d("reaction failed", "some field is null");
        }
    }




    private void onItemLongClick(int position) {
        // Get the selected message
        selectedMessage = messageAdapter.getSelectedItem(position);

        reactionService.getAllReactionTypesForUserAndMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(), selectedMessage.getMessageId(), new ReactionService.OnReactionTypesFetchListener() {
            @Override
            public void onReactionTypesFetched(List<String> reactionTypes) {
                highlightSelectedReactions(reactionSelectLayout, reactionTypes);
                expand(reactionSelectLayout);
            }

            @Override
            public void onFetchFailure(String errorMessage) {

            }
        });

        // Log out the ID and text of the selected message
        if (selectedMessage != null) {
            Log.d("Selected message ID:", selectedMessage.getMessageId());
            Log.d("Selected message text:", selectedMessage.getMessageText() + "");
        } else {
            Log.e("Selected message:", "null");
        }


    }

    public static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density) + 1000);
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density + 500));
        v.startAnimation(a);
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
        groupService.getAllMessageIdsInGroup(currentGroup.getId(), new GroupService.MessageIdsFetchListener() {
            @Override
            public void onMessageIdsFetched(List<String> messageIds) {
                betaMessageService.getMessagesFromIdList(messageIds, new BetaMessageService.OnMessageListFetchListener() {
                    @Override
                    public void onMessageListFetched(List<BetaGroupMessage> messages) {
                        messageList.clear();
                        messageList.addAll(messages);
                        messageAdapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            messageRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }

                    @Override
                    public void onFetchFailure(String errorMessage) {
                        Log.e("BetaGroupChatActivity", "Failed to load messages: " + errorMessage);
                    }
                });
            }

            @Override
            public void onFetchFailed(Exception e) {
                Log.e("BetaGroupChatActivity", "Failed to load messages: " + e.getMessage());
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
                        loadMessagesFromFirebase();
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
                                                loadMessagesFromFirebase();
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
