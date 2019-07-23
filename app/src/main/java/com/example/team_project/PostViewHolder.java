package com.example.team_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.team_project.models.Post;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.IOException;
import java.util.Map;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView mAuthor;
    public ImageButton mLikeButton;
    public TextView mLikeCount;
    public TextView mBody;
    public ImageView mProfilePicture;

    public PostViewHolder(View itemView) {
        super(itemView);

        mAuthor = itemView.findViewById(R.id.username_text_view);
        mLikeButton = itemView.findViewById(R.id.like_button);
        mLikeCount = itemView.findViewById(R.id.like_count_text_view);
        mBody = itemView.findViewById(R.id.body_text_view);
        mProfilePicture = itemView.findViewById(R.id.profile_image_view);
    }

    public void bindToPost(final Post post, View.OnClickListener starClickListener) {
        mAuthor.setText(post.author);
        mLikeCount.setText(String.valueOf(post.likeCount));
        mBody.setText(post.body);

        mLikeButton.setOnClickListener(starClickListener);

        Query query = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("username");
        query.addChildEventListener(new ChildEventListener() {// Retrieve new posts as they are added to Firebase
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newUser = (Map<String, Object>) snapshot.getValue();
                // check if user is the current user
                if (newUser.get("uid").toString().equals(post.uid)) {
                    if (newUser.get("profile_picture") != null) {
                        String imageUrl = newUser.get("profile_picture").toString();
                        // if profile pic is already set
                        if (!imageUrl.equals("")) {
                            Log.i("PostViewHolder", "imageUrl: " + imageUrl);
                            try {
                                // set profile picture
                                Bitmap realImage = decodeFromFirebaseBase64(imageUrl);

                                Log.i("PostViewHolder", "realImage: " + realImage);
                                mProfilePicture.setImageBitmap(realImage);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e("PostViewHolder", "Profile pic issue", e);
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

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }
}
