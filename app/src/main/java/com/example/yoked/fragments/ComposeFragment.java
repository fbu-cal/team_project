package com.example.yoked.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.yoked.MainActivity;
import com.example.yoked.R;
import com.example.yoked.models.Calendar;
import com.example.yoked.models.Match;
import com.example.yoked.models.NotifMatch;
import com.example.yoked.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComposeFragment extends Fragment implements View.OnClickListener {

    //These buttons will pass in the data to the server
    private Button mSubmitCalendarButton;
    private ImageButton mFridayMorningButton, mFridayAfternoon, mFridayEvening;
    private ImageButton mSaturdayMorningButton, mSaturdayAfternoon, mSaturdayEvening;
    private ImageButton mSundayMorning, mSundayAfternoon, mSundayEvening;

    // List of strings with all keys for times
    private ArrayList<String> mAllTimesList = new ArrayList<String>();
    private ArrayList<ImageButton> mAllButtonsList = new ArrayList<ImageButton>();

    DatabaseReference mReference;
    Calendar mCalendar;
    public HashMap<String, Boolean> mFreeTime;
    private FirebaseAuth mAuth;
    String userId;
    private HashMap<String, Boolean> mPosts;
    public List<String> mUserFreeTime;
    public HashMap<String, Object> mNewCalendar;
    private int deleteCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_compose, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        deleteCount = 0;
        mPosts = new HashMap<String, Boolean>();
        mFreeTime = new HashMap<String, Boolean>();
        //set up the variables with their buttons
        mSubmitCalendarButton = view.findViewById(R.id.editCalendarButton);
        mUserFreeTime = new ArrayList<>();

        //set up the variables with their buttons
        initializeImageButtons(view);

        // puts all keys into mAllTimesList and all image buttons into mAllButtonsList
        setAllTimesAndButtonsList();

        for (ImageButton button : mAllButtonsList) {
            button.setOnClickListener(this);
        }

        //Pass in information to Calendar class so then can be packaged to FireBase
        mReference = FirebaseDatabase.getInstance().getReference();
        //link the user with there calendar using there uid
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        getUserCalendar();

        /**
         * when clicked data will be sent from ArrayList here to the other
         * file one and then push to data base
         */

        mSubmitCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFreeTime.size() != 0 || deleteCount > 0) {
                    //getCurrentUserData();
                    writeNewCalendarCheck(userId, mFreeTime);
                    //Toast.makeText(getActivity(), "data inserted successfully", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < mAllButtonsList.size(); i++) {
            if (v.getId() == mAllButtonsList.get(i).getId()) {
                handleButtonOnClick(mAllTimesList.get(i), mAllButtonsList.get(i));
            }
        }
    }

    /**
     * After this the on Click for the selection is made
     * it will call this function and it will add
     * to the times the user is available
     */

    private void addToAvailableTimes(String freeTime) {
        mFreeTime.put(freeTime, true);
    }

    /**
     * This allows to access the data of a user and there calendar
     * through the map in the calendar class
     * the logs are for testing of the data is being gotten
     */
    private void getUserCalendar() {
        mReference.child("user-calendar/" + userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mNewCalendar = (HashMap<String, Object>) dataSnapshot.getValue();
                if (mNewCalendar != null) {
                    if (mNewCalendar.get("mFreeTime") != null) {
                        mPosts = (HashMap<String, Boolean>) mNewCalendar.get("mFreeTime");
                        makeComplete();
                        Log.i("CalendarActivity", "mPosts: " + mPosts);
                        checkData();
                    } else {
                        makeCompleteStart();
                    }
                } else {
                    makeCompleteStart();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * It broke because if there is no calendar data
     * it needs to do something and would give me null pointers
     */
    private void makeCompleteStart() {
        for (String timeKey : mAllTimesList) {
            mPosts.put(timeKey, false);
        }
    }

    public void deleteCalendar() {
        DatabaseReference deleteUserCalendar = FirebaseDatabase.getInstance().getReference
                ("/user-calendar/" + userId);
        deleteUserCalendar.removeValue();
    }

    private void makeComplete() {
        for (String timeKey : mAllTimesList) {
            if (!mPosts.containsKey(timeKey)) {
                mPosts.put(timeKey, false);
            }
        }
    }

    private void checkData() {
        Log.i("CalendarActivity", "mPosts: " + mPosts.get("fridayMorning"));
        Log.i("CalendarActivity", "mPosts: " + mPosts);
        int yokedYellow = Color.argb(255, 253, 174, 19);
        for (int i = 0; i < mAllTimesList.size(); i++) {
            // if the time is in the user's calendar, add to available times and tint button
            if (mPosts.get(mAllTimesList.get(i))) {
                addToAvailableTimes(mAllTimesList.get(i));
                tintImageButton(mAllButtonsList.get(i), yokedYellow);
            }
        }
    }

    /**
     *  this method adds the data to the calender class
     *  which then later puts it in toMap in the class
     *  the structure of the data base is decided with the
     *  child updates , then it launches to the home screen
     */
    private void writeNewCalendarCheck(String userId, HashMap mFreeTime) {
        deleteCalendar();
        String calendarKey = mReference.child("calendar").push().getKey();
        Calendar calendar = new Calendar(userId, mFreeTime);
        Map<String, Object> postValues = calendar.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/calendar/" + calendarKey, postValues);
        childUpdates.put("/user-calendar/" + userId + "/" , postValues);
        Log.i("CalendarActivity", "Key: " + userId);
        mReference.updateChildren(childUpdates);
        Intent launchPosts = new Intent(getActivity(), MainActivity.class);
        startActivity(launchPosts);
    }

    // tints the given image button to the given color
    private void tintImageButton(ImageButton button, int color) {
        button.setColorFilter(color);
    }

    // handles on click for given button
    private void handleButtonOnClick(String time, ImageButton button) {
        if (mPosts.containsKey(time)) {
            if (mPosts.get(time)) {
                mFreeTime.remove(time);
                button.setColorFilter(null);
                deleteCount++;
            } else {
                addToAvailableTimes(time);
                button.setColorFilter(Color.argb(255, 253, 174, 19));
            }
        }
    }

    public void initializeImageButtons(View view) {
        mFridayEvening = view.findViewById(R.id.fridayEveningMoonImageButton);
        mFridayAfternoon = view.findViewById(R.id.fridayAfternoonSunsetImageButton);
        mFridayMorningButton = view.findViewById(R.id.fridayMorningSunImageButton);
        mSaturdayMorningButton = view.findViewById(R.id.saturdayMorningSunImageButton);
        mSaturdayAfternoon = view.findViewById(R.id.saturdayAfternoonSunsetImageButton);
        mSaturdayEvening = view.findViewById(R.id.saturdayEveningImageButton);
        mSundayMorning = view.findViewById(R.id.sundayMorningSunImageButton);
        mSundayAfternoon = view.findViewById(R.id.sundayAfternoonSunsetImageButton);
        mSundayEvening = view.findViewById(R.id.sundayEveningImageButton);
    }

    public void setAllTimesAndButtonsList() {
        mAllTimesList.add("fridayMorning");
        mAllTimesList.add("fridayAfternoon");
        mAllTimesList.add("fridayEvening");
        mAllTimesList.add("saturdayMorning");
        mAllTimesList.add("saturdayAfternoon");
        mAllTimesList.add("saturdayEvening");
        mAllTimesList.add("sundayMorning");
        mAllTimesList.add("sundayAfternoon");
        mAllTimesList.add("sundayEvening");
        mAllButtonsList.add(mFridayMorningButton);
        mAllButtonsList.add(mFridayAfternoon);
        mAllButtonsList.add(mFridayEvening);
        mAllButtonsList.add(mSaturdayMorningButton);
        mAllButtonsList.add(mSaturdayAfternoon);
        mAllButtonsList.add(mSaturdayEvening);
        mAllButtonsList.add(mSundayMorning);
        mAllButtonsList.add(mSundayAfternoon);
        mAllButtonsList.add(mSundayEvening);
    }


    /**
     * This is where I can get current user info like friends
     */
    /*private void getCurrentUserData() {
        mReference.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("friendList").getValue() != null) {
                    String currentName = (String) dataSnapshot.child("username").getValue();
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

    *//**
     * In this method I just want to get the current users free time
     * and the one they are communicating with, this calls make complete
     * which sets the times they are not free to false to not get
     * a null pointer, checkFunc checks overlapping times and adds to
     * the array to display
     *//*

    private void getUserCalendar(final String otherUserId, final String friendName, final String currentName) {
        mReference.child("user-calendar/").
                child(userId).addListenerForSingleValueEvent(new ValueEventListener() {

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
                                makeCompleteCheck(currentUserFreeTime, otherUserTime, otherUserId, friendName, currentName);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    *//**
     * This puts false on the times the users is not free,
     * so there is no null pointer, this is called by getUserCalendar
     *//*

    private void makeCompleteCheck(HashMap currentUserFreeTime, HashMap otherUserTime, String otherUserId,
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

    *//**
     * anyMatchCheck is an int that checks if they have any times they can hang
     * if they can't then it puts the status as denied, this helps because it cuts
     * loops short and helps performance, so the first if checks if they have that
     * matching time, if they do i add the user to an array, the next loop checks
     * if it is in it for that time already, if it is not then it wont add it,
     * this is important bc we only want it the first time it runs and catches it
     *//*

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
        mReference.child("user-notif-match").child(userId).child(otherUserId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String swipeMessage = "Click here to swipe";
                        HashMap<String, Object> notifSentCheck = (HashMap<String, Object>) dataSnapshot.getValue();
                        if (notifSentCheck == null) {
                            sendNotification(userId, otherUserId, swipeMessage, currentName);
                            sendNotification(otherUserId, userId, swipeMessage , friendName);
                            writeNotifSent(userId, otherUserId, true);
                            writeNotifSent(otherUserId, userId, true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    *//**
     * mOtherUserStatus would be a hash map of their user comparing with status with all users
     *//*

    private void getOtherUserStatus(final String otherUserId, final String freeTime) {
        //String matchKey = mReference.child("user-match/" + userId).push().getKey();
        //Log.i("MatchActivity", "key: " + matchKey);
        mReference.child("user-match").child(userId).child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
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
                writeNewCalendarCheck(userId, otherUserId, freeTime, currentUserStatus, otherUserStatus, finished);
                writeNewCalendarCheck(otherUserId, userId, freeTime, otherUserStatus, currentUserStatus, finished);
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
        mReference.updateChildren(childUpdates);
    }

    *//**
     * @param userId - checks current user
     *//*

    private void writeNewCalendarCheck(String userId, String otherUserId, String freeTime,
                                       Boolean currentUserStatus, Boolean otherUserStatus, Boolean finished) {
        //deleteMatch(userId);
        //String matchKey = mReference.child("match").push().getKey();
        Match match = new Match(userId, otherUserId, freeTime, currentUserStatus, otherUserStatus, finished);
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
        String key = mReference.child("notification").push().getKey();
        Map<String, Object> notifValues = notif.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-notifications/" + toUid + "/" + key, notifValues);
        mReference.updateChildren(childUpdates);
        // update user-feed
    }

*/}