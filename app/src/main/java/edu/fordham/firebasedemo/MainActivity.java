package edu.fordham.firebasedemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String MESSAGES_CHILD = "messages";
    public static final String USERS_CHILD = "users";
    private static final int RC_SIGN_IN = 123;
    private static final int RC_IMAGE = 234;
    RecyclerView messageList;
    TextView nameTextView;
    EditText messageEditText;
    DatabaseReference firebaseDatabaseRef;
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
        firebaseDatabaseRef = FirebaseDatabase.getInstance().getReference();
        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(firebaseDatabaseRef.child(MESSAGES_CHILD), Message.class)
                        .build();

        firebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message, viewGroup, false);
                return new MessageViewHolder(view, firebaseDatabaseRef.child(USERS_CHILD));
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

        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                messageList.scrollToPosition(positionStart);
            }
        });

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
                    firebaseDatabaseRef.child(USERS_CHILD).child(user.getUid()).setValue(user.getDisplayName());
                }
            } else {
                Toast.makeText(this, "Sign in failed: " + response.getError(), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == RC_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                final Uri uri = data.getData();
                Log.d("mobdev", "Uri: " + uri.toString());

                // Create a new message ID
                String key = firebaseDatabaseRef.child(MESSAGES_CHILD).push().getKey();

                // Build a StorageReference and then upload the file
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                StorageReference storageReference =
                        FirebaseStorage.getInstance()
                                .getReference(user.getUid())
                                .child(key)
                                .child(uri.getLastPathSegment());

                // Upload the image to Cloud Storage
                storageReference.putFile(uri)
                        .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // After the image loads, get a public downloadUrl for the image
                                // and add it to the message.
                                taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Log.i("mobdev", "Update message " + key + ": " + uri);
                                                final FirebaseUser user = firebaseAuth.getCurrentUser();
                                                Message msg = new Message(
                                                        null, user.getUid(), uri.toString());
                                                firebaseDatabaseRef.child(MESSAGES_CHILD)
                                                        .child(key)
                                                        .setValue(msg);
                                            }
                                        });
                            }
                        });
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
                Message(messageEditText.getText().toString().trim(),
                user.getUid(),
                null /* no image */);

        firebaseDatabaseRef.child(MESSAGES_CHILD).push().setValue(message);
        messageEditText.setText("");
    }

    public void addMessage(View view) {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, RC_IMAGE);
    }
}