package com.kotharbaksho;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSetupActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;
    private static final String DEFAULT_AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/marufsapp.appspot.com/o/man.png?alt=media&token=e3649ae1-b755-4c07-b27b-fb3a75ea9c46";
    private final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    private CircleImageView ivSetupProfile;
    private View btnPickImage;
    private TextView tvTapToChange;
    private View phoneSetupContainer;
    private EditText etSetupPhone;
    private EditText etSetupName, etSetupEmail, etSetupStatus;
    private AppCompatButton btnFinishSetup;
    private ProgressBar setupProgressBar;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri selectedImageUri;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(ProfileSetupActivity.this, login.class));
            finish();
            return;
        }

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        if ((phoneNumber == null || phoneNumber.isEmpty()) && auth.getCurrentUser().getPhoneNumber() != null) {
            phoneNumber = auth.getCurrentUser().getPhoneNumber();
        }

        ivSetupProfile = findViewById(R.id.ivSetupProfile);
        btnPickImage = findViewById(R.id.btnPickImage);
        tvTapToChange = findViewById(R.id.tvTapToChange);
        phoneSetupContainer = findViewById(R.id.phoneSetupContainer);
        etSetupPhone = findViewById(R.id.etSetupPhone);
        etSetupName = findViewById(R.id.etSetupName);
        etSetupEmail = findViewById(R.id.etSetupEmail);
        etSetupStatus = findViewById(R.id.etSetupStatus);
        btnFinishSetup = findViewById(R.id.btnFinishSetup);
        setupProgressBar = findViewById(R.id.setupProgressBar);

        // If phone number was verified via OTP, hide phone input and just ask for Email (Optional)
        // If phone number is missing (e.g. email registration), show phone input to add number
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            phoneSetupContainer.setVisibility(View.GONE);
        } else {
            phoneSetupContainer.setVisibility(View.VISIBLE);
            if (auth.getCurrentUser().getEmail() != null) {
                etSetupEmail.setText(auth.getCurrentUser().getEmail());
            }
        }

        View.OnClickListener pickImageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
            }
        };

        ivSetupProfile.setOnClickListener(pickImageListener);
        btnPickImage.setOnClickListener(pickImageListener);
        tvTapToChange.setOnClickListener(pickImageListener);

        btnFinishSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ivSetupProfile.setImageURI(selectedImageUri);
            tvTapToChange.setText("Change photo");
        }
    }

    private void saveUserProfile() {
        final String name = etSetupName.getText().toString().trim();
        final String email = etSetupEmail.getText().toString().trim();
        String status = etSetupStatus.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etSetupName.setError("Please enter your name");
            etSetupName.requestFocus();
            return;
        }

        String phoneStr = phoneNumber != null ? phoneNumber : "";
        if (phoneSetupContainer.getVisibility() == View.VISIBLE) {
            String enteredPhone = etSetupPhone.getText().toString().trim();
            if (TextUtils.isEmpty(enteredPhone)) {
                etSetupPhone.setError("Please add your phone number");
                etSetupPhone.requestFocus();
                return;
            }
            String cleanPhone = enteredPhone.replaceAll("[\\s\\-\\(\\)]", "");
            if (cleanPhone.startsWith("0")) {
                cleanPhone = cleanPhone.substring(1);
            }
            if (cleanPhone.length() < 7) {
                etSetupPhone.setError("Enter a valid phone number");
                etSetupPhone.requestFocus();
                return;
            }
            phoneStr = "+880" + cleanPhone;
        }

        if (!TextUtils.isEmpty(email) && !email.matches(emailPattern)) {
            etSetupEmail.setError("Please enter a valid email address");
            etSetupEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(status)) {
            status = "Hey there! I am using KotharBaksho";
        }

        final String finalStatus = status;
        final String uid = auth.getUid();
        final String finalPhoneStr = phoneStr;

        setLoading(true);

        if (selectedImageUri != null) {
            // Upload profile picture to Firebase Storage
            StorageReference storageRef = storage.getReference().child("upload").child(uid);
            storageRef.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                writeUserData(uid, name, finalPhoneStr, email, uri.toString(), finalStatus);
                            }
                        }).addOnFailureListener(e -> writeUserData(uid, name, finalPhoneStr, email, DEFAULT_AVATAR_URL, finalStatus));
                    } else {
                        // Fallback to default avatar if upload fails
                        writeUserData(uid, name, finalPhoneStr, email, DEFAULT_AVATAR_URL, finalStatus);
                    }
                }
            });
        } else {
            // No custom image selected: use default avatar
            writeUserData(uid, name, finalPhoneStr, email, DEFAULT_AVATAR_URL, finalStatus);
        }
    }

    private void writeUserData(String uid, String name, String phone, String email, String imageUri, String status) {
        DatabaseReference userRef = database.getReference().child("user").child(uid);
        Users user = new Users(uid, name, email, "", imageUri, status, phone);
        user.setOnlineStatus("Online");

        userRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setLoading(false);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Failed to save profile";
                    Toast.makeText(ProfileSetupActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            btnFinishSetup.setText("");
            btnFinishSetup.setEnabled(false);
            setupProgressBar.setVisibility(View.VISIBLE);
        } else {
            btnFinishSetup.setText("Next");
            btnFinishSetup.setEnabled(true);
            setupProgressBar.setVisibility(View.GONE);
        }
    }
}
