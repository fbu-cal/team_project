package com.example.team_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.team_project.models.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class OtherUserProfileActivity extends AppCompatActivity {

    private String uid;
    private String username;

    private TextView mUsername, mFullname;
    private ImageView mProfileImage;
    private Button mAddFriendButton;
    private RecyclerView mRecyclerView;

    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private LinearLayoutManager mManager;

    String tempUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        mUsername = findViewById(R.id.username_text_view);
        mFullname = findViewById(R.id.fullname_text_view);
        mProfileImage = findViewById(R.id.profile_image_view);
        mAddFriendButton = findViewById(R.id.add_friend_button);

        mRecyclerView = findViewById(R.id.post_recycler_view);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecyclerView.setHasFixedSize(true);
        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mManager);

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
                if (model.likes.containsKey(getUid())) {
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

        // unwrap the post passed in via intent, using its simple name as a key
        uid = getIntent().getStringExtra("uid");
        // set variables with content from post
        findUser();


        Log.i("OtherUser", "EEEP: "+tempUsername);

        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("OtherUser", "Maybe one day you will be friends :')");
            }
        });
    }

    public void findUser () {
        Query query = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("username");
            query.addChildEventListener(new ChildEventListener() {// Retrieve new posts as they are added to Firebase
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newUser = (Map<String, Object>) snapshot.getValue();
                if (newUser.get("uid").toString().equals(uid)) {
                    uid = newUser.get("uid").toString();
                    username = newUser.get("username").toString();
                    mUsername.setText(username);
                    tempUsername = username;

                    String imageUrl = newUser.get("profile_picture").toString();
                    // if profile pic is already set
                    if (!imageUrl.equals("")) {
                        Log.i("PostViewHolder", "imageUrl: " + imageUrl);
                        try {
                            // set profile picture
                            Bitmap realImage = decodeFromFirebaseBase64(imageUrl);
//                                Bitmap circularImage = getCircleBitmap(realImage);
                            Log.i("PostViewHolder", "realImage: " + realImage);
                            mProfileImage.setImageBitmap(realImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("PostViewHolder", "Profile pic issue", e);
                        }
                    }
                    mFullname.setText(newUser.get("fullname").toString());
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

    private void onLikeClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.likes.containsKey(getUid())) {
                    // Unlike the post and remove self from likes
                    p.likeCount = p.likeCount - 1;
                    p.likes.remove(getUid());
                } else {
                    // Unlike the post and add self to likes
                    p.likeCount = p.likeCount + 1;
                    p.likes.put(getUid(), true);
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

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery = databaseReference.child("posts")
                .orderByChild("uid")
                .equalTo(getUid())
                .limitToFirst(20);
        // [END recent_posts_query]

        return recentPostsQuery;
    }
}
