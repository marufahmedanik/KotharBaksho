package com.kotharbaksho;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class setting extends AppCompatActivity {

    private CircleImageView setprofile;
    private View avatarContainer;
    private EditText setname, setstatus, setemail;
    private View donebut;
    private ImageView backbtn;
    private ProgressBar saveProgress;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri setImageUri;
    private String currentProfilePic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(setting.this, login.class));
            finish();
            return;
        }

        setprofile = findViewById(R.id.settingprofile);
        avatarContainer = findViewById(R.id.avatar_container);
        setname = findViewById(R.id.settingname);
        setstatus = findViewById(R.id.settingstatus);
        setemail = findViewById(R.id.settingemail);
        donebut = findViewById(R.id.donebutt);
        backbtn = findViewById(R.id.back_btn);
        saveProgress = findViewById(R.id.save_progress);

        backbtn.setOnClickListener(v -> finish());

        // Load current user profile from Firebase Realtime Database
        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("userName").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String email = snapshot.child("mail").getValue(String.class);
                    currentProfilePic = snapshot.child("profilepic").getValue(String.class);

                    if (name != null) setname.setText(name);
                    if (status != null) setstatus.setText(status);
                    if (email != null) {
                        setemail.setText(email);
                    } else if (auth.getCurrentUser() != null) {
                        setemail.setText(auth.getCurrentUser().getEmail());
                    }

                    if (currentProfilePic != null && !currentProfilePic.isEmpty()) {
                        Picasso.get().load(currentProfilePic).placeholder(R.drawable.man).into(setprofile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(setting.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });

        // Choose image on avatar click
        View.OnClickListener pickImageListener = v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), 10);
        };
        avatarContainer.setOnClickListener(pickImageListener);
        setprofile.setOnClickListener(pickImageListener);

        // Save changes
        donebut.setOnClickListener(v -> saveProfileChanges(reference));
    }

    private void saveProfileChanges(DatabaseReference reference) {
        String name = setname.getText().toString().trim();
        String status = setstatus.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            setname.setError("Please enter your name");
            setname.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(status)) {
            status = "Hey I am using KotharBaksho";
        }

        setLoading(true);
        final String finalStatus = status;

        if (setImageUri != null) {
            StorageReference storageReference = storage.getReference().child("Upload").child(auth.getUid());
            storageReference.putFile(setImageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("userName", name);
                        updates.put("status", finalStatus);
                        updates.put("profilepic", downloadUrl);

                        updateDatabase(reference, updates);
                    }).addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(setting.this, "Failed to get photo URL", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    setLoading(false);
                    Toast.makeText(setting.this, "Photo upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Map<String, Object> updates = new HashMap<>();
            updates.put("userName", name);
            updates.put("status", finalStatus);
            updateDatabase(reference, updates);
        }
    }

    private void updateDatabase(DatabaseReference reference, Map<String, Object> updates) {
        reference.updateChildren(updates).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                Toast.makeText(setting.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(setting.this, "Failed to save profile changes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (saveProgress != null) {
            saveProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (donebut != null) {
            donebut.setEnabled(!isLoading);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            setImageUri = data.getData();
            setprofile.setImageURI(setImageUri);
        }
    }
}
