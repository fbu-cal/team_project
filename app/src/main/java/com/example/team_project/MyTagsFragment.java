package com.example.team_project;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.team_project.fragments.ProfileFragment;
import com.example.team_project.models.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyTagsFragment extends Fragment {

    // context for rendering
    Context context;
    private String mCurrentUserUid;

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private LinearLayoutManager mManager;
    private boolean mShouldRefreshOnResume;

    private TabLayout mTabLayout;
    private Fragment fragmentOne;
    private Fragment fragmentTwo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // get the context and create the inflater
        context = parent.getContext();
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_my_posts, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mRecyclerView = view.findViewById(R.id.post_recycler_view);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecyclerView.setHasFixedSize(true);
        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getContext());
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
                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra("uid", model.uid);
                        intent.putExtra("postRefKey", postRef.getKey());
                        startActivity(intent);
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
                    viewHolder.bindToPost(model, postRef.getKey(), new View.OnClickListener() {
                                @Override
                                public void onClick(View starView) {
                                    // Need to write to both places the post is stored
                                    // update in user posts
                                    Query userPostQuery = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());
                                    String userPostPath = "/user-posts/" + model.uid + "/" + postRef.getKey();
                                    onLikeClicked(userPostQuery, userPostPath);
                                    // update in user tagged posts
                                    updateTaggedLikes(model, postRef);
                                    // update feeds
                                    updateAllFeedsLikes(postRef.getKey());
                                }
                            },
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // nothing. already on correct activity
                                }
                            }, (new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // go to user profile when tagged clicked
                                    findTaggedUser(model);
                                }
                            }));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    private void findTaggedUser(Post model) {
        final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String taggedUsername = model.taggedFriend;
        Query query = mDatabase.child("users").orderByChild("username").equalTo(taggedUsername);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Map<String, Object> newUser = (HashMap<String, Object>) data.getValue();
                    if (newUser.get("uid").toString().equals(currentUid)) {
                        Toast.makeText(getActivity(), "clicking on your own profile!", Toast.LENGTH_LONG);
                    }
                    else {
                        Intent toOtherProfile = new Intent (getActivity(), OtherUserProfileActivity.class);
                        toOtherProfile.putExtra("uid", newUser.get("uid").toString());
                        startActivity(toOtherProfile);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public Query getQuery(DatabaseReference databaseReference) {
        Query recentPostsQuery = databaseReference.child("user-tagged-posts")
                .child(mCurrentUserUid)
                .limitToFirst(20);
        return recentPostsQuery;
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
                    likeCount = likeCount + 1;
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
    private void updateAllFeedsLikes(final String postRefKey) {
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

    public void updateTaggedLikes(final Post model, final DatabaseReference postRef) {
        final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String taggedUsername = model.taggedFriend;
        Query query = mDatabase.child("users").orderByChild("username").equalTo(taggedUsername);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Map<String, Object> newUser = (HashMap<String, Object>) data.getValue();
                    String taggedUid = newUser.get("uid").toString();
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    Query userTaggedPostQuery = mDatabase.child("user-tagged-posts").child(taggedUid).child(postRef.getKey());
                    String userTaggedPostPath = "/user-tagged-posts/" + taggedUid + "/" + postRef.getKey();
                    onLikeClicked(userTaggedPostQuery, userTaggedPostPath);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        // Check should we need to refresh the fragment
//        if(mShouldRefreshOnResume){
//            refreshFragment();
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        mShouldRefreshOnResume = true;
//    }
//
//    public void refreshFragment()
//    {
//        Fragment fragment = new MyPostsFragment();
//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        fragmentManager.beginTransaction().replace(R.id.container_flowlayout, fragment).commit();
//    }

}
