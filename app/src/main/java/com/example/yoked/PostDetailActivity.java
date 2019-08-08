package com.example.yoked;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yoked.models.Comment;
import com.example.yoked.models.Utilities;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private TextView mFullname, mUsername, mBody, mTagged, mTime, mLikeCount, mCommentCount;
    private EditText mCommentEditText;
    private ImageView mProfileImage, mPostImage;
    private ImageButton mLikeButton, mCommentButton, mBackButton;
    private Button mPostCommentButton;

    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Comment, CommentViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    private String mPostOwnerUid;
    private String mPostRefKey;
    private String mCurrentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mFullname = findViewById(R.id.fullname_text_view);
        mUsername = findViewById(R.id.background_text_view);
        mBody = findViewById(R.id.body_text_view);
        mTagged = findViewById(R.id.tagged_text_view);
        mTime = findViewById(R.id.time_text_view);
        mLikeCount = findViewById(R.id.like_count_text_view);
        mCommentCount = findViewById(R.id.comment_count_text_view);
        mProfileImage = findViewById(R.id.profile_image_view);
        mPostImage = findViewById(R.id.post_image_view);
        mLikeButton = findViewById(R.id.like_image_button);
        mCommentButton = findViewById(R.id.comment_image_button);
        mCommentEditText = findViewById(R.id.comment_edit_text);
        mPostCommentButton = findViewById(R.id.post_comment_button);
        mBackButton = findViewById(R.id.back_image_button);

        // get uid from intent
        Intent intent = getIntent();
        mPostOwnerUid = intent.getStringExtra("uid");
        mPostRefKey = intent.getStringExtra("postRefKey");

        // update ui with user and post information
        findUser();
        findPost();
        findCommentCount(mPostRefKey);

        // Set up recycler view for comments
        mRecycler = (RecyclerView) findViewById(R.id.comment_recycler_view);
        mRecycler.setHasFixedSize(true);
        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(PostDetailActivity.this);
        //mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);
        // Set up FirebaseRecyclerAdapter with the Query
        Query commentsQuery = mDatabase.child("post-comments")
                .child(mPostRefKey)
                .limitToFirst(100);
        mAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(Comment.class, R.layout.item_comment,
                CommentViewHolder.class, commentsQuery) {
            @Override
            protected void populateViewHolder(final CommentViewHolder viewHolder, final Comment model, final int position) {
                final DatabaseReference postRef = getRef(position);
                // Bind Post to ViewHolder, setting OnClickListener for the like button and author
                try {
                    viewHolder.bindToPost(model);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mRecycler.setAdapter(mAdapter);

        // set on click listener for like button
        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query userPostQuery = mDatabase.child("user-posts").child(mPostOwnerUid).child(mPostRefKey);
                String userPostPath = "/user-posts/" + mPostOwnerUid + "/" + mPostRefKey;
                onLikeClicked(userPostQuery, userPostPath);
                updateAllFeedsLikes();
            }
        });

        // set on click listener for post comment button
        mPostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = mCommentEditText.getText().toString();
                if (comment.trim().equals(""))
                    Toast.makeText(PostDetailActivity.this, "Comment unsuccessful! Missing comment!", Toast.LENGTH_LONG);
                else {
                    writeComment(comment);
                }
            }
        });

        mTagged.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToTaggedProfile();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toHome = new Intent (PostDetailActivity.this, MainActivity.class);
                startActivity(toHome);
            }
        });

    }

    private void goToTaggedProfile() {
        Query query = mDatabase.child("user-posts").child(mPostOwnerUid).child(mPostRefKey).child("taggedFriendUid");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String taggedFriendUid = (String) dataSnapshot.getValue();
                if (taggedFriendUid.equals(mCurrentUserUid)) {
                    Toast.makeText(PostDetailActivity.this,"clicking on your own profile!", Toast.LENGTH_LONG);
                }
                else {
                    Intent toOtherProfile = new Intent (PostDetailActivity.this, OtherUserProfileActivity.class);
                    toOtherProfile.putExtra("uid", taggedFriendUid);
                    startActivity(toOtherProfile);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // find comment count and set it
    private void findCommentCount(String postRefKey) {
        mDatabase.child("post-comments").child(postRefKey)
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

    // write comment to firebase
    private void writeComment(String text) {
        String key = mDatabase.child("comments").push().getKey();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        String timestamp = simpleDateFormat.format(new Date());
        Comment comment = new Comment(mCurrentUserUid, text, timestamp);
        Map<String, Object> commentValues = comment.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/post-comments/" + mPostRefKey + "/" + key, commentValues);
        mDatabase.updateChildren(childUpdates);
        Toast.makeText(PostDetailActivity.this, "Post Successful!", Toast.LENGTH_LONG).show();
        mCommentEditText.setText("");
        findCommentCount(mPostRefKey);
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
                    mTime.setText(Utilities.getRelativeTimeAgo(newPost.get("timestamp").toString()));
                }
                if (newPost.get("taggedFriend") != null) {
                    mTagged.setText("with " + newPost.get("taggedFriend"));
                }
                if (newPost.get("postImageUrl")!=null) {
                    String imageUrl = newPost.get("postImageUrl").toString();
                    // if profile pic is already set
                    if (!imageUrl.equals("")) {
                        try {
                            // set post image
                            Bitmap realImage = Utilities.decodeFromFirebaseBase64(imageUrl);
                            mPostImage.setImageBitmap(realImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("PostDetail", "Post image issue", e);
                        }
                    }
                }

                Map<String, Object> likeMap = (Map<String, Object>) dataSnapshot.child("likes").getValue();
                // Determine if the current user has liked this post and set UI accordingly
                if (likeMap != null) {
                    if (likeMap.containsKey(mCurrentUserUid)) {
                        mLikeButton.setImageResource(R.drawable.ufi_heart_active);
                    } else {
                        mLikeButton.setImageResource(R.drawable.ufi_heart);
                    }
                }
                mLikeCount.setText(newPost.get("likeCount").toString());
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
                            Bitmap realImage = Utilities.decodeFromFirebaseBase64(imageUrl);
                            Bitmap circularImage = Utilities.getCircleBitmap(realImage);
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
                    mLikeButton.setImageResource(R.drawable.ufi_heart_active);
                }
                else {
                    if (likesMap.containsKey(mCurrentUserUid)) {
                        likeCount = likeCount - 1;
                        likesMap.remove(mCurrentUserUid);
                        mDatabase.child(likesPath).removeValue();
                        mLikeButton.setImageResource(R.drawable.ufi_heart);
                    }
                    else {
                        likeCount = likeCount + 1;
                        likesMap.put(mCurrentUserUid, true);
                        mLikeButton.setImageResource(R.drawable.ufi_heart_active);
                    }
                }
                mDatabase.child(likesPath).updateChildren(likesMap);
                mDatabase.child(likeCountPath).setValue(likeCount);
                mLikeCount.setText(likeCount.toString());
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
}