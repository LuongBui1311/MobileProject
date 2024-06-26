package com.hcmute.endsemesterproject.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmute.endsemesterproject.Controllers.ChatActivity;
import com.hcmute.endsemesterproject.Controllers.ImageViewerActivity;
import com.hcmute.endsemesterproject.Controllers.VideoViewerActivity;
import com.hcmute.endsemesterproject.Models.Messages;
import com.hcmute.endsemesterproject.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private String messageReceiverID, messageReceiverName, messageReceiverImage;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private static final String TAG = "MyActivity";


    public MessageAdapter (List<Messages> userMessagesList, String messageReceiverID, String messageReceiverName, String messageReceiverImage)
    {
        this.userMessagesList = userMessagesList;
        this.messageReceiverID = messageReceiverID;
        this.messageReceiverName = messageReceiverName;
        this.messageReceiverImage = messageReceiverImage;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;
        public VideoView messageSenderVideo, messageReceiverVideo;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageSenderVideo = itemView.findViewById(R.id.message_sender_video_view);
            messageReceiverVideo = itemView.findViewById(R.id.message_receiver_video_view);
        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int position)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.messageSenderVideo.setVisibility(View.GONE);
        messageViewHolder.messageReceiverVideo.setVisibility(View.GONE);


        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                displayText(messageViewHolder.senderMessageText, R.drawable.sender_messages_layout, messages);
            }
            else
            {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                displayText(messageViewHolder.receiverMessageText, R.drawable.receiver_messages_layout, messages);
            }
        }else if (fromMessageType.equals("image")){
            if (fromUserID.equals(messageSenderId)){
                displayImage(messageViewHolder.messageSenderPicture, messages);
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                displayImage(messageViewHolder.messageReceiverPicture, messages);
            }
        } else if (fromMessageType.equals("pdf") || (fromMessageType.equals("docx"))){
            if (fromUserID.equals(messageSenderId)) {
                displayFile(messageViewHolder.messageSenderPicture);
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                displayFile(messageViewHolder.messageReceiverPicture);
            }
        } else if (fromMessageType.equals("video")) {
            if (fromUserID.equals(messageSenderId)){
                displayVideo(messageViewHolder.messageSenderVideo, messageViewHolder.itemView.getContext(), messages);
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                displayVideo(messageViewHolder.messageReceiverVideo, messageViewHolder.itemView.getContext(), messages);
            }
        }

        if (fromUserID.equals(messageSenderId)){
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and View This Document",
                                "Delete for Everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                    reloadChat(messageViewHolder);
                                } else if (which == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                } else if (which == 2){
                                    deleteMessageForEveryOne(position, messageViewHolder);
                                    reloadChat(messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Delete for Everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                    reloadChat(messageViewHolder);
                                } else if (which == 1){
                                    deleteMessageForEveryOne(position, messageViewHolder);
                                    reloadChat(messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View This Image",
                                "Delete for Everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                    reloadChat(messageViewHolder);
                                } else if (which == 1){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                } else if (which == 2){
                                    deleteMessageForEveryOne(position,  messageViewHolder);
                                    reloadChat(messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("video")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View This Video",
                                "Delete for Everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                    reloadChat(messageViewHolder);
                                } else if (which == 1){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), VideoViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                } else if (which == 2){
                                    deleteMessageForEveryOne(position,  messageViewHolder);
                                    reloadChat(messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        } else {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and View This Document",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteReceivedMessage(position, messageViewHolder);
                                    reloadChat(messageViewHolder);
                                } else if (which == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteReceivedMessage(position, messageViewHolder);
                                    reloadChat(messageViewHolder);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View This Image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteReceivedMessage(position, messageViewHolder);
                                    reloadChat(messageViewHolder);
                                } else if (which == 1){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("video")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View This Video",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    deleteSentMessage(position, messageViewHolder);
                                    reloadChat(messageViewHolder);
                                } else if (which == 1){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), VideoViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    private void reloadChat(MessageViewHolder messageViewHolder) {
        Intent intent = new Intent(messageViewHolder.itemView.getContext(), ChatActivity.class);
        intent.putExtra("visit_user_id", messageReceiverID);
        intent.putExtra("visit_user_name", messageReceiverName);
        intent.putExtra("visit_image", messageReceiverImage);
        messageViewHolder.itemView.getContext().startActivity(intent);
    }

    private void displayVideo(VideoView messageVideo, Context context, Messages messages) {
        messageVideo.setVisibility(View.VISIBLE);
        messageVideo.setVideoURI(Uri.parse(messages.getMessage()));

        MediaController mediaController = new MediaController(context);
        messageVideo.setMediaController(mediaController);
        mediaController.setAnchorView(messageVideo);
    }

    private void displayFile(ImageView messageFile) {
        messageFile.setVisibility(View.VISIBLE);
        Picasso.get()
                .load("https://firebasestorage.googleapis.com/v0/b/android-chat-app-b6cae.appspot.com/o/Image%20Files%2Fsendfile.jpg?alt=media&token=972f7b0c-1529-4a54-9db1-09e22b8ffbc2")
                .into(messageFile);
    }

    private void displayImage(ImageView messagePicture, Messages messages) {
        messagePicture.setVisibility(View.VISIBLE);
        Picasso.get().load(messages.getMessage()).into(messagePicture);
    }

    private void displayText(TextView messageText, int messagesLayout, Messages messages) {
        messageText.setVisibility(View.VISIBLE);

        messageText.setBackgroundResource(messagesLayout);
        messageText.setTextColor(Color.BLACK);
        messageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

    private void deleteSentMessage (final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(holder.itemView.getContext(), "Delete Successfully.",  Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Error Occurred.",  Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteReceivedMessage (final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(holder.itemView.getContext(), "Delete Successfully.",  Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Error Occurred.",  Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteMessageForEveryOne (final int position, final MessageViewHolder holder){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            rootRef.child("Messages")
                                    .child(userMessagesList.get(position).getFrom())
                                    .child(userMessagesList.get(position).getTo())
                                    .child(userMessagesList.get(position).getMessageID())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(holder.itemView.getContext(), "Delete Successfully.",  Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });


                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Error Occurred.",  Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}