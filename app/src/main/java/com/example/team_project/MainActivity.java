package com.example.team_project;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.team_project.fragments.ComposeFragment;
import com.example.team_project.fragments.NotificationFragment;
import com.example.team_project.fragments.PostsFragment;
import com.example.team_project.fragments.ProfileFragment;
import com.example.team_project.models.Match;
import com.example.team_project.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final int MESSENGER_REQUEST_CODE = 100;
    private BottomNavigationView mBottomNavigationView;
    DatabaseReference mReference;
    Fragment fragment;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mReference = FirebaseDatabase.getInstance().getReference();
        mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final FragmentManager fragmentManager = getSupportFragmentManager();
        // handle bottom navigation selection
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_home:
                        fragment = new PostsFragment();
                        break;
                    case R.id.action_compose:
                        fragment = new ComposeFragment();
                        break;
                    case R.id.action_notification:
                        fragment = new NotificationFragment();
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.container_flowlayout, fragment).commit();
                return true;
            }
        });
        // Set default selection
        mBottomNavigationView.setSelectedItemId(R.id.action_home);
        getCurrentUserData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        Drawable drawable = menu.findItem(R.id.miMessenger).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this,R.color.white));
        menu.findItem(R.id.miMessenger).setIcon(drawable);
        Drawable drawable2 = menu.findItem(R.id.miSearch).getIcon();
        drawable2 = DrawableCompat.wrap(drawable2);
        DrawableCompat.setTint(drawable2, ContextCompat.getColor(this,R.color.white));
        menu.findItem(R.id.miSearch).setIcon(drawable2);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.miMessenger:
                goToMessenger();
                return true;
            case R.id.miSearch:
                goToSearch();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToSearch() {
        Intent toSearch = new Intent(this, SearchActivity.class);
        startActivity(toSearch);
    }

    // open ComposeActivity to create a new tweet
    private void goToMessenger() {
        Intent toMessenger = new Intent(this, MainMessenger.class);
        startActivityForResult(toMessenger, MESSENGER_REQUEST_CODE);
    }

    /**
     * This is where I can get current user info like friends
     */
    private void getCurrentUserData() {
        mReference.child("users").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
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
        mReference.child("user-calendar/").
                child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> currentUserCalendar = (HashMap<String, Object>) dataSnapshot.getValue();
                HashMap<String, Boolean> currentUserFreeTime = null;
                if (currentUserCalendar != null) {
                    if ((HashMap<String, Boolean>) currentUserCalendar.get("mFreeTime") != null) {
                        currentUserFreeTime = (HashMap<String, Boolean>) currentUserCalendar.get("mFreeTime");
                    }
                    if (currentUserCalendar != null) {
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
        mReference.child("user-calendar").child(otherUserId).
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
        checkFunc(currentUserFreeTime, otherUserTime, otherUserId, friendName, currentName);
    }

    /**
     * anyMatchCheck is an int that checks if they have any times they can hang
     * if they can't then it puts the status as denied, this helps because it cuts
     * loops short and helps performance, so the first if checks if they have that
     * matching time, if they do i add the user to an array, the next loop checks
     * if it is in it for that time already, if it is not then it wont add it,
     * this is important bc we only want it the first time it runs and catches it
     */

    private void checkFunc(HashMap currentUserFreeTime, HashMap otherUserTime, String otherUserId,
                           String friendName, String currentName) {
        Log.i("CalendarActivity", "!!!Map: " + currentUserFreeTime);
        int anyMatchCheck = 0;
        String freeTime = null;
        if (freeTime == null) {
            if (currentUserFreeTime.get("fridayMorning") == currentUserFreeTime.get("fridayMorning")) {
                freeTime = "friMor";
                anyMatchCheck++;
            }
        }
        if (freeTime == null) {
            if (otherUserTime.get("fridayAfternoon") == otherUserTime.get("fridayAfternoon")) {
                freeTime = "friAfter";
            }
        }
        if (freeTime == null) {
            if (currentUserFreeTime.get("fridayEvening") == otherUserTime.get("fridayEvening")) {
                freeTime = "friEve";
            }
        }
        if (freeTime == null) {
            if (currentUserFreeTime.get("saturdayMorning") == otherUserTime.get("saturdayMorning")) {
                freeTime = "satMor";
            }
        }
        if (freeTime == null) {
            if (currentUserFreeTime.get("saturdayAfternoon") == otherUserTime.get("saturdayAfternoon")) {
                freeTime = "satAfter";
            }
        }
        if (freeTime == null) {
            if (currentUserFreeTime.get("saturdayEvening") == otherUserTime.get("saturdayEvening")) {
                freeTime = "satEve";
            }
        }
        if (freeTime == null) {
            if (currentUserFreeTime.get("sundayMorning") == otherUserTime.get("sundayMorning")) {
                freeTime = "sunMor";
            }
        }
        if (freeTime == null) {
            if (currentUserFreeTime.get("sundayAfternoon") == otherUserTime.get("sundayAfternoon")) {
                freeTime = "sunAfter";
            }
        }
        if (freeTime == null) {
            if (currentUserFreeTime.get("sundayEvening") == otherUserTime.get("sundayEvening")) {
                freeTime = "sunEve";
            }
        }
        if (anyMatchCheck > 0) {
            getOtherUserStatus(otherUserId, freeTime);
            //writeNewPost(mUserId, otherUserId, freeTime);
            //checkNotification(friendName, otherUserId, currentName);
            sendNotification(mUserId, otherUserId, "You have free time that" +
                    " overlaps with " + currentName);
            sendNotification(otherUserId, mUserId, "You have free time that" +
                    " overlaps with " + friendName);
        }
    }

    /*private void checkNotification(final String friendName,final String otherUserId, final String currentName) {
        mReference.child("user-notif-sent").child(mUserId).child(otherUserId).
                addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> notifCheck = (HashMap<String, Object>) dataSnapshot.getValue();
                if (notifCheck != null) {
                    if (notifCheck.get("firstNotif") != null) {
                        if ((Boolean) notifCheck.get("firstNotif")) {
                            // don't do anything because user has notification already
                        }
                    }
                } else {
                    sendNotification(mUserId, otherUserId, "You have free time that" +
                            " overlaps with " + currentName);
                    sendNotification(otherUserId, mUserId, "You have free time that" +
                            " overlaps with " + friendName);
                    *//*writeNewNotif(mUserId, otherUserId, true,false);
                    writeNewNotif(otherUserId, mUserId, true, false);*//*
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    /**
     * mOtherUserStatus would be a hash map of their user comparing with status with all users
     */

   private void getOtherUserStatus(final String otherUserId, final String freeTime) {
       //String matchKey = mReference.child("user-match/" + userId).push().getKey();
       //Log.i("MatchActivity", "key: " + matchKey);
       mReference.child("user-match").child(mUserId).child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               HashMap<String, Object> otherUserCheck = (HashMap<String, Object>) dataSnapshot.getValue();
               Boolean otherUserStatus = false;
               Boolean currentUserStatus = false;
               if (otherUserCheck != null) {
                   if (otherUserCheck.containsKey("otherUserStatus")) {
                       otherUserStatus = (Boolean) otherUserCheck.get("otherUserStatus");
                   }
                   if (otherUserCheck.containsKey("currentUserStatus")) {
                       currentUserStatus = (Boolean) otherUserCheck.get("currentUserStatus");
                   }
               }
               writeNewPost(mUserId, otherUserId, freeTime, currentUserStatus, otherUserStatus);
               writeNewPost(otherUserId, mUserId, freeTime, otherUserStatus, currentUserStatus);
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
   }

    /**
     * @param userId - checks current user
     */

    private void writeNewPost(String userId, String otherUserId, String freeTime,
                               Boolean currentUserStatus, Boolean otherUserStatus) {
        //deleteMatch(userId);
        //String matchKey = mReference.child("match").push().getKey();
        Match match = new Match(userId, otherUserId, freeTime, currentUserStatus, otherUserStatus);
        Map<String, Object> postValues = match.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/match/", postValues);
        childUpdates.put("/user-match/" + userId + "/" + otherUserId + "/", postValues);
        Log.i("CalendarActivity", "Key: " + userId);
        mReference.updateChildren(childUpdates);
    }

    private void sendNotification(final String fromUid, final String toUid, final String body) {
        Query query = mReference.child("users").child(fromUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                String title = "Match";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
                String timestamp = simpleDateFormat.format(new Date());
                //Uri uri = Uri.parse("R.drawable.match.png");
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
        String key = mReference.child("notification").push().getKey();
        Map<String, Object> notifValues = notif.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-notifications/" + toUid + "/" + key, notifValues);
        mReference.updateChildren(childUpdates);
        // update user-feed
    }
}