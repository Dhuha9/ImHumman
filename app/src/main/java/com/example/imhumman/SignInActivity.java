package com.example.imhumman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    Button btnSignIn;
    EditText edtEmail, edtPassword;
    TextView txtSignUpNow, txtForgotPassword;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    View.OnClickListener signUpListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);

        }
    };
    private View.OnClickListener signInListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            if (email.isEmpty()) {
                edtEmail.setError("يجب كتابة الايميل");
            } else if (password.equals("")) {
                edtPassword.setError("يجب كتابة الباسورد");
            } else {
                signInUser(email, password);
            }
        }
    };
    private View.OnClickListener restPassword = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(SignInActivity.this, ResetPassword.class);
            startActivity(intent);
        }
    };

    private void signInUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(SignInActivity.this, AllPostsActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(SignInActivity.this, "ايميل او باسورد غير صحيح", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setToolbar();
        getLayoutViews();
        mAuth = FirebaseAuth.getInstance();
    }

    private void getLayoutViews() {
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(signInListener);

        txtSignUpNow = findViewById(R.id.txtSignUpNow);
        txtSignUpNow.setOnClickListener(signUpListener);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        progressBar = findViewById(R.id.progressBar);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtForgotPassword.setOnClickListener(restPassword);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
    }


}
