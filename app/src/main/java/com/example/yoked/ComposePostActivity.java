package com.example.yoked;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yoked.models.Notification;
import com.example.yoked.models.Post;
import com.example.yoked.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class ComposePostActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 111;
    private static final int REQUEST_IMAGE_UPLOAD = 222;
    private DatabaseReference mDatabase;
    private String mCurrentUser;
    private String mImageEncoded;

    private EditText mDescription;
    private Button mPostButton, mTagFriendButton, mTakePictureButton, mUploadPictureButton;
    private ImageButton mBackImageButton;
    private ImageView mPostImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_post);
        mDescription = findViewById(R.id.description_edit_text);
        mPostButton = findViewById(R.id.post_button);
        mPostImage = findViewById(R.id.post_image_view);
        mTagFriendButton = findViewById(R.id.tag_friends_button);
        mTakePictureButton = findViewById(R.id.take_picture_button);
        mUploadPictureButton = findViewById(R.id.upload_picture_button);
        mBackImageButton = findViewById(R.id.back_image_button);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        addSpinner();

        mBackImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toHome = new Intent (ComposePostActivity.this, MainActivity.class);
                startActivity(toHome);
            }
        });

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

                // get user;s username
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
                                    Toast.makeText(ComposePostActivity.this,
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
                final SpinnerDialog spinnerDialog = new SpinnerDialog(ComposePostActivity.this, friendList, "Select Friend");
                spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
                    @Override
                    public void onClick(String s, int i) {
                        Toast.makeText(ComposePostActivity.this, "Selected: " + s, Toast.LENGTH_LONG);
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
        Bitmap imageBitmap = null;
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
        }
        else if (requestCode == REQUEST_IMAGE_UPLOAD) {
            if (data != null) {
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // Configure byte output stream
        if (imageBitmap != null) {
            // compress bitmap
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
            // crop bitmap and encode
            int dimension = getSquareCropDimensionForBitmap(imageBitmap);
            Bitmap croppedBitmap = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
            encodeBitmap(croppedBitmap);
            mPostImage.setImageBitmap(croppedBitmap);
        }
    }

    public void onLaunchCamera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void onLaunchGallery(View view)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 222);
    }

    private void writeNewPost(final String userId, final String username, final String description, final String postImageUrl) {
        // update posts and user-posts
        final String key = mDatabase.child("posts").push().getKey();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        final String timestamp = simpleDateFormat.format(new Date());
        Log.d("MainActivity", "Current Timestamp: " + timestamp);
        String taggedFriend = mTagFriendButton.getText().toString();
        if (postImageUrl!=null && !postImageUrl.equals("") && !description.equals("")) {
            if (taggedFriend.equals("Tag Friends"))
                taggedFriend = null;
            else
                taggedFriend = taggedFriend.split(" ")[1].substring(1);
            //final Post post = new Post(userId, username, description, postImageUrl, timestamp, taggedFriend);
            // send notification to tagged user if tagged user exists
            // add taggedUid to post
            // add tagged post to user-tagged-posts
            if (taggedFriend != null) {
                String taggedUsername = taggedFriend;
                Query query = FirebaseDatabase.getInstance().getReference("users")
                        .orderByChild("username").equalTo(taggedUsername);
                final String finalTaggedFriend = taggedFriend;
                query.addChildEventListener(new ChildEventListener() {// Retrieve new posts as they are added to Firebase
                    @Override
                    public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                        Map<String, Object> newUser = (Map<String, Object>) snapshot.getValue();
                        String taggedUid = newUser.get("uid").toString();
                        sendFirebaseNotification(mCurrentUser, taggedUid, "has tagged you in a post", key);
                        // create post object
                        Post post = new Post(userId, username, description, postImageUrl, timestamp, finalTaggedFriend, taggedUid);
                        // MainActivity.notificationBadge.setVisibility(View.VISIBLE);
                        // post to user-tagged-posts
                        Map<String, Object> postValues = post.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/user-tagged-posts/" + taggedUid + "/" + key, postValues);
                        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);
                        mDatabase.updateChildren(childUpdates);
                        // update user-feed
                        updateAllFeeds(postValues, key);
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
            // if no tagged friend
            else {
                Post post = new Post(userId, username, description, postImageUrl, timestamp, null, null);
                // upload post to user-posts
                final Map<String, Object> postValues = post.toMap();
                final Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/user-posts/" + userId + "/" + key, postValues);
                mDatabase.updateChildren(childUpdates);
                // update user-feed
                updateAllFeeds(postValues, key);
                Toast.makeText(ComposePostActivity.this, "Post Successful!", Toast.LENGTH_LONG).show();
                mDescription.setText("");
            }
            Intent launchPosts = new Intent(ComposePostActivity.this, MainActivity.class);
            startActivity(launchPosts);
        }
        else {
            Toast.makeText(ComposePostActivity.this, "Post Unsuccessful! Missing image or description!", Toast.LENGTH_LONG).show();
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
        bitmap.compress(Bitmap.CompressFormat.PNG, 40, baos);
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
        Toast.makeText(ComposePostActivity.this, "Sent Notification", Toast.LENGTH_LONG).show();
    }

}
