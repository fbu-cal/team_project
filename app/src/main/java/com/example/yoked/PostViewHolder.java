package com.example.yoked;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yoked.models.Post;
import com.example.yoked.models.Utilities;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Map;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView mAuthor, mLikeCount, mBody, mTime, mTagged, mCommentCount;
    public ImageButton mLikeButton;
    public ImageView mProfilePicture, mPostImage, mCommentImage;

    public PostViewHolder(View itemView) {
        super(itemView);

        mAuthor = itemView.findViewById(R.id.background_text_view);
        mLikeButton = itemView.findViewById(R.id.like_button);
        mLikeCount = itemView.findViewById(R.id.like_count_text_view);
        mBody = itemView.findViewById(R.id.body_text_view);
        mProfilePicture = itemView.findViewById(R.id.profile_image_view);
        mPostImage = itemView.findViewById(R.id.post_image_view);
        mTime = itemView.findViewById(R.id.time_text_view);
        mTagged = itemView.findViewById(R.id.tagged_text_view);
        mCommentImage = itemView.findViewById(R.id.comment_image_view);
        mCommentCount = itemView.findViewById(R.id.comment_count_text_view);
    }

    public void bindToPost(final Post post, String postRefKey, View.OnClickListener likeClickListener, View.OnClickListener authorClickListener, View.OnClickListener taggedClickListener) throws IOException {
        mAuthor.setText("@" + post.author);
        mLikeCount.setText(String.valueOf(post.likeCount));
        mBody.setText(post.body);
        if (post.timestamp != null) {
            mTime.setText(Utilities.getRelativeTimeAgo(post.timestamp));
        }
        if (post.postImageUrl != null) {
            mPostImage.setImageBitmap(Utilities.decodeFromFirebaseBase64(post.postImageUrl));
        }
        if (post.taggedFriend != null) {
            mTagged.setText("with " + post.taggedFriend);
        }

        mLikeButton.setOnClickListener(likeClickListener);

        mAuthor.setOnClickListener(authorClickListener);

        mTagged.setOnClickListener(taggedClickListener);

        findCommentCount(postRefKey);

        Query query = FirebaseDatabase.getInstance().getReference("users")
                .child(post.uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                if (newUser.get("profile_picture") != null) {
                    String imageUrl = newUser.get("profile_picture").toString();
                    // if profile pic is already set
                    if (!imageUrl.equals("")) {
                        try {
                            // set profile picture
                            Bitmap realImage = Utilities.decodeFromFirebaseBase64(imageUrl);
                            Bitmap circularImage = Utilities.getCircleBitmap(realImage);
                            Log.i("PostViewHolder", "realImage: " + realImage);
                            mProfilePicture.setImageBitmap(circularImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("PostViewHolder", "Profile pic issue", e);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // find comment count and set it
    private void findCommentCount(String postRefKey) {
        FirebaseDatabase.getInstance().getReference()
                .child("post-comments").child(postRefKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long count = dataSnapshot.getChildrenCount();
                        mCommentCount.setText(count+"");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
}
