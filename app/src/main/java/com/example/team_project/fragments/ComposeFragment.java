package com.example.team_project.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.team_project.MainActivity;
import com.example.team_project.R;
import com.example.team_project.models.Post;
import com.example.team_project.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ComposeFragment extends Fragment {

    private DatabaseReference mDatabase;

    EditText mDescription;
    Button mPostButton;

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

        mDatabase = FirebaseDatabase.getInstance().getReference();

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
                                    writeNewPost(userId, user.username, description);
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

    private void writeNewPost(String userId, String username, String description) {
        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post(userId, username, description);
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
}
