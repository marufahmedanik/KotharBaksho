package com.kotharbaksho;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailLoginActivity extends AppCompatActivity {

    private ImageView btnBackEmail;
    private EditText etEmail, etPassword;
    private AppCompatButton btnEmailSignIn;
    private ProgressBar emailProgress;
    private FirebaseAuth auth;
    private final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        auth = FirebaseAuth.getInstance();

        btnBackEmail = findViewById(R.id.btnBackEmail);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnEmailSignIn = findViewById(R.id.btnEmailSignIn);
        emailProgress = findViewById(R.id.emailProgress);

        btnBackEmail.setOnClickListener(v -> finish());

        btnEmailSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailStr = etEmail.getText().toString().trim();
                String passStr = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(emailStr)) {
                    etEmail.setError("Please enter your email");
                    etEmail.requestFocus();
                } else if (!emailStr.matches(emailPattern)) {
                    etEmail.setError("Enter a valid email address");
                    etEmail.requestFocus();
                } else if (TextUtils.isEmpty(passStr)) {
                    etPassword.setError("Please enter your password");
                    etPassword.requestFocus();
                } else {
                    setLoading(true);
                    auth.signInWithEmailAndPassword(emailStr, passStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            setLoading(false);
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(EmailLoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                String error = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                                Toast.makeText(EmailLoginActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            btnEmailSignIn.setText("");
            btnEmailSignIn.setEnabled(false);
            emailProgress.setVisibility(View.VISIBLE);
        } else {
            btnEmailSignIn.setText("Sign In");
            btnEmailSignIn.setEnabled(true);
            emailProgress.setVisibility(View.GONE);
        }
    }
}
