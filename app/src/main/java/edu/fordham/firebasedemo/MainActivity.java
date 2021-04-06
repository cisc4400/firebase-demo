package edu.fordham.firebasedemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    RecyclerView messageList;
    TextView nameTextView;
    EditText messageEditText;
    DatabaseReference firebaseDatabase;
    FirebaseRecyclerAdapter<Message, MessageViewHolder> firebaseAdapter;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameTextView = findViewById(R.id.nameTextView);
        messageEditText = findViewById(R.id.messageEditText);
        messageList = findViewById(R.id.messageList);

        // Initialize Realtime Database
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(firebaseDatabase.child("messages"), Message.class)
                        .build();

        firebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message, viewGroup, false);
                return new MessageViewHolder(view, firebaseDatabase.child("users"));
            }

            @Override
            protected void onBindViewHolder(MessageViewHolder vh, int position, Message message) {
                vh.bindMessage(message);
            }
        };
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        messageList.setLayoutManager(lm);
        messageList.setAdapter(firebaseAdapter);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            createSignInIntent();
            return;
        }
        nameTextView.setText(user.getDisplayName());
    }

    @Override
    public void onStop() {
        firebaseAdapter.stopListening();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAdapter.startListening();
    }


    public void createSignInIntent() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = new ArrayList<>();
        providers.add(new AuthUI.IdpConfig.EmailBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = firebaseAuth.getCurrentUser();
                nameTextView.setText(user.getDisplayName());
                if (response.isNewUser()) {
                    firebaseDatabase.child("users").child(user.getUid()).setValue(user.getDisplayName());
                }
            } else {
                Toast.makeText(this, "Sign in failed: " + response.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.signOutMenuItem) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        createSignInIntent();
                    }
                });
    }

    public void send(View view) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        Message message = new
                Message(messageEditText.getText().toString(),
                user.getUid(), user.getDisplayName());

        firebaseDatabase.child("messages").push().setValue(message);
        messageEditText.setText("");
    }
}