package com.example.team_project.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.team_project.PostAdapter;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Map;

public class PostsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private PostAdapter mPostAdapter;
    private ArrayList<Map<String, Object>> mPosts;
    private LinearLayoutManager mLinearLayoutManager;

    private SwipeRefreshLayout mSwipeContainer;

    private DatabaseReference mDatabaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
//        return inflater.inflate(R.layout.fragment_posts, parent, false);
        super.onCreateView(inflater, parent, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_posts, parent, false);

        // [START create_database_reference]
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.post_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        return rootView;
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Set up Layout Manager, reverse layout
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        // find the RecyclerView
        mRecyclerView = (RecyclerView) view.findViewById(R.id.post_recycler_view);
        // init the arraylist (data source)
        mPosts = new ArrayList<Map<String, Object>>();
        // construct the adapter from this data source
        mPostAdapter = new PostAdapter(mPosts);
        // RecyclerView setup (layout manager, use adapter)
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        // set the adapter
        mRecyclerView.setAdapter(mPostAdapter);

        populateTimeline();

        // Lookup the swipe container view
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        // Setup refresh listener which triggers new data loading
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync();
            }
        });
        // Configure the refreshing colors
        mSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    // get new timeline upon pulldown refresh
    public void fetchTimelineAsync() {
        // clear the timeline
        mPostAdapter.clear();
        // regenerate the timeline() with most recent data
        populateTimeline();
        // turn off the refresh animation
        mSwipeContainer.setRefreshing(false);
    }

    // get tweet data and put them on the timeline
    private void populateTimeline() {
        // Set up FirebaseRecyclerAdapter with the Query
        final Query postsQuery = getQuery(mDatabaseReference);
        Log.i("PostsFragment", postsQuery.toString());
        // Retrieve new posts as they are added to Firebase
        postsQuery.addChildEventListener(new ChildEventListener() {
            // Retrieve new posts as they are added to Firebase
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newPost = (Map<String, Object>) snapshot.getValue();
                Log.i("PostsFragment", "Author: " + newPost.get("author"));
                Log.i("PostsFragment", "Description: " + newPost.get("body"));
                mPosts.add(newPost);
                mPostAdapter.notifyItemInserted(mPosts.size() - 1);
//                System.out.println("Author: " + newPost.get("author"));
//                System.out.println("Title: " + newPost.get("body"));
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

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery = databaseReference.child("posts")
                .limitToFirst(20);
        // [END recent_posts_query]

        return recentPostsQuery;
    }
}
