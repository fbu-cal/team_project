package com.example.team_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.example.team_project.models.Post;
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

import java.io.IOException;
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
                            DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                            DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());
                            // Run two transactions
                            onLikeClicked(globalPostRef);
                            onLikeClicked(userPostRef);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        // TODO
        // Set button to "Add friend" if both haven't sent request
        // Set button to "Request sent" if user sent and disable button
        // Set button to "Accept request" if other sent
        // Set button to "Message friend" if users are friends

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
                // check button text
                // if button text = "add friend"
                    // create new friend object and post to firebase for both users
                    // change button text to "Request sent"
                // if button text = "accept request"
                    // update friend object for both users
                    // change button text to "Message friend"
                // if button text = "message friend"
                    // redirect user to message
            }
        });
    }

    private void acceptRequest() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference();
        // update for current user
        Query query = reference.child("users").child(mCurrentUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String statusPath = "/users/" + mCurrentUserUid + "/friendStatuses";
                Map<String, Object> userFriends = (Map<String, Object>) dataSnapshot.child("friendStatuses").getValue();
                userFriends.put(mProfileOwnerUid, "Already Friends");
                reference.child(statusPath).updateChildren(userFriends);
                String listPath = "/users/" + mCurrentUserUid + "/friendList";
                Map<String, Object> userFriendsList = (Map<String, Object>) dataSnapshot.child("friendList").getValue();
                if (userFriendsList == null) {
                    userFriendsList = new HashMap<>();
                    userFriendsList.put(mProfileOwnerUid, true);
                    reference.child(listPath).updateChildren(userFriendsList);
                }
                else {
                    userFriendsList.put(mProfileOwnerUid, true);
                    reference.child(listPath).updateChildren(userFriendsList);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
        // update for other user
        query = reference.child("users").child(mProfileOwnerUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String statusPath = "/users/" + mProfileOwnerUid + "/friendStatuses";
                Map<String, Object> userFriends = (Map<String, Object>) dataSnapshot.child("friendStatuses").getValue();
                userFriends.put(mCurrentUserUid, "Already Friends");
                reference.child(statusPath).updateChildren(userFriends);
                String listPath = "/users/" + mProfileOwnerUid + "/friendList";
                Map<String, Object> userFriendsList = (Map<String, Object>) dataSnapshot.child("friendList").getValue();
                if (userFriendsList == null) {
                    userFriendsList = new HashMap<>();
                    userFriendsList.put(mCurrentUserUid, true);
                    reference.child(listPath).updateChildren(userFriendsList);
                }
                else {
                    userFriendsList.put(mCurrentUserUid, true);
                    reference.child(listPath).updateChildren(userFriendsList);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
        mAddFriendButton.setText("Already Friends");
        mAddFriendButton.setEnabled(false);
    }

    public void updateTextButton () {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference();
        Query query = reference.child("users").child(mCurrentUserUid);
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


    public void addFriend () {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference();
        Query query = reference.child("users").child(mCurrentUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String path = "/users/" + mCurrentUserUid + "/friendStatuses";
                Map<String, Object> userFriends = (Map<String, Object>) dataSnapshot.child("friendStatuses").getValue();
                if (userFriends == null) {
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(mProfileOwnerUid, "Sent Request");
                    reference.child(path).updateChildren(result);
                    mAddFriendButton.setText("Sent Request");
                    mAddFriendButton.setEnabled(false);
                    updateOtherUser("Received Request");
                }
                else {
                    if (!userFriends.containsKey(mProfileOwnerUid)) {
                        userFriends.put(mProfileOwnerUid, "Sent Request");
                        reference.child(path).updateChildren(userFriends);
                        mAddFriendButton.setText("Sent Request");
                        mAddFriendButton.setEnabled(false);
                        updateOtherUser("Received Request");
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }

    public void updateOtherUser (final String status) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference();
        Query query = reference.child("users").child(mProfileOwnerUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String path = "/users/" + mProfileOwnerUid + "/friendStatuses";
                Map<String, Object> userFriends = (Map<String, Object>) dataSnapshot.child("friendStatuses").getValue();
                if (userFriends == null) {
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(mCurrentUserUid, status);
                    reference.child(path).updateChildren(result);
                    mAddFriendButton.setText(status);
                    mAddFriendButton.setEnabled(false);
                }
                else {
                    if (!userFriends.containsKey(mCurrentUserUid)) {
                        userFriends.put(mCurrentUserUid, status);
                        reference.child(path).updateChildren(userFriends);
                        mAddFriendButton.setText(status);
                        mAddFriendButton.setEnabled(false);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }

    public void findUser () {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference();
        Query query = reference.child("users").child(mProfileOwnerUid);
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
//                          Bitmap circularImage = getCircleBitmap(realImage);
                            mProfileImage.setImageBitmap(realImage);
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

    private void onLikeClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.likes.containsKey(mCurrentUserUid)) {
                    // Unlike the post and remove self from likes
                    p.likeCount = p.likeCount - 1;
                    p.likes.remove(mCurrentUserUid);
                } else {
                    // Unlike the post and add self to likes
                    p.likeCount = p.likeCount + 1;
                    p.likes.put(mCurrentUserUid, true);
                }
                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d("OtherUser", "postTransaction onComplete: " + databaseError);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public Query getQuery(DatabaseReference databaseReference) {
        // Last 100 posts, these are automatically the 100 most recent
        Query recentPostsQuery = databaseReference.child("posts")
                .orderByChild("uid")
                .equalTo(mProfileOwnerUid)
                .limitToFirst(20);

        return recentPostsQuery;
    }
}
