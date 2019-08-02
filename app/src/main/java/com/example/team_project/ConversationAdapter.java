package com.example.team_project;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class ConversationAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> implements ListAdapter {

    private ArrayList<Map<String, Object>> mMessages;
    Context context;

    public ConversationAdapter(Context context, ArrayList<Map<String, Object>> messages) {
        this.context=context;
        mMessages = messages;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View messageView = inflater.inflate(R.layout.item_message,parent,false);
        MessageAdapter.ViewHolder viewHolder = new MessageAdapter.ViewHolder(messageView);
        return viewHolder;
    }

//    @Override
//    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder viewHolder, int i) {
//
//    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder viewHolder, int position) {
        final Map<String, Object> message = mMessages.get(position);

        //populate the view according to Message model
        //viewHolder.mUsername.setText();
        viewHolder.mMessageText.setText(message.get("messageText").toString());

        String stringDate = message.get("timeSent").toString();
        Date date = new Date();
        try {
            date = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy").parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        viewHolder.mMessageTimeStamp.setText(getRelativeTimeAgo(date));
//        onOptionsItemSelected(android.R.id.home);

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mUsername;
        public TextView mMessageText;
        public TextView mMessageTimeStamp;
        public ImageView mProfileImage;

        public ViewHolder(View itemView) {
            super(itemView);

            mUsername = itemView.findViewById(R.id.tvUsername);
            mMessageText = itemView.findViewById(R.id.tvMessageText);
            mMessageTimeStamp = itemView.findViewById(R.id.tvDate);
            mProfileImage = itemView.findViewById(R.id.ivProfileImage);
        }

        public void onClick(View v) {

        }
    }
    public static String getRelativeTimeAgo(Date date) {
        String relativeDate = "";
        long dateMillis = date.getTime();
        relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        return relativeDate;
    }
}
