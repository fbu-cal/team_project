package com.example.yoked;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> implements ListAdapter {

    private ArrayList<Map<String, Object>> mMessages;
    Context context;

    public MessageAdapter(Context context, ArrayList<Map<String, Object>> messages) {
        this.context=context;
        mMessages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View messageView = inflater.inflate(R.layout.message_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(messageView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder viewHolder, int position) {
        final Map<String, Object> message = mMessages.get(position);

        //populate the view according to Message model
        viewHolder.mUsername.setText(message.get("userId").toString());
        viewHolder.mMessageText.setText(message.get("messageText").toString());
        //viewHolder.mMessageTimeStamp.setText((int) message.getMessageTimeStamp());
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
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
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

        @Override
        public void onClick(View v) {

        }
    }
}