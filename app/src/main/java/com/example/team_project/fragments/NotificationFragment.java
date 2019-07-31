package com.example.team_project.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.team_project.NotificationViewHolder;
import com.example.team_project.OtherUserProfileActivity;
import com.example.team_project.PostDetailActivity;
import com.example.team_project.PostViewHolder;
import com.example.team_project.R;
import com.example.team_project.models.Notification;
import com.example.team_project.models.Post;
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

public class NotificationFragment extends Fragment {

    public String mCurrentUserUid;
    private static final String TAG = "PostsFragment";

    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<Notification, NotificationViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_notification, parent, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = (RecyclerView) rootView.findViewById(R.id.notification_recycler_view);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query notifQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<Notification, NotificationViewHolder>(Notification.class, R.layout.item_notification,
                NotificationViewHolder.class, notifQuery) {
            @Override
            protected void populateViewHolder(final NotificationViewHolder viewHolder, final Notification model, final int position) {
                final DatabaseReference notifRef = getRef(position);
                // Set click listener for the whole post view
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (model.type.equals("friend")) {
                            Intent intent = new Intent(getActivity(), OtherUserProfileActivity.class);
                            intent.putExtra("uid", model.fromUid);
                            startActivity(intent);
                            markNotifAsSeen(model, notifRef.getKey(), viewHolder.itemView);
                        }
                        if (model.type.equals("tagged")) {
                            Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                            intent.putExtra("uid", model.fromUid);
                            intent.putExtra("postRefKey", model.key);
                            startActivity(intent);
                            markNotifAsSeen(model, notifRef.getKey(), viewHolder.itemView);
                        }
                        // TODO - implement on click for other types (Calendar Match & Message)
                    }
                });
                try {
                    if (!model.seen) {
                        viewHolder.itemView.setBackgroundColor(Color.parseColor("#D3D3D3"));
                        Log.i("Notification Fragment", "hello");
                    }
                    viewHolder.bindToPost(model);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mRecycler.setAdapter(mAdapter);

    }

    private void markNotifAsSeen(Notification model, final String postRefKey, View itemView) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("user-notifications")
                .child(mCurrentUserUid)
                .child(postRefKey)
                .child("seen");
        ref.setValue(true);
        itemView.setBackgroundColor(Color.parseColor("#D3D3D3"));
    }

    public Query getQuery(DatabaseReference databaseReference) {
        Query recentPostsQuery = databaseReference.child("user-notifications")
                .child(mCurrentUserUid)
                .limitToFirst(20);
        return recentPostsQuery;
    }

}
