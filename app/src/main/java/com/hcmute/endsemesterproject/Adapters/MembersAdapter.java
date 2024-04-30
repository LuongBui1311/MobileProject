package com.hcmute.endsemesterproject.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hcmute.endsemesterproject.Models.UserDetails;
import com.hcmute.endsemesterproject.R;
import com.squareup.picasso.Picasso;
import java.util.List;

public class MembersAdapter extends ArrayAdapter<UserDetails> {

    private Context mContext;
    private int mResource;
    private List<UserDetails> mMembers;
    private List<UserDetails> mSelectedUsers; // New field for selected users

    public MembersAdapter(Context context, int resource, List<UserDetails> members, List<UserDetails> selectedUsers) {
        super(context, resource, members);
        mContext = context;
        mResource = resource;
        mMembers = members;
        mSelectedUsers = selectedUsers; // Initialize selected users
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
        }

        UserDetails member = mMembers.get(position);

        ImageView imageView = convertView.findViewById(R.id.image_member);
        TextView nameTextView = convertView.findViewById(R.id.text_member_name);
        TextView statusTextView = convertView.findViewById(R.id.text_member_status);

        nameTextView.setText(member.getName());
        statusTextView.setText(member.getStatus());

        // Load image using Picasso library
        Picasso.get()
                .load(member.getImage())
                .placeholder(R.drawable.profile_image) // Placeholder image for null or empty URLs
                .error(R.drawable.profile_image) // Error image if loading fails
                .into(imageView);

        // Check if the current member is selected
        if (mSelectedUsers.contains(member)) {
            // Change the background color of the selected item
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.selected_item_color));
        } else {
            // Reset the background color of the unselected item
            convertView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        }

        return convertView;
    }

}
