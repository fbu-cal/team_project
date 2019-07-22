package com.example.team_project;

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

import com.example.team_project.models.Calendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
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
    public String mFridayMorning;
    public String mSaturdayMorning;
    public String mSundayMorning;
    public String mFridayAfternoon;
    public String mSaturdayAfternoon;
    public String mSundayAfternoon;
    public String mFridayEvening;
    public String mSaturdayEvening;
    public String mSundayEvening;
    private FirebaseAuth mAuth;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        //set up the variables with their buttons
        mSubmitCalendarButton = findViewById(R.id.editCalendarButton);
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

        /**if (mCalendar.toMap() != null) {
            editRoute();
        }*/

        /**
         * when clicked data will be sent from ArrayList here to the other
         * file one and then push to data base
         */
        mSubmitCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("CalendarActivity", "UserId:!!!!! " + mFridayMorning);
                Toast.makeText(CalendarActivity.this, "Calendar set up complete!", Toast.LENGTH_LONG).show();
                getUserCalendar();
                Intent launchPosts = new Intent(CalendarActivity.this, MainActivity.class);
                startActivity(launchPosts);
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
     * After this the on Click for the selection is made
     * it will call this function and it will add
     * to the times the user is available
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fridayMorningSunImageButton:
                mFridayMorning = "FriMorFree";
                writeNewPost(userId, mFridayMorning);
                mFridayMorningSunImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.fridayAfternoonSunsetImageButton:
                mFridayAfternoon = "FriAfterFree";
                writeNewPost(userId, mFridayAfternoon);
                mFridayAfternoonSunsetImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.fridayEveningMoonImageButton:
                mFridayEvening = "FriEveningFree";
                writeNewPost(userId, mFridayEvening);
                mFridayEveningMoonImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.saturdayMorningSunImageButton:
                mSaturdayMorning = "SatMorFree";
                writeNewPost(userId, mSaturdayMorning);
                mSaturdayMorningSunImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.saturdayAfternoonSunsetImageButton:
                mSaturdayAfternoon = "SatAfterFree";
                writeNewPost(userId, mSaturdayAfternoon);
                mSaturdayAfternoonSunsetImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.saturdayEveningImageButton:
                mSaturdayAfternoon = "SatEveningFree";
                writeNewPost(userId, mSaturdayEvening);
                mSaturdayEveningImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.sundayMorningSunImageButton:
                mSundayMorning = "SunMorFree";
                writeNewPost(userId, mSundayMorning);
                mSundayMorningSunImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.sundayAfternoonSunsetImageButton:
                mSundayAfternoon = "SunAfterFree";
                writeNewPost(userId, mSundayAfternoon);
                mSundayAfternoonsunsetImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            case R.id.sundayEveningImageButton:
                mSundayEvening = "SunEveningFree";
                writeNewPost(userId, mSundayEvening);
                mSundayEveningImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                break;
            default:
                break;
        }
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
                    Log.i("CalendarActivity", "FriMor: " + newCalendar.get("mFridayMorning"));
                    Log.i("CalendarActivity", "FriAfter: " + newCalendar.get("mFridayAfternoon"));
                    Log.i("CalendarActivity", "FriEven: " + newCalendar.get("mFridayEvening"));
                    Log.i("CalendarActivity", "SatMor: " + newCalendar.get("mSaturdayMorning"));
                    Log.i("CalendarActivity", "SatAfter: " + newCalendar.get("mSaturdayAfternoon"));
                    Log.i("CalendarActivity", "SatEven: " + newCalendar.get("mSaturdayEvening"));
                    Log.i("CalendarActivity", "SunMor: " + newCalendar.get("mSundayMorning"));
                    Log.i("CalendarActivity", "SunAfter: " + newCalendar.get("mSundayAfternoon"));
                    Log.i("CalendarActivity", "SunEven: " + newCalendar.get("mSundayEvening"));
                    Log.i("CalendarActivity", "UserId: " + newCalendar.get("userId"));
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
    private void writeNewPost(String userId, String mFreeDay) {
        String key = mReference.push().getKey();
        mCalendar = new Calendar(userId, mFridayMorning, mFridayAfternoon,
                mFridayEvening, mSaturdayMorning, mSaturdayAfternoon,
                mSaturdayEvening, mSundayMorning, mSundayAfternoon, mSundayEvening);
        Map<String, Object> postValues = mCalendar.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/calendar/" + key, postValues);
        childUpdates.put("/user-calendar/" + userId + "/" + key, postValues);
        mReference.updateChildren(childUpdates);
        Toast.makeText(this, "Data inserted successfully", Toast.LENGTH_SHORT).show();
    }
}