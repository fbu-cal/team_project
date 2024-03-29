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
import com.google.firebase.database.ValueEventListener;

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
    public HashMap<String, Boolean> mFreeTime;
    private FirebaseAuth mAuth;
    String userId;
    private HashMap<String, Boolean> mPosts;
    public List<String> mUserFreeTime;
    public HashMap<String, Object> mNewCalendar;
    private int deleteCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        deleteCount = 0;
        mPosts = new HashMap<String, Boolean>();
        mFreeTime = new HashMap<String, Boolean>();
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
                if (mFreeTime.size() != 0 || deleteCount > 0) {
                    writeNewPost(userId, mFreeTime);
                    Toast.makeText(CalendarActivity.this, "data inserted successfully", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    /**
     * when dates selected it will be sent to a function that will
     * add the time to an arrayList of the Users free time
     * this got more complicated because if the user wants
     * to add time there would be a different calendar model
     * created so I had to go through my arrayList to check
     * if it already contains an item so there is no duplicates
     * when dates selected it will be sent to a function that will
     * add the time to an arrayList of the Users free time
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fridayMorningSunImageButton:
                //it is in calendar so remove
                if (mPosts.containsKey("fridayMorning")) {
                    if (mPosts.get("fridayMorning")) {
                        mFreeTime.remove("fridayMorning");
                        mFridayMorningSunImageButton.setColorFilter(null);
                        deleteCount++;
                    } else {
                        addToAvailableTimes("fridayMorning");
                        mFridayMorningSunImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                    }
                }
                break;
            case R.id.fridayAfternoonSunsetImageButton:
                if (mPosts.containsKey("fridayAfternoon")) {
                    if (mPosts.get("fridayAfternoon")) {
                        mFreeTime.remove("fridayAfternoon");
                        mFridayAfternoonSunsetImageButton.setColorFilter(null);
                        deleteCount++;
                    } else {
                        addToAvailableTimes("fridayAfternoon");
                        mFridayAfternoonSunsetImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                    }
                }
                break;
            case R.id.fridayEveningMoonImageButton:
                if (mPosts.containsKey("fridayEvening")) {
                    if (mPosts.get("fridayEvening")) {
                        mFreeTime.remove("fridayEvening");
                        mFridayEveningMoonImageButton.setColorFilter(null);
                        deleteCount++;
                    } else {
                        addToAvailableTimes("fridayEvening");
                        mFridayEveningMoonImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                    }
                }
                break;
            case R.id.saturdayMorningSunImageButton:
                if (mPosts.containsKey("saturdayMorning")) {
                    if (mPosts.get("saturdayMorning")) {
                        mFreeTime.remove("saturdayMorning");
                        mSaturdayMorningSunImageButton.setColorFilter(null);
                        deleteCount++;
                    } else {
                        addToAvailableTimes("saturdayMorning");
                        mSaturdayMorningSunImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                    }
                }
                break;
            case R.id.saturdayAfternoonSunsetImageButton:
                if (mPosts.containsKey("saturdayAfternoon")) {
                    if (mPosts.get("saturdayAfternoon")) {
                        mFreeTime.remove("saturdayAfternoon");
                        mSaturdayAfternoonSunsetImageButton.setColorFilter(null);
                        deleteCount++;
                    } else {
                        addToAvailableTimes("saturdayAfternoon");
                        mSaturdayAfternoonSunsetImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                    }
                }
                break;
            case R.id.saturdayEveningImageButton:
                if (mPosts.containsKey("saturdayEvening")) {
                    if (mPosts.get("saturdayEvening")) {
                        mFreeTime.remove("saturdayEvening");
                        mSaturdayEveningImageButton.setColorFilter(null);
                        deleteCount++;
                    } else {
                        addToAvailableTimes("saturdayEvening");
                        mSaturdayEveningImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                    }
                }
                break;
            case R.id.sundayMorningSunImageButton:
                if (mPosts.containsKey("sundayMorning")) {
                    if (mPosts.get("sundayMorning")) {
                        mFreeTime.remove("sundayMorning");
                        mSundayMorningSunImageButton.setColorFilter(null);
                        deleteCount++;
                    }
                    addToAvailableTimes("sundayMorning");
                    mSundayMorningSunImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                }
                break;
            case R.id.sundayAfternoonSunsetImageButton:
                if (mPosts.containsKey("sundayMorning")) {
                    if (mPosts.get("sundayAfternoon")) {
                        mFreeTime.remove("sundayAfternoon");
                        mSundayAfternoonsunsetImageButton.setColorFilter(null);
                        deleteCount++;
                    } else {
                        addToAvailableTimes("sundayAfternoon");
                        mSundayAfternoonsunsetImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                    }
                }
                break;
            case R.id.sundayEveningImageButton:
                if (mPosts.containsKey("sundayEvening")) {
                    if (mPosts.get("sundayEvening")) {
                        mFreeTime.remove("sundayEvening");
                        mSundayEveningImageButton.setColorFilter(null);
                        deleteCount++;
                    } else {
                        addToAvailableTimes("sundayEvening");
                        mSundayEveningImageButton.setColorFilter(Color.argb(200, 200, 200, 200));
                    }
                }
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
        mPosts.put("fridayMorning", false);
        mPosts.put("fridayAfternoon", false);
        mPosts.put("fridayEvening", false);
        mPosts.put("saturdayMorning", false);
        mPosts.put("saturdayAfternoon", false);
        mPosts.put("saturdayEvening", false);
        mPosts.put("sundayMorning", false);
        mPosts.put("sundayAfternoon", false);
        mPosts.put("sundayEvening", false);
    }

    public void deleteCalendar() {
        DatabaseReference deleteUserCalendar = FirebaseDatabase.getInstance().getReference
                ("/user-calendar/" + userId);
        deleteUserCalendar.removeValue();
    }

    private void makeComplete() {
        if (!mPosts.containsKey("fridayMorning")) {
            mPosts.put("fridayMorning", false);
        }
        if (!mPosts.containsKey("fridayAfternoon")) {
            mPosts.put("fridayAfternoon", false);
        }
        if (!mPosts.containsKey("fridayEvening")) {
            mPosts.put("fridayEvening", false);
        }
        if (!mPosts.containsKey("saturdayMorning")) {
            mPosts.put("saturdayMorning", false);
        }
        if (!mPosts.containsKey("saturdayAfternoon")) {
            mPosts.put("saturdayAfternoon", false);
        }
        if (!mPosts.containsKey("saturdayEvening")) {
            mPosts.put("saturdayEvening", false);
        }
        if (!mPosts.containsKey("sundayMorning")) {
            mPosts.put("sundayMorning", false);
        }
        if (!mPosts.containsKey("sundayAfternoon")) {
            mPosts.put("sundayAfternoon", false);
        }
        if (!mPosts.containsKey("sundayEvening")) {
            mPosts.put("sundayEvening", false);
        }
    }

    private void checkData() {
        Log.i("CalendarActivity", "mPosts: " + mPosts.get("fridayMorning"));
        Log.i("CalendarActivity", "mPosts: " + mPosts);
        int grayColor = Color.argb(200, 200, 200, 200);
        if (mPosts.get("fridayMorning")) {
            addToAvailableTimes("fridayMorning");
            tintImageButton(mFridayMorningSunImageButton, grayColor);
        }
        if (mPosts.get("fridayAfternoon")) {
            addToAvailableTimes("fridayAfternoon");
            tintImageButton(mFridayAfternoonSunsetImageButton, grayColor);
        }
        if (mPosts.get("fridayEvening")) {
            addToAvailableTimes("fridayEvening");
            tintImageButton(mFridayEveningMoonImageButton, grayColor);
        }
        if (mPosts.get("saturdayMorning")) {
            addToAvailableTimes("saturdayMorning");
            tintImageButton(mSaturdayMorningSunImageButton, grayColor);
        }
        if (mPosts.get("saturdayAfternoon")) {
            addToAvailableTimes("saturdayAfternoon");
            tintImageButton(mSaturdayAfternoonSunsetImageButton, grayColor);
        }
        if (mPosts.get("saturdayEvening")) {
            addToAvailableTimes("saturdayEvening");
            tintImageButton(mSaturdayEveningImageButton, grayColor);
        }
        if (mPosts.get("sundayMorning")) {
            addToAvailableTimes("sundayMorning");
            tintImageButton(mSundayMorningSunImageButton, grayColor);
        }
        if (mPosts.get("sundayAfternoon")) {
            addToAvailableTimes("sundayAfternoon");
            tintImageButton(mSundayAfternoonsunsetImageButton, grayColor);
        }
        if (mPosts.get("sundayEvening")) {
            addToAvailableTimes("sundayEvening");
            tintImageButton(mSundayEveningImageButton, grayColor);
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
        Intent launchPosts = new Intent(this, MainActivity.class);
        startActivity(launchPosts);
    }

    // tints the given image button to the given color
    private void tintImageButton(ImageButton button, int color) {
        button.setColorFilter(color);
    }

}