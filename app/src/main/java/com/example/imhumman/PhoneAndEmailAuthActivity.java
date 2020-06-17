package com.example.imhumman;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.imhumman.firebaseModels.UserDataModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.concurrent.TimeUnit;

public class PhoneAndEmailAuthActivity extends AppCompatActivity {
    String verificationId;
    FirebaseAuth mAuth;
    EditText edt_verificationCode;
    Button btn_verify;
    ProgressBar progressBar;
    Intent intent;
    Uri userImgPath = null;
    Uri userImgUri;
    DatabaseReference mDatabase;
    DatabaseReference userTable;
    boolean isPhoneNumberVerified = false;
    boolean isFromUserProfile = false;

    private View.OnClickListener verifyCodeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String code = edt_verificationCode.getText().toString();
            if (code.isEmpty() || code.length() < 6) {
                edt_verificationCode.setError("الرمز غير كامل");
            } else {
                verifyCode(code);
            }
        }
    };

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            Toast.makeText(PhoneAndEmailAuthActivity.this, "onCodeSent", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                edt_verificationCode.setText(code);
                verifyCode(code);
                Toast.makeText(PhoneAndEmailAuthActivity.this, "onVerificationCompleted", Toast.LENGTH_LONG).show();

            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(PhoneAndEmailAuthActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_authentication);
        setToolbar();

        edt_verificationCode = findViewById(R.id.edtVerificationCode);

        btn_verify = findViewById(R.id.btnVerify);
        btn_verify.setOnClickListener(verifyCodeListener);

        progressBar = findViewById(R.id.progressBar);

        intent = getIntent();

        mAuth = FirebaseAuth.getInstance();

        sendVerificationCode(getPhoneNumberFromExtras());

        if (getCallingActivity() != null) {

            if (getCallingActivity().getClassName().equals(UserProfileActivity.class.getName())) {
                Intent intent = getIntent();
                String phoneNumber = intent.getExtras().getString("phoneNumber");
                isFromUserProfile = true;
                sendVerificationCode(phoneNumber);
            }
        }
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
    }

    private String getPhoneNumberFromExtras() {

        if (intent.getExtras() != null) {
            return intent.getExtras().getString("phoneNumber");

        } else {
            return "nothing";
        }
    }


    private void uploadUserImage() {
        if (intent.getData() != null) {
            userImgPath = intent.getData();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            final StorageReference imgRef = storageReference.child("userImages/" + userImgPath.getLastPathSegment());
            UploadTask uploadTask = imgRef.putFile(userImgPath);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PhoneAndEmailAuthActivity.this, "Failed to upload", Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(PhoneAndEmailAuthActivity.this, "Successful upload", Toast.LENGTH_LONG).show();

                }
            });

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imgRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        userImgUri = task.getResult();
                        AddUserData();
                        insertUserDataToDB();
                    }
                }
            });
        } else {
            userImgUri = Uri.parse("NO_USER_PHOTO");
            AddUserData();
            insertUserDataToDB();
        }
    }

    private void AddUserData() {

        FirebaseUser user = mAuth.getCurrentUser();
        UpdateUserInfo userInfo = new UpdateUserInfo(user, PhoneAndEmailAuthActivity.this);
        userInfo.updateName(intent.getExtras().getString("fullName"));
        userInfo.updateImageUri(userImgUri);
        Intent intent = new Intent(PhoneAndEmailAuthActivity.this, AllPostsActivity.class);
        startActivity(intent);


    }

    private void insertUserDataToDB() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userTable = mDatabase.child("users");
        UserDataModel userData = new UserDataModel();
        userData.setFull_name(intent.getExtras().getString("fullName"));
        userData.setEmail(intent.getExtras().getString("email"));
        userData.setPhone_number(intent.getExtras().getString("phoneNumber"));
        userData.setPassword(intent.getExtras().getString("password"));
        userData.setUser_photo(userImgUri.toString());

        userTable.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(PhoneAndEmailAuthActivity.this, "user data saved to db", Toast.LENGTH_SHORT).show();
            }

        });

    }


    private void sendVerificationCode(String phoneNumber) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+964" + phoneNumber,
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks
        );
    }

    private void verifyCode(String code) {
        PhoneAuthCredential phoneCredential = PhoneAuthProvider.getCredential(verificationId, code);
        if (isFromUserProfile) {
            Intent intent = new Intent();
            intent.putExtra("PhoneNumberCredential", phoneCredential);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            signInWithCredential(phoneCredential);
            Toast.makeText(PhoneAndEmailAuthActivity.this, "verifyCode", Toast.LENGTH_LONG).show();
        }
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            isPhoneNumberVerified = true;
                            linkWithAnotherCredential();

                            Toast.makeText(PhoneAndEmailAuthActivity.this, "signInWithCredential", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(PhoneAndEmailAuthActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void linkWithAnotherCredential() {

        String email = intent.getExtras().getString("email");
        String password = intent.getExtras().getString("password");

        if (email != null && password != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
//            Toast.makeText(PhoneAndEmailAuthActivity.this, "Credential" + credential, Toast.LENGTH_LONG).show();
//            Toast.makeText(PhoneAndEmailAuthActivity.this, "auth " + mAuth, Toast.LENGTH_LONG).show();
//            Toast.makeText(PhoneAndEmailAuthActivity.this, "user " + mAuth.getCurrentUser(), Toast.LENGTH_LONG).show();

            mAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(PhoneAndEmailAuthActivity.this, "linkWithAnotherCredential", Toast.LENGTH_LONG).show();

                                emailVerification();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PhoneAndEmailAuthActivity.this, "error " + e.getMessage(), Toast.LENGTH_LONG).show();

                }
            });
        }
    }

    private void emailVerification() {
        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(PhoneAndEmailAuthActivity.this, "يرجى مراجعة البريد الالكتروني لاكمال عملية تسجيل الدخول", Toast.LENGTH_SHORT).show();
                    uploadUserImage();
                    signInUser();
                }
            }
        });
    }

    private void signInUser() {
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified() && isPhoneNumberVerified) {
            Intent savedPostsIntent = new Intent(PhoneAndEmailAuthActivity.this, AllPostsActivity.class);
            savedPostsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(savedPostsIntent);
        }
    }
}
