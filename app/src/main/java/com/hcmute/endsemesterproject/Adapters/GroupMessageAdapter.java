package com.hcmute.endsemesterproject.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.endsemesterproject.Models.GroupMessage;
import com.hcmute.endsemesterproject.R;

import java.util.List;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolder> {

    private static final int MESSAGE_SENT = 1;
    private static final int MESSAGE_RECEIVED = 2;
    private List<GroupMessage> messageList;
    private String currentUserId;

    // Constructor
    public GroupMessageAdapter(List<GroupMessage> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
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
        GroupMessage message = messageList.get(position);
        holder.messageText.setText(message.getMessage());
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textViewMessageContent);
        }
    }
}
