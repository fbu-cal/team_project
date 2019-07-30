package com.example.team_project;

import android.content.Intent;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private TextView mFullname, mUsername, mBody, mTagged, mTime, mLikeCount, mCommentCount;
    private ImageView mProfileImage, mPostImage;
    private ImageButton mLikeButton, mCommentButton;
    private DatabaseReference mDatabase;

    private String mPostOwnerUid;
    private String mPostRefKey;
    private String mCurrentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mFullname = findViewById(R.id.fullname_text_view);
        mUsername = findViewById(R.id.username_text_view);
        mBody = findViewById(R.id.body_text_view);
        mTagged = findViewById(R.id.tagged_text_view);
        mTime = findViewById(R.id.time_text_view);
        mLikeCount = findViewById(R.id.like_count_text_view);
        // mCommentCount = findViewById(R.id.comment_count_text_view);
        mProfileImage = findViewById(R.id.profile_image_view);
        mPostImage = findViewById(R.id.post_image_view);
        mLikeButton = findViewById(R.id.like_image_button);
        // mCommentButton = findViewById(R.id.comment_image_button);

        // get uid from intent
        Intent intent = getIntent();
        mPostOwnerUid = intent.getStringExtra("uid");
        mPostRefKey = intent.getStringExtra("postRefKey");

        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        findUser();
        findPost();

        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Query globalPostQuery = mDatabase.child("posts").child(postRef.getKey());
                Query userPostQuery = mDatabase.child("user-posts").child(mPostOwnerUid).child(mPostRefKey);
                //String globalPostPath = "/posts/" + postRef.getKey();
                String userPostPath = "/user-posts/" + mPostOwnerUid + "/" + mPostRefKey;
                //onLikeClicked(globalPostQuery, globalPostPath);
                onLikeClicked(userPostQuery, userPostPath);
                updateAllFeedsLikes();
            }
        });
    }

    private void findPost() {
        Query query = mDatabase.child("user-posts").child(mPostOwnerUid).child(mPostRefKey);
        Log.i("PostDetail", query.toString());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newPost = (Map<String, Object>) dataSnapshot.getValue();
                mBody.setText(newPost.get("body").toString());
                if (newPost.get("timestamp") != null) {
                    mTime.setText(getRelativeTimeAgo(newPost.get("timestamp").toString()));
                }
                if (newPost.get("taggedFriend") != null) {
                    mTagged.setText("with " + newPost.get("taggedFriend").toString().split(" ")[1]);
                }

                //Log.i("PostDetail", newPost.get("postImageUrl").toString());
                if (newPost.get("postImageUrl")!=null) {
                    String imageUrl = newPost.get("postImageUrl").toString();
                    // if profile pic is already set
                    if (!imageUrl.equals("")) {
                        try {
                            // set post image
                            Bitmap realImage = decodeFromFirebaseBase64(imageUrl);
                            mPostImage.setImageBitmap(realImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("PostDetail", "Post image issue", e);
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

    // find the profile owner's info and set information on screen
    public void findUser () {
        Query query = mDatabase.child("users").child(mPostOwnerUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                mFullname.setText(newUser.get("fullname").toString());
                mUsername.setText("@" + newUser.get("username").toString());
                if (newUser.get("profile_picture")!=null) {
                    String imageUrl = newUser.get("profile_picture").toString();
                    // if profile pic is already set
                    if (!imageUrl.equals("")) {
                        try {
                            // set profile picture
                            Bitmap realImage = decodeFromFirebaseBase64(imageUrl);
                            Bitmap circularImage = getCircleBitmap(realImage);
                            mProfileImage.setImageBitmap(circularImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("PostDetail", "Profile pic issue", e);
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

    private void onLikeClicked (Query query, final String path) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String likesPath = path + "/likes";
                String likeCountPath = path + "/likeCount";
                Map<String, Object> likesMap = (Map<String, Object>) dataSnapshot.child("likes").getValue();
                Long likeCount = (Long) dataSnapshot.child("likeCount").getValue();
                if (likesMap == null) {
                    likesMap = new HashMap<>();
                    likeCount = Long.valueOf(1);
                    likesMap.put(mCurrentUserUid, true);
                }
                else {
                    if (likesMap.containsKey(mCurrentUserUid)) {
                        likeCount = likeCount - 1;
                        likesMap.remove(mCurrentUserUid);
                        mDatabase.child(likesPath).removeValue();
                    }
                    else {
                        likeCount = likeCount + 1;
                        likesMap.put(mCurrentUserUid, true);
                    }
                }
                mDatabase.child(likesPath).updateChildren(likesMap);
                mDatabase.child(likeCountPath).setValue(likeCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // updates likes for post in all user feeds
    private void updateAllFeedsLikes() {
        Query query = mDatabase.child("users").child(mCurrentUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // update current user's feed
                Query userTempQuery = mDatabase.child("user-feed").child(mCurrentUserUid).child(mPostRefKey);
                String userTempPath = "/user-feed/" + mCurrentUserUid + "/" + mPostRefKey;
                onLikeClicked(userTempQuery, userTempPath);
                // update current user's friend's feeds
                Map<String, Object> friendMap = (Map<String, Object>) dataSnapshot.child("friendList").getValue();
                if (friendMap!=null) {
                    for (String friend : friendMap.keySet()) {
                        Query tempQuery = mDatabase.child("user-feed").child(friend).child(mPostRefKey);
                        String tempPath = "/user-feed/" + friend + "/" + mPostRefKey;
                        onLikeClicked(tempQuery, tempPath);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
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
}
