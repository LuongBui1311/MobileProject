package com.hcmute.endsemesterproject.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmute.endsemesterproject.Models.Contacts;
import com.hcmute.endsemesterproject.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UsersRef, ContactRef;
    private FirebaseAuth mAuth;
    private String currentUserId, TAG, txtFindFriend="";
    private List<String> contactIdList;

    private Button btnFindFriend;
    private EditText edtFindFriend;
    FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        init();

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

        //hanle event
        handleFindFriend();
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        loadUsers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void loadUsers(){
        FindFriendsRecyclerList.removeAllViewsInLayout();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(UsersRef, Contacts.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@androidx.annotation.NonNull FindFriendViewHolder holder, int position, @androidx.annotation.NonNull Contacts model) {
                DataSnapshot snapshot =  getSnapshots().getSnapshot(position);
                String id = snapshot.getKey();

                try {
                        if (!TextUtils.isEmpty(model.getName()) && !isHiddenUser(id, model)){
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
                        } else hideUser(holder);

                } catch (Exception e){
                    Toast.makeText(FindFriendsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @androidx.annotation.NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new FindFriendViewHolder(view);
            }

        };

        FindFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    private void handleFindFriend(){
        btnFindFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtFindFriend = edtFindFriend.getText().toString().trim().toLowerCase();
                loadUsers();
                adapter.startListening();
            }
        });

    }

    private boolean isHiddenUser(String userId, Contacts model){
        try {
            boolean isCurrentUser = currentUserId.equals(userId);
            boolean isNotFinds = !TextUtils.isEmpty(txtFindFriend) && !model.getName().toLowerCase().contains(txtFindFriend);
            boolean isExistContact = isExistContact(userId);
            return isCurrentUser || isNotFinds || isExistContact;
        } catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return true;
        }

    }

    private boolean isExistContact(String id){
        for (String contactId: contactIdList) {
            if (contactId.equals(id)){
               return true;
            }
        }
        return false;
    }

    private void hideUser(FindFriendViewHolder holder) {
        holder.itemView.setVisibility(View.INVISIBLE);
        holder.itemView.getLayoutParams().height = 0;
        holder.itemView.getLayoutParams().width = 0;
    }

    private void init(){
        contactIdList = new ArrayList<String>();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);

        btnFindFriend = (Button) findViewById(R.id.btn_FindFriend);
        edtFindFriend = (EditText)  findViewById(R.id.txt_FindFriend);
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