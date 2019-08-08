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
import android.widget.Button;
import android.widget.Toast;

import com.example.yoked.ComposePostActivity;
import com.example.yoked.MainActivity;
import com.example.yoked.OtherUserProfileActivity;
import com.example.yoked.PostDetailActivity;
import com.example.yoked.PostViewHolder;
import com.example.yoked.PostViewHolder;
import com.example.yoked.R;
import com.example.yoked.models.Match;
import com.example.yoked.models.NotifMatch;
import com.example.yoked.models.Notification;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

public class PostsFragment extends Fragment {

    private DatabaseReference mDatabase;

    private Button mCreatePostButton;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private String mUserId;
    private ArrayList<String> mAllTimesList = new ArrayList<String>();

    private boolean mShouldRefreshOnResume = false;

    public PostsFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_posts, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mCreatePostButton = rootView.findViewById(R.id.create_post_button);
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
        mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // set up create post button
        mCreatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toCreatePost = new Intent(getActivity(), ComposePostActivity.class);
                startActivity(toCreatePost);
            }
        });

        checkBadgeStatus();

        setAllTimesAList();

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = mDatabase.child("user-feed")
                .child(mUserId)
                .limitToFirst(100);
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
                if (model.likes.containsKey(mUserId)) {
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
        getCurrentUserData();
    }

    private void goToTaggedProfile(Post model) {
        String taggedUid = model.taggedFriendUid;
        Intent toOtherProfile = new Intent (getActivity(), OtherUserProfileActivity.class);
        toOtherProfile.putExtra("uid", taggedUid);
        startActivity(toOtherProfile);
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
                    likesMap.put(mUserId, true);
                }
                else {
                    if (likesMap.containsKey(mUserId)) {
                        likeCount = likeCount - 1;
                        likesMap.remove(mUserId);
                        mDatabase.child(likesPath).removeValue();
                    }
                    else {
                        likeCount = likeCount + 1;
                        likesMap.put(mUserId, true);
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
        Query query = mDatabase.child("users").child(mUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // update current user's feed
                Query userTempQuery = mDatabase.child("user-feed").child(mUserId).child(postRefKey);
                String userTempPath = "/user-feed/" + mUserId + "/" + postRefKey;
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

    private void checkBadgeStatus() {
        MainActivity.notificationBadge.setVisibility(GONE);
        mDatabase.child("user-notifications").child(mUserId)
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
    /**
     * This is where I can get current user info like friends
     */
    private void getCurrentUserData() {
        mDatabase.child("users").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("friendList").getValue() != null) {
                    String currentName = (String) dataSnapshot.child("fullname").getValue();
                    Map<String, String> friendList = (Map<String, String>) dataSnapshot.child("friendList").getValue();
                    if (friendList != null) {
                        if (friendList.size() > 0) {
                            for (String friendUid : friendList.keySet()) {
                                String otherUserId = friendUid;
                                String friendName = friendList.get(friendUid);
                                //writeNewPost(mUserId, status);
                                getUserCalendar(friendUid, friendName, currentName);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * In this method I just want to get the current users free time
     * and the one they are communicating with, this calls make complete
     * which sets the times they are not free to false to not get
     * a null pointer, checkFunc checks overlapping times and adds to
     * the array to display
     */

    private void getUserCalendar(final String otherUserId, final String friendName, final String currentName) {
        mDatabase.child("user-calendar/").
                child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> currentUserCalendar = (HashMap<String, Object>) dataSnapshot.getValue();
                HashMap<String, Boolean> currentUserFreeTime = null;
                if (currentUserCalendar != null) {
                    if ((HashMap<String, Boolean>) currentUserCalendar.get("mFreeTime") != null) {
                        currentUserFreeTime = (HashMap<String, Boolean>) currentUserCalendar.get("mFreeTime");
                    }
                    if (currentUserFreeTime != null) {
                        getOtherUserCalendar(otherUserId, currentUserFreeTime, friendName, currentName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getOtherUserCalendar(final String otherUserId, final HashMap currentUserFreeTime,
                                      final String friendName, final String currentName) {
        mDatabase.child("user-calendar").child(otherUserId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        HashMap<String, Object> otherUserCalendar = (HashMap<String, Object>) dataSnapshot.getValue();
                        HashMap<String, Boolean> otherUserTime = null;
                        if (otherUserCalendar != null) {
                            if (otherUserCalendar.get("mFreeTime") != null) {
                                otherUserTime = (HashMap<String, Boolean>) otherUserCalendar.get("mFreeTime");
                                makeComplete(currentUserFreeTime, otherUserTime, otherUserId, friendName, currentName);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    /**
     * This puts false on the times the users is not free,
     * so there is no null pointer, this is called by getUserCalendar
     */

    private void makeComplete(HashMap currentUserFreeTime, HashMap otherUserTime, String otherUserId,
                              String friendName, String currentName) {
        if (!currentUserFreeTime.containsKey("fridayMorning")) {
            currentUserFreeTime.put("fridayMorning", false);
        }
        if (!currentUserFreeTime.containsKey("fridayAfternoon")) {
            currentUserFreeTime.put("fridayAfternoon", false);
        }
        if (!currentUserFreeTime.containsKey("fridayEvening")) {
            currentUserFreeTime.put("fridayEvening", false);
        }
        if (!currentUserFreeTime.containsKey("saturdayMorning")) {
            currentUserFreeTime.put("saturdayMorning", false);
        }
        if (!currentUserFreeTime.containsKey("saturdayAfternoon")) {
            currentUserFreeTime.put("saturdayAfternoon", false);
        }
        if (!currentUserFreeTime.containsKey("saturdayEvening")) {
            currentUserFreeTime.put("saturdayEvening", false);
        }
        if (!currentUserFreeTime.containsKey("sundayMorning")) {
            currentUserFreeTime.put("sundayMorning", false);
        }
        if (!currentUserFreeTime.containsKey("sundayAfternoon")) {
            currentUserFreeTime.put("sundayAfternoon", false);
        }
        if (!currentUserFreeTime.containsKey("sundayEvening")) {
            currentUserFreeTime.put("sundayEvening", false);
        }

        //I could not add an and because the users data is different
        if (!otherUserTime.containsKey("fridayMorning")) {
            otherUserTime.put("fridayMorning", false);
        }
        if (!otherUserTime.containsKey("fridayAfternoon")) {
            otherUserTime.put("fridayAfternoon", false);
        }
        if (!otherUserTime.containsKey("fridayEvening")) {
            otherUserTime.put("fridayEvening", false);
        }
        if (!otherUserTime.containsKey("saturdayMorning")) {
            otherUserTime.put("saturdayMorning", false);
        }
        if (!otherUserTime.containsKey("saturdayAfternoon")) {
            otherUserTime.put("saturdayAfternoon", false);
        }
        if (!otherUserTime.containsKey("saturdayEvening")) {
            otherUserTime.put("saturdayEvening", false);
        }
        if (!otherUserTime.containsKey("sundayMorning")) {
            otherUserTime.put("sundayMorning", false);
        }
        if (!otherUserTime.containsKey("sundayAfternoon")) {
            otherUserTime.put("sundayAfternoon", false);
        }
        if (!otherUserTime.containsKey("sundayEvening")) {
            otherUserTime.put("sundayEvening", false);
        }
        checkFuncNotif(currentUserFreeTime, otherUserTime, otherUserId, friendName, currentName);
    }

    /**
     * anyMatchCheck is an int that checks if they have any times they can hang
     * if they can't then it puts the status as denied, this helps because it cuts
     * loops short and helps performance, so the first if checks if they have that
     * matching time, if they do i add the user to an array, the next loop checks
     * if it is in it for that time already, if it is not then it wont add it,
     * this is important bc we only want it the first time it runs and catches it
     */

    private void checkFuncNotif(HashMap currentUserFreeTime, HashMap otherUserTime, String otherUserId,
                                String friendName, String currentName) {
        Log.i("CalendarActivity", "!!!Map: " + currentUserFreeTime);
        int anyMatchCheck = 0;
        String freeTime = null;

        for (String timeKey : mAllTimesList) {
            if (freeTime == null) {
                if ( (Boolean) currentUserFreeTime.get(timeKey) && (Boolean) otherUserTime.get(timeKey)) {
                    freeTime = timeKey;
                    anyMatchCheck++;
                }
            }
        }
        if (anyMatchCheck > 0) {
            getOtherUserStatus(otherUserId, freeTime);
            checkNotification(friendName, otherUserId, currentName);
        }
    }

    private void checkNotification(final String friendName, final String otherUserId, final String currentName) {
        mDatabase.child("user-notif-match").child(mUserId).child(otherUserId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String swipeMessage = "Click here to swipe";
                        HashMap<String, Object> notifSentCheck = (HashMap<String, Object>) dataSnapshot.getValue();
                        if (notifSentCheck == null) {
                            sendNotification(mUserId, otherUserId, swipeMessage, currentName);
                            sendNotification(otherUserId, mUserId, swipeMessage , friendName);
                            writeNotifSent(mUserId, otherUserId, true);
                            writeNotifSent(otherUserId, mUserId, true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    /**
     * mOtherUserStatus would be a hash map of their user comparing with status with all users
     */

    private void getOtherUserStatus(final String otherUserId, final String freeTime) {
        //String matchKey = mReference.child("user-match/" + userId).push().getKey();
        //Log.i("MatchActivity", "key: " + matchKey);
        mDatabase.child("user-match").child(mUserId).child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> otherUserCheck = (HashMap<String, Object>) dataSnapshot.getValue();
                Boolean otherUserStatus = false;
                Boolean currentUserStatus = false;
                Boolean finished = false;
                if (otherUserCheck != null) {
                    if (otherUserCheck.containsKey("otherUserStatus")) {
                        otherUserStatus = (Boolean) otherUserCheck.get("otherUserStatus");
                    }
                    if (otherUserCheck.containsKey("currentUserStatus")) {
                        currentUserStatus = (Boolean) otherUserCheck.get("currentUserStatus");
                    }
                    if (otherUserCheck.containsKey("finished")) {
                        finished = (Boolean) otherUserCheck.get("finished");
                    }
                }
                writeNewPost(mUserId, otherUserId, freeTime, currentUserStatus, otherUserStatus, finished);
                writeNewPost(otherUserId, mUserId, freeTime, otherUserStatus, currentUserStatus, finished);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void writeNotifSent(String userId, String otherUserId, boolean check) {
        NotifMatch notifMatch = new NotifMatch(userId, otherUserId, check);
        Map<String, Object> postValues = notifMatch.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user-notif-match/" + userId + "/" + otherUserId + "/", postValues);
        Log.i("CalendarActivity", "Key: " + userId);
        mDatabase.updateChildren(childUpdates);
    }

    /**
     * @param userId - checks current user
     */

    private void writeNewPost(String userId, String otherUserId, String freeTime,
                              Boolean currentUserStatus, Boolean otherUserStatus, Boolean finished) {
        //deleteMatch(userId);
        //String matchKey = mReference.child("match").push().getKey();
        Match match = new Match(userId, otherUserId, freeTime, currentUserStatus, otherUserStatus, finished);
        Map<String, Object> postValues = match.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/match/", postValues);
        childUpdates.put("/user-match/" + userId + "/" + otherUserId + "/", postValues);
        Log.i("CalendarActivity", "Key: " + userId);
        mDatabase.updateChildren(childUpdates);
    }

    private void sendNotification(final String fromUid, final String toUid, final String body, final String name) {
        Query query = mDatabase.child("users").child(fromUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                String title = "Match with " + name;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
                String timestamp = simpleDateFormat.format(new Date());
                String imageUrl = "";
                if (newUser.get("profile_picture") != null)
                    imageUrl = newUser.get("profile_picture").toString();
                Notification notif = new Notification
                        ("match", imageUrl, title, body, timestamp, toUid, fromUid);
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
    }

    public void setAllTimesAList() {
        mAllTimesList.add("fridayMorning");
        mAllTimesList.add("fridayAfternoon");
        mAllTimesList.add("fridayEvening");
        mAllTimesList.add("saturdayMorning");
        mAllTimesList.add("saturdayAfternoon");
        mAllTimesList.add("saturdayEvening");
        mAllTimesList.add("sundayMorning");
        mAllTimesList.add("sundayAfternoon");
        mAllTimesList.add("sundayEvening");

    }
}
