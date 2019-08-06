package com.example.yoked.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.yoked.OtherUserProfileActivity;
import com.example.yoked.PostDetailActivity;
import com.example.yoked.PostViewHolder;
import com.example.yoked.PostViewHolder;
import com.example.yoked.R;
import com.example.yoked.models.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PostsFragment extends Fragment {

    private static final String TAG = "PostsFragment";

    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    private boolean mShouldRefreshOnResume = false;

    public PostsFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_posts, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = (RecyclerView) rootView.findViewById(R.id.post_recycler_view);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

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
                if (model.likes.containsKey(getUid())) {
                    viewHolder.mLikeButton.setImageResource(R.drawable.ufi_heart_active);
                } else {
                    viewHolder.mLikeButton.setImageResource(R.drawable.ufi_heart);
                }

                // Bind Post to ViewHolder, setting OnClickListener for the like button and author
                try {
                    viewHolder.bindToPost(model, postRef.getKey(), new View.OnClickListener() {
                        @Override
                        public void onClick(View likeView) {
                            // Need to write to both places the post is stored
                            // update in user posts
                            Query userPostQuery = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());
                            String userPostPath = "/user-posts/" + model.uid + "/" + postRef.getKey();
                            onLikeClicked(userPostQuery, userPostPath);
                            // update in user tagged posts
                            updateTaggedLikes(model, postRef);
                            // update feeds
                            updateAllFeedsLikes(postRef.getKey());
//                            likePost(postRef.getKey());
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // go to user profile when author clicked
                            Intent intent = new Intent(getActivity(), OtherUserProfileActivity.class);
                            intent.putExtra("uid", model.uid);
                            // show the activity
                            startActivity(intent);
                        }
                    }, (new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // go to user profile when tagged clicked
                            goToTaggedProfile(model);
                        }
                    }));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    private void goToTaggedProfile(Post model) {
        final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String taggedUid = model.taggedFriendUid;
        if (taggedUid.equals(currentUid)) {
            Toast.makeText(getActivity(), "clicking on your own profile!", Toast.LENGTH_LONG);
        }
        else {
            Intent toOtherProfile = new Intent (getActivity(), OtherUserProfileActivity.class);
            toOtherProfile.putExtra("uid", taggedUid.toString());
            startActivity(toOtherProfile);
        }
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
                    likesMap.put(getUid(), true);
                }
                else {
                    if (likesMap.containsKey(getUid())) {
                        likeCount = likeCount - 1;
                        likesMap.remove(getUid());
                        mDatabase.child(likesPath).removeValue();
                    }
                    else {
                        likeCount = likeCount + 1;
                        likesMap.put(getUid(), true);
                    }
                }
                mDatabase.child(likesPath).updateChildren(likesMap);
                mDatabase.child(likeCountPath).setValue(likeCount);

                Log.i("PostsFragment", FirebaseAuth.getInstance().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // updates likes for post in all user feeds
    private void updateAllFeedsLikes(final String postRefKey) {
        Query query = mDatabase.child("users").child(getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // update current user's feed
                Query userTempQuery = mDatabase.child("user-feed").child(getUid()).child(postRefKey);
                String userTempPath = "/user-feed/" + getUid() + "/" + postRefKey;
                onLikeClicked(userTempQuery, userTempPath);
                // update current user's friend's feeds
                Map<String, Object> friendMap = (Map<String, Object>) dataSnapshot.child("friendList").getValue();
                if (friendMap!=null) {
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

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (mAdapter != null) {
//            mAdapter.cleanup();
//        }
//    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Query getQuery(DatabaseReference databaseReference) {
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery = databaseReference.child("user-feed")
                .child(getUid())
                .limitToFirst(100);
        return recentPostsQuery;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check should we need to refresh the fragment
        if(mShouldRefreshOnResume){
            refreshFragment();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mShouldRefreshOnResume = true;
    }

    public void refreshFragment()
    {
        Fragment fragment = new PostsFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container_flowlayout, fragment).commit();
    }

    public void updateTaggedLikes(final Post model, final DatabaseReference postRef) {
        String taggedUid = model.taggedFriendUid;
        if (taggedUid != null) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            Query userTaggedPostQuery = mDatabase.child("user-tagged-posts").child(taggedUid).child(postRef.getKey());
            String userTaggedPostPath = "/user-tagged-posts/" + taggedUid + "/" + postRef.getKey();
            onLikeClicked(userTaggedPostQuery, userTaggedPostPath);
        }
    }
}