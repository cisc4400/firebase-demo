package edu.fordham.firebasedemo;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    private final DatabaseReference usersDatabase;

    TextView messageTextView;
    ImageView messageImageView;
    TextView messengerTextView;

    public MessageViewHolder(View v, DatabaseReference usersDatabase) {
        super(v);
        messageTextView = itemView.findViewById(R.id.messageTextView);
        messageImageView = itemView.findViewById(R.id.messageImageView);
        messengerTextView = itemView.findViewById(R.id.messengerTextView);
        this.usersDatabase = usersDatabase;
    }

    public void bindMessage(Message message) {
        if (message.getText() != null) {
            messageTextView.setText(message.getText());
            messageTextView.setVisibility(TextView.VISIBLE);
            messageImageView.setVisibility(ImageView.GONE);
        }
        usersDatabase.child(message.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    String name = (String) task.getResult().getValue();
                    messengerTextView.setText(name);
                }
            }
        });
    }
}
