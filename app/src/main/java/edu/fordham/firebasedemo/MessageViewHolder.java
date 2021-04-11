package edu.fordham.firebasedemo;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    private final DatabaseReference usersDatabaseRef;

    TextView messageTextView;
    ImageView messageImageView;
    TextView messengerTextView;

    public MessageViewHolder(View v, DatabaseReference dbref) {
        super(v);
        messageTextView = itemView.findViewById(R.id.messageTextView);
        messageImageView = itemView.findViewById(R.id.messageImageView);
        messengerTextView = itemView.findViewById(R.id.messengerTextView);
        usersDatabaseRef = dbref;
    }

    public void bindMessage(Message message) {
        if (message.getText() != null) {
            messageTextView.setText(message.getText());
            messageTextView.setVisibility(TextView.VISIBLE);
            messageImageView.setVisibility(ImageView.GONE);
        } else if (message.getImageUrl() != null) {
            Picasso.get()
                    .load(message.getImageUrl())
                    .into(messageImageView);
            messageImageView.setVisibility(ImageView.VISIBLE);
            messageTextView.setVisibility(TextView.GONE);
        }

        usersDatabaseRef.child(message.getUid()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.getValue();
                messengerTextView.setText(name);
            }
        });
    }
}
