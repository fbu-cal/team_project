package com.example.yoked.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yoked.MainActivity;
import com.example.yoked.MessageDetailsActivity;
import com.example.yoked.NotificationViewHolder;
import com.example.yoked.OtherUserProfileActivity;
import com.example.yoked.PostDetailActivity;
import com.example.yoked.R;
import com.example.yoked.models.Notification;
import com.example.yoked.MainMessenger;
import com.example.yoked.MatchActivity;
import com.example.yoked.NotificationViewHolder;
import com.example.yoked.OtherUserProfileActivity;
import com.example.yoked.R;
import com.example.yoked.models.Notification;
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

import static android.view.View.GONE;

public class NotificationFragment extends Fragment {

    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Notification, NotificationViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private String mCurrentUserUid;

    private boolean mShouldRefreshOnResume = false;

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

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        checkBadgeStatus();

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
                        if (model.type.equals("match")) {
                            // Todo add a done so it no apear
                            findmatch(viewHolder, model, notifRef);
                        }
                        if (model.type.equals("final match")) {
                            findUserName(viewHolder, model, notifRef);
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

    private void findmatch(final NotificationViewHolder viewHolder, final Notification model, final DatabaseReference notifRef) {
        mDatabase.child("user-match").child(model.toUid).child(model.fromUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> matchInfo = (HashMap<String, Object>) dataSnapshot.getValue();
                if (matchInfo != null) {
                    if ((Boolean) matchInfo.get("currentUserStatus")) {
                        findUserName(viewHolder, model, notifRef);
                    } else {
                        Intent intent = new Intent(getActivity(), MatchActivity.class);
                        startActivity(intent);
                        markNotifAsSeen(model, notifRef.getKey(), viewHolder.itemView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void findUserName(final NotificationViewHolder viewHolder, final Notification model, final DatabaseReference notifRef) {
        mDatabase.child("users").child(model.fromUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> userInfo = (HashMap<String, Object>) dataSnapshot.getValue();
                if (userInfo != null) {
                    String username = (String) userInfo.get("username");
                    Intent intent = new Intent(getActivity(), MessageDetailsActivity.class);
                    intent.putExtra("uid", model.fromUid);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    markNotifAsSeen(model, notifRef.getKey(), viewHolder.itemView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkBadgeStatus() {
        MainActivity.notificationBadge.setVisibility(GONE);
        mDatabase.child("user-notifications").child(mCurrentUserUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Notification notification = snapshot.getValue(Notification.class);
                            if (!notification.seen) {
                                MainActivity.notificationBadge.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
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
        Fragment fragment = new NotificationFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container_flowlayout, fragment).commit();
    }

}
