package com.kotharbaksho;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class login extends AppCompatActivity {

    public static PhoneAuthProvider.ForceResendingToken resendToken;

    private TextView etCountryCode;
    private EditText etPhoneNumber;
    private AppCompatButton btnSendOtp;
    private ProgressBar loginProgressBar;
    private TextView tvEmailLogin;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        etCountryCode = findViewById(R.id.etCountryCode);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        loginProgressBar = findViewById(R.id.loginProgressBar);
        tvEmailLogin = findViewById(R.id.tvEmailLogin);

        // Auto-remove leading 0 in real time since country code (+880) already includes it
        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (input.startsWith("0")) {
                    etPhoneNumber.setText(input.replaceFirst("^0+", ""));
                    etPhoneNumber.setSelection(etPhoneNumber.getText().length());
                }
            }
        });

        btnSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePhoneVerification();
            }
        });

        tvEmailLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(login.this, EmailLoginActivity.class));
            }
        });
    }

    private void initiatePhoneVerification() {
        String code = etCountryCode.getText().toString().trim();
        String number = etPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            code = "+880";
        }

        if (TextUtils.isEmpty(number)) {
            etPhoneNumber.setError("Please enter your phone number");
            etPhoneNumber.requestFocus();
            return;
        }

        // Clean up common input formats (e.g. 01712345678 -> 1712345678 when code is +880)
        String cleanNumber = number.replaceAll("[\\s\\-\\(\\)]", "");
        cleanNumber = cleanNumber.replaceFirst("^0+", "");

        if (cleanNumber.length() < 7) {
            etPhoneNumber.setError("Please enter a valid phone number");
            etPhoneNumber.requestFocus();
            return;
        }

        final String fullPhoneNumber = code + cleanNumber;

        setLoading(true);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(fullPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Instant auto-verification
                        signInWithCredential(credential, fullPhoneNumber);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        setLoading(false);
                        Toast.makeText(login.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        setLoading(false);
                        login.resendToken = token;

                        Intent intent = new Intent(login.this, OtpActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("phoneNumber", fullPhoneNumber);
                        startActivity(intent);
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithCredential(PhoneAuthCredential credential, final String phone) {
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                setLoading(false);
                if (task.isSuccessful() && auth.getCurrentUser() != null) {
                    final String uid = auth.getCurrentUser().getUid();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(uid);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.child("userName").getValue() != null
                                    && !TextUtils.isEmpty(snapshot.child("userName").getValue(String.class))) {
                                // User already has complete profile -> Go to Main chat
                                Intent intent = new Intent(login.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                // New user -> Go to Profile Setup
                                Intent intent = new Intent(login.this, ProfileSetupActivity.class);
                                intent.putExtra("phoneNumber", phone);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Intent intent = new Intent(login.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Sign in failed";
                    Toast.makeText(login.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            btnSendOtp.setText("");
            btnSendOtp.setEnabled(false);
            loginProgressBar.setVisibility(View.VISIBLE);
        } else {
            btnSendOtp.setText("Next");
            btnSendOtp.setEnabled(true);
            loginProgressBar.setVisibility(View.GONE);
        }
    }
}
