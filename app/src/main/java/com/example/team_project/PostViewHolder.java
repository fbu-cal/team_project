package com.example.team_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.team_project.models.Post;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView mAuthor, mLikeCount, mBody, mTime, mTagged, mCommentCount;
    public ImageButton mLikeButton;
    public ImageView mProfilePicture, mPostImage, mCommentImage;

    public PostViewHolder(View itemView) {
        super(itemView);

        mAuthor = itemView.findViewById(R.id.username_text_view);
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
            mTime.setText(getRelativeTimeAgo(post.timestamp));
        }
        if (post.postImageUrl != null) {
            mPostImage.setImageBitmap(decodeFromFirebaseBase64(post.postImageUrl));
        }
        if (post.taggedFriend != null) {
            mTagged.setText("with " + post.taggedFriend.split(" ")[1]);
        }

        mLikeButton.setOnClickListener(likeClickListener);

        mAuthor.setOnClickListener(authorClickListener);

        mTagged.setOnClickListener(taggedClickListener);

        findCommentCount(postRefKey);

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
                            try {
                                // set profile picture
                                Bitmap realImage = decodeFromFirebaseBase64(imageUrl);
                                Bitmap circularImage = getCircleBitmap(realImage);
                                Log.i("PostViewHolder", "realImage: " + realImage);
                                mProfilePicture.setImageBitmap(circularImage);
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

    private void findCommentCount(String postRefKey) {
        FirebaseDatabase.getInstance().getReference().child("post-comments").child(postRefKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int count = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            //Comment comment = snapshot.getValue(Comment.class);
                            count++;
                        }
                        mCommentCount.setText(count+"");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
}
