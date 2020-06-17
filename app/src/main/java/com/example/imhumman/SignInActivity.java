package com.example.imhumman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    Button btnSignIn;
    EditText edtEmail, edtPassword;
    TextView txtSignUpNow;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
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

            if (edtEmail.length() < 11) {
                //dialog showing error
            }

            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            if (email.equals("") || password.equals("")) {
                //dialog error embty
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(SignInActivity.this, AllPostsActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SignInActivity.this, "invalid email or password", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setToolbar();
        mAuth = FirebaseAuth.getInstance();

        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(signInListener);

        txtSignUpNow = findViewById(R.id.txtSignUpNow);
        txtSignUpNow.setOnClickListener(signUpListener);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
    }

}
