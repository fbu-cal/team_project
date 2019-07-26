package com.example.team_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.team_project.models.Calendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MatchingActivity extends Activity {

    private ArrayList<String> mMatches;
    private ArrayAdapter<String> arrayAdapter;
    private int i;
    public HashMap<String, Object> mCurrentUserNewCalendar;
    public HashMap<String, Object> mOtherUserNewCalendar;
    DatabaseReference mReference;
    private HashMap<String, Boolean> mCurrentUserTime;
    private HashMap<String, Boolean> mOtherUserTime;
    private String mUserId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching);
        mReference = FirebaseDatabase.getInstance().getReference();
        mCurrentUserTime = new HashMap<String, Boolean>();
        mOtherUserTime = new HashMap<String, Boolean>();
        mAuth = FirebaseAuth.getInstance();
        mUserId = mAuth.getCurrentUser().getUid();
        getUserCalendar();
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
             *If it comes out left then the user does not want to
             * hangout with them so nothing needs to be done
             */

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                //makeToast, "Left!");
                Toast.makeText(MatchingActivity.this, "swiped left", Toast.LENGTH_SHORT).show();

            }

            /**
             * If swiped right then add it to somewhere on database
             * and check if other user also swiped right
             * if they do hit them both with a notification
             * if not let them know later
             */
            @Override
            public void onRightCardExit(Object dataObject) {
                Toast.makeText(MatchingActivity.this, "swiped right", Toast.LENGTH_SHORT).show();
                //makeToast(MyActivity.this, "Right!");
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                mMatches.add("XML ".concat(String.valueOf(i)));
                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++;
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });


        /**
         * Can implement a closer look if wanted here
         */
        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

            }
        });
    }

    private void getUserCalendar() {
        final Query otherQuery = mReference.child("user-calendar/OIFAGk70sfPS81IjUNvsOwZTTmf2");
        otherQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mOtherUserNewCalendar = (HashMap<String, Object>) dataSnapshot.getValue();
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
        final Query currentQuery = mReference.child("user-calendar/" + mUserId);
        currentQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //for (DataSnapshot data : dataSnapshot.getChildren()) {
                mCurrentUserNewCalendar = (HashMap<String, Object>) dataSnapshot.getValue();
                Log.i("CalendarActivity", "Free Time: " + mCurrentUserNewCalendar.get("mFreeTime"));
                Log.i("CalendarActivity", "UserId: " + mCurrentUserNewCalendar.get("userId"));
                Log.i("CalendarActivity", "!!!Map: " + mCurrentUserNewCalendar);
                //}
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
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mCurrentUserNewCalendar != null && mOtherUserNewCalendar != null) {
                    mCurrentUserTime = (HashMap<String, Boolean>) mCurrentUserNewCalendar.get("mFreeTime");
                    mOtherUserTime = (HashMap<String, Boolean>) mOtherUserNewCalendar.get("mFreeTime");
                    makeComplete();
                    checkFunc();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makeComplete() {
        if (!mCurrentUserTime.containsKey("fridayMorning")) {
            mCurrentUserTime.put("fridayMorning", false);
        }
        if (!mCurrentUserTime.containsKey("fridayAfternoon")) {
            mCurrentUserTime.put("fridayAfternoon", false);
        }
        if (!mCurrentUserTime.containsKey("fridayEvening")) {
            mCurrentUserTime.put("fridayEvening", false);
        }
        if (!mCurrentUserTime.containsKey("saturdayMorning")) {
            mCurrentUserTime.put("saturdayMorning", false);
        }
        if (!mCurrentUserTime.containsKey("saturdayAfternoon")) {
            mCurrentUserTime.put("saturdayAfternoon", false);
        }
        if (!mCurrentUserTime.containsKey("saturdayEvening")) {
            mCurrentUserTime.put("saturdayEvening", false);
        }
        if (!mCurrentUserTime.containsKey("sundayMorning")) {
            mCurrentUserTime.put("sundayMorning", false);
        }
        if (!mCurrentUserTime.containsKey("sundayAfternoon")) {
            mCurrentUserTime.put("sundayAfternoon", false);
        }
        if (!mCurrentUserTime.containsKey("sundayEvening")) {
            mCurrentUserTime.put("sundayEvening", false);
        }

        //I could not add an and because the users data is different
        if (!mOtherUserTime.containsKey("fridayMorning")) {
            mOtherUserTime.put("fridayMorning", false);
        }
        if (!mOtherUserTime.containsKey("fridayAfternoon")) {
            mOtherUserTime.put("fridayAfternoon", false);
        }
        if (!mOtherUserTime.containsKey("fridayEvening")) {
            mOtherUserTime.put("fridayEvening", false);
        }
        if (!mOtherUserTime.containsKey("saturdayMorning")) {
            mOtherUserTime.put("saturdayMorning", false);
        }
        if (!mOtherUserTime.containsKey("saturdayAfternoon")) {
            mOtherUserTime.put("saturdayAfternoon", false);
        }
        if (!mOtherUserTime.containsKey("saturdayEvening")) {
            mOtherUserTime.put("saturdayEvening", false);
        }
        if (!mOtherUserTime.containsKey("sundayMorning")) {
            mOtherUserTime.put("sundayMorning", false);
        }
        if (!mOtherUserTime.containsKey("sundayAfternoon")) {
            mOtherUserTime.put("sundayAfternoon", false);
        }
        if (!mOtherUserTime.containsKey("sundayEvening")) {
            mOtherUserTime.put("sundayEvening", false);
        }
    }

    private void checkFunc() {
        Log.i("CalendarActivity", "!!!Map: " + mCurrentUserTime);
        if (mCurrentUserTime.get("fridayMorning") == mOtherUserTime.get("fridayMorning")) {
            if (!mMatches.contains("OIFAGk70sfPS81IjUNvsOwZTTmf2")) {
                mMatches.add("OIFAGk70sfPS81IjUNvsOwZTTmf2");
            }
        }
    }

    /**private void writeNewPost(String Status, HashMap userFreeMatchBefore, HashMap userFreeMatchFinal) {
        String calendarKey = mReference.child("calendar").push().getKey();
        Calendar calendar = new Calendar(mUserId, mFreeTime);
        Map<String, Object> postValues = calendar.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/calendar/" + calendarKey, postValues);
        childUpdates.put("/user-calendar/" + userId + "/" + calendarKey, postValues);

        Log.i("CalendarActivity", "Key: " + calendarKey);
        Log.i("CalendarActivity", "Key: " + userId);
        mReference.updateChildren(childUpdates);
        //Toast.makeText(this, "Post Successful!", Toast.LENGTH_LONG).show();
        Intent launchPosts = new Intent(this, MainActivity.class);
        startActivity(launchPosts);
    }*/

}

