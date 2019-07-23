package com.example.team_project.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.team_project.CalendarActivity;
import com.example.team_project.FirstActivity;
import com.example.team_project.LoginActivity;
import com.example.team_project.ProfilePictureActivity;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.IOException;
import java.util.Map;


public class ProfileFragment extends Fragment {
    private Button mCalendarButton, mLogoutButton, mUploadPictureButton;
    private ImageView mProfileImage;
    private TextView mFullname, mUsername;
    // context for rendering
    Context context;
    private String uid;

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
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mCalendarButton = view.findViewById(R.id.calendar_button);
        mLogoutButton = view.findViewById(R.id.logout_button);
        mUploadPictureButton = view.findViewById(R.id.upload_picture_button);
        mProfileImage = view.findViewById(R.id.profile_image_view);
        mFullname = view.findViewById(R.id.fullname_text_view);
        mUsername = view.findViewById(R.id.username_text_view);

        mCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCalendar();
            }
        });

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(getActivity(), FirstActivity.class);
                startActivity(i);
            }
        });

        mUploadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toUpload = new Intent(getActivity(), ProfilePictureActivity.class);
                startActivity(toUpload);
            }
        });

        setUserInformation();
    }

    private void launchCalendar() {
        final Intent intent = new Intent(getActivity(), CalendarActivity.class);
        startActivity(intent);
    }

    public void setUserInformation () {
        Query query = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("username");
        query.addChildEventListener(new ChildEventListener() {// Retrieve new posts as they are added to Firebase
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newUser = (Map<String, Object>) snapshot.getValue();
                // check if user is the current user
                if (newUser.get("uid").toString().equals(uid)) {
                    // set fullname and username
                    mFullname.setText(newUser.get("fullname").toString());
                    mUsername.setText("@" + newUser.get("username").toString());
                    String imageUrl = newUser.get("profile_picture").toString();
                    // if profile pic is already set
                    if (!imageUrl.equals("")) {
                        Log.i("ProfileFragment", "imageUrl: " + imageUrl);
                        try {
                            // set profile picture
                            Bitmap realImage = decodeFromFirebaseBase64(imageUrl);
                            Log.i("ProfileFragment", "realImage: " + realImage);
                            mProfileImage.setImageBitmap(realImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("ProfileFragment", "Profile pic issue", e);
                        }
                    }
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

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }
}
