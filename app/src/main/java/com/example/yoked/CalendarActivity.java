package com.example.yoked;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.yoked.models.Calendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity implements View.OnClickListener {
    //These buttons will pass in the data to the server
    private Button mSubmitCalendarButton;
    private ImageButton mFridayMorningSunImageButton;
    private ImageButton mSaturdayMorningSunImageButton;
    private ImageButton mSundayMorningSunImageButton;
    private ImageButton mFridayAfternoonSunsetImageButton;
    private ImageButton mSaturdayAfternoonSunsetImageButton;
    private ImageButton mSundayAfternoonsunsetImageButton;
    private ImageButton mFridayEveningMoonImageButton;
    private ImageButton mSaturdayEveningImageButton;
    private ImageButton mSundayEveningImageButton;
    DatabaseReference mReference;
    Calendar mCalendar;
    public ArrayList<String> mFreeTime = new ArrayList<String>();
    private FirebaseAuth mAuth;
    String userId;
    private ArrayList<Map<String, Object>> mPosts;
    public List<String> mUserFreeTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mPosts = new ArrayList<Map<String, Object>>();
        mFreeTime = new ArrayList<String>();
        //set up the variables with their buttons
        mSubmitCalendarButton = findViewById(R.id.editCalendarButton);
        mUserFreeTime = new ArrayList<>();
        //set up the variables with their buttons
        mFridayEveningMoonImageButton = findViewById(R.id.fridayEveningMoonImageButton);
        mFridayAfternoonSunsetImageButton = findViewById(R.id.fridayAfternoonSunsetImageButton);
        mFridayMorningSunImageButton = findViewById(R.id.fridayMorningSunImageButton);
        mSaturdayMorningSunImageButton = findViewById(R.id.saturdayMorningSunImageButton);
        mSaturdayAfternoonSunsetImageButton = findViewById(R.id.saturdayAfternoonSunsetImageButton);
        mSaturdayEveningImageButton = findViewById(R.id.saturdayEveningImageButton);
        mSundayMorningSunImageButton = findViewById(R.id.sundayMorningSunImageButton);
        mSundayAfternoonsunsetImageButton = findViewById(R.id.sundayAfternoonSunsetImageButton);
        mSundayEveningImageButton = findViewById(R.id.sundayEveningImageButton);
        /**
         * calling on click so I do not have to make multiple functions, instead it
         * can be on the onClick below
         */
        mFridayMorningSunImageButton.setOnClickListener(this);
        mFridayAfternoonSunsetImageButton.setOnClickListener(this);
        mFridayEveningMoonImageButton.setOnClickListener(this);
        mSaturdayMorningSunImageButton.setOnClickListener(this);
        mSaturdayAfternoonSunsetImageButton.setOnClickListener(this);
        mSaturdayEveningImageButton.setOnClickListener(this);
        mSundayMorningSunImageButton.setOnClickListener(this);
        mSundayAfternoonsunsetImageButton.setOnClickListener(this);
        mSundayEveningImageButton.setOnClickListener(this);
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
                if (mFreeTime.size() != 0) {
                    writeNewPost(userId, mFreeTime);
                    Toast.makeText(CalendarActivity.this, "data inserted successfully", Toast.LENGTH_LONG).show();
                    Log.i("CalendarActivity", "!!!Map22: " + mPosts);
                } else {
                    Toast.makeText(CalendarActivity.this, "nothing is in calendar", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    /**private void editRoute() {
     getUserCalendar();
     }*/

    /**
     * when dates selected it will be sent to a function that will
     * add the time to an arrayList of the Users free time
     * this got more complicated because if the user wants
     * to add time there would be a different calendar model
     * created so I had to go through my arrayList to check
     * if it already contains an item so there is no duplicates
     */


    /**
     * when dates selected it will be sent to a function that will
     * add the time to an arrayList of the Users free time
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fridayMorningSunImageButton:
                if (mPosts.contains("fridayMorning")) {
                    mFridayMorningSunImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                } else {
                    addToAvailableTimes("fridayMorning");
                    mFridayMorningSunImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                }
                break;
            case R.id.fridayAfternoonSunsetImageButton:
                addToAvailableTimes("fridayAfternoon");
                mFridayAfternoonSunsetImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.fridayEveningMoonImageButton:
                addToAvailableTimes("fridayEvening");
                mFridayEveningMoonImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.saturdayMorningSunImageButton:
                addToAvailableTimes("saturdayMorning");
                mSaturdayMorningSunImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.saturdayAfternoonSunsetImageButton:
                addToAvailableTimes("saturdayAfternoon");
                mSaturdayAfternoonSunsetImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.saturdayEveningImageButton:
                addToAvailableTimes("saturdayEvening");
                mSaturdayEveningImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.sundayMorningSunImageButton:
                addToAvailableTimes("sundayMorning");
                mSundayMorningSunImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.sundayAfternoonSunsetImageButton:
                addToAvailableTimes("sundayAfternoon");
                mSundayAfternoonsunsetImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.sundayEveningImageButton:
                addToAvailableTimes("sundayEvening");
                mSundayEveningImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                addToAvailableTimes("fridayMorning");
                break;
            default:
                break;
        }
    }

    /**
     * After this the on Click for the selection is made
     * it will call this function and it will add
     * to the times the user is available
     */

    private void addToAvailableTimes(String freeTime) {
        mFreeTime.add(freeTime);
    }

    /**
     * This allows to access the data of a user and there calendar
     * through the map in the calendar class
     * the logs are for testing of the data is being gotten
     */
    private void getUserCalendar() {
        final Query query = mReference.child("calendar");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Map<String, Object> newCalendar = (Map<String, Object>) dataSnapshot.getValue();
                    Log.i("CalendarActivity", "Free Time: " + newCalendar.get("mFreeTime"));
                    Log.i("CalendarActivity", "UserId: " + newCalendar.get("userId"));
                    mPosts.add(newCalendar);
                    Log.i("CalendarActivity", "!!!Map: " + mPosts);
                }
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

    /**
     *  this method adds the data to the calender class
     *  which then later puts it in toMap in the class
     *  the structure of the data base is decided with the
     *  child updates , then it launches to the home screen
     */
    private void writeNewPost(String userId, ArrayList mFreeTime) {
        String calendarKey = mReference.push().getKey();
        Calendar calendar = new Calendar(userId, mFreeTime);
        Map<String, Object> postValues = calendar.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/calendar/" + calendarKey, postValues);
        childUpdates.put("/user-calendar/" + userId + "/" + calendarKey, postValues);
        Log.i("CalendarActivity", "Key: " + calendarKey);
        mReference.updateChildren(childUpdates);
        //Toast.makeText(this, "Post Successful!", Toast.LENGTH_LONG).show();
        Intent launchPosts = new Intent(this, MainActivity.class);
        startActivity(launchPosts);
    }

}

