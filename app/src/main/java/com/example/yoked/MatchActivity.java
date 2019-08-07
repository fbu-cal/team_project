package com.example.yoked;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.yoked.R;
import com.example.yoked.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import com.example.yoked.models.Match;
import java.util.Map;


public class MatchActivity extends Activity {

    private ArrayList<String> mMatches;
    private ArrayAdapter<String> arrayAdapter;
    private int i;
    DatabaseReference mReference;
    private String mUserId;
    private FirebaseAuth mAuth;
    HashMap<String, String> mNameToId;
    private ArrayList mArrayOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        mAuth = FirebaseAuth.getInstance();
        mUserId = mAuth.getCurrentUser().getUid();
        mReference = FirebaseDatabase.getInstance().getReference();
        mNameToId = new HashMap<String, String>();

        getUserMatch();
        /**
         * Adding to mMatches is the name card,
         * i should keep the userId here and
         * add a new box with info about the user
         * maybe profile image
         */

            mMatches = new ArrayList<>();

            arrayAdapter = new ArrayAdapter<>(this, R.layout.item_choice, R.id.helloText, mMatches);

            /**
             * Removes the cards from the array here
             */
            SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
            flingContainer.setAdapter(arrayAdapter);
            flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {

                @Override
                public void removeFirstObjectInAdapter() {
                    // this is the simplest way to delete an object from the Adapter (/AdapterView)
                    Log.d("LIST", "removed object!");
                    mMatches.remove(0);
                    arrayAdapter.notifyDataSetChanged();
                }

                /**
                 * If it comes out left then the user does not want to
                 * hangout with them so nothing needs to be done
                 */

                @Override
                public void onLeftCardExit(Object dataObject) {
                    //Do something on the left!
                    //You also have access to the original object.
                    //If you want to use it just cast it (String) dataObject
                    //makeToast, "Left!");
                    Toast.makeText(MatchActivity.this, "swiped left", Toast.LENGTH_SHORT).show();
                    //writeNewPost(mUserId, status);

                }

                /**
                 * If swiped right then add it to somewhere on database
                 * and check if other user also swiped right
                 * if they do hit them both with a notification
                 * if not let them know later
                 */

                @Override
                public void onRightCardExit(Object dataObject) {
                    Toast.makeText(MatchActivity.this, "swiped right", Toast.LENGTH_SHORT).show();
                    String dataDigest = (String) dataObject;
                    String otherUserName = dataDigest.split(" ")[0];
                    rightUserMatch(otherUserName);
                    //writeNewPost(mUserId, otherUserId, );
                }

                @Override
                public void onAdapterAboutToEmpty(int itemsInAdapter) {
                }

                @Override
                public void onScroll(float scrollProgressPercent) {

                }
            });


            /**
             * Can implement a closer look if wanted here
             */
            flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
                @Override
                public void onItemClicked(int itemPosition, Object dataObject) {

                }
            });
    }

    private void getUserMatch () {
        mReference.child("user-match").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> matchInfo = null;
                matchInfo = (HashMap<String, Object>) dataSnapshot.getValue();
                //HashMap<String, Object> matchHold = null;
                if (matchInfo != null) {
                    Log.i("MatchActivity", "match info: " + matchInfo);
                    for (Map.Entry<String, Object> matchHold : matchInfo.entrySet()) {
                        HashMap<String, Object> matchValueCurrent = (HashMap<String, Object>) matchHold.getValue();
                        if (matchValueCurrent.get("currentUserStatus") != null) {
                            if (!(Boolean) matchValueCurrent.get("currentUserStatus")) {
                                getUserInfo(matchValueCurrent);
                                Log.i("MatchActivity", "match info loop: " + matchValueCurrent);
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

    private void getUserInfo(final HashMap matchHold) {
        String otherUserId = (String) matchHold.get("otherUserId");
        Log.i("MatchActivity", "other user Id: " + matchHold.get("otherUserId"));
        mReference.child("users").child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> userInfo = null;
                if (dataSnapshot != null) {
                    userInfo = (HashMap<String, Object>) dataSnapshot.getValue();
                    if (userInfo != null) {
                        mNameToId.put((String) userInfo.get("username"), (String) matchHold.get("otherUserId"));
                        String timeAndName =  userInfo.get("username") + " @ " + matchHold.get("freeTime");

                        mMatches.add(timeAndName);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void rightUserMatch (final String otherUserName) {
        //String matchKey = mReference.child("user-match/" + userId).push().getKey();
        //Log.i("MatchActivity", "key: " + matchKey);
        mReference.child("user-match").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> matchInfo = null;
                matchInfo = (HashMap<String, Object>) dataSnapshot.getValue();
                String otherUserId = mNameToId.get(otherUserName);
                if (matchInfo != null) {
                    HashMap<String, Object> dataToWrite =
                            (HashMap<String, Object>) matchInfo.get(otherUserId);
                    writeNewPost(mUserId, otherUserId, (String)dataToWrite.get("freeTime"),
                            true, (Boolean) dataToWrite.get("otherUserStatus"));
                    writeNewPost(otherUserId, mUserId, (String)dataToWrite.get("freeTime"),
                            (Boolean) dataToWrite.get("otherUserStatus"), true);
                    if ((Boolean) dataToWrite.get("otherUserStatus")) {
                        getCurrentUsername(otherUserId, otherUserName);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getCurrentUsername(final String otherUserId, final String otherUserName) {
        mReference.child("users").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> userInfo = null;
                String matchBody = "Click here to be redirected to messages";
                if (dataSnapshot != null) {
                    userInfo = (HashMap<String, Object>) dataSnapshot.getValue();
                    if (userInfo != null) {
                        String currentUsername = ((String) userInfo.get("username"));
                        //finalMatchCheck(otherUserId, otherUserName, currentUsername);
                        sendNotification(mUserId, otherUserId, matchBody, currentUsername);
                        sendNotification(otherUserId, mUserId, matchBody, otherUserName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * @param userId - checks user
     */

    private void writeNewPost(String userId, String otherUserId, String freeTime,
                              Boolean currentUserStatus, Boolean otherUserStatus) {
        Match match = new Match(userId, otherUserId, freeTime, currentUserStatus, otherUserStatus);
        Map<String, Object> postValues = match.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/match/", postValues);
        childUpdates.put("/user-match/" + userId + "/" + otherUserId + "/", postValues);
        Log.i("CalendarActivity", "Key: " + userId);
        mReference.updateChildren(childUpdates);
    }

    private void sendNotification(final String fromUid, final String toUid, final String body, final String name) {
        Query query = mReference.child("users").child(fromUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                String title = "Invite from " + name;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
                String timestamp = simpleDateFormat.format(new Date());
                String imageUrl = "";
                if (newUser.get("profile_picture") != null)
                    imageUrl = newUser.get("profile_picture").toString();

                Notification notif = new Notification
                        ("final match", imageUrl, title, body, timestamp, toUid, fromUid);

                updateNotification(toUid, notif);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }
    private void updateNotification(String toUid, Notification notif) {
        String key = mReference.child("notification").push().getKey();
        Map<String, Object> notifValues = notif.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-notifications/" + toUid + "/" + key, notifValues);
        mReference.updateChildren(childUpdates);
        // update user-feed
    }

}
