package com.example.team_project;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.team_project.models.User;
import com.example.team_project.models.Utilities;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.IOException;
import java.util.Map;

public class SearchViewHolder extends RecyclerView.ViewHolder {

    public TextView mUsername;
    public ImageView mProfileImage;

    public SearchViewHolder(View itemView) {
        super(itemView);

        mUsername = itemView.findViewById(R.id.username_text_view);
        mProfileImage = itemView.findViewById(R.id.profile_image_view);
    }

    public void bindToPost(final User user) throws IOException {
        mUsername.setText(user.username);

        Query query = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("username");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newUser = (Map<String, Object>) snapshot.getValue();
                // check if user is the current user
                if (newUser.get("uid").toString().equals(user.uid)) {
                    if (newUser.get("profile_picture") != null) {
                        String imageUrl = newUser.get("profile_picture").toString();
                        // if profile pic is already set
                        if (!imageUrl.equals("")) {
                            try {
                                // set profile picture
                                Bitmap realImage = Utilities.decodeFromFirebaseBase64(imageUrl);
                                Bitmap circularImage = Utilities.getCircleBitmap(realImage);
                                Log.i("SearchViewHolder", "realImage: " + realImage);
                                mProfileImage.setImageBitmap(circularImage);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e("SearchViewHolder", "Profile pic issue", e);
                            }
                        }
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
}
