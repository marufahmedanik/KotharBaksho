package com.kotharbaksho;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {
    private TextView logsignup;
    private Button button;
    private EditText email, password;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logbutton);
        email = findViewById(R.id.editTexLogEmail);
        password = findViewById(R.id.editTextLogPassword);
        logsignup = findViewById(R.id.logsignup);
        progressBar = findViewById(R.id.loginProgressBar);

        logsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(login.this, registration.class));
                finish();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailStr = email.getText().toString().trim();
                String passStr = password.getText().toString().trim();

                if (TextUtils.isEmpty(emailStr)) {
                    email.setError("Please enter your email");
                    email.requestFocus();
                } else if (!emailStr.matches(emailPattern)) {
                    email.setError("Enter a valid email address");
                    email.requestFocus();
                } else if (TextUtils.isEmpty(passStr)) {
                    password.setError("Please enter your password");
                    password.requestFocus();
                } else if (passStr.length() < 6) {
                    password.setError("Password must be at least 6 characters");
                    password.requestFocus();
                } else {
                    setLoading(true);
                    auth.signInWithEmailAndPassword(emailStr, passStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            setLoading(false);
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(login.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                String error = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                                Toast.makeText(login.this, error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            button.setText("");
            button.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            button.setText("Log In");
            button.setEnabled(true);
            progressBar.setVisibility(View.GONE);
        }
    }
}
