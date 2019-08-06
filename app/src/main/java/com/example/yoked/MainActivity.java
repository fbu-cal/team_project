package com.example.yoked;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.example.yoked.fragments.ComposeFragment;
import com.example.yoked.fragments.NotificationFragment;
import com.example.yoked.fragments.PostsFragment;
import com.example.yoked.fragments.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    private static final int MESSENGER_REQUEST_CODE = 100;
    public BottomNavigationView mBottomNavigationView;

    public static View notificationBadge;

    public String mCurrentUserUid;
    public SpinnerDialog mSpinnerDialog;
    public ImageButton mSearchButton, mMessageButton;

    Fragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // runAsync();

        // Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

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

        mMessageButton = findViewById(R.id.message_image_button);
        mSearchButton = findViewById(R.id.search_image_button);

        mMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMessenger();
            }
        });

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSearch();
            }
        });

        // Add badge view for notifications
        addBadgeView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
//        Drawable drawable = menu.findItem(R.id.miMessenger).getIcon();
//        drawable = DrawableCompat.wrap(drawable);
//        DrawableCompat.setTint(drawable, ContextCompat.getColor(this,R.color.white));
//        menu.findItem(R.id.miMessenger).setIcon(drawable);
//        Drawable drawable2 = menu.findItem(R.id.miSearch).getIcon();
//        drawable2 = DrawableCompat.wrap(drawable2);
//        DrawableCompat.setTint(drawable2, ContextCompat.getColor(this,R.color.white));
//        menu.findItem(R.id.miSearch).setIcon(drawable2);

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
        addSpinner();
    }

    // open ComposeActivity to create a new tweet
    private void goToMessenger() {
        Intent toMessenger = new Intent(this, MainMessenger.class);
        startActivityForResult(toMessenger, MESSENGER_REQUEST_CODE);
    }

    private void addBadgeView() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) mBottomNavigationView.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(2);

        notificationBadge = LayoutInflater.from(this).inflate(R.layout.view_notification_badge, menuView, false);

        itemView.addView(notificationBadge);
        notificationBadge.setVisibility(GONE);
    }

    private void addSpinner() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("users");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> userList = new ArrayList<String>();
                final ArrayList<String> userUidList = new ArrayList<String>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Map<String, Object> newUser = (Map<String, Object>) data.getValue();
                    userList.add(newUser.get("username").toString());
                    userUidList.add(newUser.get("uid").toString());
                }
                // spinner
                mSpinnerDialog = new SpinnerDialog(MainActivity.this, userList, "Search Users");
                mSpinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
                    @Override
                    public void onClick(String s, int i) {
                        Intent toOtherProfile = new Intent(MainActivity.this, OtherUserProfileActivity.class);
                        toOtherProfile.putExtra("uid", userUidList.get(i));
                        startActivity(toOtherProfile);
                    }
                });
                mSpinnerDialog.showSpinerDialog();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }

//    private void runAsync() {
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                checkNotififcations();
//            }
//        });
//    }
//
//    private void checkNotififcations() {
//        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        FirebaseDatabase.getInstance().getReference().child("user-notifications").child(currentUid)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                            String key = snapshot.getKey();
//                            Notification notification = snapshot.getValue(Notification.class);
//                            if (!notification.seen) {
//                                MainActivity.notificationBadge.setVisibility(View.VISIBLE);
//                            }
//                            // TODO - show push notification
//                            if (!notification.pushed) {
//                                createPushNotification(notification.title, notification.body, key);
//                            }
//                        }
//                        runAsync();
//                    }
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        runAsync();
//                    }
//                });
//    }
//
//    private void createPushNotification(String title, String body, String key) {
//        // create notification
//        // Configure the channel
//        int importance = NotificationManager.IMPORTANCE_DEFAULT;
//        NotificationChannel channel = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            channel = new NotificationChannel("myChannelId", "My Channel", importance);
//            channel.setDescription("Reminders");
//            // Register the channel with the notifications manager
//            NotificationManager mNotificationManager =
//                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//            mNotificationManager.createNotificationChannel(channel);
//            NotificationCompat.Builder mBuilder =
//                    // Builder class for devices targeting API 26+ requires a channel ID
//                    new NotificationCompat.Builder(this, "myChannelId")
//                            .setSmallIcon(R.drawable.instagram_user_outline_24)
//                            .setContentTitle(title)
//                            .setContentText(body);
//            int id = createID();
//            mNotificationManager.notify(id, mBuilder.build());
//        }
//        // update notification to show pushed=true
//        updateNotificationPushedStatus(key);
//        // if notification is clicked, then show correct activity
//        // if notificiation is clicked, update so seen=true
//    }
//
//    private void updateNotificationPushedStatus(final String key) {
//        DatabaseReference ref = FirebaseDatabase.getInstance()
//                .getReference("user-notifications")
//                .child(mCurrentUserUid)
//                .child(key)
//                .child("pushed");
//        ref.setValue(true);
//    }
//
//    public int createID(){
//        Date now = new Date();
//        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
//        return id;
//    }
}