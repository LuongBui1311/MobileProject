package com.hcmute.endsemesterproject.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hcmute.endsemesterproject.Models.Contacts;
import com.hcmute.endsemesterproject.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contacts> {
    private Context mContext;
    private int mResource;

    private List<Contacts> mContactsList;
    private List<Boolean> mSelectedItems;

    public ContactAdapter(@NonNull Context context, int resource, @NonNull List<Contacts> contactsList) {
        super(context, resource, contactsList);
        mContext = context;
        mResource = resource;
        mContactsList = contactsList;
        mSelectedItems = new ArrayList<>(Collections.nCopies(200, false));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        }

        Contacts contact = getItem(position);

        ImageView imageViewContact = convertView.findViewById(R.id.imageViewContact);
        TextView textViewName = convertView.findViewById(R.id.textViewName);
        TextView textViewStatus = convertView.findViewById(R.id.textViewStatus);

        // Set contact information to views
        if (contact != null) {
            textViewName.setText(contact.getName());
            textViewStatus.setText(contact.getStatus());

            // Load contact image using Picasso
            if (contact.getImage() != null && !contact.getImage().isEmpty()) {
                Picasso.get().load(contact.getImage()).into(imageViewContact);
            } else {
                // If no image available, you can set a default image
                Picasso.get().load(R.drawable.profile_image).into(imageViewContact);
            }

            // Highlight selected items
            if (mSelectedItems.get(position)) {
                convertView.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_blue_light));
            } else {
                convertView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            }
        }

        return convertView;
    }

    public void toggleSelection(int position) {
        mSelectedItems.set(position, !mSelectedItems.get(position));
        notifyDataSetChanged();
    }

    public List<Contacts> getSelectedContacts() {
        List<Contacts> selectedContacts = new ArrayList<>();
        for (int i = 0; i < mContactsList.size(); i++) {
            if (mSelectedItems.get(i)) {
                selectedContacts.add(mContactsList.get(i));
            }
        }
        return selectedContacts;
    }
}
