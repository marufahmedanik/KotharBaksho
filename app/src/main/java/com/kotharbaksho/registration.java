package com.kotharbaksho;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class registration extends AppCompatActivity {
    private TextView loginbut;
    private EditText rg_username, rg_email, rg_phone, rg_password, rg_repassword;
    private Button rg_signup;
    private ProgressBar progressBar;
    private CircleImageView rg_profileImg;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri imageURI;
    private final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static final int PICK_IMAGE_REQUEST = 10;
    private static final String DEFAULT_AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/marufsapp.appspot.com/o/man.png?alt=media&token=e3649ae1-b755-4c07-b27b-fb3a75ea9c46";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        loginbut = findViewById(R.id.loginbut);
        rg_username = findViewById(R.id.rgusername);
        rg_email = findViewById(R.id.rgemail);
        rg_phone = findViewById(R.id.rgphone);
        rg_password = findViewById(R.id.rgpassword);
        rg_repassword = findViewById(R.id.rgrepassword);
        rg_profileImg = findViewById(R.id.profilerg0);
        rg_signup = findViewById(R.id.signupbutton);
        progressBar = findViewById(R.id.regProgressBar);

        loginbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(registration.this, login.class));
                finish();
            }
        });

        rg_profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
            }
        });

        rg_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = rg_username.getText().toString().trim();
                String email = rg_email.getText().toString().trim();
                String phone = rg_phone.getText().toString().trim();
                String password = rg_password.getText().toString().trim();
                String cPassword = rg_repassword.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    rg_username.setError("Please enter your name");
                    rg_username.requestFocus();
                } else if (TextUtils.isEmpty(email)) {
                    rg_email.setError("Please enter your email");
                    rg_email.requestFocus();
                } else if (!email.matches(emailPattern)) {
                    rg_email.setError("Please enter a valid email");
                    rg_email.requestFocus();
                } else if (TextUtils.isEmpty(phone)) {
                    rg_phone.setError("Please add your phone number");
                    rg_phone.requestFocus();
                } else {
                    String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
                    if (cleanPhone.startsWith("0")) {
                        cleanPhone = cleanPhone.substring(1);
                    }
                    if (cleanPhone.length() < 7) {
                        rg_phone.setError("Please enter a valid phone number");
                        rg_phone.requestFocus();
                        return;
                    }
                    final String fullPhoneNumber = "+880" + cleanPhone;

                    if (TextUtils.isEmpty(password) || password.length() < 6) {
                        rg_password.setError("Password must be at least 6 characters");
                        rg_password.requestFocus();
                    } else if (!password.equals(cPassword)) {
                        rg_repassword.setError("Passwords do not match");
                        rg_repassword.requestFocus();
                    } else {
                        setLoading(true);
                        registerUser(name, email, fullPhoneNumber, password);
                    }
                }
            }
        });
    }

    private void registerUser(String name, String email, String phone, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                    String uid = task.getResult().getUser().getUid();
                    DatabaseReference userRef = database.getReference().child("user").child(uid);
                    String defaultStatus = "Hey there! I am using KotharBaksho";

                    if (imageURI != null) {
                        StorageReference storageRef = storage.getReference().child("upload").child(uid);
                        storageRef.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> uploadTask) {
                                if (uploadTask.isSuccessful()) {
                                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            saveUserToDatabase(userRef, uid, name, email, phone, uri.toString(), defaultStatus);
                                        }
                                    }).addOnFailureListener(e -> saveUserToDatabase(userRef, uid, name, email, phone, DEFAULT_AVATAR_URL, defaultStatus));
                                } else {
                                    saveUserToDatabase(userRef, uid, name, email, phone, DEFAULT_AVATAR_URL, defaultStatus);
                                }
                            }
                        });
                    } else {
                        saveUserToDatabase(userRef, uid, name, email, phone, DEFAULT_AVATAR_URL, defaultStatus);
                    }
                } else {
                    setLoading(false);
                    String error = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                    Toast.makeText(registration.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void saveUserToDatabase(DatabaseReference userRef, String uid, String name, String email, String phone, String profilePic, String status) {
        Users newUser = new Users(uid, name, email, "", profilePic, status, phone);
        newUser.setOnlineStatus("Online");

        userRef.setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setLoading(false);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(registration.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(registration.this, "Failed to save user profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            rg_signup.setText("");
            rg_signup.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            rg_signup.setText("Create Account");
            rg_signup.setEnabled(true);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageURI = data.getData();
            rg_profileImg.setImageURI(imageURI);
        }
    }
}
