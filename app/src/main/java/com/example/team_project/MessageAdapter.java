package com.example.team_project;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.team_project.model.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;
    private Context context;

    public MessageAdapter(Context context, List<Message> messages) {
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
        Message message = mMessages.get(position);

        //populate the view according to Message model
        viewHolder.mUsername.setText(message.getUsername());
        viewHolder.mMessageText.setText(message.getMessageText());
        viewHolder.mMessageTimeStamp.setText(message.getMessageTimeStamp());
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
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