package com.kotharbaksho;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    private ImageView btnBackOtp;
    private TextView tvPhoneNumberTarget, tvWrongNumber, tvResendTimer, btnResendOtp;
    private EditText etOtpCode;
    private AppCompatButton btnVerifyOtp;
    private ProgressBar otpProgressBar;

    private FirebaseAuth auth;
    private String verificationId;
    private String phoneNumber;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        auth = FirebaseAuth.getInstance();

        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        btnBackOtp = findViewById(R.id.btnBackOtp);
        tvPhoneNumberTarget = findViewById(R.id.tvPhoneNumberTarget);
        tvWrongNumber = findViewById(R.id.tvWrongNumber);
        tvResendTimer = findViewById(R.id.tvResendTimer);
        btnResendOtp = findViewById(R.id.btnResendOtp);
        etOtpCode = findViewById(R.id.etOtpCode);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        otpProgressBar = findViewById(R.id.otpProgressBar);

        if (phoneNumber != null) {
            tvPhoneNumberTarget.setText("Code sent to " + phoneNumber);
        }

        btnBackOtp.setOnClickListener(v -> finish());
        tvWrongNumber.setOnClickListener(v -> finish());

        startResendTimer();

        btnResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode();
            }
        });

        // Auto-verify when 6 digits are typed
        etOtpCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() == 6) {
                    verifyCode(s.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = etOtpCode.getText().toString().trim();
                if (TextUtils.isEmpty(code) || code.length() < 6) {
                    etOtpCode.setError("Enter 6-digit code");
                    etOtpCode.requestFocus();
                    return;
                }
                verifyCode(code);
            }
        });
    }

    private void startResendTimer() {
        btnResendOtp.setVisibility(View.GONE);
        tvResendTimer.setVisibility(View.VISIBLE);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvResendTimer.setText(String.format(Locale.getDefault(), "Resend SMS in %02ds", seconds));
            }

            @Override
            public void onFinish() {
                tvResendTimer.setVisibility(View.GONE);
                btnResendOtp.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void resendVerificationCode() {
        if (phoneNumber == null) return;

        setLoading(true);

        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        signInWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        setLoading(false);
                        Toast.makeText(OtpActivity.this, "Resend failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        setLoading(false);
                        verificationId = newVerificationId;
                        login.resendToken = token;
                        Toast.makeText(OtpActivity.this, "New code sent!", Toast.LENGTH_SHORT).show();
                        startResendTimer();
                    }
                });

        if (login.resendToken != null) {
            builder.setForceResendingToken(login.resendToken);
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build());
    }

    private void verifyCode(String code) {
        if (verificationId == null) {
            Toast.makeText(this, "Verification ID missing, please request a new code", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
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
                                // Existing user with setup completed -> Go to Main chat
                                Intent intent = new Intent(OtpActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                // Brand new user -> Go to Profile Setup
                                Intent intent = new Intent(OtpActivity.this, ProfileSetupActivity.class);
                                intent.putExtra("phoneNumber", phoneNumber);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Intent intent = new Intent(OtpActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Invalid verification code";
                    Toast.makeText(OtpActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            btnVerifyOtp.setText("");
            btnVerifyOtp.setEnabled(false);
            otpProgressBar.setVisibility(View.VISIBLE);
        } else {
            btnVerifyOtp.setText("Verify & Continue");
            btnVerifyOtp.setEnabled(true);
            otpProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
