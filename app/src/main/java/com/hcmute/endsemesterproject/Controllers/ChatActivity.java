package com.hcmute.endsemesterproject.Controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hcmute.endsemesterproject.Adapters.MessageAdapter;
import com.hcmute.endsemesterproject.Models.Messages;
import com.hcmute.endsemesterproject.R;
import com.hcmute.endsemesterproject.Utils.StorageConst;
import com.hcmute.endsemesterproject.Utils.TableConst;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID,
            saveCurrentTime, saveCurrentDate, checker = "", myUrl = "";
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ImageButton SendMessageButton, SendFilesButton, CameraButton;
    private EditText MessageInputText;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private Uri fileUri;
    private ProgressDialog loadingBar;
    int SEND_FILE_CODE = 438;
    int OPEN_CAMERA_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        IntializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendMessage();
            }
        });
        DisplayLastSeen();
        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[] {
                        "Images",
                        "PDF Files",
                        "MS Word Files"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            checker = "image";
                            String type = "image/*";
                            String title = "Select Image";
                            getContent(type, title);
                        }
                        if (which == 1){
                            checker = "pdf";
                            String type = "application/pdf";
                            String title = "Select PDF File";
                            getContent(type, title);
                        }
                        if (which == 2){
                            checker = "docx";
                            String type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                            String title = "Select MS Word File";
                            getContent(type, title);
                        }
                    }
                });
                builder.show();
            }
        });
        CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "image";
                openCamera();
            }
        });
    }
    private void getContent(String type, String title) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType(type);
        startActivityForResult(intent.createChooser(intent, title), SEND_FILE_CODE);
    }
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, OPEN_CAMERA_CODE);
    }
    private void IntializeControllers() {
        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        CameraButton = (ImageButton) findViewById(R.id.camera_btn);
        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SEND_FILE_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            displayLoadingBar();
            fileUri = data.getData();
            if (!checker.equals("image")){
                sendFileMessage(StorageConst.DOCUMENT);
            } else if (checker.equals("image")) {
                sendFileMessage(StorageConst.IMAGE);
            } else{
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected, Error.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == OPEN_CAMERA_CODE && resultCode == RESULT_OK && data != null) {
            displayLoadingBar();
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            fileUri = getImageUri(getApplicationContext(), bitmap);

            sendFileMessage(StorageConst.IMAGE);
        }
    }
    private void sendFileMessage(String storageName) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(storageName);

        final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
        final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference userMessageKeyRef = RootRef.child(TableConst.MESSAGES.TABLE_NAME)
                .child(messageSenderID).child(messageReceiverID).push();

        final String messagePushID = userMessageKeyRef.getKey();
        StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");
        uploadFile(filePath, messagePushID, messageSenderRef, messageReceiverRef);
    }
    private void uploadFile(StorageReference filePath, String messagePushID, String messageSenderRef, String messageReceiverRef) {
        UploadTask uploadTask = filePath.putFile(fileUri);
        Task<Uri>uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Continue with the task to get the download URL
                        return filePath.getDownloadUrl();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {
                            pushMessage(task, messagePushID, messageSenderRef, messageReceiverRef);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void pushMessage(Task<Uri> task, String messagePushID, String messageSenderRef, String messageReceiverRef) {
        myUrl = task.getResult().toString();

        Map messageTextBody = new HashMap();
        messageTextBody.put("message", myUrl);
        messageTextBody.put("name", fileUri.getLastPathSegment());
        messageTextBody.put("type", checker);
        messageTextBody.put("from", messageSenderID);
        messageTextBody.put("to", messageReceiverID);
        messageTextBody.put("messageID", messagePushID);
        messageTextBody.put("time", saveCurrentTime);
        messageTextBody.put("date", saveCurrentDate);

        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
        messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

        notifyMessageUpdate(messageBodyDetails);
    }
    private void notifyMessageUpdate(Map messageBodyDetails) {
        RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    loadingBar.dismiss();
                    Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.dismiss();
                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
                MessageInputText.setText("");
            }
        });
    }
    private void displayLoadingBar() {
        loadingBar.setTitle("Sending File");
        loadingBar.setMessage("Please wait, we are sending that file...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
    }
    private Uri getImageUri(Context applicationContext, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title", null);
        return Uri.parse(path);
    }
    private void DisplayLastSeen() {
        RootRef.child(TableConst.USERS.TABLE_NAME).child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(TableConst.USERS.USER_STATE.TABLE_NAME).hasChild(TableConst.USERS.USER_STATE.STATE)) {
                            String state = dataSnapshot.child(TableConst.USERS.USER_STATE.TABLE_NAME).child(TableConst.USERS.USER_STATE.STATE).getValue().toString();
                            String date = dataSnapshot.child(TableConst.USERS.USER_STATE.TABLE_NAME).child(TableConst.USERS.USER_STATE.DATE).getValue().toString();
                            String time = dataSnapshot.child(TableConst.USERS.USER_STATE.TABLE_NAME).child(TableConst.USERS.USER_STATE.TIME).getValue().toString();
                            if (state.equals("online")) {
                                userLastSeen.setText("online");
                            } else if (state.equals("offline")) {
                                userLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                        } else {
                            userLastSeen.setText("offline");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    @Override
    protected void onStart() {
        super.onStart();
        RootRef.child(TableConst.MESSAGES.TABLE_NAME).child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        // Check if the message already exists in the list
                        boolean exists = false;
                        for (Messages msg : messagesList) {
                            if (msg.getMessageID().equals(messages.getMessageID())) {
                                exists = true;
                                break;
                            }
                        }
                        // Add the message to the list if it doesn't exist
                        if (!exists) {
                            messagesList.add(messages);
                            messageAdapter.notifyDataSetChanged();
                            userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                        }
                    }
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }
                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }
                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    private void SendMessage(){
        String messageText = MessageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        }
        else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

            notifyMessageUpdate(messageBodyDetails);
        }
    }
}