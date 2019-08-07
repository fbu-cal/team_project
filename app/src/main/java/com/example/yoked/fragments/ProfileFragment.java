package com.example.yoked.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yoked.CalendarActivity;
import com.example.yoked.MyPostsFragment;
import com.example.yoked.MyTagsFragment;
import com.example.yoked.OtherUserProfileActivity;
import com.example.yoked.PostViewHolder;
import com.example.yoked.R;
import com.example.yoked.UserSettingsActivity;
import com.example.yoked.models.Post;
import com.example.yoked.models.Utilities;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;


public class ProfileFragment extends Fragment {
    private Button mCalendarButton;
    private ImageButton mSettingsButton;
    private ImageView mProfileImage;
    private TextView mFullname, mUsername, mFriendCount;
    // context for rendering
    Context context;
    private String mCurrentUserUid;

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private LinearLayoutManager mManager;
    private boolean mShouldRefreshOnResume;

    private TabLayout mTabLayout;
    private Fragment fragmentOne;
    private Fragment fragmentTwo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // get the context and create the inflater
        context = parent.getContext();
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_profile, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mCalendarButton = view.findViewById(R.id.calendar_button);
        mProfileImage = view.findViewById(R.id.profile_image_view);
        mFullname = view.findViewById(R.id.fullname_text_view);
        mUsername = view.findViewById(R.id.username_text_view);
        mFriendCount = view.findViewById(R.id.friend_count_text_view);
        mSettingsButton = view.findViewById(R.id.settings_button);

        mCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCalendar();
            }
        });

        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSettings();
            }
        });

        mFriendCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSpinner();
            }
        });

        setUserInformation();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        getAllWidgets(view);
        bindWidgetsWithAnEvent();
        setupTabLayout();
    }

    private void launchCalendar() {
        final Intent intent = new Intent(getActivity(), CalendarActivity.class);
        startActivity(intent);
    }

    private void launchSettings() {
        final Intent intent = new Intent(getActivity(), UserSettingsActivity.class);
        startActivity(intent);
    }

    public void setUserInformation () {
        Query query = FirebaseDatabase.getInstance().getReference("users")
                .child(mCurrentUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                // set fullname and username
                mFullname.setText(newUser.get("fullname").toString());
                mUsername.setText("@" + newUser.get("username").toString());
                if (newUser.get("profile_picture")!=null) {
                    String imageUrl = newUser.get("profile_picture").toString();
                    // if profile pic is already set
                    if (!imageUrl.equals("")) {
                        try {
                            // set profile picture
                            Bitmap realImage = Utilities.decodeFromFirebaseBase64(imageUrl);
                            Bitmap circleImage = Utilities.getCircleBitmap(realImage);
                            mProfileImage.setImageBitmap(circleImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("ProfileFragment", "Profile pic issue", e);
                        }
                    }
                }
                long friendCount = dataSnapshot.child("friendList").getChildrenCount();
                if (friendCount == 1)
                    mFriendCount.setText(friendCount + " Friend");
                else
                    mFriendCount.setText(friendCount + " Friends");

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getAllWidgets(View view) {
        mTabLayout = (TabLayout) view.findViewById(R.id.tabs);
    }

    private void setupTabLayout() {
        fragmentOne = new MyPostsFragment();
        fragmentTwo = new MyTagsFragment();
        mTabLayout.addTab(mTabLayout.newTab().setText("MY POSTS"),true);
        mTabLayout.addTab(mTabLayout.newTab().setText("TAGGED POSTS"));
    }
    private void bindWidgetsWithAnEvent()
    {
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setCurrentTabFragment(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
    private void setCurrentTabFragment(int tabPosition)
    {
        switch (tabPosition)
        {
            case 0 :
                replaceFragment(fragmentOne);
                break;
            case 1 :
                replaceFragment(fragmentTwo);
                break;
        }
    }
    public void replaceFragment(Fragment fragment) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame_container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    private void addSpinner() {
        Query query = mDatabase.child("users").child(mCurrentUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> friendMap = (Map<String, Object>) dataSnapshot.child("friendList").getValue();
                final ArrayList<String> friendList = new ArrayList<String>();
                if (friendMap!=null) {
                    for (String userId : friendMap.keySet()) {
                        friendList.add(friendMap.get(userId).toString());
                    }
                }
                // spinner
                final SpinnerDialog spinnerDialog = new SpinnerDialog(getActivity(), friendList, "Select Friend");
                spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
                    @Override
                    public void onClick(String s, int i) {
                        Intent toOtherProfile = new Intent(getActivity(), OtherUserProfileActivity.class);
                        toOtherProfile.putExtra("uid", friendList.get(i));
                        startActivity(toOtherProfile);
                    }
                });
                spinnerDialog.showSpinerDialog();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }
}