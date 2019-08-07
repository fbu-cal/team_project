package com.example.yoked.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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
                    writeNewPost(userId, mFreeTime);
                    Toast.makeText(getActivity(), "data inserted successfully", Toast.LENGTH_LONG).show();
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
                    if ( mNewCalendar.get("mFreeTime") != null) {
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
        int grayColor = Color.argb(200, 200, 200, 200);
        for (int i = 0; i < mAllTimesList.size(); i++) {
            // if the time is in the user's calendar, add to available times and tint button
            if (mPosts.get(mAllTimesList.get(i))) {
                addToAvailableTimes(mAllTimesList.get(i));
                tintImageButton(mAllButtonsList.get(i), grayColor);
            }
        }
    }

    /**
     *  this method adds the data to the calender class
     *  which then later puts it in toMap in the class
     *  the structure of the data base is decided with the
     *  child updates , then it launches to the home screen
     */
    private void writeNewPost(String userId, HashMap mFreeTime) {
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
                button.setColorFilter(Color.argb(200, 200, 200, 200));
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

}