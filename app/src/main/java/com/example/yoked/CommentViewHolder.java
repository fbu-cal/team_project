package com.example.yoked;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yoked.models.Comment;
import com.example.yoked.models.Utilities;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Map;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    public TextView mUsername, mCommentText, mTimestamp;
    public ImageView mProfilePicture;

    public CommentViewHolder(View itemView) {
        super(itemView);

        mUsername = itemView.findViewById(R.id.background_text_view);
        mCommentText = itemView.findViewById(R.id.comment_text_view);
        mTimestamp = itemView.findViewById(R.id.timestamp_text_view);
        mProfilePicture = itemView.findViewById(R.id.profile_image_view);
    }

    public void bindToPost(final Comment comment) throws IOException {
        mCommentText.setText(comment.text);
        mTimestamp.setText(Utilities.getRelativeTimeAgo(comment.timestamp));
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("users").child(comment.uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                String username = newUser.get("username").toString();
                mUsername.setText(username);
                if (newUser.get("profile_picture")!=null) {
                    String imageUrl = newUser.get("profile_picture").toString();
                    // if profile pic is already set
                    if (!imageUrl.equals("")) {
                        try {
                            // set profile picture
                            Bitmap realImage = Utilities.decodeFromFirebaseBase64(imageUrl);
                            Bitmap circularImage = Utilities.getCircleBitmap(realImage);
                            mProfilePicture.setImageBitmap(circularImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("CommentViewHolder", "Profile pic issue", e);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }
}
