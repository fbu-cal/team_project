package com.example.team_project;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.team_project.models.Calendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

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
    public List<String> mUserFreeTime;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        mUserFreeTime = new ArrayList<>();
        //set up the variables with their buttons
        mSubmitCalendarButton = findViewById(R.id.submitCalendarButton);
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
        mReference = FirebaseDatabase.getInstance().getReference().child("Calendar");
        //link the user with there calendar using there uid
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        mCalendar = new Calendar();
        mCalendar.setUserId(userId);
        /**
         * when clicked data will be sent from ArrayList here to the other
         * file one and then push to data base
         */
        mSubmitCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendar.setmFreeTime(mUserFreeTime);
                mReference.push().setValue(mCalendar);
                Toast.makeText(CalendarActivity.this, "data inserted successfully", Toast.LENGTH_LONG).show();

            }
        });
    }

    /**
     * when dates selected it will be sent to a function that will
     * add the time to an arrayList of the Users free time
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fridayMorningSunImageButton:
                addToAvailableTimes("fridayMorning");
                mFridayMorningSunImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
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
        if (!mUserFreeTime.contains(freeTime)) {
            mUserFreeTime.add(freeTime);
        }
    }
}