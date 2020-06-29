package com.example.imhumman;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPassword extends AppCompatActivity {
    FirebaseUser user;
    UpdateUserInfo userInfo;
    EditText email;
    Button btnSend;
    ProgressBar progressBar;
    View.OnClickListener resetPasswordListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            resetPassword();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        getLayoutViews();
    }

    private void getLayoutViews() {
        email = findViewById(R.id.edtEmail);
        btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(resetPasswordListener);
        progressBar = findViewById(R.id.progressBar);
    }

    private void resetPassword() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        userInfo = new UpdateUserInfo(user, ResetPassword.this);

        String emailTxt = email.getText().toString();
        if (emailTxt.isEmpty()) {
            Toast.makeText(this, "يجب كتابة الايميل اولا", Toast.LENGTH_SHORT).show();
        } else {
            userInfo.updatePassword(emailTxt, progressBar);

        }
    }

}
