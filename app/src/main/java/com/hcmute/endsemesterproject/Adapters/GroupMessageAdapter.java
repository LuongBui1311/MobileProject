package com.hcmute.endsemesterproject.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.endsemesterproject.Models.BetaGroupMessage;
import com.hcmute.endsemesterproject.R;
import com.hcmute.endsemesterproject.Services.ReactionService;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolder> {

    private static final int MESSAGE_SENT = 1;
    private static final int MESSAGE_RECEIVED = 2;
    private List<BetaGroupMessage> messageList;
    private String currentUserId;
    private int selectedItem = RecyclerView.NO_POSITION;
    private ReactionService reactionService;

    // Constructor
    public GroupMessageAdapter(List<BetaGroupMessage> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        reactionService = new ReactionService();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == MESSAGE_SENT) {
            view = inflater.inflate(R.layout.group_chat_sent_message_item, parent, false);
        } else {
            view = inflater.inflate(R.layout.group_chat_received_message_item, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BetaGroupMessage message = messageList.get(position);
        String messageType = message.getMessageType();

        // Convert timestamp to a human-readable format
        String formattedTime = formatTimestamp(message.getTimestamp());
        holder.messageTime.setText(formattedTime);

        // Displaying sender's name
        if (holder.senderName != null) {
            if (message.getSenderId().equals(currentUserId)) {
                holder.senderName.setVisibility(View.GONE); // Hide sender's name for sent messages
            } else {
                holder.senderName.setVisibility(View.VISIBLE);
                // Set sender's name for received messages
                holder.senderName.setText(message.getSenderName());
            }
        }

        // Displaying images for image messages
        if (messageType.equals("image")) {
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.VISIBLE); // Show progress bar
            Picasso.get().load(message.getFileUrl()).into(holder.messageImage, new Callback() {
                @Override
                public void onSuccess() {
                    holder.progressBar.setVisibility(View.GONE); // Hide progress bar on success
                }

                @Override
                public void onError(Exception e) {
                    holder.progressBar.setVisibility(View.GONE); // Hide progress bar on error
                    holder.messageImage.setImageResource(R.drawable.ic_default_fail); // Show default fail image
                }
            });
            holder.messageText.setText(getFileNameFromUrl(message.getOriginalFileName())); // Show image name
            holder.fileIcon.setVisibility(View.GONE);
        } else if (messageType.equals("text")) {
            holder.messageText.setText(message.getMessageText());
            holder.messageImage.setVisibility(View.GONE);
            holder.fileIcon.setVisibility(View.GONE); // Hide file icon for text messages
        } else {
            holder.messageText.setText(getFileNameFromUrl(message.getOriginalFileName())); // Show file name for other file types
            holder.messageImage.setVisibility(View.GONE);
            holder.fileIcon.setVisibility(View.VISIBLE);
            // Set default file icon for other file types
            holder.fileIcon.setImageResource(R.drawable.default_file_icon);
        }

        reactionService.getTop3ReactionsString(message.getMessageId(), new ReactionService.OnTop3ReactionsFetchListener() {
            @Override
            public void onTop3ReactionsFetched(String top3Reactions) {
                holder.reactionsTextView.setText(top3Reactions);

            }

            @Override
            public void onFetchFailure(String errorMessage) {
                Log.d("reaction failed", "failed to react message");
            }
        });

        // Set reactions text

        // Check if the item is selected and set the appropriate state
        if (position == selectedItem) {
            holder.itemView.setActivated(true);
        } else {
            holder.itemView.setActivated(false);
        }
    }

    // Helper method to extract file name from file URL
    private String getFileNameFromUrl(String fileUrl) {
        if (fileUrl != null) {
            String[] parts = fileUrl.split("/");
            return parts[parts.length - 1];
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSenderId().equals(currentUserId)) {
            return MESSAGE_SENT;
        } else {
            return MESSAGE_RECEIVED;
        }
    }

    // Method to update the currently selected item
    public void setSelectedItem(int position) {
        selectedItem = position;
        notifyDataSetChanged();
    }

    public BetaGroupMessage getSelectedItem(int position) {
        return messageList.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView senderName;
        TextView messageText;
        TextView messageTime;
        TextView reactionsTextView; // Add TextView for reactions
        ImageView messageImage;
        ImageView fileIcon;
        ProgressBar progressBar;

        ViewHolder(View itemView) {
            super(itemView);
            senderName = itemView.findViewById(R.id.textViewSenderName);
            messageText = itemView.findViewById(R.id.textViewMessageContent);
            messageTime = itemView.findViewById(R.id.textViewMessageTime);
            reactionsTextView = itemView.findViewById(R.id.reactionsTextView); // Initialize reactions TextView
            messageImage = itemView.findViewById(R.id.imageViewMessageImage);
            fileIcon = itemView.findViewById(R.id.imageViewFileIcon);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    // Helper method to format timestamp
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }
}
