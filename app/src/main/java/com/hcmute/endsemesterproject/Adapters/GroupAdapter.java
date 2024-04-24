package com.hcmute.endsemesterproject.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hcmute.endsemesterproject.Controllers.BetaGroupFragment;
import com.hcmute.endsemesterproject.Models.Group;
import com.hcmute.endsemesterproject.R;

import java.util.List;

public class GroupAdapter extends ArrayAdapter<Group> {
    private Context context;
    private List<Group> groups;

    public interface LeaveGroupCallback {
        void onLeaveGroup(Group group);
    }

    private  LeaveGroupCallback callback;

    public void setLeaveGroupCallback(LeaveGroupCallback callback) {
        this.callback = callback;
    }

    public GroupAdapter(Context context, List<Group> groups) {
        super(context, 0, groups);
        this.context = context;
        this.groups = groups;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        int layoutId;

        Group group = groups.get(position);

        if (group.isPublic()) {
            layoutId = R.layout.public_group_item;
        } else {
            layoutId = R.layout.private_group_item;
        }

        if (convertView == null) {

            convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
            viewHolder = new ViewHolder();
            if (!group.isPublic()) {
                viewHolder.tvGroupName = convertView.findViewById(R.id.tvPrivateGroupName);
                viewHolder.tvGroupDescription = convertView.findViewById(R.id.tvPrivateGroupDescription);
                viewHolder.tvNumberOfMembers = convertView.findViewById(R.id.tvPrivateNumberOfMembers);

                viewHolder.leaveGroupButton = convertView.findViewById(R.id.btnLeaveGroup);

                viewHolder.leaveGroupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callback != null) {
                            callback.onLeaveGroup(group);
                        }
                    }
                });
            } else {
                viewHolder.tvGroupName = convertView.findViewById(R.id.tvGroupName);
                viewHolder.tvGroupDescription = convertView.findViewById(R.id.tvGroupDescription);
                viewHolder.tvNumberOfMembers = convertView.findViewById(R.id.tvNumberOfMembers);

            }

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvGroupName.setText(group.getName() != null ? group.getName() : "");
        viewHolder.tvGroupDescription.setText(group.getDescription() != null ? group.getDescription() : "");
        viewHolder.tvNumberOfMembers.setText(group.getNumberOfMembers() + " members");

        Log.d("convertView debug", convertView.toString());

        return convertView;
    }

    static class ViewHolder {
        TextView tvGroupName;
        TextView tvGroupDescription;
        TextView tvNumberOfMembers;
        Button leaveGroupButton;
    }
}
