package com.example.team_project;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
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
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.team_project.models.Notification;
import com.example.team_project.models.Post;
import com.example.team_project.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OtherUserProfileActivity extends AppCompatActivity {

    private String mProfileOwnerUid;
    private String mCurrentUserUid;
    private String username;

    private TextView mUsernameText, mFullnameText;
    private ImageView mProfileImage;
    private Button mAddFriendButton;
    private RecyclerView mRecyclerView;

    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private LinearLayoutManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        mUsernameText = findViewById(R.id.username_text_view);
        mFullnameText = findViewById(R.id.fullname_text_view);
        mProfileImage = findViewById(R.id.profile_image_view);
        mAddFriendButton = findViewById(R.id.add_friend_button);
        mRecyclerView = findViewById(R.id.post_recycler_view);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // get current logged in user uid
        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mRecyclerView.setHasFixedSize(true);
        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mManager);

        // unwrap the post passed in via intent, using its simple name as a key
        mProfileOwnerUid = getIntent().getStringExtra("uid");
        // set variables with content from post
        findUser();

        // update mAddFriend button to reflect current friend status
        updateTextButton();

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class, R.layout.item_post,
                PostViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, final Post model, final int position) {
                final DatabaseReference postRef = getRef(position);
                // Set click listener for the whole post view
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
//                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
//                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
//                        startActivity(intent);
                    }
                });
                // Determine if the current user has liked this post and set UI accordingly
                if (model.likes.containsKey(mCurrentUserUid)) {
                    viewHolder.mLikeButton.setImageResource(R.drawable.ufi_heart_active);
                } else {
                    viewHolder.mLikeButton.setImageResource(R.drawable.ufi_heart);
                }
                // Bind Post to ViewHolder, setting OnClickListener for the star button
                try {
                    viewHolder.bindToPost(model, new View.OnClickListener() {
                                @Override
                                public void onClick(View starView) {
                                    // Need to write to both places the post is stored
                                    //Query globalPostQuery = mDatabase.child("posts").child(postRef.getKey());
                                    Query userPostQuery = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());
                                    //String globalPostPath = "/posts/" + postRef.getKey();
                                    String userPostPath = "/user-posts/" + model.uid + "/" + postRef.getKey();
                                    //onLikeClicked(globalPostQuery, globalPostPath);
                                    onLikeClicked(userPostQuery, userPostPath);
                                    updateAllFeedsLikes(postRef.getKey());
                                }
                            },
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // nothing. already on correct page
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = mAddFriendButton.getText().toString();
                if (buttonText.equals("Add Friend")) {
                    addFriend();
                }
                else if (buttonText.equals("Accept Request")) {
                    acceptRequest();
                }
                // TODO
                // if button text = "message friend"
                    // redirect user to message
            }
        });
    }

    // Method for accepting friend requests. Will update friendStatuses and friendList for both users.
    private void acceptRequest() {
        // update for current user
        Query query = mDatabase.child("users").child(mCurrentUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // update friendStatus
                String statusPath = "/users/" + mCurrentUserUid + "/friendStatuses";
                Map<String, Object> userFriends = (Map<String, Object>) dataSnapshot.child("friendStatuses").getValue();
                userFriends.put(mProfileOwnerUid, "Already Friends");
                mDatabase.child(statusPath).updateChildren(userFriends);
                // update friendList
                String listPath = "/users/" + mCurrentUserUid + "/friendList";
                Map<String, Object> userFriendsList = (Map<String, Object>) dataSnapshot.child("friendList").getValue();
                if (userFriendsList == null)
                    userFriendsList = new HashMap<>();
                // finds user's name and adds it as the value
                acceptRequestHelper(userFriendsList, mProfileOwnerUid, listPath);
//                userFriendsList.put(mProfileOwnerUid, true);
//                mDatabase.child(listPath).updateChildren(userFriendsList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
        // update for other user
        query = mDatabase.child("users").child(mProfileOwnerUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String statusPath = "/users/" + mProfileOwnerUid + "/friendStatuses";
                Map<String, Object> userFriends = (Map<String, Object>) dataSnapshot.child("friendStatuses").getValue();
                userFriends.put(mCurrentUserUid, "Already Friends");
                mDatabase.child(statusPath).updateChildren(userFriends);
                String listPath = "/users/" + mProfileOwnerUid + "/friendList";
                Map<String, Object> userFriendsList = (Map<String, Object>) dataSnapshot.child("friendList").getValue();
                if (userFriendsList == null)
                    userFriendsList = new HashMap<>();
                acceptRequestHelper(userFriendsList, mCurrentUserUid, listPath);
//                userFriendsList.put(mCurrentUserUid, true);
//                mDatabase.child(listPath).updateChildren(userFriendsList);
                sendNotification(mCurrentUserUid, mProfileOwnerUid, "has accepted your friend request");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
        mAddFriendButton.setText("Already Friends");
        mAddFriendButton.setEnabled(false);
        // updates feed for both users
        updateFriendsFeed(mCurrentUserUid, mProfileOwnerUid);
        updateFriendsFeed(mProfileOwnerUid, mCurrentUserUid);
    }

    private void acceptRequestHelper(final Map<String, Object> userFriendsList, final String uid, final String listPath) {
        Query query = mDatabase.child("users").child(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                String username = newUser.get("username").toString();
                userFriendsList.put(uid, username);
                mDatabase.child(listPath).updateChildren(userFriendsList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });

    }

    // update the text on the button depending on the current user's relationship with the profile owner
    public void updateTextButton () {
        Query query = mDatabase.child("users").child(mCurrentUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String path = "/users/" + mCurrentUserUid + "/friendStatuses";
                Map<String, Object> userFriends = (Map<String, Object>) dataSnapshot.child("friendStatuses").getValue();
                if (userFriends == null) {
                    mAddFriendButton.setText("Add Friend");
                }
                else {
                    if (!userFriends.containsKey(mProfileOwnerUid)) {
                        mAddFriendButton.setText("Add Friend");
                    }
                    else {
                        if (userFriends.get(mProfileOwnerUid).equals("Sent Request")) {
                            mAddFriendButton.setText("Request Sent");
                            mAddFriendButton.setEnabled(false);
                        }
                        else if (userFriends.get(mProfileOwnerUid).equals("Received Request")) {
                            mAddFriendButton.setText("Accept Request");
                        }
                        else if (userFriends.get(mProfileOwnerUid).equals("Already Friends")) {
                            mAddFriendButton.setText("Already Friends");
                            mAddFriendButton.setEnabled(false);
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

    // updates current user's friendStatuses when they add another user
    public void addFriend () {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference();
        Query query = reference.child("users").child(mCurrentUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String path = "/users/" + mCurrentUserUid + "/friendStatuses";
                Map<String, Object> userFriends = (Map<String, Object>) dataSnapshot.child("friendStatuses").getValue();
                if (userFriends == null)
                    userFriends = new HashMap<>();
                userFriends.put(mProfileOwnerUid, "Sent Request");
                reference.child(path).updateChildren(userFriends);
                mAddFriendButton.setText("Sent Request");
                mAddFriendButton.setEnabled(false);
                updateAddedUser("Received Request");
                sendNotification(mCurrentUserUid, mProfileOwnerUid, "has sent you a friend request");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }

    // updates friendStatuses for the user that got added by the current user
    public void updateAddedUser (final String status) {
        Query query = mDatabase.child("users").child(mProfileOwnerUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String path = "/users/" + mProfileOwnerUid + "/friendStatuses";
                Map<String, Object> userFriends = (Map<String, Object>) dataSnapshot.child("friendStatuses").getValue();
                if (userFriends == null)
                    userFriends = new HashMap<>();
                userFriends.put(mCurrentUserUid, status);
                mDatabase.child(path).updateChildren(userFriends);
                mAddFriendButton.setText(status);
                mAddFriendButton.setEnabled(false);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }

    // find the profile owner's info and set information on screen
    public void findUser () {
        Query query = mDatabase.child("users").child(mProfileOwnerUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                mProfileOwnerUid = newUser.get("uid").toString();
                username = newUser.get("username").toString();
                mUsernameText.setText(username);
                mFullnameText.setText(newUser.get("fullname").toString());
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
                            Log.e("PostViewHolder", "Profile pic issue", e);
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

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public Query getQuery(DatabaseReference databaseReference) {
        Query recentPostsQuery = databaseReference.child("user-posts")
                .child(mProfileOwnerUid)
                .limitToFirst(20);
        return recentPostsQuery;
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

    // updates likes for post in Firebase (user-posts, user-feed)
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
    private void updateAllFeedsLikes (final String postRefKey) {
        Query query = mDatabase.child("users").child(mCurrentUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // update current user's feed
                Query userTempQuery = mDatabase.child("user-feed").child(mCurrentUserUid).child(postRefKey);
                String userTempPath = "/user-feed/" + mCurrentUserUid + "/" + postRefKey;
                onLikeClicked(userTempQuery, userTempPath);
                // update current user's friend's feeds
                Map<String, Object> friendMap = (Map<String, Object>) dataSnapshot.child("friendList").getValue();
                if (friendMap != null) {
                    for (String friend : friendMap.keySet()) {
                        Query tempQuery = mDatabase.child("user-feed").child(friend).child(postRefKey);
                        String tempPath = "/user-feed/" + friend + "/" + postRefKey;
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

    // updates new friend's feed with their new friend's posts
    private void updateFriendsFeed (final String feedOwnerUid, String postOwnerUid) {
        FirebaseDatabase.getInstance().getReference().child("user-posts").child(postOwnerUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Post post = snapshot.getValue(Post.class);
                            writePostToFeed(feedOwnerUid, post, snapshot.getKey());
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    // helper method to write post to feed for updateFriendsFeed
    private void writePostToFeed(String feedOwnerUid, Post post, String key) {
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user-feed/" + feedOwnerUid + "/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
    }

    private void sendNotification(final String fromUid, final String toUid, final String body) {
        Query query = mDatabase.child("users").child(fromUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                String title = newUser.get("username").toString();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
                String timestamp = simpleDateFormat.format(new Date());
                String imageUrl = "";
                if (newUser.get("profile_picture")!=null)
                    imageUrl = newUser.get("profile_picture").toString();
                Notification notif = new Notification
                        ("friend", imageUrl, title, body, timestamp, toUid, fromUid);
                updateNotification(toUid, notif);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }

    private void updateNotification(String toUid, Notification notif) {
        String key = mDatabase.child("notification").push().getKey();
        Map<String, Object> notifValues = notif.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-notifications/" + toUid + "/" + key, notifValues);
        mDatabase.updateChildren(childUpdates);
        // update user-feed
        Toast.makeText(OtherUserProfileActivity.this, "Sent Notification", Toast.LENGTH_LONG).show();
    }
}
