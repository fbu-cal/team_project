package com.example.team_project.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.team_project.MainActivity;
import com.example.team_project.R;
import com.example.team_project.models.Post;
import com.example.team_project.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ComposeFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 111;
    private DatabaseReference mDatabase;
    String imageEncoded;

    EditText mDescription;
    Button mPostButton;
    ImageView mPostImage;

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

        mDatabase = FirebaseDatabase.getInstance().getReference();

        onLaunchCamera(view);

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
                                    writeNewPost(userId, user.username, description, imageEncoded);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            encodeBitmap(imageBitmap);
            mPostImage.setImageBitmap(imageBitmap);
        }
    }

    public void onLaunchCamera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void writeNewPost(String userId, String username, String description, String postImageUrl) {
        String key = mDatabase.child("posts").push().getKey();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        String timestamp = simpleDateFormat.format(new Date());
        Log.d("MainActivity", "Current Timestamp: " + timestamp);
        Post post = new Post(userId, username, description, postImageUrl, timestamp);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);

        Toast.makeText(getActivity(), "Post Successful!", Toast.LENGTH_LONG).show();
        mDescription.setText("");

        Intent launchPosts = new Intent(getActivity(), MainActivity.class);
        startActivity(launchPosts);
    }

    public void encodeBitmap(Bitmap bitmap) {
        // save image to firebase
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
         imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}
