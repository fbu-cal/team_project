package com.example.team_project.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.team_project.CalendarActivity;
import com.example.team_project.LoginActivity;
import com.example.team_project.R;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {
    private Button mCalendarButton;
    private Button mLogoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_profile, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mCalendarButton = view.findViewById(R.id.calendar_button);
        mLogoutButton = view.findViewById(R.id.logout_button);

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
                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivity(i);
            }
        });
    }

    private void launchCalendar() {
        final Intent intent = new Intent(getActivity(), CalendarActivity.class);
        startActivity(intent);
    }
}
