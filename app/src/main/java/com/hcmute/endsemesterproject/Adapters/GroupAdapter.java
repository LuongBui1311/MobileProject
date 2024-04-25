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

import org.w3c.dom.Text;

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

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public int getItemViewType(int position) {
        return (groups.get(position).isPublic()) ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v  = convertView;
        int type = getItemViewType(position);
        if (v == null) {
            if (type == 0) {
                v = LayoutInflater.from(context).inflate(R.layout.public_group_item, parent, false);
            } else {
                v = LayoutInflater.from(context).inflate(R.layout.private_group_item, parent, false);
            }
        }

        Group group = groups.get(position);

        TextView tvGroupName = v.findViewById(R.id.tvGroupName);
        TextView tvGroupDescription = v.findViewById(R.id.tvGroupDescription);
        TextView tvNumberOfMembers = v.findViewById(R.id.tvNumberOfMembers);

        tvGroupName.setText(group.getName());
        tvGroupDescription.setText(group.getDescription());
        tvNumberOfMembers.setText(group.getNumberOfMembers()+" members");

        if (type == 1) {
            Button leaveGroupButton = v.findViewById(R.id.btnLeaveGroup);
            leaveGroupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) {
                        callback.onLeaveGroup(group);
                    }
                }
            });
        }

        return  v;
    }
}
