package com.example.team_project.fragments;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.team_project.MainActivity;
import com.example.team_project.OtherUserProfileActivity;
import com.example.team_project.R;
import com.example.team_project.models.Notification;
import com.example.team_project.models.Post;
import com.example.team_project.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class ComposeFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 111;
    private static final int REQUEST_IMAGE_UPLOAD = 222;
    private DatabaseReference mDatabase;
    private String mCurrentUser;
    private String mImageEncoded;

    private EditText mDescription;
    private Button mPostButton, mTagFriendButton, mTakePictureButton, mUploadPictureButton;
    private ImageView mPostImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_compose, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mDescription = view.findViewById(R.id.description_edit_text);
        mPostButton = view.findViewById(R.id.post_button);
        mPostImage = view.findViewById(R.id.post_image_view);
        mTagFriendButton = view.findViewById(R.id.tag_friends_button);
        mTakePictureButton = view.findViewById(R.id.take_picture_button);
        mUploadPictureButton = view.findViewById(R.id.upload_picture_button);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        addSpinner();

        mTakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchCamera(v);
            }
        });

        mUploadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchGallery(v);
            }
        });

        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final String description = mDescription.getText().toString();

                mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Get user value
                                User user = dataSnapshot.getValue(User.class);
                                // [START_EXCLUDE]
                                if (user == null) {
                                    // User is null, error out
                                    Log.e("ComposeFragment", "User " + userId + " is unexpectedly null");
                                    Toast.makeText(getActivity(),
                                            "Error: could not fetch user.",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    // Write new post
                                    writeNewPost(userId, user.username, description, mImageEncoded);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w("ComposeFragment", "getUser:onCancelled", databaseError.toException());
                            }
                        }
                );
            }
        });
    }

    private void addSpinner() {
        Query query = mDatabase.child("users").child(mCurrentUser);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> friendMap = (Map<String, Object>) dataSnapshot.child("friendList").getValue();
                ArrayList<String> friendList = new ArrayList<String>();
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
                        Toast.makeText(getActivity(), "Selected: " + s, Toast.LENGTH_LONG);
                        mTagFriendButton.setText("Tagged @" + s);
                    }
                });

                mTagFriendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        spinnerDialog.showSpinerDialog();
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            int dimension = getSquareCropDimensionForBitmap(imageBitmap);
            Bitmap croppedBitmap = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
            encodeBitmap(croppedBitmap);
            mPostImage.setImageBitmap(croppedBitmap);
        }
        else if (requestCode == REQUEST_IMAGE_UPLOAD) {
            Bitmap imageBitmap = null;
            if (data != null) {
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            int dimension = getSquareCropDimensionForBitmap(imageBitmap);
            Bitmap croppedBitmap = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
            encodeBitmap(croppedBitmap);
            mPostImage.setImageBitmap(croppedBitmap);
        }
    }

    public void onLaunchCamera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void onLaunchGallery(View view)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), 222);
    }

    private void writeNewPost(String userId, String username, String description, String postImageUrl) {
        // update posts and user-posts
        final String key = mDatabase.child("posts").push().getKey();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        String timestamp = simpleDateFormat.format(new Date());
        Log.d("MainActivity", "Current Timestamp: " + timestamp);
        String taggedFriend = mTagFriendButton.getText().toString();
        if (taggedFriend.equals("Tag Friends"))
            taggedFriend = null;
        if (postImageUrl!=null && !postImageUrl.equals("") && !description.equals("")) {
            Post post = new Post(userId, username, description, postImageUrl, timestamp, taggedFriend);
            Map<String, Object> postValues = post.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            //childUpdates.put("/posts/" + key, postValues);
            childUpdates.put("/user-posts/" + userId + "/" + key, postValues);
            mDatabase.updateChildren(childUpdates);
            // update user-feed
            updateAllFeeds(postValues, key);
            Toast.makeText(getActivity(), "Post Successful!", Toast.LENGTH_LONG).show();
            mDescription.setText("");

            // send notification to tagged user if tagged user exists
            if (taggedFriend!=null) {
                String taggedUsername = taggedFriend.split(" ")[1].substring(1);
                Query query = FirebaseDatabase.getInstance().getReference("users")
                        .orderByChild("username").equalTo(taggedUsername);
                query.addChildEventListener(new ChildEventListener() {// Retrieve new posts as they are added to Firebase
                    @Override
                    public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                        Map<String, Object> newUser = (Map<String, Object>) snapshot.getValue();
                        String taggedUid = newUser.get("uid").toString();
                        sendFirebaseNotification(mCurrentUser, taggedUid, "has tagged you in a post", key);
                        MainActivity.notificationBadge.setVisibility(View.VISIBLE);
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

            Intent launchPosts = new Intent(getActivity(), MainActivity.class);
            startActivity(launchPosts);
        }
        else {
            Toast.makeText(getActivity(), "Post Unsuccessful! Missing image or description!", Toast.LENGTH_LONG).show();
        }
    }

    // updates user feeds for all the friends of the current user when a new post is made
    private void updateAllFeeds(final Map<String, Object> postValues, final String key) {
        updateIndividualFeed(postValues, key, mCurrentUser);
        Query query = mDatabase.child("users").child(mCurrentUser);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> friendMap = (Map<String, Object>) dataSnapshot.child("friendList").getValue();
                if (friendMap!=null) {
                    for (String friend : friendMap.keySet()) {
                        updateIndividualFeed(postValues, key, friend);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }

    public void updateIndividualFeed (Map<String, Object> postValues, String key, String friend) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/user-feed/" + friend + "/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
    }

    public void encodeBitmap(Bitmap bitmap) {
        // save image to firebase
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        mImageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    public int getSquareCropDimensionForBitmap(Bitmap bitmap)
    {
        //use the smallest dimension of the image to crop to
        return Math.min(bitmap.getWidth(), bitmap.getHeight());
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private void sendFirebaseNotification(final String fromUid, final String toUid, final String body, final String key) {
        Query query = mDatabase.child("users").child(fromUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                String title = newUser.get("username").toString();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
                String timestamp = simpleDateFormat.format(new Date());
                String imageUrl = "";
                if (newUser.get("profile_picture")!=null)
                    imageUrl = newUser.get("profile_picture").toString();
                Notification notif = new Notification
                        ("tagged", imageUrl, title, body, timestamp, toUid, fromUid);
                notif.key = key;
                updateFirebaseNotification(toUid, notif);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }

    private void updateFirebaseNotification(String toUid, Notification notif) {
        String key = mDatabase.child("notification").push().getKey();
        Map<String, Object> notifValues = notif.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-notifications/" + toUid + "/" + key, notifValues);
        mDatabase.updateChildren(childUpdates);
        // update user-feed
        Toast.makeText(getActivity(), "Sent Notification", Toast.LENGTH_LONG).show();
    }
}
