package com.example.yoked;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yoked.models.Notification;
import com.example.yoked.models.Utilities;

import java.io.IOException;

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


