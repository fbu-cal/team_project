package com.example.yoked;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yoked.models.Match;
import com.example.yoked.models.Utilities;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MatchArrayAdapter extends ArrayAdapter<Match> {

    private final Context context;
    private final ArrayList<Match> data;
    private final int layoutResourceId;

    public MatchArrayAdapter(Context context, int layoutResourceId, ArrayList<Match> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.data = data;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.mTitleText = row.findViewById(R.id.title_text_view);
            holder.mTimeText = row.findViewById(R.id.time_text_view);
            holder.mProfileImage = row.findViewById(R.id.profile_image_view);

            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)row.getTag();
        }

        Match match = data.get(position);
        findUser(holder, match);

        return row;
    }

    static class ViewHolder
    {
        TextView mTitleText, mTimeText;
        ImageView mProfileImage;
    }

    // find user information and set it in the holder
    private void findUser (final ViewHolder holder, final Match match) {
        String uid = match.otherUserId;
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                holder.mTitleText.setText("@" + newUser.get("username").toString());
                if (newUser.get("profile_picture") != null) {
                    String imageUrl = newUser.get("profile_picture").toString();
                    // if profile pic is already set
                    if (!imageUrl.equals("")) {
                        try {
                            // set profile picture
                            Bitmap realImage = Utilities.decodeFromFirebaseBase64(imageUrl);
                            Bitmap circularImage = Utilities.getCircleBitmap(realImage);
                            holder.mProfileImage.setImageBitmap(circularImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("PostDetail", "Profile pic issue", e);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
        holder.mTimeText.setText(match.freeTime);
    }

}
