package com.hcmute.endsemesterproject.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmute.endsemesterproject.Models.Contacts;
import com.hcmute.endsemesterproject.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UsersRef, ContactRef;
    private FirebaseAuth mAuth;
    private String currentUserId, TAG;
    private List<String> contactIdList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        contactIdList = new ArrayList<String>();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);

        ContactRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot ds:
                     snapshot.getChildren()) {
                    contactIdList.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });

        FindFriendsRecyclerList = (RecyclerView) findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }
    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(UsersRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder,final int position, @NonNull Contacts model)
                    {
                        DataSnapshot snapshot =  getSnapshots().getSnapshot(position);
                        String id = snapshot.getKey();

                        UsersRef.child(id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                                if (!snapshot.child("name").exists()){
                                    hideUser(holder);
                                } else {
                                    if (currentUserId.equals(id)){
                                        hideUser(holder);
                                    } else {
                                        for (String contactId:
                                                contactIdList) {
                                            if (contactId.equals(id)){
                                                hideUser(holder);
                                            }
                                        }
                                        holder.userName.setText(model.getName());
                                        holder.userStatus.setText(model.getStatus());
                                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                String visit_user_id = getRef(position).getKey();

                                                Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                                //pass data into ProfileActivity
                                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                                startActivity(profileIntent);
                                            }
                                        });
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                            }
                        });
                    }
                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                        return viewHolder;
                    }
                };
        FindFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }
    private void hideUser(FindFriendViewHolder holder) {
        holder.itemView.setVisibility(View.INVISIBLE);
        holder.itemView.getLayoutParams().height = 0;
        holder.itemView.getLayoutParams().width = 0;
    }
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;
        public FindFriendViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}