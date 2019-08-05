package com.example.team_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.team_project.models.Notification;
import com.example.team_project.models.Post;
import com.example.team_project.models.Utilities;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

public class NotificationViewHolder extends RecyclerView.ViewHolder {
    public TextView mTitle, mBody, mTime;
    public ImageView mIcon;

    public NotificationViewHolder(View itemView) {
        super(itemView);

        mTitle = itemView.findViewById(R.id.title_text_view);
        mBody = itemView.findViewById(R.id.body_text_view);
        mTime = itemView.findViewById(R.id.time_text_view);
        mIcon = itemView.findViewById(R.id.icon_image_view);
    }

    public void bindToPost(final Notification notif) throws IOException {
        mTitle.setText(notif.title);
        mBody.setText(notif.body);
        mTime.setText(Utilities.getRelativeTimeAgo(notif.timestamp));
        if (notif.icon != null) {
            if (!notif.icon.equals(""))
                mIcon.setImageBitmap(Utilities.getCircleBitmap(Utilities.decodeFromFirebaseBase64(notif.icon)));
        }
        if (!notif.seen) {
            MainActivity.notificationBadge.setVisibility(View.VISIBLE);
        }
    }
}


