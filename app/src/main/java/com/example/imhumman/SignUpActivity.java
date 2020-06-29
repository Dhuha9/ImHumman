package com.example.imhumman;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SignUpActivity extends AppCompatActivity {

    ImageView userImage;
    Button btnSignUp;
    EditText edtFirstName, edtLastName, edtPassword, edtRePassword, edtEmail, edtUserPhoneNumber;
    int PICK_IMAGE_REQUEST = 1;
    Uri userImgPath;
    Uri userImgUri;
    View.OnClickListener signUpListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendUserDataToAuthenticate();
        }
    };

    View.OnClickListener imgSelecting = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setToolbar();
        getLayoutViews();
    }

    private void getLayoutViews() {
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtPassword = findViewById(R.id.edtPassword);
        edtRePassword = findViewById(R.id.edtRePassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtUserPhoneNumber = findViewById(R.id.edtUserPhoneNumber);
        userImage = findViewById(R.id.userImage);
        userImage.setOnClickListener(imgSelecting);

        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(signUpListener);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                userImgPath = data.getData();
                userImage.setImageURI(userImgPath);
            }
        }
    }

    private void sendUserDataToAuthenticate() {
        String firstName = edtFirstName.getText().toString();
        String lastName = edtLastName.getText().toString();
        String email = edtEmail.getText().toString();
        String phoneNO = edtUserPhoneNumber.getText().toString();
        String password = edtPassword.getText().toString();
        String reEnteredPassword = edtRePassword.getText().toString();

        if (phoneNO.length() < 10 || password.length() < 8 || !password.equals(reEnteredPassword) || firstName.isEmpty() || lastName.isEmpty()) {

            if (email.isEmpty()) {
                edtEmail.setError("يجب كتابة الايميل");
            }

            if (phoneNO.length() < 11) {
                edtUserPhoneNumber.setError("رقم الهاتف اقل من 11 رقم");
            }
            if (password.length() < 8) {
                edtPassword.setError("يجب ان يكون الباسورد متكول من 8 مقاطع");
            }
            if (!password.equals(reEnteredPassword)) {
                edtRePassword.setError("غير مطابق لحقل الباسورد اعلاه");
            }
            if (firstName.isEmpty()) {
                edtFirstName.setError("يجب ادخال الاسم كاملا");
            }
            if (lastName.isEmpty()) {
                edtLastName.setError("يجب ادخال الاسم كاملا");
            }

        } else {
            String fullName = firstName + " " + lastName;
            Intent intent = new Intent(SignUpActivity.this, PhoneAndEmailAuthActivity.class);
            intent.putExtra("fullName", fullName);
            intent.putExtra("email", email);
            intent.putExtra("phoneNumber", phoneNO);
            intent.putExtra("password", password);
            intent.setData(userImgPath);
            startActivity(intent);

        }
    }

}